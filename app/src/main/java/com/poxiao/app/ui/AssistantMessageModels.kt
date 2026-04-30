package com.poxiao.app.ui

import com.poxiao.app.data.AssistantConversation

internal data class AssistantSummaryInjection(
    val conversations: List<AssistantConversation>,
    val prompt: String,
)
