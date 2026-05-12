package com.poxiao.app.data.agent

import com.poxiao.app.data.AssistantProviderType

/**
 * 通用消息体，与大模型无关的抽象消息结构。
 * 用于 AgentClient 统一接口。
 */
data class Message(
    val role: String,   // "system" / "user" / "assistant"
    val content: String,
)

/**
 * 统一的大模型客户端接口 (LLM Network Abstraction)。
 *
 * 所有大模型接入（星火、DeepSeek 等）均实现此接口，
 * 提供流式对话能力。
 */
interface AgentClient {
    /**
     * 发起流式对话请求。
     *
     * @param messages  完整的对话上下文消息列表。
     * @param onChunk   每收到一个文本分片时回调（可能被多次调用）。
     * @return Result.success(fullText) 若对话成功完成；
     *         Result.failure(exception) 若发生网络或协议错误。
     */
    suspend fun chat(
        messages: List<Message>,
        onChunk: (String) -> Unit,
    ): Result<String>

    /** 标识当前客户端对应的大模型提供商类型。 */
    val providerType: AssistantProviderType
}
