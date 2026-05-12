package com.poxiao.app.data.agent

import com.poxiao.app.data.AssistantCapability
import com.poxiao.app.data.AssistantProviderType
import com.poxiao.app.data.ChatMessage
import com.poxiao.app.data.HiagentGateway
import java.util.UUID

/**
 * 多智能体路由网关 —— 桥接 [HiagentGateway] 旧接口与 [MultiAgentRouter] 新架构。
 *
 * 该类实现了原有的 [HiagentGateway] 接口，内部通过 [MultiAgentRouter]
 * 进行意图分类与分发，使得现有 UI 层无需任何改动即可享受多模型路由能力。
 *
 * ## 流式支持
 * 通过 [onChunk] 回调实现逐字流式输出，外部可通过构造函数注入
 * 自定义流式处理逻辑（如更新 UI 状态）。
 *
 * @param router       多智能体路由器
 * @param onStreamChunk 流式分片回调（可选），用于 UI 实时更新
 */
class RoutedHiagentGateway(
    private val router: MultiAgentRouter,
    private val onStreamChunk: ((String) -> Unit)? = null,
) : HiagentGateway {

    override suspend fun sendText(
        message: String,
        history: List<ChatMessage>,
    ): ChatMessage {
        val now = System.currentTimeMillis()
        val trimmed = message.trim()

        if (trimmed.isBlank()) {
            return ChatMessage(
                id = "assistant-empty-$now",
                role = "assistant",
                content = "请输入你想咨询的问题。",
                timestamp = now,
            )
        }

        // 转换 ChatMessage -> Message
        val messages = history.map {
            Message(role = it.role, content = it.content)
        } + Message(role = "user", content = trimmed)

        // 预判路由目标，给出系统提示
        val targetProvider = router.classify(trimmed)
        val systemPrompt = if (targetProvider == AssistantProviderType.DEEPSEEK) {
            "【DeepSeek 推理引擎已接管】\n"
        } else {
            "" // 星火处理日常，不需要特别提示
        }
        
        if (systemPrompt.isNotEmpty()) {
            onStreamChunk?.invoke(systemPrompt)
        }

        // 通过路由器分发
        val fullContent = StringBuilder(systemPrompt)
        val routeResult = router.route(messages) { chunk ->
            fullContent.append(chunk)
            onStreamChunk?.invoke(chunk)
        }

        return if (routeResult.isSuccess) {
            ChatMessage(
                id = "assistant-${UUID.randomUUID()}",
                role = "assistant",
                content = fullContent.toString().trim().ifBlank {
                    "智能体没有返回有效内容，请稍后再试。"
                },
                timestamp = System.currentTimeMillis(),
            )
        } else {
            ChatMessage(
                id = "assistant-error-$now",
                role = "assistant",
                content = buildErrorText(routeResult),
                timestamp = System.currentTimeMillis(),
            )
        }
    }

    override suspend fun sendVoice(audioUri: String): ChatMessage {
        return ChatMessage(
            id = "assistant-voice-${System.currentTimeMillis()}",
            role = "assistant",
            content = "当前接入的是多智能体路由中枢，语音入口暂未启用。",
            timestamp = System.currentTimeMillis(),
        )
    }

    override suspend fun sendImage(imageUri: String, prompt: String): ChatMessage {
        return ChatMessage(
            id = "assistant-image-${System.currentTimeMillis()}",
            role = "assistant",
            content = "当前接入的是多智能体路由中枢，图像入口暂未启用。",
            timestamp = System.currentTimeMillis(),
        )
    }

    override fun capability(): AssistantCapability {
        return AssistantCapability(
            voiceEnabled = false,
            textEnabled = true,
            imageEnabled = false,
        )
    }

    override fun providerType(): AssistantProviderType {
        // 路由模式下返回 PREVIEW 表示多模型混合
        return AssistantProviderType.PREVIEW
    }

    // ──── 辅助方法 ────

    private fun buildErrorText(result: RouteResult): String {
        val providerName = when (result.provider) {
            AssistantProviderType.DEEPSEEK -> "DeepSeek"
            AssistantProviderType.SPARK -> "星火助手"
            else -> "智能体"
        }
        val detail = result.error.orEmpty().trim()
        return if (detail.isBlank()) {
            "$providerName 暂时不可用，请稍后再试。"
        } else {
            "$providerName 暂时不可用，原因：$detail"
        }
    }
}
