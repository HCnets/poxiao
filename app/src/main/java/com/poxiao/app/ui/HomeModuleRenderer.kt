package com.poxiao.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.poxiao.app.data.AssistantContextSummary
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantToolDefinition
import com.poxiao.app.review.ReviewItem
import com.poxiao.app.todo.TodoTask

@Composable
internal fun HomeModuleRenderer(
    module: HomeModule,
    modifier: Modifier,
    paired: Boolean,
    moduleSizes: Map<HomeModule, HomeModuleSize>,
    collapsedModules: List<HomeModule>,
    todayClassCount: Int,
    pendingExamItems: List<ExamWeekItem>,
    pendingTodoCount: Int,
    focusedMinutes: Int,
    todayTimeline: List<HomeLineData>,
    topFocusTask: FocusTaskStat?,
    pendingReviewItems: List<ReviewItem>,
    urgentReviewItem: ReviewItem?,
    pendingGoalTodo: TodoTask?,
    favoritePoints: List<String>,
    recentPoints: List<String>,
    conversations: List<AssistantConversation>,
    activeConversationId: String,
    activeConversation: AssistantConversation,
    assistantTools: List<AssistantToolDefinition>,
    assistantSummaries: List<AssistantContextSummary>,
    reviewExecutionSummary: ReviewBridgeExecutionSummary?,
    reviewExecutionHistory: List<ReviewBridgeExecutionSummary>,
    expandedReviewExecutionAt: Long?,
    prompt: String,
    assistantBusy: Boolean,
    onToggleModuleCollapsed: (HomeModule) -> Unit,
    onOpenMap: () -> Unit,
    onOpenScheduleDay: () -> Unit,
    onOpenScheduleExamWeek: () -> Unit,
    onOpenTodoPending: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
    onOpenReviewPlannerSeeded: (ReviewBridgeExecutionSummary) -> Unit,
    onOpenAssistantPermissions: () -> Unit,
    onBindReviewFocus: (ReviewItem) -> Unit,
    onBindGoalTodoFocus: (TodoTask) -> Unit,
    onSelectConversation: (String) -> Unit,
    onCreateConversation: () -> Unit,
    onPromptTool: (AssistantToolDefinition) -> Unit,
    onInjectSummary: (AssistantContextSummary) -> Unit,
    onToggleExecutionExpanded: (Long) -> Unit,
    onUndoExecution: (ReviewBridgeExecutionSummary) -> Unit,
    onReplayExecution: (ReviewBridgeExecutionSummary) -> Unit,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val moduleSize = moduleSizes[module] ?: defaultHomeModuleSize(module)
    val collapsed = module in collapsedModules && isHomeModuleCollapsible(module)
    when (module) {
        HomeModule.Metrics -> HomeMetricsModuleCard(
            modifier = modifier,
            paired = paired,
            moduleSize = moduleSize,
            todayClassCount = todayClassCount,
            pendingExamCount = pendingExamItems.size,
            pendingTodoCount = pendingTodoCount,
            focusedMinutes = focusedMinutes,
            onOpenScheduleDay = onOpenScheduleDay,
            onOpenScheduleExamWeek = onOpenScheduleExamWeek,
            onOpenTodoPending = onOpenTodoPending,
            onOpenPomodoro = onOpenPomodoro,
        )

        HomeModule.Rhythm -> HomeRhythmModuleCard(
            modifier = modifier,
            paired = paired,
            moduleSize = moduleSize,
            collapsed = collapsed,
            todayTimeline = todayTimeline,
            onToggleCollapsed = { onToggleModuleCollapsed(module) },
            onOpenScheduleDay = onOpenScheduleDay,
            onOpenScheduleExamWeek = onOpenScheduleExamWeek,
            onOpenTodoPending = onOpenTodoPending,
            onOpenPomodoro = onOpenPomodoro,
            onOpenReviewPlanner = onOpenReviewPlanner,
        )

        HomeModule.Learning -> HomeLearningModuleCard(
            modifier = modifier,
            paired = paired,
            moduleSize = moduleSize,
            collapsed = collapsed,
            pendingExamItems = pendingExamItems,
            topFocusTask = topFocusTask,
            pendingReviewItems = pendingReviewItems,
            urgentReviewItem = urgentReviewItem,
            pendingGoalTodo = pendingGoalTodo,
            onToggleCollapsed = { onToggleModuleCollapsed(module) },
            onOpenReviewPlanner = onOpenReviewPlanner,
            onOpenScheduleExamWeek = onOpenScheduleExamWeek,
            onOpenPomodoro = onOpenPomodoro,
            onOpenTodoPending = onOpenTodoPending,
            onBindReviewFocus = onBindReviewFocus,
            onBindGoalTodoFocus = onBindGoalTodoFocus,
        )

        HomeModule.QuickPoints -> HomeQuickPointsModuleCard(
            modifier = modifier,
            paired = paired,
            moduleSize = moduleSize,
            favoritePoints = favoritePoints,
            onOpenMap = onOpenMap,
        )

        HomeModule.RecentPoints -> HomeRecentPointsModuleCard(
            modifier = modifier,
            paired = paired,
            moduleSize = moduleSize,
            recentPoints = recentPoints,
            onOpenMap = onOpenMap,
        )

        HomeModule.Assistant -> HomeAssistantModuleCard(
            modifier = modifier,
            paired = paired,
            moduleSize = moduleSize,
            collapsed = collapsed,
            conversations = conversations,
            activeConversationId = activeConversationId,
            activeConversation = activeConversation,
            assistantTools = assistantTools,
            assistantSummaries = assistantSummaries,
            reviewExecutionSummary = reviewExecutionSummary,
            reviewExecutionHistory = reviewExecutionHistory,
            expandedReviewExecutionAt = expandedReviewExecutionAt,
            prompt = prompt,
            assistantBusy = assistantBusy,
            onToggleCollapsed = { onToggleModuleCollapsed(module) },
            onSelectConversation = onSelectConversation,
            onCreateConversation = onCreateConversation,
            onOpenAssistantPermissions = onOpenAssistantPermissions,
            onPromptTool = onPromptTool,
            onInjectSummary = onInjectSummary,
            onToggleExecutionExpanded = onToggleExecutionExpanded,
            onOpenReviewPlannerSeeded = onOpenReviewPlannerSeeded,
            onOpenTodoPending = onOpenTodoPending,
            onOpenPomodoro = onOpenPomodoro,
            onUndoExecution = onUndoExecution,
            onReplayExecution = onReplayExecution,
            onPromptChange = onPromptChange,
            onSend = onSend,
        )
    }
}
