package com.poxiao.app.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantSessionStore
import com.poxiao.app.data.ChatMessage
import java.time.LocalTime
import java.util.UUID

@Composable
internal fun HomeAssistantGreetingEffect(
    conversations: List<AssistantConversation>,
    activeConversationId: String,
    assistantSummaries: List<com.poxiao.app.data.AssistantContextSummary>,
    onConversationUpdate: (AssistantConversation) -> Unit,
) {
    LaunchedEffect(activeConversationId) {
        val active = conversations.firstOrNull { it.id == activeConversationId } ?: return@LaunchedEffect
        // 如果当前会话是默认的新开会话打招呼内容，则用伴读内容覆盖它
        val isDefaultGreeting = active.messages.size == 1 && active.messages.first().id.startsWith("seed-")
        
        if (active.messages.isEmpty() || isDefaultGreeting) {
            // 提取上下文摘要生成千人千面的问候
            val classSummary = assistantSummaries.firstOrNull { it.id == "today_schedule" }?.body
            val todoSummary = assistantSummaries.firstOrNull { it.id == "today_todo" }?.body

            val greetingText = buildString {
                // ── v1.10.0 分时段感性情绪引擎 ──
                val now = LocalTime.now()
                val hour = now.hour

                val greetingPrefix = when {
                    // 05:00 - 09:00 晨间
                    hour in 5..8 -> {
                        if (classSummary != null && classSummary.isNotBlank())
                            "早安，迎着晨光去上课吧！"
                        else
                            "早安，今天是个轻松的早晨。"
                    }
                    // 09:00 - 12:00 上午
                    hour in 9..11 -> "上午好！保持专注。"
                    // 12:00 - 14:00 午间
                    hour in 12..13 -> "中午好，记得按时吃午饭，休息一下大脑。"
                    // 14:00 - 18:00 下午
                    hour in 14..17 -> "下午好，喝杯水，继续战斗吧。"
                    // 18:00 - 22:00 晚间
                    hour in 18..21 -> "晚上好！今天的任务完成得怎么样了？"
                    // 22:00 - 05:00 深夜
                    else -> {
                        if (todoSummary != null && todoSummary.isNotBlank())
                            "夜深了，虽然还有待办，但也别熬太晚哦。"
                        else
                            "夜深了，早点休息，明天见。"
                    }
                }
                append(greetingPrefix)

                if (classSummary != null && classSummary.isNotBlank()) {
                    append(" 关于今天的课表：$classSummary。")
                } else {
                    append(" 今天似乎没有排课，可以自由支配时间。")
                }

                if (todoSummary != null && todoSummary.isNotBlank()) {
                    append(" 关于待办：$todoSummary。")
                }

                append(" 需要我帮你安排番茄钟，或者解答学习上的疑问吗？")
            }

            val greetingMsg = ChatMessage(
                id = "system-greeting-${UUID.randomUUID()}",
                role = "assistant",
                content = greetingText,
                timestamp = System.currentTimeMillis()
            )
            onConversationUpdate(
                active.copy(messages = listOf(greetingMsg))
            )
        }
    }
}

@Composable
internal fun HomeAssistantHistoryFocusEffect(
    initialAssistantHistoryFocusAt: Long?,
    onExpandedReviewExecutionAtChange: (Long?) -> Unit,
    onAssistantHistoryFocusConsumed: () -> Unit,
) {
    LaunchedEffect(initialAssistantHistoryFocusAt) {
        initialAssistantHistoryFocusAt?.let {
            onExpandedReviewExecutionAtChange(it)
            onAssistantHistoryFocusConsumed()
        }
    }
}

@Composable
internal fun HomeGradeSearchEffect(
    searchQuery: String,
    authPrefs: SharedPreferences,
    campusPrefs: SharedPreferences,
    onLoadingChange: (Boolean) -> Unit,
    onStatusChange: (String) -> Unit,
    onCardsChange: (List<com.poxiao.app.data.FeedCard>) -> Unit,
) {
    LaunchedEffect(searchQuery) {
        onLoadingChange(true)
        onStatusChange(if (searchQuery.isBlank()) "" else "正在检索成绩...")
        val refreshResult = refreshHomeGradeSearch(
            searchQuery = searchQuery,
            authPrefs = authPrefs,
            campusPrefs = campusPrefs,
        )
        onCardsChange(refreshResult.cards)
        onStatusChange(refreshResult.status)
        onLoadingChange(refreshResult.loading)
    }
}

@Composable
internal fun HomeAssistantDraftEffect(
    activeConversationId: String,
    activeConversation: AssistantConversation,
    onPromptChange: (String) -> Unit,
) {
    LaunchedEffect(activeConversationId) {
        onPromptChange(activeConversation.draftInput)
    }
}

@Composable
internal fun HomeAssistantPersistenceEffect(
    activeConversationId: String,
    prompt: String,
    conversations: List<AssistantConversation>,
    assistantStore: AssistantSessionStore,
) {
    LaunchedEffect(
        activeConversationId,
        prompt,
        conversations.joinToString("|") { "${it.id}:${it.updatedAt}:${it.messages.size}:${it.title}" },
    ) {
        val snapshot = buildAssistantConversationSnapshot(
            conversations = conversations,
            activeConversationId = activeConversationId,
            prompt = prompt,
        )
        assistantStore.saveConversations(snapshot, activeConversationId)
    }
}
