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
import androidx.compose.foundation.layout.ColumnScope
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
import com.poxiao.app.review.ReviewPlannerStore
import com.poxiao.app.settings.NotificationPreferencesScreen
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.schedule.HitaScheduleUiState
import com.poxiao.app.schedule.HitaScheduleRepository
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
import org.json.JSONArray
import org.json.JSONObject

private data class HomeLineData(
    val timeLabel: String,
    val title: String,
    val subtitle: String,
)

private data class HomeHeroState(
    val badge: String,
    val headline: String,
    val detail: String,
    val accent: Color,
)

private enum class HomeDestination {
    ScheduleDay,
    ScheduleExamWeek,
    Todo,
    Pomodoro,
}

private enum class HomeModule(
    val title: String,
) {
    Metrics("鏍稿績鎸囨爣"),
    Rhythm("浠婂ぉ鐨勮妭锟?),
    Learning("瀛︿範鎺ㄨ繘"),
    QuickPoints("蹇嵎鐐逛綅"),
    RecentPoints("鏈€杩戣锟?),
    Assistant("鏅鸿兘锟?),
}

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
    val gateway = remember { AssistantGatewayFactory.create() }
    val assistantStore = remember { AssistantSessionStore(context) }
    val permissionStore = remember { AssistantPermissionStore(context) }
    val toolKit = remember { AssistantToolKit() }
    val summaryProvider = remember { AppSummaryProvider(context) }
    val mapPrefs = remember { context.getSharedPreferences("campus_map_prefs", 0) }
    val homePrefs = remember { context.getSharedPreferences("home_workbench", Context.MODE_PRIVATE) }
    val todoPrefs = remember { context.getSharedPreferences("todo_board", Context.MODE_PRIVATE) }
    val focusPrefs = remember { context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE) }
    val focusRecordPrefs = remember { context.getSharedPreferences("focus_records", Context.MODE_PRIVATE) }
    val schedulePrefs = remember { context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE) }
    val scheduleAuthPrefs = remember { context.getSharedPreferences("schedule_exam_week", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("schedule_auth", 0) }
    val campusPrefs = remember { context.getSharedPreferences("campus_services_prefs", 0) }
    val noteStore = remember { CourseNoteStore(context) }
    val reviewStore = remember { ReviewPlannerStore(context) }
    val assistantBridgePrefs = remember { context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE) }
    val initialConversations = remember {
        assistantStore.loadConversations().ifEmpty { listOf(assistantStore.defaultConversation()) }
    }
    val conversations = remember {
        mutableStateListOf<AssistantConversation>().apply {
            addAll(initialConversations)
        }
    }
    var activeConversationId by remember {
        mutableStateOf(
            assistantStore.loadActiveConversationId()
                .takeIf { target -> initialConversations.any { it.id == target } }
                ?: initialConversations.first().id,
        )
    }
    var prompt by remember {
        mutableStateOf(initialConversations.firstOrNull { it.id == activeConversationId }?.draftInput.orEmpty())
    }
    var assistantBusy by remember { mutableStateOf(false) }
    var assistantPermissionState by remember { mutableStateOf(permissionStore.load()) }
    var searchQuery by remember { mutableStateOf("") }
    var gradeSearchLoading by remember { mutableStateOf(false) }
    var gradeSearchStatus by remember { mutableStateOf("") }
    var gradeSearchCards by remember { mutableStateOf(loadHomeGradeCache(campusPrefs)) }
    val cachedGradeCards = remember(searchQuery, gradeSearchCards.size) { loadHomeGradeCache(campusPrefs) }
    val searchHistory = remember {
        mutableStateListOf<String>().apply {
            addAll(loadStringList(homePrefs, "search_history"))
        }
    }
    val quickKeywords = remember {
        mutableStateListOf<String>().apply {
            addAll(loadStringList(homePrefs, "search_keywords"))
            if (isEmpty()) {
                addAll(listOf("鏈哄櫒瀛︿範", "瀹為獙鎶ュ憡", "A锟?, "鑰冭瘯", "楂樹紭锟?))
            }
        }
    }
    val visibleModules = remember {
        mutableStateListOf<HomeModule>().apply {
            addAll(loadHomeModules(homePrefs))
        }
    }
    val collapsedModules = remember {
        mutableStateListOf<HomeModule>().apply {
            addAll(loadCollapsedHomeModules(homePrefs))
        }
    }
    val moduleSizes = remember {
        mutableStateMapOf<HomeModule, HomeModuleSize>().apply {
            putAll(loadHomeModuleSizes(homePrefs))
            HomeModule.entries.forEach { module ->
                putIfAbsent(module, defaultHomeModuleSize(module))
            }
        }
    }
    val favoritePoints = remember {
        val homeQuick = mapPrefs.getString("home_quick_points", "").orEmpty()
        val fallback = mapPrefs.getString("favorite_points", "").orEmpty()
        val raw = if (homeQuick.isNotBlank()) homeQuick else fallback
        raw
            .split("|")
            .filter { it.isNotBlank() }
    }
    val recentPoints = remember {
        (mapPrefs.getString("recent_points", "") ?: "")
            .split("|")
            .filter { it.isNotBlank() }
    }
    val cachedSchedule = remember { loadPrimaryScheduleState(authPrefs, schedulePrefs) }
    val scheduleEvents = remember { loadPrimaryScheduleEvents(authPrefs, schedulePrefs) }
    val todoTasks = remember { loadTodoTasks(todoPrefs) }
    val courseNotes = remember { noteStore.loadNotes() }
    val reviewItems = remember { reviewStore.loadItems() }
    val focusRecords = remember { loadFocusRecords(focusRecordPrefs) }
    val completedExamWeekIds = remember { loadStringList(scheduleAuthPrefs, "completed_ids") }
    val pendingTodoCount = remember(todoTasks) { todoTasks.count { !it.done } }
    val todayClassCount = remember(cachedSchedule) { cachedSchedule?.selectedDateCourses?.size ?: 0 }
    val focusedMinutes = remember(focusRecords) { focusRecords.map { it.seconds }.sum() / 60 }
    val focusTaskStats = remember(focusRecords.size) { buildFocusTaskStats(focusRecords) }
    val nextCourse = remember(cachedSchedule) {
        cachedSchedule?.selectedDateCourses
            ?.sortedBy { it.majorIndex }
            ?.firstOrNull()
    }
    val priorityTodo = remember(todoTasks) {
        todoTasks.firstOrNull { !it.done && it.priority == TodoPriority.High }
            ?: todoTasks.firstOrNull { !it.done }
    }
    val pendingGoalTodo = remember(todoTasks) {
        todoTasks.firstOrNull { !it.done && it.focusGoal > 0 && it.focusCount < it.focusGoal }
    }
    val pendingReviewItems = remember(reviewItems) {
        val tomorrowStart = java.time.LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        reviewItems.filter { it.nextReviewAt < tomorrowStart }
    }
    val urgentReviewItem = remember(pendingReviewItems) { pendingReviewItems.minByOrNull { it.nextReviewAt } }
    val boundTask = remember { SecurePrefs.getString(focusPrefs, "bound_task_title_secure", "bound_task_title") }
    val homeExamItems = remember(cachedSchedule, scheduleEvents, completedExamWeekIds) {
        cachedSchedule?.weekSchedule?.let { buildExamWeekItems(it, scheduleEvents, completedExamWeekIds) }.orEmpty()
    }
    val pendingExamItems = remember(homeExamItems) { homeExamItems.filter { !it.finished } }
    val urgentExamItem = remember(pendingExamItems) { pendingExamItems.firstOrNull() }
    val topFocusTask = remember(focusTaskStats) { focusTaskStats.firstOrNull() }
    var draggingModule by remember { mutableStateOf<HomeModule?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var homeEditMode by remember { mutableStateOf(false) }
    val dragSwapThreshold = with(LocalDensity.current) { 46.dp.toPx() }
    val assistantSummaries = remember(
        todayClassCount,
        pendingTodoCount,
        focusedMinutes,
        boundTask,
        gradeSearchCards.size,
    ) { summaryProvider.loadSummaries() }
    var reviewExecutionSummary by remember {
        mutableStateOf(loadReviewBridgeExecutionSummary(assistantBridgePrefs))
    }
    var reviewExecutionHistory by remember {
        mutableStateOf(loadReviewBridgeExecutionHistory(assistantBridgePrefs))
    }
    var expandedReviewExecutionAt by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(initialAssistantHistoryFocusAt) {
        initialAssistantHistoryFocusAt?.let {
            expandedReviewExecutionAt = it
            onAssistantHistoryFocusConsumed()
        }
    }
    val assistantTools = remember(assistantPermissionState) { toolKit.availableTools(assistantPermissionState) }
    val todayLabel = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("M鏈坉锟?)) }
    val buildingCandidates = remember(mapPrefs) {
        buildList {
            val favorite = mapPrefs.getString("favorite_points", "").orEmpty()
            val recent = mapPrefs.getString("recent_points", "").orEmpty()
            val homeQuick = mapPrefs.getString("home_quick_points", "").orEmpty()
            listOf(favorite, recent, homeQuick).forEach { raw ->
                raw.split("|").filter { it.isNotBlank() }.forEach { point ->
                    if (point !in this) add(point)
                }
            }
        }
    }
    val todayTimeline = remember(nextCourse, priorityTodo, pendingGoalTodo, boundTask, urgentExamItem, topFocusTask) {
        buildList {
            nextCourse?.let { add(HomeLineData("浠婃棩璇剧▼", it.courseName, it.classroom.ifBlank { "鏁欏寰呰ˉ锟? })) }
            urgentExamItem?.let { add(HomeLineData("鑰冭瘯锟?, it.title, "${it.countdownLabel} 路 ${it.subtitle}")) }
            urgentReviewItem?.let { add(HomeLineData("浠婃棩澶嶄範", it.noteTitle, "${it.courseName} 路 寤鸿 ${it.recommendedMinutes} 鍒嗛挓")) }
            priorityTodo?.let { add(HomeLineData("寰呭姙浼樺厛", it.title, "${it.listName} 路 ${it.dueText}")) }
            pendingGoalTodo?.let {
                add(HomeLineData("涓撴敞鐩爣", it.title, "杩樺樊 ${it.focusGoal - it.focusCount} 锟?路 ${it.dueText}"))
            }
            if (boundTask.isNotBlank()) add(HomeLineData("涓撴敞缁戝畾", boundTask, "鐣寗閽熷凡鍚屾褰撳墠浠诲姟")) else {
                topFocusTask?.let { add(HomeLineData("涓撴敞瓒嬪娍", it.title, "绱 ${it.minutes} 鍒嗛挓 路 ${it.count} 锟?)) }
            }
        }
    }
    val heroState = remember(urgentReviewItem, priorityTodo, nextCourse, pendingGoalTodo, focusedMinutes, pendingTodoCount) {
        when {
            urgentReviewItem != null -> HomeHeroState(
                badge = "浠婃棩澶嶄範",
                headline = "鍏堟敹浣忚繖杞蹇嗙獥锟?,
                detail = "${urgentReviewItem.courseName} 路 ${urgentReviewItem.noteTitle} 路 寤鸿 ${urgentReviewItem.recommendedMinutes} 鍒嗛挓",
                accent = ForestGreen,
            )

            priorityTodo != null -> HomeHeroState(
                badge = "楂樹紭鍏堝緟锟?,
                headline = "鍏堟帹杩涙渶鍏抽敭鐨勪竴锟?,
                detail = "${priorityTodo.title} 路 ${priorityTodo.dueText} 路 ${priorityTodo.listName}",
                accent = Ginkgo,
            )

            nextCourse != null -> HomeHeroState(
                badge = "涓嬩竴闂ㄨ",
                headline = "璇剧▼鑺傚宸茬粡鎺掑ソ",
                detail = "${nextCourse.courseName} 路 ${nextCourse.classroom.ifBlank { "鏁欏寰呰ˉ锟? }} 路 ${nextCourse.teacher.ifBlank { "鏁欏笀寰呰ˉ锟? }}",
                accent = MossGreen,
            )

            pendingGoalTodo != null -> HomeHeroState(
                badge = "涓撴敞鐩爣",
                headline = "缁х画瀹屾垚杩欑粍涓撴敞杞",
                detail = "${pendingGoalTodo.title} 路 杩樺樊 ${(pendingGoalTodo.focusGoal - pendingGoalTodo.focusCount).coerceAtLeast(0)} 锟?,
                accent = TeaGreen,
            )

            else -> HomeHeroState(
                badge = "浠婃棩鎬昏",
                headline = "璇捐〃銆佸緟鍔炲拰涓撴敞宸叉眹鎴愪竴寮犻潰锟?,
                detail = "褰撳墠寰呭姙 $pendingTodoCount 椤癸紝绱涓撴敞 $focusedMinutes 鍒嗛挓锟?,
                accent = ForestGreen,
            )
        }
    }
    val localSearchResults = remember(searchQuery, cachedSchedule, todoTasks, buildingCandidates, courseNotes) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val keyword = searchQuery.trim()
            buildList {
                cachedSchedule?.weekSchedule?.courses
                    ?.filter {
                        it.courseName.contains(keyword, ignoreCase = true) ||
                            it.teacher.contains(keyword, ignoreCase = true) ||
                            it.classroom.contains(keyword, ignoreCase = true)
                    }
                    ?.distinctBy { "${it.courseName}_${it.classroom}_${it.teacher}_${it.dayOfWeek}_${it.majorIndex}" }
                    ?.take(6)
                    ?.forEach { course ->
                        add(
                            HomeSearchResult(
                                id = "course_${course.courseName}_${course.dayOfWeek}_${course.majorIndex}",
                                category = HomeSearchCategory.Course,
                                title = course.courseName,
                                subtitle = course.teacher.ifBlank { "鏁欏笀寰呰ˉ锟? },
                                detail = course.classroom.ifBlank { "鏁欏寰呰ˉ锟? },
                            ),
                        )
                    }
                courseNotes.filter {
                    it.courseName.contains(keyword, ignoreCase = true) ||
                        it.title.contains(keyword, ignoreCase = true) ||
                        it.content.contains(keyword, ignoreCase = true) ||
                        it.tags.any { tag -> tag.contains(keyword, ignoreCase = true) }
                }.take(6).forEach { note ->
                    add(
                        HomeSearchResult(
                            id = "note_${note.id}",
                            category = HomeSearchCategory.Note,
                            title = note.courseName,
                            subtitle = note.title,
                            detail = note.tags.joinToString(" 路 ").ifBlank { "璇剧▼绗旇" },
                        ),
                    )
                }
                todoTasks.filter {
                    it.title.contains(keyword, ignoreCase = true) ||
                        it.note.contains(keyword, ignoreCase = true) ||
                        it.tags.any { tag -> tag.contains(keyword, ignoreCase = true) }
                }.take(6).forEach { task ->
                    add(
                        HomeSearchResult(
                            id = "todo_${task.id}",
                            category = HomeSearchCategory.Todo,
                            title = task.title,
                            subtitle = task.listName,
                            detail = task.dueText,
                        ),
                    )
                }
                buildingCandidates.filter { it.contains(keyword, ignoreCase = true) }
                    .take(6)
                    .forEach { building ->
                        add(
                            HomeSearchResult(
                                id = "building_$building",
                                category = HomeSearchCategory.Building,
                                title = building,
                                subtitle = "鏍″洯鍦板浘",
                                detail = "鐐瑰嚮鍓嶅線鍦板浘涓庡锟?,
                            ),
                        )
                    }
            }
        }
    }
    val gradeSearchResults = remember(searchQuery, gradeSearchCards, cachedGradeCards) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val keyword = searchQuery.trim()
            val liveMatches = gradeSearchCards.filter {
                it.title.contains(keyword, ignoreCase = true) ||
                    it.description.contains(keyword, ignoreCase = true) ||
                    it.source.contains(keyword, ignoreCase = true)
            }
            val fallbackMatches = cachedGradeCards.filter {
                it.title.contains(keyword, ignoreCase = true) ||
                    it.description.contains(keyword, ignoreCase = true) ||
                    it.source.contains(keyword, ignoreCase = true)
            }
            (if (liveMatches.isNotEmpty()) liveMatches else fallbackMatches).take(6).map { card ->
                HomeSearchResult(
                    id = "grade_${card.id}",
                    category = HomeSearchCategory.Grade,
                    title = card.title,
                    subtitle = card.source,
                    detail = card.description,
                )
            }
        }
    }
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            gradeSearchCards = emptyList()
            gradeSearchStatus = ""
            gradeSearchLoading = false
            return@LaunchedEffect
        }
        val studentId = SecurePrefs.getString(authPrefs, "student_id_secure", "student_id")
        val password = SecurePrefs.getString(authPrefs, "password_secure", "password")
        if (studentId.isBlank() || password.isBlank()) {
            gradeSearchCards = loadHomeGradeCache(campusPrefs)
            gradeSearchStatus = if (gradeSearchCards.isEmpty()) "鐧诲綍鏁欏姟鍚庡彲鎼滅储鐪熷疄鎴愮哗锟? else ""
            gradeSearchLoading = false
            return@LaunchedEffect
        }
        gradeSearchLoading = true
        gradeSearchStatus = "姝ｅ湪妫€绱㈡垚锟?.."
        try {
            val gateway = HitaAcademicGateway(studentId, password)
            val recentTerms: List<HitaTerm> = gateway.fetchTerms().take(3)
            val cards: List<FeedCard> = buildList {
                recentTerms.forEach { term ->
                    addAll(gateway.fetchGradesForTerm(term))
                }
            }
            if (cards.isNotEmpty()) {
                gradeSearchCards = cards
                gradeSearchStatus = ""
            } else {
                gradeSearchCards = loadHomeGradeCache(campusPrefs)
                gradeSearchStatus = if (gradeSearchCards.isEmpty()) "褰撳墠娌℃湁鍙悳绱㈢殑鎴愮哗璁板綍锟? else "褰撳墠鏄剧ず鐨勬槸鏈€杩戝悓姝ョ殑鎴愮哗缂撳瓨锟?
            }
        } catch (error: Throwable) {
            gradeSearchCards = loadHomeGradeCache(campusPrefs)
            gradeSearchStatus = if (gradeSearchCards.isEmpty()) (error.message ?: "鎴愮哗妫€绱㈠け璐ワ拷?) else "褰撳墠鏄剧ず鐨勬槸鏈€杩戝悓姝ョ殑鎴愮哗缂撳瓨锟?
        }
        gradeSearchLoading = false
    }
    val activeConversation = conversations.firstOrNull { it.id == activeConversationId } ?: conversations.first()
    LaunchedEffect(activeConversationId) {
        prompt = activeConversation.draftInput
    }
    fun injectAssistantSummary(summary: AssistantContextSummary) {
        val currentConversation = conversations.firstOrNull { it.id == activeConversationId } ?: return
        val injectedMessage = ChatMessage(
            id = "context-${summary.id}-${System.currentTimeMillis()}",
            role = "system",
            content = if (summary.id == "review_bridge" || summary.id == "review_exam_sync") {
                "宸叉敞鍏ュ涔犺鍒掞細${summary.body}"
            } else {
                "宸叉敞鍏ヤ笂涓嬫枃锟?{summary.source} 路 ${summary.title}\n${summary.body}"
            },
            timestamp = System.currentTimeMillis(),
        )
        val updatedMessages = (currentConversation.messages + injectedMessage).takeLast(24)
        val updatedConversations = conversations.map { conversation ->
            if (conversation.id == activeConversationId) {
                conversation.copy(
                    updatedAt = System.currentTimeMillis(),
                    messages = updatedMessages,
                )
            } else {
                conversation
            }
        }
        conversations.clear()
        conversations.addAll(updatedConversations)
        prompt = if (summary.id == "review_exam_sync") {
            "璇蜂紭鍏堟帴绠¤繖缁勪笌鑰冭瘯鍛ㄧ浉鍏崇殑澶嶄範鍐插埡椤癸紝骞剁粨鍚堝緟鍔炰笌鐣寗閽熷畨鎺掍粖澶╂渶绋崇殑鎺ㄨ繘椤哄簭锟?
        } else if (summary.id == "review_bridge") {
            "璇锋帴绠¤繖缁勫涔犺鍒掞紝骞剁粨鍚堝緟鍔炰笌鐣寗閽熺粰鎴戝畨鎺掍粖澶╃殑鎺ㄨ繘椤哄簭锟?
        } else {
            "鍩轰簬${summary.source}閲岀殑锟?{summary.title}鈥濈户缁垎鏋愶細${summary.body}"
        }
    }
    LaunchedEffect(
        activeConversationId,
        prompt,
        conversations.joinToString("|") { "${it.id}:${it.updatedAt}:${it.messages.size}:${it.title}" },
    ) {
        val snapshot = conversations.map { conversation ->
            if (conversation.id == activeConversationId) {
                conversation.copy(draftInput = prompt, updatedAt = System.currentTimeMillis())
            } else {
                conversation
            }
        }
        assistantStore.saveConversations(snapshot, activeConversationId)
    }
    val moduleRows = buildHomeModuleRows(visibleModules, moduleSizes)
    val renderHomeModule: @Composable (HomeModule, Modifier, Boolean) -> Unit = { module, modifier, paired ->
        val moduleSize = moduleSizes[module] ?: defaultHomeModuleSize(module)
        val collapsed = module in collapsedModules && isHomeModuleCollapsible(module)
        when (module) {
            HomeModule.Metrics -> {
                if (paired) {
                    GlassCard(modifier = modifier.clickable(onClick = onOpenScheduleDay)) {
                        Text("鏍稿績鎸囨爣", style = homeSectionTitleStyle(moduleSize), color = PineInk)
                        Spacer(modifier = Modifier.height(homeSectionSpacing(moduleSize)))
                        HomeLine("璇捐〃", "浠婃棩璇炬 ${todayClassCount}", "寰呭姙 ${pendingTodoCount} 路 涓撴敞 ${focusedMinutes} 鍒嗛挓", sizePreset = HomeModuleSize.Compact)
                        Spacer(modifier = Modifier.height(homeLineGap(HomeModuleSize.Compact)))
                        HomeLine("鑰冭瘯锟?, "寰呭锟?${pendingExamItems.size}", "鐐规鏌ョ湅褰撳墠鍐插埡锟?, sizePreset = HomeModuleSize.Compact)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(homeMetricSpacing(moduleSize)), modifier = modifier.horizontalScroll(rememberScrollState())) {
                        MetricCard("浠婃棩璇炬", todayClassCount.toString(), ForestGreen, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenScheduleDay))
                        MetricCard("鑰冭瘯锟?, pendingExamItems.size.toString(), TeaGreen, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenScheduleExamWeek))
                        MetricCard("寰呭畬锟?, pendingTodoCount.toString(), Ginkgo, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenTodoPending))
                        MetricCard("涓撴敞鏃堕暱", "${focusedMinutes} 鍒嗛挓", MossGreen, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenPomodoro))
                    }
                }
            }
            HomeModule.Rhythm -> GlassCard(modifier = modifier) {
                HomeModuleHeader(
                    title = "浠婂ぉ鐨勮妭锟?,
                    collapsed = collapsed,
                    collapsible = true,
                    sizePreset = moduleSize,
                    onToggleCollapsed = {
                        if (module in collapsedModules) collapsedModules.remove(module) else collapsedModules.add(module)
                        saveCollapsedHomeModules(homePrefs, collapsedModules)
                    },
                )
                Spacer(modifier = Modifier.height(homeSectionSpacing(moduleSize)))
                if (collapsed) {
                    Text("宸叉敹璧蜂粖鏃ヨ妭濂忥紝淇濈暀鎽樿鍏ュ彛锟?, style = homeSectionBodyStyle(moduleSize), color = ForestDeep.copy(alpha = 0.72f))
                } else if (todayTimeline.isEmpty()) {
                    Text("璇捐〃銆佸緟鍔炲拰涓撴敞璁板綍浼氬湪杩欓噷鑷姩姹囨€伙拷?, style = homeSectionBodyStyle(moduleSize), color = ForestDeep.copy(alpha = 0.72f))
                } else {
                    val visibleTimeline = if (paired) todayTimeline.take(2) else todayTimeline
                    visibleTimeline.forEachIndexed { index, item ->
                        HomeLine(
                            time = item.timeLabel,
                            title = item.title,
                            body = item.subtitle,
                            sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                            modifier = Modifier.clickable {
                                when (item.timeLabel) {
                                    "浠婃棩璇剧▼" -> onOpenScheduleDay()
                                    "鑰冭瘯锟? -> onOpenScheduleExamWeek()
                                    "浠婃棩澶嶄範" -> onOpenReviewPlanner()
                                    "寰呭姙浼樺厛" -> onOpenTodoPending()
                                    "涓撴敞鐩爣" -> onOpenTodoPending()
                                    "涓撴敞缁戝畾" -> onOpenPomodoro()
                                    "涓撴敞瓒嬪娍" -> onOpenPomodoro()
                                }
                            },
                        )
                        if (index != visibleTimeline.lastIndex) Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                    }
                }
            }
            HomeModule.Learning -> if (pendingExamItems.isNotEmpty() || topFocusTask != null || pendingReviewItems.isNotEmpty()) GlassCard(modifier = modifier) {
                val visibleExamItems = when {
                    paired -> pendingExamItems.take(1)
                    moduleSize == HomeModuleSize.Compact -> pendingExamItems.take(1)
                    moduleSize == HomeModuleSize.Standard -> pendingExamItems.take(2)
                    else -> pendingExamItems.take(3)
                }
                HomeModuleHeader(
                    title = "瀛︿範鎺ㄨ繘",
                    collapsed = collapsed,
                    collapsible = true,
                    sizePreset = moduleSize,
                    onToggleCollapsed = {
                        if (module in collapsedModules) collapsedModules.remove(module) else collapsedModules.add(module)
                        saveCollapsedHomeModules(homePrefs, collapsedModules)
                    },
                )
                Spacer(modifier = Modifier.height(homeSectionSpacing(moduleSize)))
                if (collapsed) {
                    val summary = buildString {
                        if (pendingExamItems.isNotEmpty()) append("寰呭锟?${pendingExamItems.size} 锟?)
                        urgentReviewItem?.let {
                            if (isNotBlank()) append(" 路 ")
                            append("澶嶄範 ${it.noteTitle}")
                        }
                        topFocusTask?.let {
                            if (isNotBlank()) append(" 路 ")
                            append("涓撴敞鎺掕 ${it.title}")
                        }
                        pendingGoalTodo?.let {
                            if (isNotBlank()) append(" 路 ")
                            append("鐩爣 ${it.title}")
                        }
                    }.ifBlank { "褰撳墠娌℃湁瀛︿範鎺ㄨ繘椤癸拷? }
                    Text(summary, style = homeSectionBodyStyle(moduleSize), color = ForestDeep.copy(alpha = 0.72f))
                } else {
                    if (pendingReviewItems.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenReviewPlanner),
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White.copy(alpha = 0.34f),
                            border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.3f)),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("澶嶄範璁″垝", style = MaterialTheme.typography.titleMedium, color = PineInk)
                                    ActionPill("鎵撳紑", ForestGreen, onClick = onOpenReviewPlanner)
                                }
                                Text(
                                    "浠婂ぉ搴斿锟?${pendingReviewItems.size} 椤癸紝鏈€绱ф€ョ殑锟?${urgentReviewItem?.noteTitle ?: "褰撳墠鐭ヨ瘑锟?}锟?,
                                    style = homeSectionBodyStyle(if (paired) HomeModuleSize.Compact else moduleSize),
                                    color = ForestDeep.copy(alpha = 0.74f),
                                )
                                urgentReviewItem?.let { review ->
                                    Text(
                                        "${review.courseName} 路 寤鸿 ${review.recommendedMinutes} 鍒嗛挓 路 ${if (review.nextReviewAt <= System.currentTimeMillis()) "宸茶繘鍏ラ仐蹇橀锟? else "鎸夎鍒掓帹锟?}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ForestDeep.copy(alpha = 0.68f),
                                    )
                                }
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    urgentReviewItem?.let { review ->
                                        ActionPill(
                                            text = "缁戝畾涓撴敞",
                                            background = MossGreen,
                                            onClick = {
                                                SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "澶嶄範锟?{review.noteTitle}")
                                                SecurePrefs.putString(focusPrefs, "bound_task_list_secure", review.courseName)
                                                onOpenPomodoro()
                                            },
                                        )
                                    }
                                    ActionPill(
                                        text = "鏌ョ湅浠婃棩澶嶄範",
                                        background = Ginkgo,
                                        onClick = onOpenReviewPlanner,
                                    )
                                }
                            }
                        }
                        if (visibleExamItems.isNotEmpty() || topFocusTask != null || pendingGoalTodo != null) {
                            Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                        }
                    }
                    visibleExamItems.forEachIndexed { index, item ->
                        HomeLine(
                            time = "鑰冭瘯锟?,
                            title = item.title,
                            body = "${item.countdownLabel} 路 ${item.detail}",
                            sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                            modifier = Modifier.clickable(onClick = onOpenScheduleExamWeek),
                        )
                        if (index != visibleExamItems.lastIndex || topFocusTask != null) {
                            Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                        }
                    }
                    urgentReviewItem?.let {
                        if (visibleExamItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                        }
                        HomeLine(
                            time = "浠婃棩澶嶄範",
                            title = it.noteTitle,
                            body = "${it.courseName} 路 寤鸿 ${it.recommendedMinutes} 鍒嗛挓",
                            sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                            modifier = Modifier.clickable(onClick = onOpenReviewPlanner),
                        )
                    }
                    topFocusTask?.let {
                        if (visibleExamItems.isNotEmpty() || urgentReviewItem != null) {
                            Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                        }
                        HomeLine(
                            time = "涓撴敞鎺掕",
                            title = it.title,
                            body = "${it.minutes} 鍒嗛挓 路 ${it.count} 锟?,
                            sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                            modifier = Modifier.clickable(onClick = onOpenPomodoro),
                        )
                    }
                    pendingGoalTodo?.let { task ->
                        if (visibleExamItems.isNotEmpty() || topFocusTask != null) {
                            Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                        }
                        HomeLine(
                            time = "涓撴敞鐩爣",
                            title = task.title,
                            body = "杩樺樊 ${task.focusGoal - task.focusCount} 锟?路 ${task.dueText}",
                            sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                            modifier = Modifier.clickable(onClick = onOpenTodoPending),
                        )
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(if (paired) HomeModuleSize.Compact else moduleSize)))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ActionPill("鏌ョ湅寰呭姙", WarmMist, onClick = onOpenTodoPending)
                            ActionPill("缁戝畾涓撴敞", ForestGreen) {
                                focusPrefs.edit()
                                    .remove("bound_task_title")
                                    .remove("bound_task_list")
                                    .apply()
                                SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                                SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                onOpenPomodoro()
                            }
                        }
                    }
                }
            }
            HomeModule.QuickPoints -> if (favoritePoints.isNotEmpty()) GlassCard(modifier = modifier.clickable(onClick = onOpenMap)) {
                val activeSize = if (paired) HomeModuleSize.Compact else moduleSize
                Text("棣栭〉蹇嵎鐐逛綅", style = homeSectionTitleStyle(activeSize), color = PineInk)
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                Text(
                    favoritePoints.take(if (activeSize == HomeModuleSize.Hero) 5 else if (activeSize == HomeModuleSize.Standard) 4 else 3).joinToString(" 路 "),
                    style = homeSectionBodyStyle(activeSize),
                    color = ForestDeep.copy(alpha = 0.76f),
                )
                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                Text(
                    "宸蹭粠鏍″洯鍦板浘鍚屾鐐逛綅锛屽彲缁х画鏌ョ湅瀵艰埅锟?,
                    style = if (activeSize == HomeModuleSize.Compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = ForestDeep.copy(alpha = 0.68f),
                )
            }
            HomeModule.RecentPoints -> if (recentPoints.isNotEmpty()) GlassCard(modifier = modifier.clickable(onClick = onOpenMap)) {
                val activeSize = if (paired) HomeModuleSize.Compact else moduleSize
                Text("鏈€杩戣锟?, style = homeSectionTitleStyle(activeSize), color = PineInk)
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                Text(
                    recentPoints.take(if (activeSize == HomeModuleSize.Hero) 4 else 3).joinToString(" 路 "),
                    style = homeSectionBodyStyle(activeSize),
                    color = ForestDeep.copy(alpha = 0.76f),
                )
                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                Text(
                    "鐐规鍥炲埌鏍″洯鍦板浘缁х画鏌ョ湅锟?,
                    style = if (activeSize == HomeModuleSize.Compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = ForestDeep.copy(alpha = 0.68f),
                )
            }
            HomeModule.Assistant -> GlassCard(modifier = modifier) {
                val activeSize = if (paired) HomeModuleSize.Compact else moduleSize
                val visibleMessages = activeConversation.messages.takeLast(
                    when {
                        paired -> 2
                        moduleSize == HomeModuleSize.Compact -> 2
                        moduleSize == HomeModuleSize.Standard -> 3
                        else -> 4
                    },
                )
                HomeModuleHeader(
                    title = "鏅鸿兘锟?,
                    collapsed = collapsed,
                    collapsible = true,
                    sizePreset = activeSize,
                    onToggleCollapsed = {
                        if (module in collapsedModules) collapsedModules.remove(module) else collapsedModules.add(module)
                        saveCollapsedHomeModules(homePrefs, collapsedModules)
                    },
                )
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                if (collapsed) {
                    Text("宸叉敹璧锋櫤鑳戒綋闈㈡澘銆傚綋鍓嶄繚鐣欐渶锟?${visibleMessages.size} 鏉″璇濇憳瑕佸叆鍙ｏ拷?, style = homeSectionBodyStyle(activeSize), color = ForestDeep.copy(alpha = 0.72f))
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        conversations.sortedByDescending { it.updatedAt }.take(6).forEach { conversation ->
                            SelectionChip(
                                text = conversation.title,
                                chosen = conversation.id == activeConversationId,
                                onClick = { activeConversationId = conversation.id },
                            )
                        }
                        ActionPill("鏂板缓浼氳瘽", WarmMist) {
                            val newConversation = assistantStore.newConversationTemplate(conversations.size)
                            conversations.add(0, newConversation)
                            activeConversationId = newConversation.id
                            prompt = ""
                        }
                        ActionPill("鏉冮檺", Ginkgo, onClick = onOpenAssistantPermissions)
                    }
                    Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                    if (assistantTools.isNotEmpty()) {
                        Text(
                            "鍙敤宸ュ叿",
                            style = if (activeSize == HomeModuleSize.Hero) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                            color = PineInk,
                        )
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            assistantTools.take(6).forEach { tool ->
                                SelectionChip(
                                    text = tool.title,
                                    chosen = false,
                                    onClick = { prompt = "${tool.title}锟?{tool.description}" },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                    }
                    if (assistantSummaries.isNotEmpty()) {
                        Text(
                            "鍙敤涓婁笅锟?,
                            style = if (activeSize == HomeModuleSize.Hero) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                            color = PineInk,
                        )
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                        assistantSummaries.take(if (activeSize == HomeModuleSize.Hero) 4 else 3).forEachIndexed { index, summary ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.42f),
                                border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.22f)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            injectAssistantSummary(summary)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text("${summary.source} 路 ${summary.title}", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                                    Text(summary.body, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                                }
                            }
                            if (index != minOf(assistantSummaries.size, if (activeSize == HomeModuleSize.Hero) 4 else 3) - 1) {
                                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                            }
                        }
                        Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                    }
                    reviewExecutionSummary?.let { execution ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.38f),
                            border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.18f)),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text("澶嶄範璁″垝鎵ц缁撴灉", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                                Text(execution.summary, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                                Text(
                                    "鎵ц锟?${formatSyncTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(execution.executedAt), java.time.ZoneId.systemDefault()))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ForestDeep.copy(alpha = 0.68f),
                                )
                                if (execution.createdTaskTitles.isNotEmpty() || execution.boundTaskTitle.isNotBlank()) {
                                    ActionPill(
                                        if (expandedReviewExecutionAt == execution.executedAt) "鏀惰捣鏄庣粏" else "灞曞紑鏄庣粏",
                                        TeaGreen.copy(alpha = 0.28f),
                                    ) {
                                        expandedReviewExecutionAt =
                                            if (expandedReviewExecutionAt == execution.executedAt) null else execution.executedAt
                                    }
                                }
                                if (expandedReviewExecutionAt == execution.executedAt) {
                                    ReviewExecutionDetailBlock(execution = execution)
                                }
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    ActionPill("鍥炲埌澶嶄範锟?, Ginkgo.copy(alpha = 0.82f)) {
                                        onOpenReviewPlannerSeeded(
                                            ReviewPlannerSeed(
                                            query = execution.createdTaskTitles.firstOrNull()?.removePrefix("澶嶄範锟?).orEmpty(),
                                            focusTitle = execution.boundTaskTitle.ifBlank {
                                                execution.createdTaskTitles.firstOrNull().orEmpty()
                                            },
                                        ),
                                        )
                                    }
                                    ActionPill("鏌ョ湅寰呭姙", Ginkgo) {
                                        onOpenTodoPending()
                                    }
                                    ActionPill("鏌ョ湅鐣寗锟?, WarmMist) {
                                        onOpenPomodoro()
                                    }
                                    ActionPill("鎾ら攢鎵ц", CloudWhite) {
                                        val result = undoReviewBridgeExecution(context)
                                        reviewExecutionSummary = loadReviewBridgeExecutionSummary(assistantBridgePrefs)
                                        reviewExecutionHistory = loadReviewBridgeExecutionHistory(assistantBridgePrefs)
                                        if (expandedReviewExecutionAt == execution.executedAt) {
                                            expandedReviewExecutionAt = null
                                        }
                                        val currentConversation = conversations.firstOrNull { it.id == activeConversationId }
                                        if (currentConversation != null) {
                                            val systemMessage = ChatMessage(
                                                id = "undo-review-${System.currentTimeMillis()}",
                                                role = "system",
                                                content = result,
                                                timestamp = System.currentTimeMillis(),
                                            )
                                            val updatedConversations = conversations.map { conversation ->
                                                if (conversation.id == activeConversationId) {
                                                    conversation.copy(
                                                        updatedAt = System.currentTimeMillis(),
                                                        messages = (conversation.messages + systemMessage).takeLast(24),
                                                    )
                                                } else {
                                                    conversation
                                                }
                                            }
                                            conversations.clear()
                                            conversations.addAll(updatedConversations)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                    }
                    if (reviewExecutionHistory.isNotEmpty()) {
                        Text("鏈€杩戞帴绠″巻锟?, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                        reviewExecutionHistory.take(3).forEachIndexed { index, execution ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.16f)),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        "鎵ц锟?${formatSyncTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(execution.executedAt), java.time.ZoneId.systemDefault()))}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ForestGreen,
                                    )
                                    Text(execution.summary, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        ActionPill(
                                            if (expandedReviewExecutionAt == execution.executedAt) "鏀惰捣鏄庣粏" else "灞曞紑鏄庣粏",
                                            TeaGreen.copy(alpha = 0.24f),
                                        ) {
                                            expandedReviewExecutionAt =
                                                if (expandedReviewExecutionAt == execution.executedAt) null else execution.executedAt
                                        }
                                        ActionPill("鍥炴斁鏂规", WarmMist) {
                                            val result = replayReviewBridgeExecution(context, execution)
                                            val latestSummary = loadReviewBridgeExecutionSummary(assistantBridgePrefs)
                                            reviewExecutionSummary = latestSummary
                                            reviewExecutionHistory = loadReviewBridgeExecutionHistory(assistantBridgePrefs)
                                            expandedReviewExecutionAt = latestSummary?.executedAt
                                            val currentConversation = conversations.firstOrNull { it.id == activeConversationId }
                                            if (currentConversation != null) {
                                                val systemMessage = ChatMessage(
                                                    id = "replay-review-${System.currentTimeMillis()}",
                                                    role = "system",
                                                    content = result,
                                                    timestamp = System.currentTimeMillis(),
                                                )
                                                val updatedConversations = conversations.map { conversation ->
                                                    if (conversation.id == activeConversationId) {
                                                        conversation.copy(
                                                            updatedAt = System.currentTimeMillis(),
                                                            messages = (conversation.messages + systemMessage).takeLast(24),
                                                        )
                                                    } else {
                                                        conversation
                                                    }
                                                }
                                                conversations.clear()
                                                conversations.addAll(updatedConversations)
                                            }
                                        }
                                        ActionPill("鍥炲埌澶嶄範锟?, Ginkgo.copy(alpha = 0.82f)) {
                                            onOpenReviewPlannerSeeded(
                                                ReviewPlannerSeed(
                                                query = execution.createdTaskTitles.firstOrNull()?.removePrefix("澶嶄範锟?).orEmpty(),
                                                focusTitle = execution.boundTaskTitle.ifBlank {
                                                    execution.createdTaskTitles.firstOrNull().orEmpty()
                                                },
                                            ),
                                            )
                                        }
                                    }
                                    if (expandedReviewExecutionAt == execution.executedAt) {
                                        ReviewExecutionDetailBlock(execution = execution)
                                    }
                                }
                            }
                            if (index != minOf(reviewExecutionHistory.size, 3) - 1) {
                                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                            }
                        }
                        Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                    }
                    visibleMessages.forEachIndexed { index, message ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (message.role) {
                                "assistant" -> Color.White.copy(alpha = 0.58f)
                                "system" -> TeaGreen.copy(alpha = 0.24f)
                                else -> Color(0x2A2F7553)
                            },
                        ) {
                            Text(
                                text = message.content,
                                modifier = Modifier.padding(14.dp),
                                style = if (activeSize == HomeModuleSize.Hero) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                color = PineInk,
                            )
                        }
                        if (index != visibleMessages.lastIndex) Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                    }
                    if (activeConversation.toolCalls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                        Text("鏈€杩戝伐鍏疯皟锟?, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                        activeConversation.toolCalls.takeLast(3).reversed().forEachIndexed { index, toolCall ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.36f),
                                border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.18f)),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text("${toolCall.title} 路 ${toolCall.status}", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                                    Text(toolCall.summary, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.72f))
                                }
                            }
                            if (index != minOf(activeConversation.toolCalls.size, 3) - 1) {
                                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = { Text("缁欐櫤鑳戒綋涓€鍙ヨ瘽") },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (assistantBusy) {
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                        Text("鏅鸿兘浣撴鍦ㄦ暣鐞嗗洖锟?..", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.68f))
                    }
                    Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize) + 2.dp))
                    Button(
                        onClick = {
                            if (prompt.isNotBlank() && !assistantBusy) {
                                val input = prompt
                                val targetConversationId = activeConversationId
                                val userMessage = ChatMessage("user-${System.currentTimeMillis()}", "user", input, System.currentTimeMillis())
                                val pendingId = "assistant-pending-${System.currentTimeMillis()}"
                                val pendingMessage = ChatMessage(pendingId, "assistant", "姝ｅ湪鏁寸悊浣犵殑闂...", System.currentTimeMillis())
                                val currentConversation = conversations.firstOrNull { it.id == targetConversationId } ?: return@Button
                                val localExecution = if (shouldExecuteReviewBridge(input, assistantSummaries)) {
                                    val executionSummary = applyReviewBridgeExecution(context)
                                    reviewExecutionSummary = loadReviewBridgeExecutionSummary(assistantBridgePrefs)
                                    reviewExecutionHistory = loadReviewBridgeExecutionHistory(assistantBridgePrefs)
                                    AssistantMockExecution(
                                        toolCall = AssistantToolCall(
                                            id = "tool-${System.currentTimeMillis()}",
                                            title = "鎵ц澶嶄範璁″垝",
                                            status = "瀹屾垚",
                                            summary = executionSummary,
                                            timestamp = System.currentTimeMillis(),
                                        ),
                                        reply = "鎴戝凡缁忔寜寰呮帴绠″涔犺鍒掑畬鎴愪簡棣栬疆钀藉湴锟?executionSummary 鎺ヤ笅鏉ヤ綘鍙互鐩存帴鍘诲緟鍔炲拰鐣寗閽熷紑濮嬫墽琛岋拷?,
                                    )
                                } else {
                                    toolKit.runMock(input, assistantPermissionState, assistantSummaries)
                                }
                                val requestPayload = buildAssistantRequestPrompt(
                                    userInput = input,
                                    summaries = assistantSummaries,
                                    availableTools = assistantTools,
                                    toolCall = localExecution.toolCall,
                                )
                                val requestHistory = currentConversation.messages.takeLast(6)
                                val updatedMessages = currentConversation.messages + userMessage + pendingMessage
                                val updatedTitle = currentConversation.title.takeIf { it != "\u667A\u80FD\u4F53" && !it.startsWith("\u4F1A\u8BDD ") }
                                    ?: input.take(12)
                                val seededConversations = conversations.map { conversation ->
                                    if (conversation.id == targetConversationId) {
                                        conversation.copy(
                                            title = updatedTitle,
                                            draftInput = "",
                                            updatedAt = System.currentTimeMillis(),
                                            messages = updatedMessages,
                                            toolCalls = conversation.toolCalls + listOfNotNull(localExecution.toolCall),
                                        )
                                    } else {
                                        conversation
                                    }
                                }
                                conversations.clear()
                                conversations.addAll(seededConversations)
                                prompt = ""
                                assistantBusy = true
                                scope.launch {
                                    try {
                                        val remoteReply = runCatching {
                                            gateway.sendText(
                                                message = requestPayload,
                                                history = requestHistory,
                                            )
                                        }.getOrElse {
                                            ChatMessage(
                                                id = "assistant-error-${System.currentTimeMillis()}",
                                                role = "assistant",
                                                content = "鏄熺伀鍔╂墜鏆傛椂涓嶅彲鐢紝璇风◢鍚庡啀璇曪拷?,
                                                timestamp = System.currentTimeMillis(),
                                            )
                                        }
                                        val stagedContent = resolveAssistantReply(
                                            remoteReply = remoteReply.content,
                                            fallback = localExecution.reply,
                                            toolCall = localExecution.toolCall,
                                        )
                                        val checkpoints = listOf(
                                            stagedContent.take((stagedContent.length * 0.35f).toInt().coerceAtLeast(1)),
                                            stagedContent.take((stagedContent.length * 0.7f).toInt().coerceAtLeast(1)),
                                            stagedContent,
                                        ).distinct()
                                        checkpoints.forEachIndexed { index, chunk ->
                                            val streamedConversations = conversations.map { conversation ->
                                                if (conversation.id == targetConversationId) {
                                                    conversation.copy(
                                                        updatedAt = System.currentTimeMillis(),
                                                        messages = conversation.messages.map { message ->
                                                            val isPendingReply = message.id == pendingId || message.id == remoteReply.id
                                                            if (isPendingReply) {
                                                                message.copy(
                                                                    id = if (index == checkpoints.lastIndex) remoteReply.id else pendingId,
                                                                    content = chunk,
                                                                    timestamp = remoteReply.timestamp,
                                                                )
                                                            } else {
                                                                message
                                                            }
                                                        },
                                                    )
                                                } else {
                                                    conversation
                                                }
                                            }
                                            conversations.clear()
                                            conversations.addAll(streamedConversations)
                                            if (index != checkpoints.lastIndex) delay(180)
                                        }
                                    } finally {
                                        assistantBusy = false
                                    }
                                }
                            }
                        },
                        enabled = prompt.isNotBlank() && !assistantBusy,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("鍙戯拷?)
                    }
                }
            }
        }
    }

    ScreenColumn {
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("鐮存檽", style = MaterialTheme.typography.headlineLarge, color = PineInk)
                        Text(
                            "$todayLabel 路 鏍″洯瀛︿範宸ヤ綔锟?,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestDeep.copy(alpha = 0.68f),
                        )
                    }
                    ActionPill(
                        text = if (homeEditMode) "瀹屾垚缂栬緫" else "缂栬緫宸ヤ綔锟?,
                        background = if (homeEditMode) ForestGreen else WarmMist,
                        onClick = {
                            homeEditMode = !homeEditMode
                            draggingModule = null
                            dragOffsetY = 0f
                        },
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (homeEditMode) {
                    Text(
                        "褰撳墠澶勪簬缂栬緫妯″紡锛屽彲璋冩暣妯″潡鏄鹃殣銆侀『搴忓拰灏哄锟?,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForestDeep.copy(alpha = 0.68f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HomeQuickEntry(
                        title = "浠婃棩璇捐〃",
                        subtitle = nextCourse?.let {
                            "${it.courseName} 路 ${it.classroom.ifBlank { "鏁欏寰呰ˉ锟? }}"
                        } ?: "鎵撳紑浠婃棩璇剧▼涓庤€冭瘯锟?,
                        accent = ForestGreen,
                        icon = PrimarySection.Schedule.icon,
                        onClick = onOpenScheduleDay,
                    )
                    HomeQuickEntry(
                        title = "寰呭姙娓呭崟",
                        subtitle = priorityTodo?.title ?: "鎵撳紑寰呭姙鏌ョ湅褰撳墠浼樺厛锟?,
                        accent = Ginkgo,
                        icon = PrimarySection.Todo.icon,
                        onClick = onOpenTodoPending,
                    )
                    HomeQuickEntry(
                        title = "澶嶄範璁″垝",
                        subtitle = urgentReviewItem?.noteTitle ?: "鏌ョ湅浠婂ぉ搴旀帹杩涚殑澶嶄範锟?,
                        accent = TeaGreen,
                        icon = Icons.Rounded.AutoAwesome,
                        onClick = onOpenReviewPlanner,
                    )
                    HomeQuickEntry(
                        title = "鐣寗锟?,
                        subtitle = boundTask.ifBlank {
                            topFocusTask?.let { "${it.minutes} 鍒嗛挓绱" } ?: "寮€濮嬩竴杞笓锟?
                        },
                        accent = MossGreen,
                        icon = PrimarySection.Pomodoro.icon,
                        onClick = onOpenPomodoro,
                    )
                }
            }
        }
        item {
            GlassCard {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White.copy(alpha = 0.28f),
                    border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.34f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        heroState.accent.copy(alpha = 0.14f),
                                        Color.White.copy(alpha = 0.04f),
                                        Color.Transparent,
                                    ),
                                ),
                            )
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ActionPill(
                                text = heroState.badge,
                                background = heroState.accent,
                                onClick = {
                                    when (heroState.badge) {
                                        "浠婃棩澶嶄範" -> onOpenReviewPlanner()
                                        "楂樹紭鍏堝緟锟? -> onOpenTodoPending()
                                        "涓嬩竴闂ㄨ" -> onOpenScheduleDay()
                                        "涓撴敞鐩爣" -> onOpenPomodoro()
                                    }
                                },
                            )
                            Text(
                                if (homeEditMode) "棣栭〉缂栨帓锟? else "浠婃棩鎬昏",
                                style = MaterialTheme.typography.labelLarge,
                                color = ForestDeep.copy(alpha = 0.64f),
                            )
                        }
                        Text(
                            heroState.headline,
                            style = MaterialTheme.typography.headlineMedium,
                            color = PineInk,
                        )
                        Text(
                            heroState.detail,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ForestDeep.copy(alpha = 0.78f),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                HomeHeroStat("浠婃棩璇捐〃", "${todayClassCount} 璇炬", ForestGreen, modifier = Modifier.weight(1f))
                                HomeHeroStat("寰呭姙寰呮帹", "${pendingTodoCount} 锟?, Ginkgo, modifier = Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                HomeHeroStat("涓撴敞绱", "${focusedMinutes} 鍒嗛挓", MossGreen, modifier = Modifier.weight(1f))
                                HomeHeroStat("澶嶄範寰呭姙", "${pendingReviewItems.size} 锟?, TeaGreen, modifier = Modifier.weight(1f))
                            }
                        }
                        if (todayTimeline.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = Color.White.copy(alpha = 0.24f),
                                border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.24f)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text("鎺ヤ笅鏉ュ厛鍋氫粈锟?, style = MaterialTheme.typography.titleMedium, color = PineInk)
                                            Text("鎶婁粖澶╂渶鍊煎緱鍏堝鐞嗙殑浜嬮」鍘嬪埌鍓嶄笁鏉★拷?, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.64f))
                                        }
                                        Text(
                                            "${todayTimeline.size} 锟?,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = heroState.accent,
                                        )
                                    }
                                    todayTimeline.take(3).forEachIndexed { index, item ->
                                        HomeLine(
                                            time = item.timeLabel,
                                            title = item.title,
                                            body = item.subtitle,
                                            sizePreset = HomeModuleSize.Standard,
                                            modifier = Modifier.clickable {
                                                when (item.timeLabel) {
                                                    "浠婃棩璇剧▼" -> onOpenScheduleDay()
                                                    "鑰冭瘯锟? -> onOpenScheduleExamWeek()
                                                    "浠婃棩澶嶄範" -> onOpenReviewPlanner()
                                                    "寰呭姙浼樺厛" -> onOpenTodoPending()
                                                    "涓撴敞鐩爣" -> onOpenTodoPending()
                                                    "涓撴敞缁戝畾" -> onOpenPomodoro()
                                                    "涓撴敞瓒嬪娍" -> onOpenPomodoro()
                                                }
                                            },
                                        )
                                        if (index != minOf(todayTimeline.size, 3) - 1) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("蹇€熸锟?, style = MaterialTheme.typography.titleLarge, color = PineInk)
                        Text(
                            "璇剧▼銆佸緟鍔炪€佹垚缁╁拰鏁欏妤肩粺涓€鎼滅储锛屼笉鐢ㄥ啀鏉ュ洖缈婚〉闈拷?,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestDeep.copy(alpha = 0.68f),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = heroState.accent.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, heroState.accent.copy(alpha = 0.24f)),
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "灏辩华" else "${(localSearchResults.size + gradeSearchResults.size).coerceAtMost(8)} 锟?,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = heroState.accent,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("鎼滅储璇剧▼銆佸緟鍔炪€佹垚缁┿€佹暀瀛︽ゼ") },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (searchQuery.isBlank()) {
                    if (searchHistory.isNotEmpty()) {
                        Text("鏈€杩戞悳锟?, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            searchHistory.take(8).forEach { history ->
                                ActionPill(history, WarmMist) { searchQuery = history }
                            }
                            ActionPill("娓呯┖", CloudWhite) {
                                searchHistory.clear()
                                saveStringList(homePrefs, "search_history", searchHistory)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Text("蹇嵎锟?, style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        quickKeywords.forEach { keyword ->
                            ActionPill(keyword, BambooGlass) { searchQuery = keyword }
                        }
                    }
                }
                if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val mergedResults = localSearchResults + gradeSearchResults
                    if (mergedResults.isEmpty()) {
                        Text(
                            text = if (gradeSearchLoading) "姝ｅ湪鏁寸悊鎼滅储缁撴灉..." else gradeSearchStatus.ifBlank { "褰撳墠娌℃湁鍖归厤缁撴灉锟? },
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestDeep.copy(alpha = 0.72f),
                        )
                    } else {
                        mergedResults.take(8).forEachIndexed { index, result ->
                            SearchResultRow(
                                result = result,
                                onClick = {
                                    rememberSearchTerm(homePrefs, searchHistory, searchQuery)
                                    when (result.category) {
                                        HomeSearchCategory.Course -> onOpenScheduleDay()
                                        HomeSearchCategory.Note -> onOpenCourseNotes(CourseNoteSeed(courseName = result.title))
                                        HomeSearchCategory.Todo -> onOpenTodoPending()
                                        HomeSearchCategory.Grade -> onOpenCampusServices()
                                        HomeSearchCategory.Building -> onOpenMap()
                                    }
                                },
                            )
                            if (index != minOf(mergedResults.size, 8) - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        if (gradeSearchStatus.isNotBlank() && gradeSearchResults.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(gradeSearchStatus, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.68f))
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("宸ヤ綔鍙版ā锟?, style = MaterialTheme.typography.titleLarge, color = PineInk)
                    Text(
                        if (homeEditMode) {
                            "褰撳墠鍙洿鎺ヨ皟鏁存ā鍧楁樉闅愩€侀『搴忓拰灏哄锟?
                        } else {
                            "涓嬮潰寮€濮嬫槸棣栭〉鐨勫彲缂栨帓妯″潡锛屾寜浣犵殑瀛︿範鑺傚鑷敱缁勫悎锟?
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForestDeep.copy(alpha = 0.68f),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.28f),
                    border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.24f)),
                ) {
                    Text(
                        text = "${visibleModules.size} 涓ā锟?,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = PineInk,
                    )
                }
            }
        }
        if (homeEditMode) item {
            GlassCard {
                Text("宸ヤ綔鍙扮紪锟?, style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                Text("閫夋嫨棣栭〉瑕佷繚鐣欏摢浜涙ā鍧楋紝鎸夎嚜宸辩殑浣跨敤鑺傚瑁佸壀宸ヤ綔鍙帮拷?, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    HomeModule.entries.forEach { module ->
                        SelectionChip(
                            text = module.title,
                            chosen = module in visibleModules,
                            onClick = {
                                if (module in visibleModules) {
                                    if (visibleModules.size > 1) visibleModules.remove(module)
                                } else {
                                    visibleModules.add(module)
                                }
                                saveHomeModules(homePrefs, visibleModules)
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("闀挎寜涓嬮潰鐨勬ā鍧楀崱鍙洿鎺ユ嫋鍔ㄦ帓搴忥拷?, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.62f))
                Spacer(modifier = Modifier.height(12.dp))
                visibleModules.forEachIndexed { index, module ->
                    key(module) {
                        val isDragging = draggingModule == module
                        val moduleSize = moduleSizes[module] ?: defaultHomeModuleSize(module)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isDragging) TeaGreen.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.24f),
                            border = BorderStroke(1.dp, if (isDragging) ForestGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.18f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(0, if (isDragging) dragOffsetY.roundToInt() else 0) }
                                .scale(if (isDragging) 1.02f else 1f)
                                .pointerInput(module, visibleModules.size) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingModule = module
                                            dragOffsetY = 0f
                                        },
                                        onDragCancel = {
                                            draggingModule = null
                                            dragOffsetY = 0f
                                        },
                                        onDragEnd = {
                                            draggingModule = null
                                            dragOffsetY = 0f
                                            saveHomeModules(homePrefs, visibleModules)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (draggingModule != module) return@detectDragGesturesAfterLongPress
                                            dragOffsetY += dragAmount.y
                                            val currentIndex = visibleModules.indexOf(module)
                                            if (dragOffsetY > dragSwapThreshold && currentIndex < visibleModules.lastIndex) {
                                                visibleModules.swap(currentIndex, currentIndex + 1)
                                                dragOffsetY -= dragSwapThreshold
                                                saveHomeModules(homePrefs, visibleModules)
                                            } else if (dragOffsetY < -dragSwapThreshold && currentIndex > 0) {
                                                visibleModules.swap(currentIndex, currentIndex - 1)
                                                dragOffsetY += dragSwapThreshold
                                                saveHomeModules(homePrefs, visibleModules)
                                            }
                                        },
                                    )
                                },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(module.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                                    Text(if (isDragging) "鎷栧姩锟? else "闀挎寜鍚庝笂涓嬫嫋锟?, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.62f))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                        HomeModuleSize.entries.forEach { sizePreset ->
                                            SelectionChip(
                                                text = sizePreset.title,
                                                chosen = moduleSize == sizePreset,
                                                onClick = {
                                                    moduleSizes[module] = sizePreset
                                                    saveHomeModuleSizes(homePrefs, moduleSizes)
                                                },
                                            )
                                        }
                                    }
                                }
                                Text("锟?, style = MaterialTheme.typography.headlineSmall, color = ForestDeep.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp, start = 12.dp))
                            }
                        }
                        if (index != visibleModules.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        moduleRows.forEach { rowModules ->
            item {
                if (rowModules.size == 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        rowModules.forEach { module ->
                            renderHomeModule(module, Modifier.weight(1f), true)
                        }
                    }
                } else {
                    renderHomeModule(rowModules.first(), Modifier.fillMaxWidth(), false)
                }
            }
        }
    }
}

private fun loadHomeModules(
    prefs: android.content.SharedPreferences,
): List<HomeModule> {
    val stored = loadStringList(prefs, "visible_modules")
    if (stored.isEmpty()) return HomeModule.entries.toList()
    val parsed = stored.mapNotNull { raw -> runCatching { HomeModule.valueOf(raw) }.getOrNull() }
    if (parsed.isEmpty()) return HomeModule.entries.toList()
    val missing = HomeModule.entries.filterNot { it in parsed }
    return parsed + missing
}

private fun defaultHomeModuleSize(module: HomeModule): HomeModuleSize =
    when (module) {
        HomeModule.Metrics -> HomeModuleSize.Compact
        HomeModule.Rhythm -> HomeModuleSize.Standard
        HomeModule.Learning -> HomeModuleSize.Hero
        HomeModule.QuickPoints -> HomeModuleSize.Compact
        HomeModule.RecentPoints -> HomeModuleSize.Compact
        HomeModule.Assistant -> HomeModuleSize.Hero
    }

private fun loadHomeModuleSizes(
    prefs: android.content.SharedPreferences,
): Map<HomeModule, HomeModuleSize> {
    val stored = loadStringList(prefs, "module_sizes")
    val parsed = stored.mapNotNull { raw ->
        val parts = raw.split(":")
        if (parts.size != 2) return@mapNotNull null
        val module = runCatching { HomeModule.valueOf(parts[0]) }.getOrNull() ?: return@mapNotNull null
        val size = runCatching { HomeModuleSize.valueOf(parts[1]) }.getOrNull() ?: return@mapNotNull null
        module to size
    }.toMap()
    return HomeModule.entries.associateWith { module -> parsed[module] ?: defaultHomeModuleSize(module) }
}

private fun saveHomeModuleSizes(
    prefs: android.content.SharedPreferences,
    moduleSizes: Map<HomeModule, HomeModuleSize>,
) {
    saveStringList(
        prefs,
        "module_sizes",
        moduleSizes.entries.map { (module, size) -> "${module.name}:${size.name}" },
    )
}

private fun saveHomeModules(
    prefs: android.content.SharedPreferences,
    modules: List<HomeModule>,
) {
    saveStringList(prefs, "visible_modules", modules.map { it.name })
}

private fun rememberSearchTerm(
    prefs: android.content.SharedPreferences,
    history: MutableList<String>,
    query: String,
) {
    val normalized = query.trim()
    if (normalized.isBlank()) return
    history.remove(normalized)
    history.add(0, normalized)
    while (history.size > 10) {
        history.removeAt(history.lastIndex)
    }
    saveStringList(prefs, "search_history", history)
}

private fun loadCollapsedHomeModules(
    prefs: android.content.SharedPreferences,
): List<HomeModule> {
    val stored = loadStringList(prefs, "collapsed_modules")
    return stored.mapNotNull { raw -> runCatching { HomeModule.valueOf(raw) }.getOrNull() }
        .filter { isHomeModuleCollapsible(it) }
}

private fun saveCollapsedHomeModules(
    prefs: android.content.SharedPreferences,
    modules: List<HomeModule>,
) {
    saveStringList(prefs, "collapsed_modules", modules.distinct().filter { isHomeModuleCollapsible(it) }.map { it.name })
}

private fun buildHomeModuleRows(
    modules: List<HomeModule>,
    moduleSizes: Map<HomeModule, HomeModuleSize>,
): List<List<HomeModule>> {
    val rows = mutableListOf<List<HomeModule>>()
    var index = 0
    while (index < modules.size) {
        val current = modules[index]
        val currentCompact = isHomeModulePairable(current, moduleSizes[current] ?: defaultHomeModuleSize(current))
        val next = modules.getOrNull(index + 1)
        val nextCompact = next != null && isHomeModulePairable(next, moduleSizes[next] ?: defaultHomeModuleSize(next))
        if (currentCompact && nextCompact) {
            rows += listOf(current, next!!)
            index += 2
        } else {
            rows += listOf(current)
            index += 1
        }
    }
    return rows
}

private fun isHomeModuleCollapsible(module: HomeModule): Boolean =
    module in setOf(
        HomeModule.Rhythm,
        HomeModule.Learning,
        HomeModule.Assistant,
    )

private fun isHomeModulePairable(
    module: HomeModule,
    sizePreset: HomeModuleSize,
): Boolean {
    if (sizePreset != HomeModuleSize.Compact) return false
    return module in setOf(
        HomeModule.QuickPoints,
        HomeModule.RecentPoints,
        HomeModule.Metrics,
    )
}

private fun loadHomeGradeCache(
    prefs: android.content.SharedPreferences,
): List<FeedCard> {
    val raw = prefs.getString("grade_cache_v1", "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    FeedCard(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        source = item.optString("source"),
                        description = item.optString("description"),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}
