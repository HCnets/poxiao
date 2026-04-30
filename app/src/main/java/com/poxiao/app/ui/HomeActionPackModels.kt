package com.poxiao.app.ui

import com.poxiao.app.data.AssistantContextSummary
import com.poxiao.app.data.AssistantToolDefinition
import com.poxiao.app.review.ReviewItem
import com.poxiao.app.todo.TodoTask

internal data class HomeContentActionPack(
    val onToggleEditMode: () -> Unit,
    val onSearchQueryChange: (String) -> Unit,
    val onSelectHistory: (String) -> Unit,
    val onClearHistory: () -> Unit,
    val onSelectKeyword: (String) -> Unit,
    val onSearchResultClick: (HomeSearchResult) -> Unit,
    val onToggleModuleVisibility: (HomeModule) -> Unit,
    val onDragStart: (HomeModule) -> Unit,
    val onDragCancel: () -> Unit,
    val onDragEnd: () -> Unit,
    val onDragMove: (HomeModule, Float) -> Unit,
    val onSelectModuleSize: (HomeModule, HomeModuleSize) -> Unit,
)

internal data class HomeModuleActionPack(
    val onToggleModuleCollapsed: (HomeModule) -> Unit,
    val onBindReviewFocus: (ReviewItem) -> Unit,
    val onBindGoalTodoFocus: (TodoTask) -> Unit,
    val onSelectConversation: (String) -> Unit,
    val onCreateConversation: () -> Unit,
    val onPromptTool: (AssistantToolDefinition) -> Unit,
    val onInjectSummary: (AssistantContextSummary) -> Unit,
    val onToggleExecutionExpanded: (Long) -> Unit,
    val onOpenReviewPlannerSeeded: (ReviewBridgeExecutionSummary) -> Unit,
    val onUndoExecution: (ReviewBridgeExecutionSummary) -> Unit,
    val onReplayExecution: (ReviewBridgeExecutionSummary) -> Unit,
    val onPromptChange: (String) -> Unit,
    val onSend: () -> Unit,
)
