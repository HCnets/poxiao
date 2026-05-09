package com.poxiao.app.ui

internal data class ScheduledReminderPlan(
    val id: String,
    val title: String,
    val body: String,
    val triggerAtMillis: Long,
)
