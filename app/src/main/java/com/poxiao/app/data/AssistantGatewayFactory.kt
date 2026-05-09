package com.poxiao.app.data

import com.poxiao.app.BuildConfig

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
    fun create(): HiagentGateway {
        return when (resolveProvider(BuildConfig.ASSISTANT_PROVIDER)) {
            AssistantProviderType.PREVIEW -> PreviewHiagentGateway()
            AssistantProviderType.DEEPSEEK -> DeepSeekAssistantGateway()
            AssistantProviderType.SPARK -> SparkAssistantGateway()
        }
    }

    private fun resolveProvider(raw: String): AssistantProviderType {
        return when (raw.trim().uppercase()) {
            "PREVIEW" -> AssistantProviderType.PREVIEW
            "DEEPSEEK" -> AssistantProviderType.DEEPSEEK
            else -> AssistantProviderType.SPARK
        }
    }
}
