package com.poxiao.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class AssistantToolCall(
    val id: String,
    val title: String,
    val status: String,
    val summary: String,
    val timestamp: Long,
)

data class AssistantConversation(
    val id: String,
    val title: String,
    val draftInput: String,
    val updatedAt: Long,
    val messages: List<ChatMessage>,
    val toolCalls: List<AssistantToolCall>,
)

class AssistantSessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("assistant_local_session", Context.MODE_PRIVATE)

    fun loadConversations(): List<AssistantConversation> {
        val raw = prefs.getString("conversation_list_v1", "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val root = array.optJSONObject(index) ?: continue
                    add(
                        AssistantConversation(
                            id = root.optString("id", "default"),
                            title = root.optString("title", "\u667A\u80FD\u4F53"),
                            draftInput = root.optString("draftInput"),
                            updatedAt = root.optLong("updatedAt", System.currentTimeMillis()),
                            messages = buildList {
                                val messagesArray = root.optJSONArray("messages") ?: JSONArray()
                                for (messageIndex in 0 until messagesArray.length()) {
                                    val item = messagesArray.optJSONObject(messageIndex) ?: continue
                                    add(
                                        ChatMessage(
                                            id = item.optString("id"),
                                            role = item.optString("role"),
                                            content = item.optString("content"),
                                            timestamp = item.optLong("timestamp"),
                                        ),
                                    )
                                }
                            },
                            toolCalls = buildList {
                                val toolArray = root.optJSONArray("toolCalls") ?: JSONArray()
                                for (toolIndex in 0 until toolArray.length()) {
                                    val item = toolArray.optJSONObject(toolIndex) ?: continue
                                    add(
                                        AssistantToolCall(
                                            id = item.optString("id"),
                                            title = item.optString("title"),
                                            status = item.optString("status"),
                                            summary = item.optString("summary"),
                                            timestamp = item.optLong("timestamp"),
                                        ),
                                    )
                                }
                            },
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun loadActiveConversationId(): String {
        return prefs.getString("active_conversation_id", "default").orEmpty().ifBlank { "default" }
    }

    fun defaultConversation(id: String = "default"): AssistantConversation {
        return AssistantConversation(
            id = id,
            title = if (id == "default") "\u667A\u80FD\u4F53" else "\u65B0\u4F1A\u8BDD",
            draftInput = "",
            updatedAt = System.currentTimeMillis(),
            messages = listOf(
                ChatMessage(
                    id = "seed-$id",
                    role = "assistant",
                    content = if (id == "default") {
                        "\u667A\u80FD\u4F53\u5165\u53E3\u5DF2\u6062\u590D\u3002\u4F60\u53EF\u4EE5\u4ECE\u8FD9\u91CC\u53D1\u8D77\u8BFE\u8868\u6574\u7406\u3001\u5F85\u529E\u62C6\u89E3\u6216\u4E13\u6CE8\u8BA1\u5212\u3002"
                    } else {
                        "\u5DF2\u521B\u5EFA\u65B0\u4F1A\u8BDD\u3002\u4F60\u53EF\u4EE5\u4ECE\u8FD9\u91CC\u5F00\u59CB\u65B0\u7684\u63D0\u95EE\u6216\u4EFB\u52A1\u62C6\u89E3\u3002"
                    },
                    timestamp = System.currentTimeMillis(),
                ),
            ),
            toolCalls = emptyList(),
        )
    }

    fun newConversationTemplate(existingCount: Int): AssistantConversation {
        val id = "conversation-${System.currentTimeMillis()}"
        return defaultConversation(id).copy(title = "\u4F1A\u8BDD ${existingCount + 1}")
    }

    fun saveConversations(
        conversations: List<AssistantConversation>,
        activeConversationId: String,
    ) {
        val normalized = if (conversations.isEmpty()) listOf(defaultConversation()) else conversations
        prefs.edit()
            .putString(
                "conversation_list_v1",
                JSONArray().apply {
                    normalized.forEach { conversation ->
                        put(
                            JSONObject().apply {
                                put("id", conversation.id)
                                put("title", conversation.title)
                                put("draftInput", conversation.draftInput)
                                put("updatedAt", conversation.updatedAt)
                                put(
                                    "messages",
                                    JSONArray().apply {
                                        conversation.messages.forEach { message ->
                                            put(
                                                JSONObject().apply {
                                                    put("id", message.id)
                                                    put("role", message.role)
                                                    put("content", message.content)
                                                    put("timestamp", message.timestamp)
                                                },
                                            )
                                        }
                                    },
                                )
                                put(
                                    "toolCalls",
                                    JSONArray().apply {
                                        conversation.toolCalls.forEach { tool ->
                                            put(
                                                JSONObject().apply {
                                                    put("id", tool.id)
                                                    put("title", tool.title)
                                                    put("status", tool.status)
                                                    put("summary", tool.summary)
                                                    put("timestamp", tool.timestamp)
                                                },
                                            )
                                        }
                                    },
                                )
                            },
                        )
                    }
                }.toString(),
            )
            .putString("active_conversation_id", activeConversationId)
            .apply()
    }
}
