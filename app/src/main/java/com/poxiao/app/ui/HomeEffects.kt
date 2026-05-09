package com.poxiao.app.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantSessionStore

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
