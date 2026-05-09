package com.poxiao.app.ui

internal data class PomodoroSession(
    val mode: PomodoroMode,
    val presetTitle: String,
    val presetSeconds: Int,
    val leftSeconds: Int,
    val running: Boolean,
    val autoNext: Boolean,
    val strictMode: Boolean,
    val boundTask: String,
    val sound: String,
    val ambientOn: Boolean,
    val focusMinutes: Int,
    val cycles: Int,
    val customMinutes: Int,
    val customSeconds: Int,
    val onlyPendingGoalTasks: Boolean,
)
