package com.poxiao.app.ui

import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val ReviewReminderUrgentWindowMinutes = 15L
private const val ReviewReminderSameDayLongLeadHours = 3L
private const val ReviewReminderImmediateLeadMinutes = 5L

internal fun buildReviewBridgeTodoTask(
    item: PendingReviewBridgeItem,
    now: LocalDateTime = LocalDateTime.now(),
): TodoTask {
    val dueAt = reviewDueDateTime(item.nextReviewAt, now)
    return TodoTask(
        id = "assistant-review-${item.id}",
        title = "复习：${item.noteTitle}",
        note = "${item.courseName} · 来源《${item.sourceTitle}》\n由智能体接管复习计划生成。",
        quadrant = TodoQuadrant.ImportantNotUrgent,
        priority = TodoPriority.High,
        dueText = buildReviewTodoDueText(item.nextReviewAt, now),
        tags = listOf("复习", "智能排程", item.courseName),
        listName = "复习计划",
        reminderText = buildReviewTodoReminderText(
            dueAt = dueAt,
            recommendedMinutes = item.recommendedMinutes,
            mastery = item.mastery,
            importanceScore = item.importanceScore,
            now = now,
        ),
        repeatText = "不重复",
        subtasks = listOf(
            TodoSubtask("回看知识点"),
            TodoSubtask("完成一轮口述或默写"),
        ),
        focusGoal = reviewTodoFocusGoal(item.recommendedMinutes),
    )
}

internal fun buildReviewReplayTodoTask(
    replayId: String,
    title: String,
    sourceExecutedAt: Long,
    now: LocalDateTime = LocalDateTime.now(),
): TodoTask {
    val dueAt = now.withHour(20).withMinute(0).withSecond(0).withNano(0)
        .let { if (it.isAfter(now)) it else it.plusDays(1) }
    return TodoTask(
        id = replayId,
        title = title,
        note = "根据历史接管记录回放生成。\n来源执行时间：${
            formatSyncTime(
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(sourceExecutedAt),
                    java.time.ZoneId.systemDefault(),
                ),
            )
        }",
        quadrant = TodoQuadrant.ImportantNotUrgent,
        priority = TodoPriority.High,
        dueText = formatReviewTodoDueText(dueAt, now),
        tags = listOf("复习", "历史回放"),
        listName = "复习计划",
        reminderText = buildReviewTodoReminderText(
            dueAt = dueAt,
            recommendedMinutes = 25,
            now = now
        ),
        repeatText = "不重复",
        subtasks = listOf(
            TodoSubtask("回看知识点"),
            TodoSubtask("完成一轮口述或默写"),
        ),
        focusGoal = 1,
    )
}

internal fun reviewTodoFocusGoal(minutes: Int): Int {
    return when {
        minutes >= 50 -> 3
        minutes >= 30 -> 2
        else -> 1
    }
}

internal fun buildReviewTodoDueText(
    nextReviewAt: Long,
    now: LocalDateTime = LocalDateTime.now(),
): String {
    return formatReviewTodoDueText(reviewDueDateTime(nextReviewAt, now), now)
}

internal fun buildReviewTodoReminderText(
    dueAt: LocalDateTime,
    recommendedMinutes: Int,
    mastery: Float = 1.0f,
    importanceScore: Int = 0,
    now: LocalDateTime = LocalDateTime.now(),
): String {
    if (!dueAt.isAfter(now)) {
        return formatTodoDateTime(now.plusMinutes(ReviewReminderImmediateLeadMinutes))
    }

    val minutesUntilDue = Duration.between(now, dueAt).toMinutes()
    if (minutesUntilDue <= ReviewReminderUrgentWindowMinutes) {
        val candidate = dueAt.minusMinutes(1).takeIf { it.isAfter(now) } ?: now.plusMinutes(1)
        return formatTodoDateTime(candidate)
    }

    // 智能偏移逻辑：掌握度越低，或者重要度越高，提醒越提前
    val masteryLeadMinutes = if (mastery < 0.4f) 15 else 0
    val importanceLeadMinutes = if (importanceScore >= 24) 15 else 0
    val totalLeadMinutes = masteryLeadMinutes + importanceLeadMinutes

    val today = now.toLocalDate()
    return when (dueAt.toLocalDate()) {
        today -> {
            if (
                recommendedMinutes >= 45 ||
                Duration.between(now, dueAt).toHours() >= ReviewReminderSameDayLongLeadHours ||
                totalLeadMinutes >= 15
            ) {
                TodoReminderPreset.Before1Hour.title
            } else {
                TodoReminderPreset.Before30Min.title
            }
        }
        today.plusDays(1) -> {
            if (recommendedMinutes >= 45 || importanceScore >= 24) {
                TodoReminderPreset.PreviousNight.title
            } else {
                TodoReminderPreset.Before1Hour.title
            }
        }
        else -> TodoReminderPreset.PreviousNight.title
    }
}

private fun reviewDueDateTime(
    nextReviewAt: Long,
    now: LocalDateTime,
): LocalDateTime {
    return runCatching {
        Instant.ofEpochMilli(nextReviewAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
    }.getOrElse { now.withHour(20).withMinute(0).withSecond(0).withNano(0) }
}

private fun formatReviewTodoDueText(
    dueAt: LocalDateTime,
    now: LocalDateTime,
): String {
    val today = now.toLocalDate()
    return when (dueAt.toLocalDate()) {
        today -> "今天 ${dueAt.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        today.plusDays(1) -> "明天 ${dueAt.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        else -> dueAt.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
    }
}
