package com.poxiao.app.ui

import android.content.SharedPreferences

private const val ReminderScheduleIdsKey = "scheduled_ids"
private const val ReminderScheduleSnapshotKey = "scheduled_snapshot"

internal data class ReminderScheduleSnapshot(
    val ids: List<String>,
    val signature: String,
)

internal fun loadReminderScheduleSnapshot(
    prefs: SharedPreferences,
): ReminderScheduleSnapshot {
    return ReminderScheduleSnapshot(
        ids = prefs.getString(ReminderScheduleIdsKey, "").orEmpty()
            .split("|")
            .filter { it.isNotBlank() },
        signature = prefs.getString(ReminderScheduleSnapshotKey, "").orEmpty(),
    )
}

internal fun saveReminderScheduleSnapshot(
    prefs: SharedPreferences,
    snapshot: ReminderScheduleSnapshot,
) {
    prefs.edit()
        .putString(ReminderScheduleIdsKey, snapshot.ids.joinToString("|"))
        .putString(ReminderScheduleSnapshotKey, snapshot.signature)
        .apply()
}

internal fun buildReminderScheduleSnapshot(
    plans: List<ScheduledReminderPlan>,
): ReminderScheduleSnapshot {
    return ReminderScheduleSnapshot(
        ids = plans.map { it.id },
        signature = plans.joinToString("|") { plan ->
            "${plan.id}@${plan.triggerAtMillis}@${plan.title.hashCode()}@${plan.body.hashCode()}"
        },
    )
}
