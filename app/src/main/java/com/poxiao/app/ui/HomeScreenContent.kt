package com.poxiao.app.ui

import androidx.compose.runtime.Composable
import com.poxiao.app.notes.CourseNoteSeed
import com.poxiao.app.review.ReviewPlannerSeed

@Composable
internal fun HomeScreenContent(
    todayLabel: String,
    homeEditMode: Boolean,
    welcomeSummary: HomeWelcomeSummary,
    heroState: HomeHeroState,
    todayClassCount: Int,
    pendingTodoCount: Int,
    focusedMinutes: Int,
    pendingReviewCount: Int,
    todayTimeline: List<HomeLineData>,
    searchQuery: String,
    searchHistory: List<String>,
    quickKeywords: List<String>,
    localSearchResults: List<HomeSearchResult>,
    gradeSearchResults: List<HomeSearchResult>,
    gradeSearchLoading: Boolean,
    gradeSearchStatus: String,
    visibleModules: List<HomeModule>,
    moduleSizes: Map<HomeModule, HomeModuleSize>,
    draggingModule: HomeModule?,
    dragOffsetY: Float,
    dragSwapThreshold: Float,
    moduleRows: List<List<HomeModule>>,
    renderHomeModule: @Composable (HomeModule, androidx.compose.ui.Modifier, Boolean) -> Unit,
    onToggleEditMode: () -> Unit,
    onOpenScheduleDay: () -> Unit,
    onOpenTodoPending: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onOpenScheduleExamWeek: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onSelectKeyword: (String) -> Unit,
    onSearchResultClick: (HomeSearchResult) -> Unit,
    onToggleModuleVisibility: (HomeModule) -> Unit,
    onDragStart: (HomeModule) -> Unit,
    onDragCancel: () -> Unit,
    onDragEnd: () -> Unit,
    onDragMove: (HomeModule, Float) -> Unit,
    onSelectModuleSize: (HomeModule, HomeModuleSize) -> Unit,
) {
    ScreenColumn {
        item {
            HomeWelcomeCard(
                todayLabel = todayLabel,
                homeEditMode = homeEditMode,
                nextCourseSubtitle = welcomeSummary.nextCourseSubtitle,
                priorityTodoTitle = welcomeSummary.priorityTodoTitle,
                urgentReviewTitle = welcomeSummary.urgentReviewTitle,
                pomodoroSubtitle = welcomeSummary.pomodoroSubtitle,
                onToggleEditMode = onToggleEditMode,
                onOpenScheduleDay = onOpenScheduleDay,
                onOpenTodoPending = onOpenTodoPending,
                onOpenReviewPlanner = onOpenReviewPlanner,
                onOpenPomodoro = onOpenPomodoro,
            )
        }
        item {
            HomeHeroOverviewCard(
                heroState = heroState,
                homeEditMode = homeEditMode,
                todayClassCount = todayClassCount,
                pendingTodoCount = pendingTodoCount,
                focusedMinutes = focusedMinutes,
                pendingReviewCount = pendingReviewCount,
                todayTimeline = todayTimeline,
                onOpenScheduleDay = onOpenScheduleDay,
                onOpenScheduleExamWeek = onOpenScheduleExamWeek,
                onOpenTodoPending = onOpenTodoPending,
                onOpenPomodoro = onOpenPomodoro,
                onOpenReviewPlanner = onOpenReviewPlanner,
            )
        }
        item {
            HomeSearchPanel(
                searchQuery = searchQuery,
                heroAccent = heroState.accent,
                searchHistory = searchHistory,
                quickKeywords = quickKeywords,
                localSearchResults = localSearchResults,
                gradeSearchResults = gradeSearchResults,
                gradeSearchLoading = gradeSearchLoading,
                gradeSearchStatus = gradeSearchStatus,
                onSearchQueryChange = onSearchQueryChange,
                onSelectHistory = onSelectHistory,
                onClearHistory = onClearHistory,
                onSelectKeyword = onSelectKeyword,
                onResultClick = onSearchResultClick,
            )
        }
        item {
            HomeWorkbenchHeader(
                homeEditMode = homeEditMode,
                visibleModulesCount = visibleModules.size,
            )
        }
        if (homeEditMode) item {
            HomeWorkbenchEditorCard(
                visibleModules = visibleModules,
                moduleSizes = moduleSizes,
                draggingModule = draggingModule,
                dragOffsetY = dragOffsetY,
                dragSwapThreshold = dragSwapThreshold,
                onToggleModuleVisibility = onToggleModuleVisibility,
                onDragStart = onDragStart,
                onDragCancel = onDragCancel,
                onDragEnd = onDragEnd,
                onDragMove = onDragMove,
                onSelectModuleSize = onSelectModuleSize,
            )
        }
        item {
            HomeModuleRowsSection(
                moduleRows = moduleRows,
                renderHomeModule = renderHomeModule,
            )
        }
    }
}
