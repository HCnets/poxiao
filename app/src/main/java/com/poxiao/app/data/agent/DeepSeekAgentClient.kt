package com.poxiao.app.data.agent

import com.poxiao.app.BuildConfig
import com.poxiao.app.data.AssistantProviderType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * DeepSeek 大模型客户端 —— 实现 [AgentClient] 流式接口。
 *
 * 采用标准 OpenAI 兼容的 HTTP 请求格式，通过 SSE (Server-Sent Events)
 * 实现流式文本生成。每收到一个 token 分片即回调 [onChunk]。
 *
 * ## 配置
 * 在 `local.properties` 或 `gradle.properties` 中设置：
 * ```
 * DEEPSEEK_BASE_URL=https://api.deepseek.com/v1/chat/completions
 * DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 * DEEPSEEK_MODEL=deepseek-chat
 * ```
 */
class DeepSeekAgentClient(
    private val baseUrl: String = resolveBaseUrl(),
    private val apiKey: String = resolveApiKey(),
    private val model: String = resolveModel(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
) : AgentClient {

    override val providerType: AssistantProviderType = AssistantProviderType.DEEPSEEK

    override suspend fun chat(
        messages: List<Message>,
        onChunk: (String) -> Unit,
    ): Result<String> {
        // 校验配置
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("DeepSeek API Key 未配置，请在 gradle.properties 中设置 DEEPSEEK_API_KEY"))
        }
        if (baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("DeepSeek Base URL 未配置"))
        }

        val requestBody = buildRequestBody(messages, model)

        val request = Request.Builder()
            .url(baseUrl)
            .post(requestBody.toString().toRequestBody(JSON_MEDIA_TYPE))
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string().orEmpty()
                        return@withContext Result.failure(
                            RuntimeException("DeepSeek API 返回 HTTP ${response.code}: $errorBody")
                        )
                    }

                    val body = response.body
                        ?: return@withContext Result.failure(RuntimeException("DeepSeek 响应体为空"))

                    val reader = BufferedReader(InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))
                    val fullContent = StringBuilder()

                    reader.use { br ->
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            val currentLine = line ?: continue
                            // SSE 数据行格式: "data: {...}"
                            if (!currentLine.startsWith("data: ")) continue

                            val jsonStr = currentLine.removePrefix("data: ").trim()
                            if (jsonStr == "[DONE]") break

                            val chunk = runCatching { JSONObject(jsonStr) }.getOrNull() ?: continue
                            val choices = chunk.optJSONArray("choices") ?: continue
                            for (i in 0 until choices.length()) {
                                val choice = choices.optJSONObject(i) ?: continue
                                val delta = choice.optJSONObject("delta") ?: continue
                                val content = delta.optString("content", "")
                                if (content.isNotBlank()) {
                                    fullContent.append(content)
                                    onChunk(content)
                                }
                            }

                            // 检查是否结束
                            for (i in 0 until choices.length()) {
                                val choice = choices.optJSONObject(i) ?: continue
                                val finishReason = choice.optString("finish_reason", "")
                                if (finishReason == "stop" || finishReason == "length") {
                                    // 流正常结束
                                }
                            }
                        }
                    }

                    val result = fullContent.toString().trim()
                    if (result.isBlank()) {
                        Result.failure(RuntimeException("DeepSeek 未返回有效内容"))
                    } else {
                        Result.success(result)
                    }
                }
            } catch (e: Exception) {
                Result.failure(RuntimeException("DeepSeek 网络请求失败: ${e.message}", e))
            }
        }
    }

    // ──── 请求体构建 ────

    private fun buildRequestBody(messages: List<Message>, model: String): JSONObject {
        val messagesArray = JSONArray().apply {
            messages.forEach { msg ->
                put(
                    JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    }
                )
            }
        }

        return JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("stream", true)
            put("temperature", 0.7)
            put("max_tokens", 4096)
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val DEFAULT_BASE_URL = "https://api.deepseek.com/v1/chat/completions"
        private const val DEFAULT_MODEL = "deepseek-v4-pro"

        private fun resolveBaseUrl(): String {
            return BuildConfig.DEEPSEEK_BASE_URL.ifBlank { DEFAULT_BASE_URL }
        }

        private fun resolveApiKey(): String {
            return BuildConfig.DEEPSEEK_API_KEY
        }

        private fun resolveModel(): String {
            return BuildConfig.DEEPSEEK_MODEL.ifBlank { DEFAULT_MODEL }
        }
    }
}
