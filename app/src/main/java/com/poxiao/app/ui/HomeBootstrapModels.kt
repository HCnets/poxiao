package com.poxiao.app.ui

import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.FeedCard

internal data class HomeAssistantSessionBootstrap(
    val initialConversations: List<AssistantConversation>,
    val activeConversationId: String,
    val initialPrompt: String,
)

internal data class HomeGradeSearchRefresh(
    val cards: List<FeedCard>,
    val status: String,
    val loading: Boolean,
)

internal data class HomeWorkbenchBootstrap(
    val visibleModules: List<HomeModule>,
    val collapsedModules: List<HomeModule>,
    val moduleSizes: Map<HomeModule, HomeModuleSize>,
)
