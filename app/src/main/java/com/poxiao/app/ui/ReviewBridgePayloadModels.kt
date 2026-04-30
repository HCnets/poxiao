package com.poxiao.app.ui

internal data class PendingReviewBridgeItem(
    val id: String,
    val courseName: String,
    val noteTitle: String,
    val sourceTitle: String,
    val recommendedMinutes: Int,
    val nextReviewAt: Long,
)

internal data class PendingReviewBridgePayload(
    val reason: String,
    val prompt: String,
    val items: List<PendingReviewBridgeItem>,
)
