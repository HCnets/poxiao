package com.poxiao.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantPermissionState
import com.poxiao.app.data.FeedCard

@Composable
internal fun rememberHomeAssistantUiState(
    assistantSessionBootstrap: HomeAssistantSessionBootstrap,
    initialPermissionState: AssistantPermissionState,
    initialReviewExecutionSummary: ReviewBridgeExecutionSummary?,
    initialReviewExecutionHistory: List<ReviewBridgeExecutionSummary>,
): HomeAssistantUiState {
    val conversations = remember(assistantSessionBootstrap) {
        mutableStateListOf<AssistantConversation>().apply {
            addAll(assistantSessionBootstrap.initialConversations)
        }
    }
    val activeConversationId = remember(assistantSessionBootstrap) {
        mutableStateOf(assistantSessionBootstrap.activeConversationId)
    }
    val prompt = remember(assistantSessionBootstrap) {
        mutableStateOf(assistantSessionBootstrap.initialPrompt)
    }
    val assistantBusy = remember { mutableStateOf(false) }
    val assistantPermissionState = remember(initialPermissionState) { mutableStateOf(initialPermissionState) }
    val reviewExecutionSummary = remember(initialReviewExecutionSummary) { mutableStateOf(initialReviewExecutionSummary) }
    val reviewExecutionHistory = remember(initialReviewExecutionHistory) { mutableStateOf(initialReviewExecutionHistory) }
    val expandedReviewExecutionAt = remember { mutableStateOf<Long?>(null) }
    return remember(
        conversations,
        activeConversationId,
        prompt,
        assistantBusy,
        assistantPermissionState,
        reviewExecutionSummary,
        reviewExecutionHistory,
        expandedReviewExecutionAt,
    ) {
        HomeAssistantUiState(
            conversations = conversations,
            activeConversationId = activeConversationId,
            prompt = prompt,
            assistantBusy = assistantBusy,
            assistantPermissionState = assistantPermissionState,
            reviewExecutionSummary = reviewExecutionSummary,
            reviewExecutionHistory = reviewExecutionHistory,
            expandedReviewExecutionAt = expandedReviewExecutionAt,
        )
    }
}

@Composable
internal fun rememberHomeSearchUiState(
    initialGradeCards: List<FeedCard>,
    initialSearchHistory: List<String>,
    initialQuickKeywords: List<String>,
): HomeSearchUiState {
    val searchQuery = remember { mutableStateOf("") }
    val gradeSearchLoading = remember { mutableStateOf(false) }
    val gradeSearchStatus = remember { mutableStateOf("") }
    val gradeSearchCards = remember(initialGradeCards) { mutableStateOf(initialGradeCards) }
    val searchHistory = remember(initialSearchHistory) {
        mutableStateListOf<String>().apply {
            addAll(initialSearchHistory)
        }
    }
    val quickKeywords = remember(initialQuickKeywords) {
        mutableStateListOf<String>().apply {
            addAll(initialQuickKeywords)
        }
    }
    return remember(
        searchQuery,
        gradeSearchLoading,
        gradeSearchStatus,
        gradeSearchCards,
        searchHistory,
        quickKeywords,
    ) {
        HomeSearchUiState(
            searchQuery = searchQuery,
            gradeSearchLoading = gradeSearchLoading,
            gradeSearchStatus = gradeSearchStatus,
            gradeSearchCards = gradeSearchCards,
            searchHistory = searchHistory,
            quickKeywords = quickKeywords,
        )
    }
}

@Composable
internal fun rememberHomeWorkbenchUiState(
    homeWorkbenchBootstrap: HomeWorkbenchBootstrap,
): HomeWorkbenchUiState {
    val visibleModules = remember(homeWorkbenchBootstrap) {
        mutableStateListOf<HomeModule>().apply {
            addAll(homeWorkbenchBootstrap.visibleModules)
        }
    }
    val collapsedModules = remember(homeWorkbenchBootstrap) {
        mutableStateListOf<HomeModule>().apply {
            addAll(homeWorkbenchBootstrap.collapsedModules)
        }
    }
    val moduleSizes = remember(homeWorkbenchBootstrap) {
        mutableStateMapOf<HomeModule, HomeModuleSize>().apply {
            putAll(homeWorkbenchBootstrap.moduleSizes)
        }
    }
    val draggingModule = remember { mutableStateOf<HomeModule?>(null) }
    val dragOffsetY = remember { mutableStateOf(0f) }
    val homeEditMode = remember { mutableStateOf(false) }
    return remember(
        visibleModules,
        collapsedModules,
        moduleSizes,
        draggingModule,
        dragOffsetY,
        homeEditMode,
    ) {
        HomeWorkbenchUiState(
            visibleModules = visibleModules,
            collapsedModules = collapsedModules,
            moduleSizes = moduleSizes,
            draggingModule = draggingModule,
            dragOffsetY = dragOffsetY,
            homeEditMode = homeEditMode,
        )
    }
}
