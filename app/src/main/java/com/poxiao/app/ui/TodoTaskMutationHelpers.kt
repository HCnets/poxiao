package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.todo.TodoTask
import java.time.LocalDateTime
import java.time.LocalTime

internal fun isRepeatingTodo(repeatText: String): Boolean {
    val normalized = repeatText.trim()
    return normalized.isNotBlank() && normalized != "不重复"
}

internal fun nextRepeatedDueAt(
    repeatText: String,
    currentDueAt: LocalDateTime,
): LocalDateTime? {
    val normalized = repeatText.trim()
    return when {
        normalized == "不重复" || normalized.isBlank() -> null
        normalized.contains("工作日") -> {
            var candidate = currentDueAt.plusDays(1)
            while (candidate.dayOfWeek.value >= 6) {
                candidate = candidate.plusDays(1)
            }
            candidate
        }
        normalized.contains("每2天") || normalized.contains("每两天") -> currentDueAt.plusDays(2)
        normalized.contains("每天") || normalized.contains("每日") -> currentDueAt.plusDays(1)
        normalized.contains("每2周") || normalized.contains("每两周") -> currentDueAt.plusWeeks(2)
        normalized.contains("每周") -> currentDueAt.plusWeeks(1)
        normalized.contains("每月") -> currentDueAt.plusMonths(1)
        normalized.contains("每年") -> currentDueAt.plusYears(1)
        else -> null
    }
}

internal fun advanceTodoReminderText(
    reminderText: String,
    currentDueAt: LocalDateTime,
    nextDueAt: LocalDateTime,
    now: LocalDateTime,
): String {
    val trimmed = reminderText.trim()
    if (trimmed.isBlank() || trimmed == TodoReminderPreset.None.title) return trimmed
    if (TodoReminderPreset.entries.any { it.title == trimmed }) return trimmed
    val currentReminderAt = parseReminderDateTime(trimmed, currentDueAt, now) ?: return trimmed
    val offset = java.time.Duration.between(currentReminderAt, currentDueAt)
    if (offset.isNegative) return trimmed
    return formatTodoDateTime(nextDueAt.minus(offset))
}

internal fun advanceRepeatingTodoTask(
    task: TodoTask,
    now: LocalDateTime = LocalDateTime.now(),
): TodoTask? {
    if (!isRepeatingTodo(task.repeatText)) return null
    val currentDueAt = parseTodoDateTime(task.dueText, now)
        ?: LocalDateTime.of(
            now.toLocalDate(),
            parseClock(Regex("""(\d{1,2}:\d{2})""").find(task.dueText)?.groupValues?.getOrNull(1) ?: extractTodoClockFallback(task.dueText))
                ?: LocalTime.of(21, 0),
        )
    val nextDueAt = nextRepeatedDueAt(task.repeatText, currentDueAt) ?: return null
    return task.copy(
        dueText = formatTodoDateTime(nextDueAt),
        reminderText = advanceTodoReminderText(task.reminderText, currentDueAt, nextDueAt, now),
        subtasks = task.subtasks.map { it.copy(done = false) },
        focusCount = 0,
        done = false,
    )
}

internal fun postponeTodoTask(
    tasks: MutableList<TodoTask>,
    taskIndex: Int,
    now: LocalDateTime = LocalDateTime.now(),
): String {
    val task = tasks.getOrNull(taskIndex) ?: return ""
    if (task.done) return "已完成任务无需顺延：${task.title}"
    val currentDueAt = parseTodoDateTime(task.dueText, now)
        ?: LocalDateTime.of(
            now.toLocalDate(),
            parseClock(
                Regex("""(\d{1,2}:\d{2})""").find(task.dueText)?.groupValues?.getOrNull(1)
                    ?: extractTodoClockFallback(task.dueText),
            ) ?: LocalTime.of(21, 0),
        )
    val nextDueAt = currentDueAt.plusDays(1)
    tasks[taskIndex] = task.copy(
        dueText = formatTodoDateTime(nextDueAt),
        reminderText = advanceTodoReminderText(task.reminderText, currentDueAt, nextDueAt, now),
    )
    return "已顺延任务：${task.title}"
}

internal fun toggleTodoTask(
    tasks: MutableList<TodoTask>,
    taskIndex: Int,
    now: LocalDateTime = LocalDateTime.now(),
): String {
    val task = tasks.getOrNull(taskIndex) ?: return ""
    if (task.done) {
        tasks[taskIndex] = task.copy(done = false)
        return "已恢复任务：${task.title}"
    }
    val repeatedTask = advanceRepeatingTodoTask(task, now)
    return if (repeatedTask != null) {
        tasks[taskIndex] = repeatedTask
        "已续期重复任务：${task.title}"
    } else {
        tasks[taskIndex] = task.copy(done = true)
        "已完成任务：${task.title}"
    }
}

internal fun applyTodoFocusProgress(
    tasks: MutableList<TodoTask>,
    boundTaskTitle: String,
): Boolean {
    if (boundTaskTitle.isBlank()) return false
    val taskIndex = tasks.indexOfFirst { it.title == boundTaskTitle }
    if (taskIndex < 0) return false
    val task = tasks[taskIndex]
    val subtasks = task.subtasks.toMutableList()
    val pendingIndex = subtasks.indexOfFirst { !it.done }
    if (pendingIndex >= 0) {
        subtasks[pendingIndex] = subtasks[pendingIndex].copy(done = true)
    }
    val updatedFocusCount = task.focusCount + 1
    val focusGoalReached = task.focusGoal <= 0 || updatedFocusCount >= task.focusGoal
    val updatedTask = task.copy(
        subtasks = subtasks,
        focusCount = updatedFocusCount,
        done = if (subtasks.isNotEmpty()) {
            subtasks.all { it.done } && focusGoalReached
        } else {
            task.done || focusGoalReached && task.focusGoal > 0
        },
    )
    tasks[taskIndex] = if (updatedTask.done) advanceRepeatingTodoTask(updatedTask) ?: updatedTask else updatedTask
    return true
}

internal fun recordTodoFocusProgress(
    prefs: SharedPreferences,
    boundTaskTitle: String,
) {
    if (boundTaskTitle.isBlank()) return
    val tasks = loadTodoTasks(prefs).toMutableList()
    if (!applyTodoFocusProgress(tasks, boundTaskTitle)) return
    saveTodoTasks(prefs, tasks)
}
