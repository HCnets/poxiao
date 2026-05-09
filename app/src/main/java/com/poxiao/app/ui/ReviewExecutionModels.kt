package com.poxiao.app.ui

internal data class ReviewBridgeExecutionSummary(
    val createdTaskIds: List<String>,
    val createdTaskTitles: List<String>,
    val linkedReviewItemIds: List<String>,
    val boundTaskTitle: String,
    val summary: String,
    val executedAt: Long,
    val replaySourceExecutedAt: Long = 0L,
    val diffSummary: String = "",
)
