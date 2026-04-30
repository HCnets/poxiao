package com.poxiao.app.ui

internal enum class PomodoroMode(val title: String) {
    Focus("专注"),
    ShortBreak("短休息"),
    LongBreak("长休息"),
}

internal data class PomodoroPreset(
    val title: String,
    val seconds: Int,
)
