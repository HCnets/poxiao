package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantSessionStore
import com.poxiao.app.review.ReviewItem
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.todo.TodoTask

internal fun toggleHomeCollapsedModule(
    collapsedModules: List<HomeModule>,
    module: HomeModule,
): List<HomeModule> {
    return if (module in collapsedModules) {
        collapsedModules.filterNot { it == module }
    } else {
        collapsedModules + module
    }
}

internal fun bindHomeReviewFocus(
    focusPrefs: SharedPreferences,
    review: ReviewItem,
) {
    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "复习：${review.noteTitle}")
    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", review.courseName)
}

internal fun bindHomeGoalTodoFocus(
    focusPrefs: SharedPreferences,
    task: TodoTask,
) {
    focusPrefs.edit()
        .remove("bound_task_title")
        .remove("bound_task_list")
        .apply()
    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
}

internal fun createHomeAssistantConversation(
    assistantStore: AssistantSessionStore,
    conversations: List<AssistantConversation>,
): HomeAssistantConversationCreation {
    val newConversation = assistantStore.newConversationTemplate(conversations.size)
    return HomeAssistantConversationCreation(
        conversations = listOf(newConversation) + conversations,
        activeConversationId = newConversation.id,
        prompt = "",
    )
}

internal fun toggleExpandedReviewExecution(
    currentExpandedAt: Long?,
    executedAt: Long,
): Long? {
    return if (currentExpandedAt == executedAt) null else executedAt
}
