package com.poxiao.app.data

import com.poxiao.app.BuildConfig
import com.poxiao.app.data.agent.DeepSeekAgentClient
import com.poxiao.app.data.agent.MultiAgentRouter
import com.poxiao.app.data.agent.RoutedHiagentGateway
import com.poxiao.app.data.agent.SparkAgentClient

data class DeepSeekAssistantConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
) {
    fun isReady(): Boolean {
        return baseUrl.isNotBlank() && apiKey.isNotBlank() && model.isNotBlank()
    }

    companion object {
        fun fromBuildConfig(): DeepSeekAssistantConfig {
            return DeepSeekAssistantConfig(
                baseUrl = BuildConfig.DEEPSEEK_BASE_URL,
                apiKey = BuildConfig.DEEPSEEK_API_KEY,
                model = BuildConfig.DEEPSEEK_MODEL,
            )
        }
    }
}

class DeepSeekAssistantGateway(
    private val config: DeepSeekAssistantConfig = DeepSeekAssistantConfig.fromBuildConfig(),
) : HiagentGateway {
    override suspend fun sendText(message: String, history: List<ChatMessage>): ChatMessage {
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
        val content = if (config.isReady()) {
            "DeepSeek 接入位已预留，当前项目尚未完成真实请求实现。"
        } else {
            "DeepSeek 配置未完成，当前还不能切换到 DeepSeek。"
        }
        return ChatMessage(
            id = "assistant-deepseek-$now",
            role = "assistant",
            content = content,
            timestamp = now,
        )
    }

    override suspend fun sendVoice(audioUri: String): ChatMessage {
        return ChatMessage(
            id = "assistant-deepseek-voice-${System.currentTimeMillis()}",
            role = "assistant",
            content = "DeepSeek 语音入口尚未接入。",
            timestamp = System.currentTimeMillis(),
        )
    }

    override suspend fun sendImage(imageUri: String, prompt: String): ChatMessage {
        return ChatMessage(
            id = "assistant-deepseek-image-${System.currentTimeMillis()}",
            role = "assistant",
            content = "DeepSeek 图像入口尚未接入。",
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

    override fun providerType(): AssistantProviderType = AssistantProviderType.DEEPSEEK
}

object AssistantGatewayFactory {

    /**
     * 创建智能体网关。
     *
     * **v1.10.0 多智能体路由逻辑：**
     * - 若 DeepSeek API Key 已配置（即 [BuildConfig.DEEPSEEK_API_KEY] 非空），
     *   则自动启用多智能体路由中枢，由 [MultiAgentRouter] 根据意图智能分发。
     * - 否则按 [BuildConfig.ASSISTANT_PROVIDER] 配置返回单一网关。
     */
    fun create(): HiagentGateway {
        // ── v1.10.0: 多智能体路由自动启用 ──
        if (isMultiAgentRoutingEnabled()) {
            return createRouted()
        }

        // ── 原有单网关逻辑（向后兼容） ──
        return when (resolveProvider(BuildConfig.ASSISTANT_PROVIDER)) {
            AssistantProviderType.PREVIEW -> PreviewHiagentGateway()
            AssistantProviderType.DEEPSEEK -> DeepSeekAssistantGateway()
            AssistantProviderType.SPARK -> SparkAssistantGateway()
        }
    }

    /**
     * 显式创建多智能体路由网关。
     *
     * 初始化 [SparkAgentClient] 和 [DeepSeekAgentClient]，
     * 交由 [MultiAgentRouter] 统一调度。
     *
     * @param onStreamChunk 可选的流式分片回调，用于 UI 实时逐字渲染。
     */
    fun createRouted(
        onStreamChunk: ((String) -> Unit)? = null,
    ): HiagentGateway {
        val sparkClient = SparkAgentClient()
        val deepSeekClient = DeepSeekAgentClient()
        val router = MultiAgentRouter(
            sparkClient = sparkClient,
            deepSeekClient = deepSeekClient,
        )
        return RoutedHiagentGateway(
            router = router,
            onStreamChunk = onStreamChunk,
        )
    }

    /**
     * 判断是否应启用多智能体路由。
     * 条件：星火和 DeepSeek 至少配置了一个。
     * 临时策略：无论 DeepSeek 是否配置，只要调用此方法，强制开启路由中枢，让 Spark 兜底。
     */
    fun isMultiAgentRoutingEnabled(): Boolean {
        return true
    }

    private fun resolveProvider(raw: String): AssistantProviderType {
        return when (raw.trim().uppercase()) {
            "PREVIEW" -> AssistantProviderType.PREVIEW
            "DEEPSEEK" -> AssistantProviderType.DEEPSEEK
            else -> AssistantProviderType.SPARK
        }
    }
}
