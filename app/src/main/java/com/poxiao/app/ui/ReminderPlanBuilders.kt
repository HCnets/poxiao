package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.settings.NotificationPreferenceState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal fun buildCourseReminderPlans(
    authPrefs: SharedPreferences,
    fallbackPrefs: SharedPreferences,
    preferenceState: NotificationPreferenceState,
): List<ScheduledReminderPlan> {
    if (!preferenceState.courseEnabled) return emptyList()
    val state = loadPrimaryScheduleState(authPrefs, fallbackPrefs) ?: return emptyList()
    val now = LocalDateTime.now()
    return state.weekSchedule.courses.mapNotNull { course ->
        val day = state.weekSchedule.days.firstOrNull { it.weekDay == course.dayOfWeek } ?: return@mapNotNull null
        val slot = state.weekSchedule.timeSlots.firstOrNull { it.majorIndex == course.majorIndex } ?: return@mapNotNull null
        val classDate = runCatching { LocalDate.parse(day.fullDate.ifBlank { day.date }) }.getOrNull() ?: return@mapNotNull null
        val startClock = slot.timeRange.substringBefore("-").trim()
        val startTime = parseReminderClock(startClock) ?: return@mapNotNull null
        val classTime = LocalDateTime.of(classDate, startTime)
        val remindAt = classTime.minusMinutes(preferenceState.courseLeadMinutes.toLong())
        if (remindAt.isBefore(now)) return@mapNotNull null
        ScheduledReminderPlan(
            id = "course_${day.fullDate}_${course.dayOfWeek}_${course.majorIndex}_${course.courseName.hashCode()}",
            title = "课前提醒",
            body = "${course.courseName} · ${slot.label} · ${course.classroom.ifBlank { "教室待补充" }}",
            triggerAtMillis = remindAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
    }
}

internal fun buildExamWeekReminderPlans(
    scheduleCachePrefs: SharedPreferences,
    scheduleAuthPrefs: SharedPreferences,
    examWeekPrefs: SharedPreferences,
    preferenceState: NotificationPreferenceState,
): List<ScheduledReminderPlan> {
    if (!preferenceState.examEnabled) return emptyList()
    val state = loadPrimaryScheduleState(scheduleAuthPrefs, scheduleCachePrefs) ?: return emptyList()
    val items = buildExamWeekItems(
        state.weekSchedule,
        loadPrimaryScheduleEvents(scheduleAuthPrefs, scheduleCachePrefs),
        loadReminderStringList(examWeekPrefs, "completed_ids"),
    )
    val now = LocalDateTime.now()
    return items.filterNot { it.finished }.mapNotNull { item ->
        val date = runCatching { LocalDate.parse(item.date) }.getOrNull() ?: return@mapNotNull null
        val remindAt = when (preferenceState.examPreset) {
            "当天早 08:00" -> LocalDateTime.of(date, LocalTime.of(8, 0))
            "提前 3 小时" -> LocalDateTime.of(date, LocalTime.of(9, 0))
            else -> LocalDateTime.of(date.minusDays(1), LocalTime.of(20, 0))
        }
        if (remindAt.isBefore(now)) return@mapNotNull null
        ScheduledReminderPlan(
            id = "exam_${item.id}",
            title = "考试周提醒",
            body = "${item.title} · ${item.countdownLabel}",
            triggerAtMillis = remindAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
    }
}

internal fun buildTodoReminderPlans(
    prefs: SharedPreferences,
    preferenceState: NotificationPreferenceState,
): List<ScheduledReminderPlan> {
    if (!preferenceState.todoEnabled) return emptyList()
    if (!prefs.contains("todo_tasks")) return emptyList()
    val now = LocalDateTime.now()
    return loadTodoTasks(prefs)
        .filterNot { it.done }
        .mapNotNull { task ->
            val dueAt = parseTodoDateTime(task.dueText, now) ?: return@mapNotNull null
            val remindAt = parseReminderDateTime(task.reminderText, dueAt, now) ?: return@mapNotNull null
            ScheduledReminderPlan(
                id = "todo_${task.id}",
                title = "待办提醒",
                body = "${task.title} · ${task.dueText}",
                triggerAtMillis = remindAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            )
        }
}

private fun parseReminderClock(raw: String): LocalTime? {
    val parts = raw.trim().split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalTime.of(hour, minute) }.getOrNull()
}

private fun loadReminderStringList(
    prefs: SharedPreferences,
    key: String,
): List<String> {
    val raw = prefs.getString(key, "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}
