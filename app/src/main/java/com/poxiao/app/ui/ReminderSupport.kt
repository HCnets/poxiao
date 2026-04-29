package com.poxiao.app.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal fun buildReminderPendingIntent(
    context: Context,
    reminderId: String,
    title: String,
    body: String,
): PendingIntent {
    val intent = Intent(context, LocalReminderReceiver::class.java).apply {
        putExtra("id", reminderId)
        putExtra("title", title)
        putExtra("body", body)
    }
    return PendingIntent.getBroadcast(
        context,
        reminderId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

internal fun buildCourseReminderPlans(
    authPrefs: android.content.SharedPreferences,
    fallbackPrefs: android.content.SharedPreferences,
    preferenceState: com.poxiao.app.settings.NotificationPreferenceState,
): List<ScheduledReminderPlan> {
    if (!preferenceState.courseEnabled) return emptyList()
    val state = loadPrimaryScheduleState(authPrefs, fallbackPrefs) ?: return emptyList()
    val now = LocalDateTime.now()
    return state.weekSchedule.courses.mapNotNull { course ->
        val day = state.weekSchedule.days.firstOrNull { it.weekDay == course.dayOfWeek } ?: return@mapNotNull null
        val slot = state.weekSchedule.timeSlots.firstOrNull { it.majorIndex == course.majorIndex } ?: return@mapNotNull null
        val classDate = runCatching { LocalDate.parse(day.fullDate.ifBlank { day.date }) }.getOrNull() ?: return@mapNotNull null
        val startClock = slot.timeRange.substringBefore("-").trim()
        val startTime = parseClock(startClock) ?: return@mapNotNull null
        val classTime = LocalDateTime.of(classDate, startTime)
        val remindAt = classTime.minusMinutes(preferenceState.courseLeadMinutes.toLong())
        if (remindAt.isBefore(now)) return@mapNotNull null
        ScheduledReminderPlan(
            id = "course_${day.fullDate}_${course.dayOfWeek}_${course.majorIndex}_${course.courseName.hashCode()}",
            title = "璇惧墠鎻愰啋",
            body = "${course.courseName} 路 ${slot.label} 路 ${course.classroom.ifBlank { "鏁欏寰呰ˉ锟? }}",
            triggerAtMillis = remindAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
    }
}

internal fun buildExamWeekReminderPlans(
    scheduleCachePrefs: android.content.SharedPreferences,
    scheduleAuthPrefs: android.content.SharedPreferences,
    examWeekPrefs: android.content.SharedPreferences,
    preferenceState: com.poxiao.app.settings.NotificationPreferenceState,
): List<ScheduledReminderPlan> {
    if (!preferenceState.examEnabled) return emptyList()
    val state = loadPrimaryScheduleState(scheduleAuthPrefs, scheduleCachePrefs) ?: return emptyList()
    val items = buildExamWeekItems(
        state.weekSchedule,
        loadPrimaryScheduleEvents(scheduleAuthPrefs, scheduleCachePrefs),
        loadStringList(examWeekPrefs, "completed_ids"),
    )
    val now = LocalDateTime.now()
    return items.filterNot { it.finished }.mapNotNull { item ->
        val date = runCatching { LocalDate.parse(item.date) }.getOrNull() ?: return@mapNotNull null
        val remindAt = when (preferenceState.examPreset) {
            "褰撳ぉ锟?08:00" -> LocalDateTime.of(date, LocalTime.of(8, 0))
            "鎻愬墠 3 灏忔椂" -> LocalDateTime.of(date, LocalTime.of(9, 0))
            else -> LocalDateTime.of(date.minusDays(1), LocalTime.of(20, 0))
        }
        if (remindAt.isBefore(now)) return@mapNotNull null
        ScheduledReminderPlan(
            id = "exam_${item.id}",
            title = "鑰冭瘯鍛ㄦ彁锟?,
            body = "${item.title} 路 ${item.countdownLabel}",
            triggerAtMillis = remindAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
    }
}

internal fun parseClock(raw: String): LocalTime? {
    val normalized = raw.trim()
    val parts = normalized.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)) }.getOrNull()
}
