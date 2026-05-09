package com.poxiao.app.ui

internal data class FocusDayStat(
    val label: String,
    val minutes: Int,
)

internal data class FocusTaskStat(
    val title: String,
    val minutes: Int,
    val count: Int,
)
