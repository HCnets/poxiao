package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
import com.poxiao.app.data.AssistantContextSummary
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantMockExecution
import com.poxiao.app.data.AssistantPermissionState
import com.poxiao.app.data.AssistantToolCall
import com.poxiao.app.data.AssistantToolDefinition
import com.poxiao.app.data.AssistantToolKit
import com.poxiao.app.data.ChatMessage
import com.poxiao.app.data.HiagentGateway
import com.poxiao.app.review.ReviewPlannerSeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal fun resolveHomeAssistantLocalExecution(
    context: Context,
    input: String,
    assistantSummaries: List<AssistantContextSummary>,
    toolKit: AssistantToolKit,
    assistantPermissionState: AssistantPermissionState,
    assistantBridgePrefs: SharedPreferences,
    currentReviewExecutionSummary: ReviewBridgeExecutionSummary?,
    currentReviewExecutionHistory: List<ReviewBridgeExecutionSummary>,
): HomeAssistantLocalExecutionResult {
    if (!shouldExecuteReviewBridge(input, assistantSummaries)) {
        return HomeAssistantLocalExecutionResult(
            localExecution = toolKit.runMock(input, assistantPermissionState, assistantSummaries),
            reviewExecutionSummary = currentReviewExecutionSummary,
            reviewExecutionHistory = currentReviewExecutionHistory,
        )
    }

    val executionSummary = applyReviewBridgeExecution(context)
    return HomeAssistantLocalExecutionResult(
        localExecution = AssistantMockExecution(
            toolCall = AssistantToolCall(
                id = "tool-${System.currentTimeMillis()}",
                title = "执行复习计划",
                status = "完成",
                summary = executionSummary,
                timestamp = System.currentTimeMillis(),
            ),
            reply = "我已经按待接管复习计划完成了首轮落地：$executionSummary 接下来你可以直接去待办和番茄钟开始执行。",
        ),
        reviewExecutionSummary = loadReviewBridgeExecutionSummary(assistantBridgePrefs),
        reviewExecutionHistory = loadReviewBridgeExecutionHistory(assistantBridgePrefs),
    )
}

internal fun buildAssistantSendPreparation(
    conversations: List<AssistantConversation>,
    activeConversationId: String,
    input: String,
    assistantSummaries: List<AssistantContextSummary>,
    assistantTools: List<AssistantToolDefinition>,
    localExecution: AssistantMockExecution,
    now: Long = System.currentTimeMillis(),
): AssistantSendPreparation? {
    val currentConversation = conversations.firstOrNull { it.id == activeConversationId } ?: return null
    val userMessage = ChatMessage(
        id = "user-$now",
        role = "user",
        content = input,
        timestamp = now,
    )
    val pendingId = "assistant-pending-$now"
    val pendingMessage = ChatMessage(
        id = pendingId,
        role = "assistant",
        content = "正在整理你的问题...",
        timestamp = now,
    )
    val updatedMessages = currentConversation.messages + userMessage + pendingMessage
    val updatedTitle = currentConversation.title
        .takeIf { it != "智能体" && !it.startsWith("会话 ") }
        ?: input.take(12)

    return AssistantSendPreparation(
        targetConversationId = activeConversationId,
        pendingId = pendingId,
        requestPayload = buildAssistantRequestPrompt(
            userInput = input,
            summaries = assistantSummaries,
            availableTools = assistantTools,
            toolCall = localExecution.toolCall,
        ),
        requestHistory = currentConversation.messages.takeLast(6),
        seededConversations = conversations.map { conversation ->
            if (conversation.id == activeConversationId) {
                conversation.copy(
                    title = updatedTitle,
                    draftInput = "",
                    updatedAt = now,
                    messages = updatedMessages,
                    toolCalls = conversation.toolCalls + listOfNotNull(localExecution.toolCall),
                )
            } else {
                conversation
            }
        },
    )
}

internal suspend fun requestAssistantReply(
    gateway: HiagentGateway,
    requestPayload: String,
    requestHistory: List<ChatMessage>,
): ChatMessage {
    return runCatching {
        gateway.sendText(
            message = requestPayload,
            history = requestHistory,
        )
    }.getOrElse {
        ChatMessage(
            id = "assistant-error-${System.currentTimeMillis()}",
            role = "assistant",
            content = "星火助手暂时不可用，请稍后再试。",
            timestamp = System.currentTimeMillis(),
        )
    }
}

internal fun buildAssistantReplyCheckpoints(
    stagedContent: String,
): List<String> {
    return listOf(
        stagedContent.take((stagedContent.length * 0.35f).toInt().coerceAtLeast(1)),
        stagedContent.take((stagedContent.length * 0.7f).toInt().coerceAtLeast(1)),
        stagedContent,
    ).distinct()
}

internal fun applyAssistantReplyChunk(
    conversations: List<AssistantConversation>,
    targetConversationId: String,
    pendingId: String,
    remoteReply: ChatMessage,
    chunk: String,
    finalChunk: Boolean,
    now: Long = System.currentTimeMillis(),
): List<AssistantConversation> {
    return conversations.map { conversation ->
        if (conversation.id == targetConversationId) {
            conversation.copy(
                updatedAt = now,
                messages = conversation.messages.map { message ->
                    val isPendingReply = message.id == pendingId || message.id == remoteReply.id
                    if (isPendingReply) {
                        message.copy(
                            id = if (finalChunk) remoteReply.id else pendingId,
                            content = chunk,
                            timestamp = remoteReply.timestamp,
                        )
                    } else {
                        message
                    }
                },
            )
        } else {
            conversation
        }
    }
}

internal fun buildReviewPlannerSeed(
    execution: ReviewBridgeExecutionSummary,
): ReviewPlannerSeed {
    return ReviewPlannerSeed(
        query = execution.createdTaskTitles.firstOrNull()?.removePrefix("复习：").orEmpty(),
        focusTitle = execution.boundTaskTitle.ifBlank {
            execution.createdTaskTitles.firstOrNull().orEmpty()
        },
    )
}

internal fun performUndoReviewExecution(
    context: Context,
    assistantBridgePrefs: SharedPreferences,
    conversations: List<AssistantConversation>,
    activeConversationId: String,
    execution: ReviewBridgeExecutionSummary,
    expandedReviewExecutionAt: Long?,
): ReviewExecutionActionResult {
    val result = undoReviewBridgeExecution(context)
    val latestSummary = loadReviewBridgeExecutionSummary(assistantBridgePrefs)
    val latestHistory = loadReviewBridgeExecutionHistory(assistantBridgePrefs)
    val nextExpandedAt = if (expandedReviewExecutionAt == execution.executedAt) null else expandedReviewExecutionAt
    val updatedConversations = appendAssistantSystemMessage(
        conversations = conversations,
        activeConversationId = activeConversationId,
        content = result,
        messagePrefix = "undo-review",
    )
    return ReviewExecutionActionResult(
        reviewExecutionSummary = latestSummary,
        reviewExecutionHistory = latestHistory,
        expandedReviewExecutionAt = nextExpandedAt,
        conversations = updatedConversations,
    )
}

internal fun performReplayReviewExecution(
    context: Context,
    assistantBridgePrefs: SharedPreferences,
    conversations: List<AssistantConversation>,
    activeConversationId: String,
    execution: ReviewBridgeExecutionSummary,
): ReviewExecutionActionResult {
    val result = replayReviewBridgeExecution(context, execution)
    val latestSummary = loadReviewBridgeExecutionSummary(assistantBridgePrefs)
    val latestHistory = loadReviewBridgeExecutionHistory(assistantBridgePrefs)
    val updatedConversations = appendAssistantSystemMessage(
        conversations = conversations,
        activeConversationId = activeConversationId,
        content = result,
        messagePrefix = "replay-review",
    )
    return ReviewExecutionActionResult(
        reviewExecutionSummary = latestSummary,
        reviewExecutionHistory = latestHistory,
        expandedReviewExecutionAt = latestSummary?.executedAt,
        conversations = updatedConversations,
    )
}

internal fun applyHomeAssistantConversationCreation(
    assistantStore: com.poxiao.app.data.AssistantSessionStore,
    assistantUiState: HomeAssistantUiState,
) {
    val creation = createHomeAssistantConversation(
        assistantStore = assistantStore,
        conversations = assistantUiState.conversations,
    )
    assistantUiState.conversations.clear()
    assistantUiState.conversations.addAll(creation.conversations)
    assistantUiState.activeConversationId.value = creation.activeConversationId
    assistantUiState.prompt.value = creation.prompt
}

internal fun applyHomeAssistantSummaryInjection(
    assistantUiState: HomeAssistantUiState,
    summary: AssistantContextSummary,
) {
    val injection = applyAssistantSummaryInjection(
        conversations = assistantUiState.conversations,
        activeConversationId = assistantUiState.activeConversationId.value,
        summary = summary,
    ) ?: return
    assistantUiState.conversations.clear()
    assistantUiState.conversations.addAll(injection.conversations)
    assistantUiState.prompt.value = injection.prompt
}

internal fun applyHomeReviewUndoAction(
    context: Context,
    assistantBridgePrefs: SharedPreferences,
    assistantUiState: HomeAssistantUiState,
    execution: ReviewBridgeExecutionSummary,
) {
    val actionResult = performUndoReviewExecution(
        context = context,
        assistantBridgePrefs = assistantBridgePrefs,
        conversations = assistantUiState.conversations,
        activeConversationId = assistantUiState.activeConversationId.value,
        execution = execution,
        expandedReviewExecutionAt = assistantUiState.expandedReviewExecutionAt.value,
    )
    assistantUiState.reviewExecutionSummary.value = actionResult.reviewExecutionSummary
    assistantUiState.reviewExecutionHistory.value = actionResult.reviewExecutionHistory
    assistantUiState.expandedReviewExecutionAt.value = actionResult.expandedReviewExecutionAt
    assistantUiState.conversations.clear()
    assistantUiState.conversations.addAll(actionResult.conversations)
}

internal fun applyHomeReviewReplayAction(
    context: Context,
    assistantBridgePrefs: SharedPreferences,
    assistantUiState: HomeAssistantUiState,
    execution: ReviewBridgeExecutionSummary,
) {
    val actionResult = performReplayReviewExecution(
        context = context,
        assistantBridgePrefs = assistantBridgePrefs,
        conversations = assistantUiState.conversations,
        activeConversationId = assistantUiState.activeConversationId.value,
        execution = execution,
    )
    assistantUiState.reviewExecutionSummary.value = actionResult.reviewExecutionSummary
    assistantUiState.reviewExecutionHistory.value = actionResult.reviewExecutionHistory
    assistantUiState.expandedReviewExecutionAt.value = actionResult.expandedReviewExecutionAt
    assistantUiState.conversations.clear()
    assistantUiState.conversations.addAll(actionResult.conversations)
}

internal fun applyHomeAssistantSend(
    scope: CoroutineScope,
    gateway: HiagentGateway,
    context: Context,
    toolKit: AssistantToolKit,
    assistantBridgePrefs: SharedPreferences,
    assistantUiState: HomeAssistantUiState,
    assistantSummaries: List<AssistantContextSummary>,
    assistantTools: List<AssistantToolDefinition>,
) {
    val prompt = assistantUiState.prompt.value
    if (prompt.isBlank() || assistantUiState.assistantBusy.value) return
    val input = prompt
    val localExecutionResult = resolveHomeAssistantLocalExecution(
        context = context,
        input = input,
        assistantSummaries = assistantSummaries,
        toolKit = toolKit,
        assistantPermissionState = assistantUiState.assistantPermissionState.value,
        assistantBridgePrefs = assistantBridgePrefs,
        currentReviewExecutionSummary = assistantUiState.reviewExecutionSummary.value,
        currentReviewExecutionHistory = assistantUiState.reviewExecutionHistory.value,
    )
    assistantUiState.reviewExecutionSummary.value = localExecutionResult.reviewExecutionSummary
    assistantUiState.reviewExecutionHistory.value = localExecutionResult.reviewExecutionHistory
    val preparation = buildAssistantSendPreparation(
        conversations = assistantUiState.conversations,
        activeConversationId = assistantUiState.activeConversationId.value,
        input = input,
        assistantSummaries = assistantSummaries,
        assistantTools = assistantTools,
        localExecution = localExecutionResult.localExecution,
    ) ?: return
    assistantUiState.conversations.clear()
    assistantUiState.conversations.addAll(preparation.seededConversations)
    assistantUiState.prompt.value = ""
    assistantUiState.assistantBusy.value = true
    scope.launch {
        try {
            val remoteReply = requestAssistantReply(
                gateway = gateway,
                requestPayload = preparation.requestPayload,
                requestHistory = preparation.requestHistory,
            )
            val stagedContent = resolveAssistantReply(
                remoteReply = remoteReply.content,
                fallback = localExecutionResult.localExecution.reply,
                toolCall = localExecutionResult.localExecution.toolCall,
            )
            val checkpoints = buildAssistantReplyCheckpoints(stagedContent)
            checkpoints.forEachIndexed { index, chunk ->
                val streamedConversations = applyAssistantReplyChunk(
                    conversations = assistantUiState.conversations,
                    targetConversationId = preparation.targetConversationId,
                    pendingId = preparation.pendingId,
                    remoteReply = remoteReply,
                    chunk = chunk,
                    finalChunk = index == checkpoints.lastIndex,
                )
                assistantUiState.conversations.clear()
                assistantUiState.conversations.addAll(streamedConversations)
                if (index != checkpoints.lastIndex) delay(180)
            }
        } finally {
            assistantUiState.assistantBusy.value = false
        }
    }
}
