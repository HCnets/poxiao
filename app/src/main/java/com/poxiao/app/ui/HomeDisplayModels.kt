package com.poxiao.app.ui

import androidx.compose.ui.graphics.Color

internal data class HomeLineData(
    val timeLabel: String,
    val title: String,
    val subtitle: String,
)

internal data class HomeHeroState(
    val badge: String,
    val headline: String,
    val detail: String,
    val accent: Color,
)

internal data class HomeWelcomeSummary(
    val nextCourseSubtitle: String,
    val priorityTodoTitle: String,
    val urgentReviewTitle: String,
    val pomodoroSubtitle: String,
)
