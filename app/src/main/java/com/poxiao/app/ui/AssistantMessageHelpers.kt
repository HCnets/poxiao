package com.poxiao.app.ui

import com.poxiao.app.data.AssistantContextSummary
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantToolCall
import com.poxiao.app.data.AssistantToolDefinition
import com.poxiao.app.data.ChatMessage

internal fun shouldExecuteReviewBridge(
    prompt: String,
    summaries: List<AssistantContextSummary>,
): Boolean {
    val normalized = prompt.lowercase()
    val hasReviewBridge = summaries.any { it.id == "review_bridge" }
    return hasReviewBridge && (
        normalized.contains("接管复习") ||
            normalized.contains("安排今天复习") ||
            normalized.contains("执行复习") ||
            normalized.contains("开始复习计划") ||
            normalized.contains("把复习计划落地")
        )
}

internal fun buildAssistantRequestPrompt(
    userInput: String,
    summaries: List<AssistantContextSummary>,
    availableTools: List<AssistantToolDefinition>,
    toolCall: AssistantToolCall?,
): String {
    return buildString {
        appendLine("你是“破晓”校园学习效率应用内的智能助理。")
        appendLine("请使用简体中文回答，优先基于给定的应用上下文提供直接、可执行的建议。")
        if (summaries.isNotEmpty()) {
            appendLine("【当前本地上下文】")
            summaries.sortedByDescending { it.priority }.take(6).forEach { summary ->
                appendLine("- ${summary.source}｜${summary.title}：${summary.body}")
            }
        }
        if (availableTools.isNotEmpty()) {
            appendLine("【当前可用的本地能力】")
            appendLine(availableTools.joinToString("、") { it.title })
        }
        toolCall?.let {
            appendLine("【刚执行的本地动作】")
            appendLine("- ${it.title}：${it.summary}")
        }
        appendLine("【用户问题】")
        append(userInput.trim())
    }.trim()
}

internal fun resolveAssistantReply(
    remoteReply: String,
    fallback: String,
    toolCall: AssistantToolCall?,
): String {
    val trimmed = remoteReply.trim()
    if (trimmed.isBlank()) return fallback
    val remoteUnavailable = trimmed.startsWith("星火助手暂时不可用")
    return if (remoteUnavailable && toolCall != null) {
        "$fallback\n\n$trimmed"
    } else {
        trimmed
    }
}

internal fun applyAssistantSummaryInjection(
    conversations: List<AssistantConversation>,
    activeConversationId: String,
    summary: AssistantContextSummary,
    now: Long = System.currentTimeMillis(),
): AssistantSummaryInjection? {
    val currentConversation = conversations.firstOrNull { it.id == activeConversationId } ?: return null
    val injectedMessage = ChatMessage(
        id = "context-${summary.id}-$now",
        role = "system",
        content = if (summary.id == "review_bridge" || summary.id == "review_exam_sync") {
            "已注入复习计划：${summary.body}"
        } else {
            "已注入上下文：${summary.source} · ${summary.title}\n${summary.body}"
        },
        timestamp = now,
    )
    val updatedMessages = (currentConversation.messages + injectedMessage).takeLast(24)
    val updatedConversations = conversations.map { conversation ->
        if (conversation.id == activeConversationId) {
            conversation.copy(
                updatedAt = now,
                messages = updatedMessages,
            )
        } else {
            conversation
        }
    }
    val prompt = when (summary.id) {
        "review_exam_sync" -> "请优先接管这组与考试周相关的复习冲刺项，并结合待办与番茄钟安排今天最稳的推进顺序。"
        "review_bridge" -> "请接管这组复习计划，并结合待办与番茄钟给我安排今天的推进顺序。"
        else -> "基于${summary.source}里的“${summary.title}”继续分析：${summary.body}"
    }
    return AssistantSummaryInjection(
        conversations = updatedConversations,
        prompt = prompt,
    )
}

internal fun appendAssistantSystemMessage(
    conversations: List<AssistantConversation>,
    activeConversationId: String,
    content: String,
    messagePrefix: String,
    now: Long = System.currentTimeMillis(),
): List<AssistantConversation> {
    val currentConversation = conversations.firstOrNull { it.id == activeConversationId } ?: return conversations
    val systemMessage = ChatMessage(
        id = "$messagePrefix-$now",
        role = "system",
        content = content,
        timestamp = now,
    )
    return conversations.map { conversation ->
        if (conversation.id == currentConversation.id) {
            conversation.copy(
                updatedAt = now,
                messages = (conversation.messages + systemMessage).takeLast(24),
            )
        } else {
            conversation
        }
    }
}

internal fun buildAssistantConversationSnapshot(
    conversations: List<AssistantConversation>,
    activeConversationId: String,
    prompt: String,
    now: Long = System.currentTimeMillis(),
): List<AssistantConversation> {
    return conversations.map { conversation ->
        if (conversation.id == activeConversationId) {
            conversation.copy(
                draftInput = prompt,
                updatedAt = now,
            )
        } else {
            conversation
        }
    }
}
