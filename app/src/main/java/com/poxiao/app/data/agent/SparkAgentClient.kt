package com.poxiao.app.data.agent

import android.util.Base64
import com.poxiao.app.BuildConfig
import com.poxiao.app.data.AssistantProviderType
import com.poxiao.app.data.SparkAssistantConfig
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

/**
 * 星火大模型客户端 —— 实现 [AgentClient] 流式接口。
 *
 * 基于科大讯飞星火 Assistant API 的 WebSocket 协议，
 * 将分片文本通过 [onChunk] 实时回调给调用方。
 *
 * **注意：** 原始 [com.poxiao.app.data.SparkAssistantGateway] 保持不变，
 * 本类是对星火网络请求逻辑在统一 AgentClient 接口下的重新封装。
 */
class SparkAgentClient(
    private val config: SparkAssistantConfig = SparkAssistantConfig.fromBuildConfig(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build(),
) : AgentClient {

    override val providerType: AssistantProviderType = AssistantProviderType.SPARK

    override suspend fun chat(
        messages: List<Message>,
        onChunk: (String) -> Unit,
    ): Result<String> {
        // 提取最后一条 user 消息作为当前问题
        val lastUserMessage = messages.lastOrNull { it.role == "user" }?.content?.trim().orEmpty()
        if (lastUserMessage.isBlank()) {
            return Result.failure(IllegalArgumentException("消息列表为空或不包含用户消息"))
        }

        if (!config.isReady()) {
            val missing = config.missingFields().joinToString(", ")
            return Result.failure(IllegalStateException("星火助手配置不完整，缺少：$missing"))
        }

        // 尝试多个 domain 兜底
        val candidateDomains = buildList {
            add(config.domain.ifBlank { DEFAULT_DOMAIN })
            if (last() != LEGACY_DOMAIN) add(LEGACY_DOMAIN)
        }.distinct()

        var lastError: String? = null
        for (domain in candidateDomains) {
            val result = requestStreaming(messages, domain, onChunk)
            if (result.isSuccess) {
                return result
            }
            lastError = result.exceptionOrNull()?.message
        }

        return Result.failure(
            RuntimeException(lastError ?: "星火助手请求失败，请稍后再试")
        )
    }

    // ──── 核心流式请求 ────

    private suspend fun requestStreaming(
        messages: List<Message>,
        domain: String,
        onChunk: (String) -> Unit,
    ): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            val finished = AtomicBoolean(false)
            val fullBuffer = StringBuilder()

            // 1. 签名字段
            val signedUrl = runCatching {
                buildSignedUrl(config.assistantUrl, config.apiKey, config.apiSecret)
            }.getOrElse { error ->
                continuation.resume(
                    Result.failure(error)
                )
                return@suspendCancellableCoroutine
            }

            // 2. 构建请求 payload
            val payload = buildStreamingPayload(config.appId, domain, messages).toString()

            val request = runCatching {
                Request.Builder().url(signedUrl).build()
            }.getOrElse { error ->
                continuation.resume(Result.failure(error))
                return@suspendCancellableCoroutine
            }

            // 3. WebSocket 监听
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    webSocket.send(payload)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    val root = runCatching { JSONObject(text) }.getOrNull()
                    if (root == null) {
                        complete(
                            webSocket,
                            Result.failure(RuntimeException("星火助手返回了无法解析的数据")),
                        )
                        return
                    }

                    val header = root.optJSONObject("header")
                    val code = header?.optInt("code", 0) ?: 0
                    if (code != 0) {
                        val msg = header?.optString("message").orEmpty().ifBlank { "请求失败 (code=$code)" }
                        complete(webSocket, Result.failure(RuntimeException(msg)))
                        return
                    }

                    // 解析 choices.text[] 中的 assistant content
                    val choices = root.optJSONObject("payload")?.optJSONObject("choices")
                    val textArray = choices?.optJSONArray("text") ?: JSONArray()
                    for (i in 0 until textArray.length()) {
                        val item = textArray.optJSONObject(i) ?: continue
                        if (item.optString("role", "assistant") != "assistant") continue
                        val content = item.optString("content")
                        if (content.isNotBlank()) {
                            fullBuffer.append(content)
                            onChunk(content)   // 流式回调
                        }
                    }

                    val status = choices?.optInt("status", header?.optInt("status", -1) ?: -1) ?: -1
                    if (status == 2) {
                        complete(webSocket, Result.success(fullBuffer.toString().trim()))
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    if (fullBuffer.isNotBlank()) {
                        complete(webSocket, Result.success(fullBuffer.toString().trim()))
                    } else {
                        complete(
                            webSocket,
                            Result.failure(
                                RuntimeException(reason.ifBlank { "连接已关闭 (code=$code)" })
                            ),
                        )
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (fullBuffer.isNotBlank()) {
                        complete(webSocket, Result.success(fullBuffer.toString().trim()))
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    complete(
                        webSocket,
                        Result.failure(
                            RuntimeException(
                                t.message ?: "网络连接失败"
                            )
                        ),
                    )
                }

                private fun complete(webSocket: WebSocket, result: Result<String>) {
                    if (finished.compareAndSet(false, true)) {
                        runCatching { webSocket.close(1000, "done") }
                        continuation.resume(result)
                    }
                }
            }

            val socket = runCatching {
                client.newWebSocket(request, listener)
            }.getOrElse { error ->
                continuation.resume(Result.failure(error))
                return@suspendCancellableCoroutine
            }

            continuation.invokeOnCancellation {
                if (finished.compareAndSet(false, true)) {
                    socket.cancel()
                }
            }
        }
    }

    // ──── Payload 构建 ────

    private fun buildStreamingPayload(
        appId: String,
        domain: String,
        messages: List<Message>,
    ): JSONObject {
        val messageArray = JSONArray().apply {
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
            put(
                "header",
                JSONObject().apply {
                    put("app_id", appId)
                    put("uid", "android-${UUID.randomUUID().toString().replace("-", "").take(16)}")
                },
            )
            put(
                "parameter",
                JSONObject().apply {
                    put(
                        "chat",
                        JSONObject().apply {
                            put("domain", domain)
                        },
                    )
                },
            )
            put(
                "payload",
                JSONObject().apply {
                    put(
                        "message",
                        JSONObject().apply {
                            put("text", messageArray)
                        },
                    )
                },
            )
        }
    }

    // ──── 签名工具 ────

    private fun buildSignedUrl(
        assistantUrl: String,
        apiKey: String,
        apiSecret: String,
    ): String {
        val uri = URI(assistantUrl)
        val host = uri.host.orEmpty()
        val path = uri.rawPath.orEmpty().ifBlank { "/" }
        require(host.isNotBlank()) { "星火助手地址缺少 host" }

        val date = DateTimeFormatter.RFC_1123_DATE_TIME.format(
            ZonedDateTime.now(ZoneOffset.UTC)
        )
        val signatureOrigin = "host: $host\ndate: $date\nGET $path HTTP/1.1"
        val signatureSha = hmacSha256Base64(signatureOrigin, apiSecret)
        val authorizationOrigin =
            "api_key=\"$apiKey\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"$signatureSha\""
        val authorization = Base64.encodeToString(
            authorizationOrigin.toByteArray(StandardCharsets.UTF_8),
            Base64.NO_WRAP,
        )

        val separator = if (assistantUrl.contains("?")) "&" else "?"
        return buildString {
            append(assistantUrl)
            append(separator)
            append("authorization=")
            append(urlEncode(authorization))
            append("&date=")
            append(urlEncode(date))
            append("&host=")
            append(urlEncode(host))
        }
    }

    private fun hmacSha256Base64(content: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(
            secret.toByteArray(StandardCharsets.UTF_8),
            "HmacSHA256",
        )
        mac.init(secretKey)
        val bytes = mac.doFinal(content.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
    }

    companion object {
        private const val DEFAULT_DOMAIN = "generalv3"
        private const val LEGACY_DOMAIN = "general"
    }
}
