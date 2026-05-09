package com.poxiao.app.ui

import com.poxiao.app.data.AssistantConversation

internal data class HomeAssistantConversationCreation(
    val conversations: List<AssistantConversation>,
    val activeConversationId: String,
    val prompt: String,
)
