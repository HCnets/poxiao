package com.poxiao.app.data

import android.util.Base64
import com.poxiao.app.BuildConfig
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

data class SparkAssistantConfig(
    val appId: String,
    val assistantUrl: String,
    val apiKey: String,
    val apiSecret: String,
    val domain: String,
) {
    fun isReady(): Boolean {
        return appId.isNotBlank() &&
            assistantUrl.isNotBlank() &&
            apiKey.isNotBlank() &&
            apiSecret.isNotBlank()
    }

    fun missingFields(): List<String> {
        return buildList {
            if (appId.isBlank()) add("SPARK_ASSISTANT_APP_ID")
            if (assistantUrl.isBlank()) add("SPARK_ASSISTANT_URL")
            if (apiKey.isBlank()) add("SPARK_ASSISTANT_API_KEY")
            if (apiSecret.isBlank()) add("SPARK_ASSISTANT_API_SECRET")
        }
    }

    companion object {
        fun fromBuildConfig(): SparkAssistantConfig {
            return SparkAssistantConfig(
                appId = BuildConfig.SPARK_ASSISTANT_APP_ID,
                assistantUrl = BuildConfig.SPARK_ASSISTANT_URL,
                apiKey = BuildConfig.SPARK_ASSISTANT_API_KEY,
                apiSecret = BuildConfig.SPARK_ASSISTANT_API_SECRET,
                domain = BuildConfig.SPARK_ASSISTANT_DOMAIN,
            )
        }
    }
}

class SparkAssistantGateway(
    private val config: SparkAssistantConfig = SparkAssistantConfig.fromBuildConfig(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build(),
) : HiagentGateway {
    override suspend fun sendText(message: String, history: List<ChatMessage>): ChatMessage {
        val now = System.currentTimeMillis()
        val trimmed = message.trim()
        if (trimmed.isBlank()) {
            return ChatMessage(
                id = "assistant-empty-$now",
                role = "assistant",
                content = "\u8bf7\u8f93\u5165\u4f60\u60f3\u54a8\u8be2\u7684\u95ee\u9898\u3002",
                timestamp = now,
            )
        }
        if (!config.isReady()) {
            return ChatMessage(
                id = "assistant-config-$now",
                role = "assistant",
                content = buildMissingConfigText(config),
                timestamp = now,
            )
        }
        val candidateDomains = buildList {
            add(config.domain.ifBlank { DEFAULT_DOMAIN })
            if (last() != LEGACY_DOMAIN) add(LEGACY_DOMAIN)
        }.distinct()
        var lastFailure: SparkGatewayFailure? = null
        for (domain in candidateDomains) {
            when (val result = requestText(trimmed, history, domain)) {
                is SparkGatewaySuccess -> {
                    return ChatMessage(
                        id = "assistant-${UUID.randomUUID()}",
                        role = "assistant",
                        content = result.content.ifBlank { "\u661f\u706b\u52a9\u624b\u6ca1\u6709\u8fd4\u56de\u6709\u6548\u5185\u5bb9\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\u3002" },
                        timestamp = System.currentTimeMillis(),
                    )
                }

                is SparkGatewayFailure -> {
                    lastFailure = result
                }
            }
        }
        return ChatMessage(
            id = "assistant-failed-$now",
            role = "assistant",
            content = buildFailureText(lastFailure),
            timestamp = System.currentTimeMillis(),
        )
    }

    override suspend fun sendVoice(audioUri: String): ChatMessage {
        return ChatMessage(
            id = "assistant-voice-${System.currentTimeMillis()}",
            role = "assistant",
            content = "\u5f53\u524d\u63a5\u5165\u7684\u662f\u661f\u706b\u6587\u672c\u52a9\u624b\uff0c\u8bed\u97f3\u5165\u53e3\u6682\u672a\u542f\u7528\u3002",
            timestamp = System.currentTimeMillis(),
        )
    }

    override suspend fun sendImage(imageUri: String, prompt: String): ChatMessage {
        return ChatMessage(
            id = "assistant-image-${System.currentTimeMillis()}",
            role = "assistant",
            content = "\u5f53\u524d\u63a5\u5165\u7684\u662f\u661f\u706b\u6587\u672c\u52a9\u624b\uff0c\u56fe\u7247\u5165\u53e3\u6682\u672a\u542f\u7528\u3002",
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

    override fun providerType(): AssistantProviderType = AssistantProviderType.SPARK

    private suspend fun requestText(
        message: String,
        history: List<ChatMessage>,
        domain: String,
    ): SparkGatewayResult {
        return suspendCancellableCoroutine { continuation ->
            val finished = AtomicBoolean(false)
            val buffer = StringBuilder()
            val signedUrl = runCatching { buildSignedUrl(config.assistantUrl, config.apiKey, config.apiSecret) }
                .getOrElse { error ->
                    continuation.resume(
                        SparkGatewayFailure(
                            message = error.message ?: "\u7b7e\u540d\u751f\u6210\u5931\u8d25",
                        ),
                    )
                    return@suspendCancellableCoroutine
                }
            val payload = buildRequestPayload(
                appId = config.appId,
                domain = domain,
                history = history,
                message = message,
            ).toString()
            val request = runCatching { Request.Builder().url(signedUrl).build() }
                .getOrElse { error ->
                    continuation.resume(
                        SparkGatewayFailure(
                            message = error.message ?: "\u8bf7\u6c42\u5730\u5740\u65e0\u6548",
                        ),
                    )
                    return@suspendCancellableCoroutine
                }
            var socket: WebSocket? = null
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    webSocket.send(payload)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    val root = runCatching { JSONObject(text) }.getOrNull()
                    if (root == null) {
                        complete(
                            webSocket,
                            SparkGatewayFailure(
                                message = "\u661f\u706b\u52a9\u624b\u8fd4\u56de\u4e86\u65e0\u6cd5\u89e3\u6790\u7684\u6570\u636e",
                            ),
                        )
                        return
                    }
                    val header = root.optJSONObject("header")
                    val code = header?.optInt("code", 0) ?: 0
                    if (code != 0) {
                        val messageText = header?.optString("message").orEmpty().ifBlank {
                            "\u8bf7\u6c42\u5931\u8d25"
                        }
                        complete(webSocket, SparkGatewayFailure(code = code, message = messageText))
                        return
                    }
                    val choices = root.optJSONObject("payload")
                        ?.optJSONObject("choices")
                    val textArray = choices?.optJSONArray("text") ?: JSONArray()
                    appendAssistantChunks(buffer, textArray)
                    val status = choices?.optInt("status", header?.optInt("status", -1) ?: -1) ?: -1
                    if (status == 2) {
                        complete(webSocket, SparkGatewaySuccess(buffer.toString().trim()))
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    if (buffer.isNotBlank()) {
                        complete(webSocket, SparkGatewaySuccess(buffer.toString().trim()))
                    } else {
                        complete(
                            webSocket,
                            SparkGatewayFailure(
                                code = code,
                                message = reason.ifBlank { "\u8fde\u63a5\u5df2\u5173\u95ed" },
                            ),
                        )
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (buffer.isNotBlank()) {
                        complete(webSocket, SparkGatewaySuccess(buffer.toString().trim()))
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    val errorText = response?.message?.takeIf { it.isNotBlank() } ?: t.message.orEmpty()
                    complete(
                        webSocket,
                        SparkGatewayFailure(
                            message = errorText.ifBlank { "\u7f51\u7edc\u8fde\u63a5\u5931\u8d25" },
                        ),
                    )
                }

                private fun complete(webSocket: WebSocket, result: SparkGatewayResult) {
                    if (!finished.compareAndSet(false, true)) return
                    runCatching { webSocket.close(1000, "done") }
                    continuation.resume(result)
                }
            }
            socket = runCatching { client.newWebSocket(request, listener) }
                .getOrElse { error ->
                    continuation.resume(
                        SparkGatewayFailure(
                            message = error.message ?: "\u521b\u5efa WebSocket \u5931\u8d25",
                        ),
                    )
                    return@suspendCancellableCoroutine
                }
            continuation.invokeOnCancellation {
                if (finished.compareAndSet(false, true)) {
                    socket?.cancel()
                }
            }
        }
    }

    private fun buildRequestPayload(
        appId: String,
        domain: String,
        history: List<ChatMessage>,
        message: String,
    ): JSONObject {
        val visibleHistory = history
            .filter { it.role == "user" || it.role == "assistant" }
            .filter { it.content.isNotBlank() }
            .takeLast(MAX_HISTORY_MESSAGES)
        val messages = JSONArray().apply {
            visibleHistory.forEach { item ->
                put(
                    JSONObject().apply {
                        put("role", item.role)
                        put("content", item.content.trim())
                    },
                )
            }
            put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                },
            )
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
                            put("text", messages)
                        },
                    )
                },
            )
        }
    }

    private fun appendAssistantChunks(
        buffer: StringBuilder,
        textArray: JSONArray,
    ) {
        for (index in 0 until textArray.length()) {
            val item = textArray.optJSONObject(index) ?: continue
            if (item.optString("role").ifBlank { "assistant" } != "assistant") continue
            val content = item.optString("content")
            if (content.isNotBlank()) {
                buffer.append(content)
            }
        }
    }

    private fun buildSignedUrl(
        assistantUrl: String,
        apiKey: String,
        apiSecret: String,
    ): String {
        val uri = URI(assistantUrl)
        val host = uri.host.orEmpty()
        val path = uri.rawPath.orEmpty().ifBlank { "/" }
        require(host.isNotBlank()) { "\u661f\u706b\u52a9\u624b\u5730\u5740\u7f3a\u5c11 host" }
        val date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))
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

    private fun hmacSha256Base64(
        content: String,
        secret: String,
    ): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val bytes = mac.doFinal(content.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
    }

    private fun buildFailureText(failure: SparkGatewayFailure?): String {
        val detail = failure?.message.orEmpty().trim()
        if (detail.isBlank()) {
            return "\u661f\u706b\u52a9\u624b\u6682\u65f6\u4e0d\u53ef\u7528\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\u3002"
        }
        return "\u661f\u706b\u52a9\u624b\u6682\u65f6\u4e0d\u53ef\u7528\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\u3002\u539f\u56e0\uff1a$detail"
    }

    private fun buildMissingConfigText(config: SparkAssistantConfig): String {
        val missing = config.missingFields().joinToString(", ")
        return "\u661f\u706b\u52a9\u624b\u6682\u65f6\u4e0d\u53ef\u7528\uff0c\u5f53\u524d\u672a\u5b8c\u6210\u914d\u7f6e\u3002\u8bf7\u5728 environment variables \u6216 gradle.properties \u4e2d\u8bbe\u7f6e\uff1a$missing"
    }

    private sealed interface SparkGatewayResult

    private data class SparkGatewaySuccess(
        val content: String,
    ) : SparkGatewayResult

    private data class SparkGatewayFailure(
        val code: Int? = null,
        val message: String,
    ) : SparkGatewayResult

    private companion object {
        const val MAX_HISTORY_MESSAGES = 6
        const val DEFAULT_DOMAIN = "generalv3"
        const val LEGACY_DOMAIN = "general"
    }
}
