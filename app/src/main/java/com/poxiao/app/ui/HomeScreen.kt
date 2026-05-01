
package com.poxiao.app.ui

import android.Manifest
import android.app.AlarmManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewKanban
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.poxiao.app.campus.CampusMapScreen
import com.poxiao.app.campus.CampusServicesScreen
import com.poxiao.app.campus.HitaAcademicGateway
import com.poxiao.app.calculator.ScientificCalculatorScreen
import com.poxiao.app.data.ChatMessage
import com.poxiao.app.data.FeedCard
import com.poxiao.app.data.AssistantContextSummary
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantMockExecution
import com.poxiao.app.data.AssistantPermissionState
import com.poxiao.app.data.AssistantPermissionStore
import com.poxiao.app.data.AssistantSessionStore
import com.poxiao.app.data.AssistantToolCall
import com.poxiao.app.data.AssistantToolKit
import com.poxiao.app.data.AppSummaryProvider
import com.poxiao.app.data.AssistantToolDefinition
import com.poxiao.app.data.AssistantGatewayFactory
import com.poxiao.app.insights.LearningDashboardScreen
import com.poxiao.app.notes.CourseNoteSeed
import com.poxiao.app.notes.CourseNoteStore
import com.poxiao.app.notes.CourseNotesScreen
import com.poxiao.app.pomodoro.NoisePlayer
import com.poxiao.app.notifications.LocalReminderReceiver
import com.poxiao.app.reports.ExportCenterScreen
import com.poxiao.app.review.ReviewPlannerScreen
import com.poxiao.app.review.ReviewPlannerSeed
import com.poxiao.app.review.ReviewPlannerSeed
import com.poxiao.app.review.ReviewPlannerStore
import com.poxiao.app.settings.NotificationPreferencesScreen
import com.poxiao.app.schedule.AcademicRepository
import com.poxiao.app.schedule.AcademicUiState
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.schedule.HitaTerm
import com.poxiao.app.schedule.HitaTimeSlot
import com.poxiao.app.schedule.HitaWeek
import com.poxiao.app.schedule.HitaWeekDay
import com.poxiao.app.schedule.HitaWeekSchedule
import com.poxiao.app.settings.loadNotificationPreferenceState
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.ui.theme.BambooGlass
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.CloudWhite
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.PoxiaoTheme
import com.poxiao.app.ui.theme.PoxiaoThemePreset
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Instant
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
@Composable
internal fun HomeScreen(
    initialAssistantHistoryFocusAt: Long?,
    onAssistantHistoryFocusConsumed: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenScheduleDay: () -> Unit,
    onOpenScheduleExamWeek: () -> Unit,
    onOpenCampusServices: () -> Unit,
    onOpenTodoPending: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
    onOpenReviewPlannerSeeded: (ReviewPlannerSeed) -> Unit,
    onOpenAssistantPermissions: () -> Unit,
    onOpenCourseNotes: (CourseNoteSeed?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val homeDependencies = rememberHomeDependencies(context)
    val gateway = homeDependencies.gateway
    val mapPrefs = homeDependencies.prefs.mapPrefs
    val homePrefs = homeDependencies.prefs.homePrefs
    val todoPrefs = homeDependencies.prefs.todoPrefs
    val focusPrefs = homeDependencies.prefs.focusPrefs
    val focusRecordPrefs = homeDependencies.prefs.focusRecordPrefs
    val schedulePrefs = homeDependencies.prefs.schedulePrefs
    val scheduleAuthPrefs = homeDependencies.prefs.scheduleAuthPrefs
    val authPrefs = homeDependencies.prefs.authPrefs
    val campusPrefs = homeDependencies.prefs.campusPrefs
    val assistantBridgePrefs = homeDependencies.prefs.assistantBridgePrefs
    val assistantStore = homeDependencies.stores.assistantStore
    val permissionStore = homeDependencies.stores.permissionStore
    val toolKit = homeDependencies.stores.toolKit
    val summaryProvider = homeDependencies.stores.summaryProvider
    val noteStore = homeDependencies.stores.noteStore
    val reviewStore = homeDependencies.stores.reviewStore
    val assistantSessionBootstrap = remember(assistantStore) {
        bootstrapHomeAssistantSession(assistantStore)
    }
    val assistantUiState = rememberHomeAssistantUiState(
        assistantSessionBootstrap = assistantSessionBootstrap,
        initialPermissionState = permissionStore.load(),
        initialReviewExecutionSummary = loadReviewBridgeExecutionSummary(assistantBridgePrefs),
        initialReviewExecutionHistory = loadReviewBridgeExecutionHistory(assistantBridgePrefs),
    )
    val conversations = assistantUiState.conversations
    var activeConversationId by assistantUiState.activeConversationId
    var prompt by assistantUiState.prompt
    var assistantBusy by assistantUiState.assistantBusy
    var assistantPermissionState by assistantUiState.assistantPermissionState
    val searchUiState = rememberHomeSearchUiState(
        initialGradeCards = loadHomeGradeCache(campusPrefs),
        initialSearchHistory = loadStringList(homePrefs, "search_history"),
        initialQuickKeywords = loadHomeQuickKeywords(homePrefs),
    )
    var searchQuery by searchUiState.searchQuery
    var gradeSearchLoading by searchUiState.gradeSearchLoading
    var gradeSearchStatus by searchUiState.gradeSearchStatus
    var gradeSearchCards by searchUiState.gradeSearchCards
    val cachedGradeCards = remember(searchQuery, gradeSearchCards.size) { loadHomeGradeCache(campusPrefs) }
    val searchHistory = searchUiState.searchHistory
    val homeWorkbenchBootstrap = remember(homePrefs) {
        bootstrapHomeWorkbench(homePrefs)
    }
    val workbenchUiState = rememberHomeWorkbenchUiState(homeWorkbenchBootstrap)
    val quickKeywords = searchUiState.quickKeywords
    val visibleModules = workbenchUiState.visibleModules
    val collapsedModules = workbenchUiState.collapsedModules
    val moduleSizes = workbenchUiState.moduleSizes
    val favoritePoints = remember(mapPrefs) { loadHomeFavoritePoints(mapPrefs) }
    val recentPoints = remember(mapPrefs) { loadHomeRecentPoints(mapPrefs) }
    val cachedSchedule = remember { loadPrimaryScheduleState(authPrefs, schedulePrefs) }
    val scheduleEvents = remember { loadPrimaryScheduleEvents(authPrefs, schedulePrefs) }
    val todoTasks = remember { loadTodoTasks(todoPrefs) }
    val courseNotes = remember { noteStore.loadNotes() }
    val reviewItems = remember { reviewStore.loadItems() }
    val focusRecords = remember { loadFocusRecords(focusRecordPrefs) }
    val completedExamWeekIds = remember { loadStringList(scheduleAuthPrefs, "completed_ids") }
    val dashboardSnapshot = remember(
        cachedSchedule,
        scheduleEvents,
        todoTasks,
        reviewItems,
        focusRecords,
        completedExamWeekIds,
    ) {
        buildHomeDashboardSnapshot(
            cachedSchedule = cachedSchedule,
            scheduleEvents = scheduleEvents,
            todoTasks = todoTasks,
            reviewItems = reviewItems,
            focusRecords = focusRecords,
            completedExamWeekIds = completedExamWeekIds,
        )
    }
    val pendingTodoCount = dashboardSnapshot.pendingTodoCount
    val todayClassCount = dashboardSnapshot.todayClassCount
    val focusedMinutes = dashboardSnapshot.focusedMinutes
    val nextCourse = dashboardSnapshot.nextCourse
    val priorityTodo = dashboardSnapshot.priorityTodo
    val pendingGoalTodo = dashboardSnapshot.pendingGoalTodo
    val pendingReviewItems = dashboardSnapshot.pendingReviewItems
    val urgentReviewItem = dashboardSnapshot.urgentReviewItem
    val boundTask = remember { SecurePrefs.getString(focusPrefs, "bound_task_title_secure", "bound_task_title") }
    val homeExamItems = dashboardSnapshot.homeExamItems
    val pendingExamItems = dashboardSnapshot.pendingExamItems
    val urgentExamItem = dashboardSnapshot.urgentExamItem
    val topFocusTask = dashboardSnapshot.topFocusTask
    var draggingModule by workbenchUiState.draggingModule
    var dragOffsetY by workbenchUiState.dragOffsetY
    var homeEditMode by workbenchUiState.homeEditMode
    val dragSwapThreshold = with(LocalDensity.current) { 46.dp.toPx() }
    val assistantSummaries = remember(
        todayClassCount,
        pendingTodoCount,
        focusedMinutes,
        boundTask,
        gradeSearchCards.size,
    ) { summaryProvider.loadSummaries() }
    var reviewExecutionSummary by assistantUiState.reviewExecutionSummary
    var reviewExecutionHistory by assistantUiState.reviewExecutionHistory
    var expandedReviewExecutionAt by assistantUiState.expandedReviewExecutionAt
    HomeAssistantHistoryFocusEffect(
        initialAssistantHistoryFocusAt = initialAssistantHistoryFocusAt,
        onExpandedReviewExecutionAtChange = { expandedReviewExecutionAt = it },
        onAssistantHistoryFocusConsumed = onAssistantHistoryFocusConsumed,
    )
    val assistantTools = remember(assistantPermissionState) { toolKit.availableTools(assistantPermissionState) }
    val todayLabel = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日")) }
    val buildingCandidates = remember(mapPrefs) { loadHomeBuildingCandidates(mapPrefs) }
    val todayTimeline = remember(nextCourse, priorityTodo, pendingGoalTodo, boundTask, urgentExamItem, urgentReviewItem, topFocusTask) {
        buildHomeTodayTimeline(
            nextCourse = nextCourse,
            urgentExamItem = urgentExamItem,
            urgentReviewItem = urgentReviewItem,
            priorityTodo = priorityTodo,
            pendingGoalTodo = pendingGoalTodo,
            boundTask = boundTask,
            topFocusTask = topFocusTask,
        )
    }
    val heroState = remember(urgentReviewItem, priorityTodo, nextCourse, pendingGoalTodo, focusedMinutes, pendingTodoCount) {
        buildHomeHeroState(
            urgentReviewItem = urgentReviewItem,
            priorityTodo = priorityTodo,
            nextCourse = nextCourse,
            pendingGoalTodo = pendingGoalTodo,
            focusedMinutes = focusedMinutes,
            pendingTodoCount = pendingTodoCount,
        )
    }
    val welcomeSummary = remember(nextCourse, priorityTodo, urgentReviewItem, boundTask, topFocusTask) {
        buildHomeWelcomeSummary(
            nextCourse = nextCourse,
            priorityTodo = priorityTodo,
            urgentReviewItem = urgentReviewItem,
            boundTask = boundTask,
            topFocusTask = topFocusTask,
        )
    }
    val localSearchResults = remember(searchQuery, cachedSchedule, todoTasks, buildingCandidates, courseNotes) {
        buildHomeLocalSearchResults(
            searchQuery = searchQuery,
            cachedSchedule = cachedSchedule,
            todoTasks = todoTasks,
            buildingCandidates = buildingCandidates,
            courseNotes = courseNotes,
        )
    }
    val gradeSearchResults = remember(searchQuery, gradeSearchCards, cachedGradeCards) {
        buildHomeGradeSearchResults(
            searchQuery = searchQuery,
            liveCards = gradeSearchCards,
            cachedCards = cachedGradeCards,
        )
    }
    HomeGradeSearchEffect(
        searchQuery = searchQuery,
        authPrefs = authPrefs,
        campusPrefs = campusPrefs,
        onLoadingChange = { gradeSearchLoading = it },
        onStatusChange = { gradeSearchStatus = it },
        onCardsChange = { gradeSearchCards = it },
    )
    val activeConversation = conversations.firstOrNull { it.id == activeConversationId } ?: conversations.first()
    HomeAssistantDraftEffect(
        activeConversationId = activeConversationId,
        activeConversation = activeConversation,
        onPromptChange = { prompt = it },
    )
    HomeAssistantPersistenceEffect(
        activeConversationId = activeConversationId,
        prompt = prompt,
        conversations = conversations,
        assistantStore = assistantStore,
    )
    val toggleCollapsedModule: (HomeModule) -> Unit = { module ->
        val updatedCollapsedModules = toggleHomeCollapsedModule(collapsedModules, module)
        collapsedModules.clear()
        collapsedModules.addAll(updatedCollapsedModules)
        saveCollapsedHomeModules(homePrefs, collapsedModules)
    }
    val bindReviewFocusAndOpenPomodoro: (ReviewItem) -> Unit = { review ->
        bindHomeReviewFocus(focusPrefs, review)
        onOpenPomodoro()
    }
    val bindGoalTodoFocusAndOpenPomodoro: (TodoTask) -> Unit = { task ->
        bindHomeGoalTodoFocus(focusPrefs, task)
        onOpenPomodoro()
    }
    val createAssistantConversationAndActivate: () -> Unit = {
        applyHomeAssistantConversationCreation(
            assistantStore = assistantStore,
            assistantUiState = assistantUiState,
        )
    }
    val toggleExecutionExpandedAt: (Long) -> Unit = { executedAt ->
        expandedReviewExecutionAt = toggleExpandedReviewExecution(
            currentExpandedAt = expandedReviewExecutionAt,
            executedAt = executedAt,
        )
    }
    val openReviewPlannerSeededFromExecution: (ReviewBridgeExecutionSummary) -> Unit = { execution ->
        onOpenReviewPlannerSeeded(buildReviewPlannerSeed(execution))
    }
    val undoExecutionAction: (ReviewBridgeExecutionSummary) -> Unit = { execution ->
        applyHomeReviewUndoAction(
            context = context,
            assistantBridgePrefs = assistantBridgePrefs,
            assistantUiState = assistantUiState,
            execution = execution,
        )
    }
    val replayExecutionAction: (ReviewBridgeExecutionSummary) -> Unit = { execution ->
        applyHomeReviewReplayAction(
            context = context,
            assistantBridgePrefs = assistantBridgePrefs,
            assistantUiState = assistantUiState,
            execution = execution,
        )
    }
    val sendAssistantPrompt: () -> Unit = {
        applyHomeAssistantSend(
            scope = scope,
            gateway = gateway,
            context = context,
            toolKit = toolKit,
            assistantBridgePrefs = assistantBridgePrefs,
            assistantUiState = assistantUiState,
            assistantSummaries = assistantSummaries,
            assistantTools = assistantTools,
        )
    }
    val moduleActions = HomeModuleActionPack(
        onToggleModuleCollapsed = toggleCollapsedModule,
        onBindReviewFocus = bindReviewFocusAndOpenPomodoro,
        onBindGoalTodoFocus = bindGoalTodoFocusAndOpenPomodoro,
        onSelectConversation = { activeConversationId = it },
        onCreateConversation = createAssistantConversationAndActivate,
        onPromptTool = { tool -> prompt = "${tool.title}：${tool.description}" },
        onInjectSummary = { summary -> applyHomeAssistantSummaryInjection(assistantUiState, summary) },
        onToggleExecutionExpanded = toggleExecutionExpandedAt,
        onOpenReviewPlannerSeeded = openReviewPlannerSeededFromExecution,
        onUndoExecution = undoExecutionAction,
        onReplayExecution = replayExecutionAction,
        onPromptChange = { prompt = it },
        onSend = sendAssistantPrompt,
    )
    val homeContentActions = buildHomeContentActionPack(
        homePrefs = homePrefs,
        searchHistory = searchHistory,
        visibleModules = visibleModules,
        moduleSizes = moduleSizes,
        searchQueryProvider = { searchQuery },
        draggingModuleProvider = { draggingModule },
        dragOffsetYProvider = { dragOffsetY },
        dragSwapThreshold = dragSwapThreshold,
        onSearchQueryChange = { searchQuery = it },
        onDraggingModuleChange = { draggingModule = it },
        onDragOffsetYChange = { dragOffsetY = it },
        onHomeEditModeToggle = { homeEditMode = !homeEditMode },
        onOpenScheduleDay = onOpenScheduleDay,
        onOpenCourseNotes = onOpenCourseNotes,
        onOpenTodoPending = onOpenTodoPending,
        onOpenCampusServices = onOpenCampusServices,
        onOpenMap = onOpenMap,
    )
    val moduleRows = buildHomeModuleRows(visibleModules, moduleSizes)
    val renderHomeModule: @Composable (HomeModule, Modifier, Boolean) -> Unit = { module, modifier, paired ->
        HomeModuleRenderer(
            module = module,
            modifier = modifier,
            paired = paired,
            moduleSizes = moduleSizes,
            collapsedModules = collapsedModules,
            todayClassCount = todayClassCount,
            pendingExamItems = pendingExamItems,
            pendingTodoCount = pendingTodoCount,
            focusedMinutes = focusedMinutes,
            todayTimeline = todayTimeline,
            topFocusTask = topFocusTask,
            pendingReviewItems = pendingReviewItems,
            urgentReviewItem = urgentReviewItem,
            pendingGoalTodo = pendingGoalTodo,
            favoritePoints = favoritePoints,
            recentPoints = recentPoints,
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
            onToggleModuleCollapsed = moduleActions.onToggleModuleCollapsed,
            onOpenMap = onOpenMap,
            onOpenScheduleDay = onOpenScheduleDay,
            onOpenScheduleExamWeek = onOpenScheduleExamWeek,
            onOpenTodoPending = onOpenTodoPending,
            onOpenPomodoro = onOpenPomodoro,
            onOpenReviewPlanner = onOpenReviewPlanner,
            onOpenReviewPlannerSeeded = moduleActions.onOpenReviewPlannerSeeded,
            onOpenAssistantPermissions = onOpenAssistantPermissions,
            onBindReviewFocus = moduleActions.onBindReviewFocus,
            onBindGoalTodoFocus = moduleActions.onBindGoalTodoFocus,
            onSelectConversation = moduleActions.onSelectConversation,
            onCreateConversation = moduleActions.onCreateConversation,
            onPromptTool = moduleActions.onPromptTool,
            onInjectSummary = moduleActions.onInjectSummary,
            onToggleExecutionExpanded = moduleActions.onToggleExecutionExpanded,
            onUndoExecution = moduleActions.onUndoExecution,
            onReplayExecution = moduleActions.onReplayExecution,
            onPromptChange = moduleActions.onPromptChange,
            onSend = moduleActions.onSend,
        )
    }

    HomeScreenContent(
        todayLabel = todayLabel,
        homeEditMode = homeEditMode,
        welcomeSummary = welcomeSummary,
        heroState = heroState,
        todayClassCount = todayClassCount,
        pendingTodoCount = pendingTodoCount,
        focusedMinutes = focusedMinutes,
        pendingReviewCount = pendingReviewItems.size,
        todayTimeline = todayTimeline,
        searchQuery = searchQuery,
        searchHistory = searchHistory,
        quickKeywords = quickKeywords,
        localSearchResults = localSearchResults,
        gradeSearchResults = gradeSearchResults,
        gradeSearchLoading = gradeSearchLoading,
        gradeSearchStatus = gradeSearchStatus,
        visibleModules = visibleModules,
        moduleSizes = moduleSizes,
        draggingModule = draggingModule,
        dragOffsetY = dragOffsetY,
        dragSwapThreshold = dragSwapThreshold,
        moduleRows = moduleRows,
        renderHomeModule = renderHomeModule,
        onToggleEditMode = homeContentActions.onToggleEditMode,
        onOpenScheduleDay = onOpenScheduleDay,
        onOpenTodoPending = onOpenTodoPending,
        onOpenReviewPlanner = onOpenReviewPlanner,
        onOpenPomodoro = onOpenPomodoro,
        onOpenScheduleExamWeek = onOpenScheduleExamWeek,
        onSearchQueryChange = homeContentActions.onSearchQueryChange,
        onSelectHistory = homeContentActions.onSelectHistory,
        onClearHistory = homeContentActions.onClearHistory,
        onSelectKeyword = homeContentActions.onSelectKeyword,
        onSearchResultClick = homeContentActions.onSearchResultClick,
        onToggleModuleVisibility = homeContentActions.onToggleModuleVisibility,
        onDragStart = homeContentActions.onDragStart,
        onDragCancel = homeContentActions.onDragCancel,
        onDragEnd = homeContentActions.onDragEnd,
        onDragMove = homeContentActions.onDragMove,
        onSelectModuleSize = homeContentActions.onSelectModuleSize,
    )
}




