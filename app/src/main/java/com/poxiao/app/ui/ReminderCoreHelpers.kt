package com.poxiao.app.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.poxiao.app.notifications.LocalReminderReceiver
import com.poxiao.app.settings.loadNotificationPreferenceState

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

internal fun refreshLocalReminderSchedule(context: Context) {
    val schedulerPrefs = context.getSharedPreferences("reminder_scheduler", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val scheduleCachePrefs = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)
    val scheduleAuthPrefs = context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE)
    val examWeekPrefs = context.getSharedPreferences("schedule_exam_week", Context.MODE_PRIVATE)
    val preferenceState = loadNotificationPreferenceState(context)
    val now = System.currentTimeMillis()
    val plans = buildTodoReminderPlans(todoPrefs, preferenceState)
        .plus(buildCourseReminderPlans(scheduleAuthPrefs, scheduleCachePrefs, preferenceState))
        .plus(buildExamWeekReminderPlans(scheduleCachePrefs, scheduleAuthPrefs, examWeekPrefs, preferenceState))
        .filter { it.triggerAtMillis > now }
        .sortedBy { it.triggerAtMillis }
        .distinctBy { it.id }
        .take(48)
    val nextSnapshot = buildReminderScheduleSnapshot(plans)
    val previousSnapshot = loadReminderScheduleSnapshot(schedulerPrefs)
    if (nextSnapshot == previousSnapshot) return
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    previousSnapshot.ids.forEach { id ->
        alarmManager.cancel(buildReminderPendingIntent(context, id, "", ""))
    }
    plans.forEach { plan ->
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            plan.triggerAtMillis,
            buildReminderPendingIntent(context, plan.id, plan.title, plan.body),
        )
    }
    saveReminderScheduleSnapshot(schedulerPrefs, nextSnapshot)
}
