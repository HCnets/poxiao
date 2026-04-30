package com.poxiao.app.ui

internal data class FreeTimeDaySummary(
    val dayLabel: String,
    val freeCount: Int,
    val labels: List<String>,
)

internal data class DayAnalysisSummary(
    val dayLabel: String,
    val highlights: List<String>,
)
