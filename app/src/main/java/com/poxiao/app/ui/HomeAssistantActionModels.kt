package com.poxiao.app.ui

import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantMockExecution
import com.poxiao.app.data.ChatMessage

internal data class HomeAssistantLocalExecutionResult(
    val localExecution: AssistantMockExecution,
    val reviewExecutionSummary: ReviewBridgeExecutionSummary?,
    val reviewExecutionHistory: List<ReviewBridgeExecutionSummary>,
)

internal data class AssistantSendPreparation(
    val targetConversationId: String,
    val pendingId: String,
    val requestPayload: String,
    val requestHistory: List<ChatMessage>,
    val seededConversations: List<AssistantConversation>,
)

internal data class ReviewExecutionActionResult(
    val reviewExecutionSummary: ReviewBridgeExecutionSummary?,
    val reviewExecutionHistory: List<ReviewBridgeExecutionSummary>,
    val expandedReviewExecutionAt: Long?,
    val conversations: List<AssistantConversation>,
)
