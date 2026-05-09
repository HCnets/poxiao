package com.poxiao.app.ui

import com.poxiao.app.data.FeedCard

internal data class GradeTrendPoint(
    val termName: String,
    val averageScore: Double,
    val averageGradePoint: Double,
    val credits: Double,
    val excellentCount: Int,
    val warningCount: Int,
    val courseCount: Int,
    val rawCards: List<FeedCard>,
)
