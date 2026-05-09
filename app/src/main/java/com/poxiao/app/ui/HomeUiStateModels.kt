package com.poxiao.app.ui

import androidx.compose.runtime.MutableState
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantPermissionState
import com.poxiao.app.data.FeedCard

internal data class HomeAssistantUiState(
    val conversations: MutableList<AssistantConversation>,
    val activeConversationId: MutableState<String>,
    val prompt: MutableState<String>,
    val assistantBusy: MutableState<Boolean>,
    val assistantPermissionState: MutableState<AssistantPermissionState>,
    val reviewExecutionSummary: MutableState<ReviewBridgeExecutionSummary?>,
    val reviewExecutionHistory: MutableState<List<ReviewBridgeExecutionSummary>>,
    val expandedReviewExecutionAt: MutableState<Long?>,
)

internal data class HomeSearchUiState(
    val searchQuery: MutableState<String>,
    val gradeSearchLoading: MutableState<Boolean>,
    val gradeSearchStatus: MutableState<String>,
    val gradeSearchCards: MutableState<List<FeedCard>>,
    val searchHistory: MutableList<String>,
    val quickKeywords: MutableList<String>,
)

internal data class HomeWorkbenchUiState(
    val visibleModules: MutableList<HomeModule>,
    val collapsedModules: MutableList<HomeModule>,
    val moduleSizes: MutableMap<HomeModule, HomeModuleSize>,
    val draggingModule: MutableState<HomeModule?>,
    val dragOffsetY: MutableState<Float>,
    val homeEditMode: MutableState<Boolean>,
)
