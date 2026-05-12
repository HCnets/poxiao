package com.poxiao.app.ui


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onOpenTodoPending: (TodoFilter) -> Unit,
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
    capabilities: EditionCapabilities = LocalEditionCapabilities.current,
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
                capabilities = capabilities,
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
                capabilities = capabilities,
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
        
        // 性能优化：将单个 item 包裹所有模块改为 items 遍历，实现 LazyColumn 的按需渲染和节点复用
        items(
            count = moduleRows.size,
            key = { index -> moduleRows[index].joinToString { it.name } }
        ) { index ->
            val rowModules = moduleRows[index]
            HomeModuleRow(
                rowModules = rowModules,
                globalIndex = index,
                renderHomeModule = renderHomeModule
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp)) // 为底部留出呼吸空间
        }
    }
}
