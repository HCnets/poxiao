
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

private enum class PrimarySection(val label: String, val navLabel: String, val icon: ImageVector) {
    Home("首页", "智能体", Icons.Rounded.AutoAwesome),
    Schedule("课表", "课表", Icons.Rounded.CalendarMonth),
    Todo("待办", "待办", Icons.Rounded.ViewKanban),
    Pomodoro("番茄钟", "番茄钟", Icons.Rounded.Timer),
    More("更多", "更多", Icons.Rounded.GridView),
}

private enum class ScheduleMode(val label: String) {
    Week("周视图"),
    Day("日视图"),
    Month("月视图"),
}

private enum class ScheduleWorkbench(val title: String) {
    Timetable("课表"),
    Grades("成绩趋势"),
    ExamWeek("考试周"),
}

private enum class ExamWeekFilter(val title: String) {
    All("全部"),
    Pending("待处理"),
    Urgent("临近"),
    Finished("已完成"),
}

private enum class ExamWeekTypeFilter(val title: String) {
    All("全部类型"),
    Exam("只看考试"),
    Assignment("只看作业"),
    Review("只看复习"),
}

private enum class TodoFilter(val title: String) {
    All("全部"),
    Focus("聚焦"),
    Today("今天"),
    Done("已完成"),
}

private enum class TodoViewMode(val title: String) {
    Flat("清单"),
    Grouped("分组"),
    Calendar("日历"),
}

private enum class TodoDuePreset(val title: String) {
    Today("今天"),
    Tonight("今晚"),
    Tomorrow("明天"),
    ThisWeek("本周内"),
    NextWeek("下周"),
}

private enum class TodoReminderPreset(val title: String) {
    None("不提醒"),
    Before10Min("提前 10 分钟"),
    Before30Min("提前 30 分钟"),
    Before1Hour("提前 1 小时"),
    PreviousNight("前一天晚 20:00"),
}

private val TodoFocusGoalOptions = listOf(0, 1, 2, 3, 4, 6, 8)

private enum class TodoFocusGoalFilter(val title: String) {
    All("全部任务"),
    WithGoal("有目标"),
    Pending("未达成"),
    Reached("已达成"),
}

private data class FocusRecord(
    val taskTitle: String,
    val modeTitle: String,
    val seconds: Int,
    val finishedAt: String,
)

private data class TodoDraft(
    val editingTaskId: String?,
    val title: String,
    val note: String,
    val duePreset: TodoDuePreset,
    val dueClock: String,
    val listName: String,
    val reminderPreset: TodoReminderPreset,
    val repeatText: String,
    val quadrant: TodoQuadrant,
    val priority: TodoPriority,
    val focusGoal: Int,
    val tagDraft: String,
    val tags: List<String>,
    val subtaskDraft: String,
    val subtasks: List<TodoSubtask>,
)

private data class PomodoroSession(
    val mode: PomodoroMode,
    val presetTitle: String,
    val presetSeconds: Int,
    val leftSeconds: Int,
    val running: Boolean,
    val autoNext: Boolean,
    val strictMode: Boolean,
    val boundTask: String,
    val sound: String,
    val ambientOn: Boolean,
    val focusMinutes: Int,
    val cycles: Int,
    val customMinutes: Int,
    val customSeconds: Int,
    val onlyPendingGoalTasks: Boolean,
)

private data class FocusDayStat(
    val label: String,
    val minutes: Int,
)

private data class FocusTaskStat(
    val title: String,
    val minutes: Int,
    val count: Int,
)

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

private enum class HomeSearchCategory(val title: String) {
    Course("课程"),
    Note("笔记"),
    Todo("待办"),
    Grade("成绩"),
    Building("楼栋"),
}

private data class HomeSearchResult(
    val id: String,
    val category: HomeSearchCategory,
    val title: String,
    val subtitle: String,
    val detail: String,
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
    Metrics("核心指标"),
    Rhythm("今天的节奏"),
    Learning("学习推进"),
    QuickPoints("快捷点位"),
    RecentPoints("最近访问"),
    Assistant("智能体"),
}

private enum class HomeModuleSize(
    val title: String,
) {
    Compact("紧凑"),
    Standard("标准"),
    Hero("强调"),
}

private enum class PomodoroMode(val title: String) {
    Focus("专注"),
    ShortBreak("短休息"),
    LongBreak("长休息"),
}

private enum class OverlayPage(val label: String) {
    CampusServices("校园服务"),
    CampusMap("校园地图"),
    AcademicAccount("教务账号"),
    Calculator("科学计算器"),
    CourseNotes("课程笔记"),
    ReviewPlanner("复习计划"),
    NotificationPreferences("通知偏好"),
    LearningDashboard("学习数据"),
    ExportCenter("导出中心"),
    Preferences("界面偏好"),
    AssistantPermissions("智能体权限"),
}

internal enum class UiDensityPreset(
    val title: String,
    val scale: Float,
) {
    Compact("紧凑", 0.92f),
    Comfortable("均衡", 1f),
    Relaxed("舒展", 1.08f),
}

internal enum class GlassStrengthPreset(
    val title: String,
    val cardAlpha: Float,
    val glowScale: Float,
) {
    Crisp("清透", 0.88f, 0.72f),
    Balanced("柔雾", 1f, 1f),
    Lush("晶润", 1.14f, 1.24f),
}

private val LocalUiDensityPreset = staticCompositionLocalOf { UiDensityPreset.Comfortable }
private val LocalGlassStrengthPreset = staticCompositionLocalOf { GlassStrengthPreset.Balanced }
private val LocalStaticGlassMode = staticCompositionLocalOf { false }

private data class ScheduleExtraEvent(
    val id: String,
    val date: String,
    val title: String,
    val time: String,
    val type: String,
    val note: String,
)

private data class ScheduleEventDraft(
    val eventId: String?,
    val date: String,
    val title: String,
    val time: String,
    val type: String,
    val note: String,
)

private data class GradeTrendPoint(
    val termName: String,
    val averageScore: Double,
    val averageGradePoint: Double,
    val credits: Double,
    val excellentCount: Int,
    val warningCount: Int,
    val courseCount: Int,
    val rawCards: List<FeedCard>,
)

private data class ExamWeekItem(
    val id: String,
    val date: String,
    val title: String,
    val subtitle: String,
    val detail: String,
    val accent: Color,
    val priority: Int,
    val countdownLabel: String,
    val finished: Boolean = false,
)

private data class DayTimelineEntry(
    val sortKey: Int,
    val title: String,
    val subtitle: String,
    val detail: String,
    val accent: Color,
    val tags: List<String>,
    val selectableCourse: HitaCourseBlock? = null,
    val extraEvent: ScheduleExtraEvent? = null,
)

private data class PomodoroPreset(
    val title: String,
    val seconds: Int,
)

@Composable
fun PoxiaoApp() {
    val context = LocalContext.current
    val uiPrefs = remember { context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE) }
    var themePreset by remember {
        mutableStateOf(
            runCatching {
                PoxiaoThemePreset.valueOf(uiPrefs.getString("theme_preset", PoxiaoThemePreset.Forest.name).orEmpty())
            }.getOrDefault(PoxiaoThemePreset.Forest),
        )
    }
    var densityPreset by remember {
        mutableStateOf(
            runCatching {
                UiDensityPreset.valueOf(uiPrefs.getString("density_preset", UiDensityPreset.Comfortable.name).orEmpty())
            }.getOrDefault(UiDensityPreset.Comfortable),
        )
    }
    var glassStrengthPreset by remember {
        mutableStateOf(
            runCatching {
                GlassStrengthPreset.valueOf(uiPrefs.getString("glass_preset", GlassStrengthPreset.Balanced.name).orEmpty())
            }.getOrDefault(GlassStrengthPreset.Balanced),
        )
    }
    var liquidGlassStylePreset by remember {
        mutableStateOf(
            runCatching {
                LiquidGlassStylePreset.valueOf(
                    uiPrefs.getString("liquid_glass_style", LiquidGlassStylePreset.IOS.name).orEmpty(),
                )
            }.getOrDefault(LiquidGlassStylePreset.IOS),
        )
    }

    LaunchedEffect(Unit) {
        refreshLocalReminderSchedule(context)
    }

    PoxiaoTheme(preset = themePreset) {
        CompositionLocalProvider(
            LocalUiDensityPreset provides densityPreset,
            LocalGlassStrengthPreset provides glassStrengthPreset,
            LocalLiquidGlassStylePreset provides liquidGlassStylePreset,
        ) {
            PoxiaoAppScaffold(
                themePreset = themePreset,
                densityPreset = densityPreset,
                glassStrengthPreset = glassStrengthPreset,
                liquidGlassStylePreset = liquidGlassStylePreset,
                onThemePresetChange = {
                    themePreset = it
                    uiPrefs.edit().putString("theme_preset", it.name).apply()
                },
                onDensityPresetChange = {
                    densityPreset = it
                    uiPrefs.edit().putString("density_preset", it.name).apply()
                },
                onGlassStrengthChange = {
                    glassStrengthPreset = it
                    uiPrefs.edit().putString("glass_preset", it.name).apply()
                },
                onLiquidGlassStyleChange = {
                    liquidGlassStylePreset = it
                    uiPrefs.edit().putString("liquid_glass_style", it.name).apply()
                },
            )
        }
    }
}

@Composable
private fun PoxiaoAppScaffold(
    themePreset: PoxiaoThemePreset,
    densityPreset: UiDensityPreset,
    glassStrengthPreset: GlassStrengthPreset,
    liquidGlassStylePreset: LiquidGlassStylePreset,
    onThemePresetChange: (PoxiaoThemePreset) -> Unit,
    onDensityPresetChange: (UiDensityPreset) -> Unit,
    onGlassStrengthChange: (GlassStrengthPreset) -> Unit,
    onLiquidGlassStyleChange: (LiquidGlassStylePreset) -> Unit,
) {
    var pendingDrawerAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var section by remember { mutableStateOf(PrimarySection.Home) }
    var previousSection by remember { mutableStateOf(PrimarySection.Home) }
    var sectionTransitionDirection by remember { mutableIntStateOf(1) }
    val sectionSweepProgress = remember { Animatable(1f) }
    var overlayPage by remember { mutableStateOf<OverlayPage?>(null) }
    var sideNavExpanded by remember { mutableStateOf(false) }
    var courseNoteSeed by remember { mutableStateOf<CourseNoteSeed?>(null) }
    var reviewPlannerSeed by remember { mutableStateOf<ReviewPlannerSeed?>(null) }
    var assistantHistoryFocusAt by remember { mutableStateOf<Long?>(null) }
    var islandVisible by remember { mutableStateOf(true) }
    var scheduleEntryMode by remember { mutableStateOf(ScheduleMode.Week) }
    var scheduleEntryWorkbench by remember { mutableStateOf(ScheduleWorkbench.Timetable) }
    var todoEntryFilter by remember { mutableStateOf(TodoFilter.All) }
    val sectionOrder = remember {
        listOf(
            PrimarySection.Home,
            PrimarySection.Schedule,
            PrimarySection.Todo,
            PrimarySection.Pomodoro,
            PrimarySection.More,
        )
    }
    val residentSections = remember { mutableStateListOf(PrimarySection.Home) }
    val repository = remember { HitaScheduleRepository() }
    val palette = PoxiaoThemeState.palette

    LaunchedEffect(section, overlayPage) {
        islandVisible = true
        delay(2200)
        islandVisible = false
    }
    LaunchedEffect(overlayPage) {
        if (overlayPage != null && sideNavExpanded) {
            sideNavExpanded = false
        }
    }
    LaunchedEffect(section) {
        if (section !in residentSections) {
            residentSections += section
        }
    }
    LaunchedEffect(section) {
        if (section != previousSection) {
            val from = previousSection
            sectionTransitionDirection = if (sectionOrder.indexOf(section) >= sectionOrder.indexOf(from)) 1 else -1
            previousSection = section
            sectionSweepProgress.snapTo(0f)
            sectionSweepProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = CubicBezierEasing(0.14f, 0.98f, 0.22f, 1f),
                ),
            )
        }
    }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        withFrameNanos { }
        delay(520)
        sectionOrder.filter { it != PrimarySection.Home }.forEach { target ->
            if (target !in residentSections) {
                residentSections += target
            }
            delay(220)
        }
    }
    BackHandler(enabled = sideNavExpanded || overlayPage != null || section != PrimarySection.Home) {
        when {
            sideNavExpanded -> {
                pendingDrawerAction = null
                sideNavExpanded = false
            }

            overlayPage != null -> {
                overlayPage = null
            }

            section != PrimarySection.Home -> {
                section = PrimarySection.Home
            }
        }
    }
    LaunchedEffect(sideNavExpanded, pendingDrawerAction) {
        val action = pendingDrawerAction ?: return@LaunchedEffect
        if (!sideNavExpanded) {
            withFrameNanos { }
            action()
            pendingDrawerAction = null
        }
    }

    LiquidGlassScene(modifier = Modifier.fillMaxSize()) {
        when (overlayPage) {
            OverlayPage.CampusServices -> CampusServicesScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = { overlayPage = null },
                onOpenMap = { overlayPage = OverlayPage.CampusMap },
            )
            OverlayPage.CampusMap -> CampusMapScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = { overlayPage = null },
            )
            OverlayPage.AcademicAccount -> AcademicAccountScreen(
                repository = repository,
                modifier = Modifier.fillMaxSize(),
                onBack = { overlayPage = null },
            )
            OverlayPage.Calculator -> ScientificCalculatorScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = { overlayPage = null },
            )
            OverlayPage.CourseNotes -> CourseNotesScreen(
                modifier = Modifier.fillMaxSize(),
                initialSeed = courseNoteSeed,
                onBack = { overlayPage = null },
            )
            OverlayPage.ReviewPlanner -> ReviewPlannerScreen(
                modifier = Modifier.fillMaxSize(),
                initialSeed = reviewPlannerSeed,
                onOpenAssistantHistory = { executedAt ->
                    assistantHistoryFocusAt = executedAt
                    overlayPage = null
                    section = PrimarySection.Home
                },
                onOpenCourseNoteSource = { seed ->
                    courseNoteSeed = seed
                    overlayPage = OverlayPage.CourseNotes
                },
                onOpenExportCenter = { overlayPage = OverlayPage.ExportCenter },
                onBack = { overlayPage = null },
            )
            OverlayPage.NotificationPreferences -> NotificationPreferencesScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = { overlayPage = null },
            )
            OverlayPage.LearningDashboard -> LearningDashboardScreen(
                modifier = Modifier.fillMaxSize(),
                onOpenExportCenter = { overlayPage = OverlayPage.ExportCenter },
                onBack = { overlayPage = null },
            )
            OverlayPage.ExportCenter -> ExportCenterScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = { overlayPage = null },
            )
            OverlayPage.Preferences -> PreferencesScreen(
                modifier = Modifier.fillMaxSize(),
                currentPreset = themePreset,
                currentDensity = densityPreset,
                currentGlassStrength = glassStrengthPreset,
                currentGlassStyle = liquidGlassStylePreset,
                onSelectPreset = onThemePresetChange,
                onSelectDensity = onDensityPresetChange,
                onSelectGlassStrength = onGlassStrengthChange,
                onSelectGlassStyle = onLiquidGlassStyleChange,
                onBack = { overlayPage = null },
            )
            OverlayPage.AssistantPermissions -> AssistantPermissionScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = { overlayPage = null },
            )
            null -> {
                val renderSections = sectionOrder.filter { it in residentSections }
                val transitionEasing = remember { CubicBezierEasing(0.14f, 0.98f, 0.22f, 1f) }
                val easedTransition = transitionEasing.transform(sectionSweepProgress.value.coerceIn(0f, 1f))
                val settleWindow = ((easedTransition - 0.9f) / 0.1f).coerceIn(0f, 1f)
                val settlePulseBase = sin((settleWindow * Math.PI).toDouble()).toFloat().coerceAtLeast(0f)
                val settlePulse = (settlePulseBase * settlePulseBase * (1f - settleWindow * 0.68f)).coerceIn(0f, 1f)
                Box(modifier = Modifier.fillMaxSize()) {
                    renderSections.forEach { residentSection ->
                        key(residentSection) {
                            val isCurrentSection = residentSection == section
                            val sectionModifier = if (isCurrentSection) {
                                Modifier
                                    .fillMaxSize()
                                    .zIndex(1f)
                                    .graphicsLayer {
                                        val reveal = 1f - easedTransition
                                        alpha = 0.99f + (easedTransition * 0.01f)
                                        translationX = 0f
                                        translationY = size.height * ((0.004f * reveal) - (0.0006f * settlePulse))
                                        scaleX = 0.9935f + (easedTransition * 0.0065f) + (0.001f * settlePulse)
                                        scaleY = 0.994f + (easedTransition * 0.006f) + (0.0008f * settlePulse)
                                        shadowElevation = 0f
                                    }
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)
                                        layout(placeable.width, placeable.height) {
                                            placeable.place(0, 0)
                                        }
                                    }
                            } else {
                                Modifier
                                    .zIndex(-1f)
                                    .layout { measurable, constraints ->
                                        measurable.measure(constraints)
                                        layout(0, 0) {}
                                    }
                            }
                            Box(
                                modifier = sectionModifier,
                            ) {
                                when (residentSection) {
                                    PrimarySection.Home -> HomeScreen(
                                        initialAssistantHistoryFocusAt = assistantHistoryFocusAt,
                                        onAssistantHistoryFocusConsumed = { assistantHistoryFocusAt = null },
                                        onOpenMap = { overlayPage = OverlayPage.CampusMap },
                                        onOpenScheduleDay = {
                                            scheduleEntryWorkbench = ScheduleWorkbench.Timetable
                                            scheduleEntryMode = ScheduleMode.Day
                                            section = PrimarySection.Schedule
                                        },
                                        onOpenScheduleExamWeek = {
                                            scheduleEntryWorkbench = ScheduleWorkbench.ExamWeek
                                            section = PrimarySection.Schedule
                                        },
                                        onOpenCampusServices = { overlayPage = OverlayPage.CampusServices },
                                        onOpenTodoPending = {
                                            todoEntryFilter = TodoFilter.All
                                            section = PrimarySection.Todo
                                        },
                                        onOpenPomodoro = { section = PrimarySection.Pomodoro },
                                        onOpenReviewPlanner = {
                                            reviewPlannerSeed = null
                                            overlayPage = OverlayPage.ReviewPlanner
                                        },
                                        onOpenReviewPlannerSeeded = { seed ->
                                            reviewPlannerSeed = seed
                                            overlayPage = OverlayPage.ReviewPlanner
                                        },
                                        onOpenAssistantPermissions = { overlayPage = OverlayPage.AssistantPermissions },
                                        onOpenCourseNotes = { seed ->
                                            courseNoteSeed = seed
                                            overlayPage = OverlayPage.CourseNotes
                                        },
                                    )
                                    PrimarySection.Schedule -> ScheduleScreen(
                                        repository,
                                        initialMode = scheduleEntryMode,
                                        initialWorkbench = scheduleEntryWorkbench,
                                        onOpenAcademicAccount = { section = PrimarySection.More },
                                        onOpenCourseNotes = { seed ->
                                            courseNoteSeed = seed
                                            overlayPage = OverlayPage.CourseNotes
                                        },
                                    )
                                    PrimarySection.Todo -> TodoScreen(initialFilter = todoEntryFilter)
                                    PrimarySection.Pomodoro -> PomodoroScreen(active = isCurrentSection)
                                    PrimarySection.More -> MoreScreen(
                                        repository = repository,
                                        onOpenAcademicAccount = { overlayPage = OverlayPage.AcademicAccount },
                                        onOpenCampusServices = { overlayPage = OverlayPage.CampusServices },
                                        onOpenCalculator = { overlayPage = OverlayPage.Calculator },
                                        onOpenCourseNotes = {
                                            courseNoteSeed = null
                                            overlayPage = OverlayPage.CourseNotes
                                        },
                                        onOpenReviewPlanner = {
                                            reviewPlannerSeed = null
                                            overlayPage = OverlayPage.ReviewPlanner
                                        },
                                        onOpenNotificationPreferences = { overlayPage = OverlayPage.NotificationPreferences },
                                        onOpenLearningDashboard = { overlayPage = OverlayPage.LearningDashboard },
                                        onOpenExportCenter = { overlayPage = OverlayPage.ExportCenter },
                                        onOpenPreferences = { overlayPage = OverlayPage.Preferences },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        IslandHint(
            text = overlayPage?.label ?: section.label,
            icon = section.icon,
            visible = islandVisible && overlayPage == null && !sideNavExpanded,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp),
        )

        if (overlayPage == null) {
            SideNavToggleButton(
                expanded = sideNavExpanded,
                onClick = { sideNavExpanded = !sideNavExpanded },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .zIndex(5f)
                    .padding(
                        start = 16.dp,
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp,
                    ),
            )

            SideNavigationDrawer(
                expanded = sideNavExpanded,
                currentSection = section,
                currentOverlay = overlayPage,
                onDismiss = {
                    pendingDrawerAction = null
                    sideNavExpanded = false
                },
                onSelectSection = { target ->
                    pendingDrawerAction = {
                        overlayPage = null
                        section = target
                    }
                    sideNavExpanded = false
                },
                onOpenOverlay = { target ->
                    pendingDrawerAction = {
                        when (target) {
                            OverlayPage.CourseNotes -> {
                                courseNoteSeed = null
                                overlayPage = OverlayPage.CourseNotes
                            }

                            OverlayPage.ReviewPlanner -> {
                                reviewPlannerSeed = null
                                overlayPage = OverlayPage.ReviewPlanner
                            }

                            else -> {
                                overlayPage = target
                            }
                        }
                    }
                    sideNavExpanded = false
                },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(4f),
            )

            BottomDock(
                current = section,
                onSelect = { section = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 10.dp,
                    ),
            )
        }
    }
}

@Composable
private fun SideNavToggleButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val tint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.2f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.14f)
    }
    val border = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.24f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.34f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.28f)
    }
    val glow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.04f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.03f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.12f)
    }
    val lineColor = if (expanded) palette.primary else palette.pillOn
    val interactionSource = remember { MutableInteractionSource() }

    LiquidGlassSurface(
        modifier = modifier
            .size(36.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        cornerRadius = 18.dp,
        shapeOverride = CircleShape,
        contentPadding = PaddingValues(0.dp),
        tint = tint,
        borderColor = border,
        glowColor = glow,
        shadowColor = Color.Transparent,
        blurRadius = 10.dp,
        refractionHeight = 8.dp,
        refractionAmount = 10.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.requiredWidth(16.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                listOf(14.dp, if (expanded) 10.dp else 14.dp, 14.dp).forEach { width ->
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(lineColor.copy(alpha = 0.96f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun SideNavigationDrawer(
    expanded: Boolean,
    currentSection: PrimarySection,
    currentOverlay: OverlayPage?,
    onDismiss: () -> Unit,
    onSelectSection: (PrimarySection) -> Unit,
    onOpenOverlay: (OverlayPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val panelEasing = remember { CubicBezierEasing(0.2f, 0.92f, 0.22f, 1f) }
    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 340, easing = panelEasing),
        label = "side-nav-progress",
    )

    val scrimAlpha = 0.28f * progress
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val panelTint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.2f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.03f)
        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.12f)
    }
    val panelBorder = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.24f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.34f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.28f)
    }
    val panelGlow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.04f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.14f)
    }
    Box(
        modifier = modifier.drawWithContent {
            if (progress > 0.001f) {
                drawContent()
            }
        },
    ) {
        val panelModifier = if (progress > 0.001f || expanded) {
            Modifier
                .align(Alignment.CenterStart)
                .padding(
                    start = 12.dp,
                    top = statusTop + 8.dp,
                    bottom = navigationBottom + 8.dp,
                )
                .fillMaxHeight()
                .requiredWidth(308.dp)
                .offset(x = (-28.dp) * (1f - progress))
                .alpha((0.001f + 0.999f * progress).coerceIn(0f, 1f))
                .scale(0.96f + 0.04f * progress)
        } else {
            Modifier
                .requiredWidth(1.dp)
                .requiredHeight(1.dp)
                .offset(x = (-640).dp, y = (-640).dp)
                .alpha(0f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .then(
                    if (progress > 0.001f) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(onTap = { onDismiss() })
                        }
                    } else {
                        Modifier
                    },
                ),
        )

        LiquidGlassSurface(
            modifier = panelModifier,
            cornerRadius = 34.dp,
            shapeOverride = RoundedCornerShape(34.dp),
            contentPadding = PaddingValues(0.dp),
            tint = panelTint,
            borderColor = panelBorder,
            glowColor = panelGlow,
            blurRadius = 16.dp,
            refractionHeight = 12.dp,
            refractionAmount = 16.dp,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SideNavHeader(
                        currentLabel = currentOverlay?.label ?: sectionDisplayTitle(currentSection),
                    )
                }
                item {
                    SideNavGroupTitle(title = "主分区", detail = "5 项")
                }
                items(
                    listOf(
                        PrimarySection.Home,
                        PrimarySection.Schedule,
                        PrimarySection.Todo,
                        PrimarySection.Pomodoro,
                        PrimarySection.More,
                    ),
                ) { item ->
                    SideNavEntry(
                        title = sectionDisplayTitle(item),
                        icon = item.icon,
                        accent = sideNavAccentForSection(item),
                        active = currentOverlay == null && currentSection == item,
                        onClick = { onSelectSection(item) },
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    SideNavGroupTitle(title = "快捷入口", detail = "8 项")
                }
                item {
                    SideNavEntry(
                        title = "课程笔记",
                        accent = MossGreen,
                        active = currentOverlay == OverlayPage.CourseNotes,
                        onClick = { onOpenOverlay(OverlayPage.CourseNotes) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "复习计划",
                        accent = TeaGreen,
                        active = currentOverlay == OverlayPage.ReviewPlanner,
                        onClick = { onOpenOverlay(OverlayPage.ReviewPlanner) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "学习数据",
                        accent = Ginkgo,
                        active = currentOverlay == OverlayPage.LearningDashboard,
                        onClick = { onOpenOverlay(OverlayPage.LearningDashboard) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "导出中心",
                        accent = WarmMist,
                        active = currentOverlay == OverlayPage.ExportCenter,
                        onClick = { onOpenOverlay(OverlayPage.ExportCenter) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "校园服务",
                        accent = BambooGlass,
                        active = currentOverlay == OverlayPage.CampusServices || currentOverlay == OverlayPage.CampusMap,
                        onClick = { onOpenOverlay(OverlayPage.CampusServices) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "科学计算器",
                        accent = CloudWhite,
                        active = currentOverlay == OverlayPage.Calculator,
                        onClick = { onOpenOverlay(OverlayPage.Calculator) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "通知偏好",
                        accent = ForestGreen,
                        active = currentOverlay == OverlayPage.NotificationPreferences,
                        onClick = { onOpenOverlay(OverlayPage.NotificationPreferences) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "界面偏好",
                        accent = palette.primary,
                        active = currentOverlay == OverlayPage.Preferences,
                        onClick = { onOpenOverlay(OverlayPage.Preferences) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SideNavGroupTitle(
    title: String,
    detail: String,
) {
    val palette = PoxiaoThemeState.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = palette.softText,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = palette.softText.copy(alpha = 0.68f),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            palette.cardBorder.copy(alpha = 0.4f),
                            palette.cardBorder.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun SideNavHeader(
    currentLabel: String,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = palette.card.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.62f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.primary.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.2f)),
                ) {
                    Text(
                        text = "全局导航",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(palette.primary.copy(alpha = 0.88f)),
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = currentLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.ink,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SideNavHeaderChip(
                    title = "主分区",
                    value = "5",
                )
                SideNavHeaderChip(
                    title = "快捷入口",
                    value = "8",
                )
            }
        }
    }
}

@Composable
private fun SideNavHeaderChip(
    title: String,
    value: String,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = palette.ink,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = palette.softText,
            )
        }
    }
}

@Composable
private fun SideNavEntry(
    title: String,
    subtitle: String = "",
    accent: Color,
    active: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val palette = PoxiaoThemeState.palette
    val shape = RoundedCornerShape(26.dp)
    val backgroundBrush = if (active) {
        Brush.linearGradient(
            listOf(
                accent.copy(alpha = 0.16f),
                accent.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.1f),
            ),
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.1f),
                palette.card.copy(alpha = 0.28f),
            ),
        )
    }
    val border = if (active) accent.copy(alpha = 0.3f) else palette.cardBorder.copy(alpha = 0.48f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = if (active) 0.24f else 0.12f),
                    border = BorderStroke(1.dp, accent.copy(alpha = if (active) 0.24f else 0.16f)),
                ) {
                    Box(
                        modifier = Modifier.size(if (active) 38.dp else 34.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (active) accent else palette.ink.copy(alpha = 0.78f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accent.copy(alpha = 0.88f),
                                    accent.copy(alpha = 0.42f),
                                ),
                            ),
                        ),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.ink,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.softText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (active) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, if (active) accent.copy(alpha = 0.18f) else palette.cardBorder.copy(alpha = 0.4f)),
            ) {
                Text(
                    text = if (active) "当前" else "进入",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) accent else palette.softText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                )
            }
        }
    }
}

private fun sectionDisplayTitle(section: PrimarySection): String =
    when (section) {
        PrimarySection.Home -> "智能体"
        else -> section.label
    }

private fun sideNavAccentForSection(section: PrimarySection): Color =
    when (section) {
        PrimarySection.Home -> ForestGreen
        PrimarySection.Schedule -> TeaGreen
        PrimarySection.Todo -> WarmMist
        PrimarySection.Pomodoro -> Ginkgo
        PrimarySection.More -> BambooGlass
    }

@Composable
private fun IslandHint(
    text: String,
    icon: ImageVector,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val islandEasing = remember { CubicBezierEasing(0.2f, 0.92f, 0.22f, 1f) }
    val islandCloseEasing = remember { CubicBezierEasing(0.32f, 0f, 0.2f, 1f) }
    val expandedWidth = remember(text) {
        when (text.length) {
            0, 1 -> 102.dp
            2 -> 114.dp
            3 -> 126.dp
            else -> 138.dp
        }
    }
    val islandTint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.18f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.12f)
    }
    val islandBorder = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.2f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.36f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.28f)
    }
    val islandGlow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.04f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.03f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.24f)
    }
    val islandBlur = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> 6.dp
        LiquidGlassStylePreset.IOS -> 7.dp
        LiquidGlassStylePreset.Hyper -> 8.dp
    }
    val islandRefraction = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> 4.dp
        LiquidGlassStylePreset.IOS -> 7.dp
        LiquidGlassStylePreset.Hyper -> 10.dp
    }
    val transition = updateTransition(targetState = visible, label = "island-shell")
    val shellProgress by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = if (targetState) 400 else 300,
                easing = if (targetState) islandEasing else islandCloseEasing,
            )
        },
        label = "island-progress",
    ) { shown ->
        if (shown) 1f else 0f
    }
    val collapsedWidth = 38.dp
    val shellWidth = collapsedWidth + (expandedWidth - collapsedWidth) * shellProgress
    val shellAlpha = shellProgress.coerceIn(0f, 1f)
    val contentAlpha = if (visible) {
        ((shellProgress - 0.34f) / 0.66f).coerceIn(0f, 1f)
    } else {
        (shellProgress / 0.42f).coerceIn(0f, 1f)
    }
    val sheenWidthFraction = 0.1f + (0.34f - 0.1f) * shellProgress

    Box(
        modifier = modifier
            .requiredWidth(shellWidth)
            .requiredHeight(36.dp)
            .alpha(shellAlpha),
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxSize(),
            cornerRadius = 18.dp,
            shapeOverride = CircleShape,
            contentPadding = PaddingValues(0.dp),
            tint = islandTint,
            borderColor = islandBorder,
            glowColor = islandGlow,
            shadowColor = Color.Transparent,
            blurRadius = islandBlur,
            refractionHeight = islandBlur,
            refractionAmount = islandRefraction,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 3.dp)
                    .fillMaxWidth(sheenWidthFraction)
                    .requiredHeight(7.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(
                                    alpha = when (stylePreset) {
                                        LiquidGlassStylePreset.Harmony -> 0.12f
                                        LiquidGlassStylePreset.IOS -> 0.32f
                                        LiquidGlassStylePreset.Hyper -> 0.16f
                                    },
                                ),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .alpha(contentAlpha),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.pillOn.copy(alpha = 0.96f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(12.dp),
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.pillOn,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
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
                addAll(listOf("机器学习", "实验报告", "A栋", "考试", "高优先"))
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
    val todayLabel = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日")) }
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
            nextCourse?.let { add(HomeLineData("今日课程", it.courseName, it.classroom.ifBlank { "教室待补充" })) }
            urgentExamItem?.let { add(HomeLineData("考试周", it.title, "${it.countdownLabel} · ${it.subtitle}")) }
            urgentReviewItem?.let { add(HomeLineData("今日复习", it.noteTitle, "${it.courseName} · 建议 ${it.recommendedMinutes} 分钟")) }
            priorityTodo?.let { add(HomeLineData("待办优先", it.title, "${it.listName} · ${it.dueText}")) }
            pendingGoalTodo?.let {
                add(HomeLineData("专注目标", it.title, "还差 ${it.focusGoal - it.focusCount} 轮 · ${it.dueText}"))
            }
            if (boundTask.isNotBlank()) add(HomeLineData("专注绑定", boundTask, "番茄钟已同步当前任务")) else {
                topFocusTask?.let { add(HomeLineData("专注趋势", it.title, "累计 ${it.minutes} 分钟 · ${it.count} 轮")) }
            }
        }
    }
    val heroState = remember(urgentReviewItem, priorityTodo, nextCourse, pendingGoalTodo, focusedMinutes, pendingTodoCount) {
        when {
            urgentReviewItem != null -> HomeHeroState(
                badge = "今日复习",
                headline = "先收住这轮记忆窗口",
                detail = "${urgentReviewItem.courseName} · ${urgentReviewItem.noteTitle} · 建议 ${urgentReviewItem.recommendedMinutes} 分钟",
                accent = ForestGreen,
            )

            priorityTodo != null -> HomeHeroState(
                badge = "高优先待办",
                headline = "先推进最关键的一项",
                detail = "${priorityTodo.title} · ${priorityTodo.dueText} · ${priorityTodo.listName}",
                accent = Ginkgo,
            )

            nextCourse != null -> HomeHeroState(
                badge = "下一门课",
                headline = "课程节奏已经排好",
                detail = "${nextCourse.courseName} · ${nextCourse.classroom.ifBlank { "教室待补充" }} · ${nextCourse.teacher.ifBlank { "教师待补充" }}",
                accent = MossGreen,
            )

            pendingGoalTodo != null -> HomeHeroState(
                badge = "专注目标",
                headline = "继续完成这组专注轮次",
                detail = "${pendingGoalTodo.title} · 还差 ${(pendingGoalTodo.focusGoal - pendingGoalTodo.focusCount).coerceAtLeast(0)} 轮",
                accent = TeaGreen,
            )

            else -> HomeHeroState(
                badge = "今日总览",
                headline = "课表、待办和专注已汇成一张面板",
                detail = "当前待办 $pendingTodoCount 项，累计专注 $focusedMinutes 分钟。",
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
                                subtitle = course.teacher.ifBlank { "教师待补充" },
                                detail = course.classroom.ifBlank { "教室待补充" },
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
                            detail = note.tags.joinToString(" · ").ifBlank { "课程笔记" },
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
                                subtitle = "校园地图",
                                detail = "点击前往地图与导航",
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
            gradeSearchStatus = if (gradeSearchCards.isEmpty()) "登录教务后可搜索真实成绩。" else ""
            gradeSearchLoading = false
            return@LaunchedEffect
        }
        gradeSearchLoading = true
        gradeSearchStatus = "正在检索成绩..."
        runCatching {
            val gateway = HitaAcademicGateway(studentId, password)
            gateway.fetchTerms()
                .take(3)
                .flatMap { term -> gateway.fetchGradesForTerm(term) }
        }.onSuccess { cards ->
            if (cards.isNotEmpty()) {
                gradeSearchCards = cards
                gradeSearchStatus = ""
            } else {
                gradeSearchCards = loadHomeGradeCache(campusPrefs)
                gradeSearchStatus = if (gradeSearchCards.isEmpty()) "当前没有可搜索的成绩记录。" else "当前显示的是最近同步的成绩缓存。"
            }
        }.onFailure {
            gradeSearchCards = loadHomeGradeCache(campusPrefs)
            gradeSearchStatus = if (gradeSearchCards.isEmpty()) (it.message ?: "成绩检索失败。") else "当前显示的是最近同步的成绩缓存。"
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
                "已注入复习计划：${summary.body}"
            } else {
                "已注入上下文：${summary.source} · ${summary.title}\n${summary.body}"
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
            "请优先接管这组与考试周相关的复习冲刺项，并结合待办与番茄钟安排今天最稳的推进顺序。"
        } else if (summary.id == "review_bridge") {
            "请接管这组复习计划，并结合待办与番茄钟给我安排今天的推进顺序。"
        } else {
            "基于${summary.source}里的“${summary.title}”继续分析：${summary.body}"
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
                        Text("核心指标", style = homeSectionTitleStyle(moduleSize), color = PineInk)
                        Spacer(modifier = Modifier.height(homeSectionSpacing(moduleSize)))
                        HomeLine("课表", "今日课次 ${todayClassCount}", "待办 ${pendingTodoCount} · 专注 ${focusedMinutes} 分钟", sizePreset = HomeModuleSize.Compact)
                        Spacer(modifier = Modifier.height(homeLineGap(HomeModuleSize.Compact)))
                        HomeLine("考试周", "待处理 ${pendingExamItems.size}", "点此查看当前冲刺项", sizePreset = HomeModuleSize.Compact)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(homeMetricSpacing(moduleSize)), modifier = modifier.horizontalScroll(rememberScrollState())) {
                        MetricCard("今日课次", todayClassCount.toString(), ForestGreen, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenScheduleDay))
                        MetricCard("考试周", pendingExamItems.size.toString(), TeaGreen, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenScheduleExamWeek))
                        MetricCard("待完成", pendingTodoCount.toString(), Ginkgo, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenTodoPending))
                        MetricCard("专注时长", "${focusedMinutes} 分钟", MossGreen, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenPomodoro))
                    }
                }
            }
            HomeModule.Rhythm -> GlassCard(modifier = modifier) {
                HomeModuleHeader(
                    title = "今天的节奏",
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
                    Text("已收起今日节奏，保留摘要入口。", style = homeSectionBodyStyle(moduleSize), color = ForestDeep.copy(alpha = 0.72f))
                } else if (todayTimeline.isEmpty()) {
                    Text("课表、待办和专注记录会在这里自动汇总。", style = homeSectionBodyStyle(moduleSize), color = ForestDeep.copy(alpha = 0.72f))
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
                                    "今日课程" -> onOpenScheduleDay()
                                    "考试周" -> onOpenScheduleExamWeek()
                                    "今日复习" -> onOpenReviewPlanner()
                                    "待办优先" -> onOpenTodoPending()
                                    "专注目标" -> onOpenTodoPending()
                                    "专注绑定" -> onOpenPomodoro()
                                    "专注趋势" -> onOpenPomodoro()
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
                    title = "学习推进",
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
                        if (pendingExamItems.isNotEmpty()) append("待处理 ${pendingExamItems.size} 项")
                        urgentReviewItem?.let {
                            if (isNotBlank()) append(" · ")
                            append("复习 ${it.noteTitle}")
                        }
                        topFocusTask?.let {
                            if (isNotBlank()) append(" · ")
                            append("专注排行 ${it.title}")
                        }
                        pendingGoalTodo?.let {
                            if (isNotBlank()) append(" · ")
                            append("目标 ${it.title}")
                        }
                    }.ifBlank { "当前没有学习推进项。" }
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
                                    Text("复习计划", style = MaterialTheme.typography.titleMedium, color = PineInk)
                                    ActionPill("打开", ForestGreen, onClick = onOpenReviewPlanner)
                                }
                                Text(
                                    "今天应复习 ${pendingReviewItems.size} 项，最紧急的是 ${urgentReviewItem?.noteTitle ?: "当前知识点"}。",
                                    style = homeSectionBodyStyle(if (paired) HomeModuleSize.Compact else moduleSize),
                                    color = ForestDeep.copy(alpha = 0.74f),
                                )
                                urgentReviewItem?.let { review ->
                                    Text(
                                        "${review.courseName} · 建议 ${review.recommendedMinutes} 分钟 · ${if (review.nextReviewAt <= System.currentTimeMillis()) "已进入遗忘风险" else "按计划推进"}",
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
                                            text = "绑定专注",
                                            background = MossGreen,
                                            onClick = {
                                                SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "复习：${review.noteTitle}")
                                                SecurePrefs.putString(focusPrefs, "bound_task_list_secure", review.courseName)
                                                onOpenPomodoro()
                                            },
                                        )
                                    }
                                    ActionPill(
                                        text = "查看今日复习",
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
                            time = "考试周",
                            title = item.title,
                            body = "${item.countdownLabel} · ${item.detail}",
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
                            time = "今日复习",
                            title = it.noteTitle,
                            body = "${it.courseName} · 建议 ${it.recommendedMinutes} 分钟",
                            sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                            modifier = Modifier.clickable(onClick = onOpenReviewPlanner),
                        )
                    }
                    topFocusTask?.let {
                        if (visibleExamItems.isNotEmpty() || urgentReviewItem != null) {
                            Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                        }
                        HomeLine(
                            time = "专注排行",
                            title = it.title,
                            body = "${it.minutes} 分钟 · ${it.count} 轮",
                            sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                            modifier = Modifier.clickable(onClick = onOpenPomodoro),
                        )
                    }
                    pendingGoalTodo?.let { task ->
                        if (visibleExamItems.isNotEmpty() || topFocusTask != null) {
                            Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                        }
                        HomeLine(
                            time = "专注目标",
                            title = task.title,
                            body = "还差 ${task.focusGoal - task.focusCount} 轮 · ${task.dueText}",
                            sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                            modifier = Modifier.clickable(onClick = onOpenTodoPending),
                        )
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(if (paired) HomeModuleSize.Compact else moduleSize)))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ActionPill("查看待办", WarmMist, onClick = onOpenTodoPending)
                            ActionPill("绑定专注", ForestGreen) {
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
                Text("首页快捷点位", style = homeSectionTitleStyle(activeSize), color = PineInk)
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                Text(
                    favoritePoints.take(if (activeSize == HomeModuleSize.Hero) 5 else if (activeSize == HomeModuleSize.Standard) 4 else 3).joinToString(" · "),
                    style = homeSectionBodyStyle(activeSize),
                    color = ForestDeep.copy(alpha = 0.76f),
                )
                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                Text(
                    "已从校园地图同步点位，可继续查看导航。",
                    style = if (activeSize == HomeModuleSize.Compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = ForestDeep.copy(alpha = 0.68f),
                )
            }
            HomeModule.RecentPoints -> if (recentPoints.isNotEmpty()) GlassCard(modifier = modifier.clickable(onClick = onOpenMap)) {
                val activeSize = if (paired) HomeModuleSize.Compact else moduleSize
                Text("最近访问", style = homeSectionTitleStyle(activeSize), color = PineInk)
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                Text(
                    recentPoints.take(if (activeSize == HomeModuleSize.Hero) 4 else 3).joinToString(" · "),
                    style = homeSectionBodyStyle(activeSize),
                    color = ForestDeep.copy(alpha = 0.76f),
                )
                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                Text(
                    "点此回到校园地图继续查看。",
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
                    title = "智能体",
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
                    Text("已收起智能体面板。当前保留最近 ${visibleMessages.size} 条对话摘要入口。", style = homeSectionBodyStyle(activeSize), color = ForestDeep.copy(alpha = 0.72f))
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
                        ActionPill("新建会话", WarmMist) {
                            val newConversation = assistantStore.newConversationTemplate(conversations.size)
                            conversations.add(0, newConversation)
                            activeConversationId = newConversation.id
                            prompt = ""
                        }
                        ActionPill("权限", Ginkgo, onClick = onOpenAssistantPermissions)
                    }
                    Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                    if (assistantTools.isNotEmpty()) {
                        Text(
                            "可用工具",
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
                                    onClick = { prompt = "${tool.title}：${tool.description}" },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                    }
                    if (assistantSummaries.isNotEmpty()) {
                        Text(
                            "可用上下文",
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
                                    Text("${summary.source} · ${summary.title}", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
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
                                Text("复习计划执行结果", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                                Text(execution.summary, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                                Text(
                                    "执行于 ${formatSyncTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(execution.executedAt), java.time.ZoneId.systemDefault()))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ForestDeep.copy(alpha = 0.68f),
                                )
                                if (execution.createdTaskTitles.isNotEmpty() || execution.boundTaskTitle.isNotBlank()) {
                                    ActionPill(
                                        if (expandedReviewExecutionAt == execution.executedAt) "收起明细" else "展开明细",
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
                                    ActionPill("回到复习页", Ginkgo.copy(alpha = 0.82f)) {
                                        onOpenReviewPlannerSeeded(
                                            ReviewPlannerSeed(
                                            query = execution.createdTaskTitles.firstOrNull()?.removePrefix("复习：").orEmpty(),
                                            focusTitle = execution.boundTaskTitle.ifBlank {
                                                execution.createdTaskTitles.firstOrNull().orEmpty()
                                            },
                                        ),
                                        )
                                    }
                                    ActionPill("查看待办", Ginkgo) {
                                        onOpenTodoPending()
                                    }
                                    ActionPill("查看番茄钟", WarmMist) {
                                        onOpenPomodoro()
                                    }
                                    ActionPill("撤销执行", CloudWhite) {
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
                        Text("最近接管历史", style = MaterialTheme.typography.titleMedium, color = PineInk)
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
                                        "执行于 ${formatSyncTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(execution.executedAt), java.time.ZoneId.systemDefault()))}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ForestGreen,
                                    )
                                    Text(execution.summary, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        ActionPill(
                                            if (expandedReviewExecutionAt == execution.executedAt) "收起明细" else "展开明细",
                                            TeaGreen.copy(alpha = 0.24f),
                                        ) {
                                            expandedReviewExecutionAt =
                                                if (expandedReviewExecutionAt == execution.executedAt) null else execution.executedAt
                                        }
                                        ActionPill("回放方案", WarmMist) {
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
                                        ActionPill("回到复习页", Ginkgo.copy(alpha = 0.82f)) {
                                            onOpenReviewPlannerSeeded(
                                                ReviewPlannerSeed(
                                                query = execution.createdTaskTitles.firstOrNull()?.removePrefix("复习：").orEmpty(),
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
                        Text("最近工具调用", style = MaterialTheme.typography.titleMedium, color = PineInk)
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
                                    Text("${toolCall.title} · ${toolCall.status}", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
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
                        label = { Text("给智能体一句话") },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (assistantBusy) {
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                        Text("智能体正在整理回复...", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.68f))
                    }
                    Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize) + 2.dp))
                    Button(
                        onClick = {
                            if (prompt.isNotBlank() && !assistantBusy) {
                                val input = prompt
                                val targetConversationId = activeConversationId
                                val userMessage = ChatMessage("user-${System.currentTimeMillis()}", "user", input, System.currentTimeMillis())
                                val pendingId = "assistant-pending-${System.currentTimeMillis()}"
                                val pendingMessage = ChatMessage(pendingId, "assistant", "正在整理你的问题...", System.currentTimeMillis())
                                val currentConversation = conversations.firstOrNull { it.id == targetConversationId } ?: return@Button
                                val localExecution = if (shouldExecuteReviewBridge(input, assistantSummaries)) {
                                    val executionSummary = applyReviewBridgeExecution(context)
                                    reviewExecutionSummary = loadReviewBridgeExecutionSummary(assistantBridgePrefs)
                                    reviewExecutionHistory = loadReviewBridgeExecutionHistory(assistantBridgePrefs)
                                    AssistantMockExecution(
                                        toolCall = AssistantToolCall(
                                            id = "tool-${System.currentTimeMillis()}",
                                            title = "执行复习计划",
                                            status = "完成",
                                            summary = executionSummary,
                                            timestamp = System.currentTimeMillis(),
                                        ),
                                        reply = "我已经按待接管复习计划完成了首轮落地：$executionSummary 接下来你可以直接去待办和番茄钟开始执行。",
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
                                                content = "星火助手暂时不可用，请稍后再试。",
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
                        Text("发送")
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
                        Text("破晓", style = MaterialTheme.typography.headlineLarge, color = PineInk)
                        Text(
                            "$todayLabel · 校园学习工作台",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestDeep.copy(alpha = 0.68f),
                        )
                    }
                    ActionPill(
                        text = if (homeEditMode) "完成编辑" else "编辑工作台",
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
                        "当前处于编辑模式，可调整模块显隐、顺序和尺寸。",
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
                        title = "今日课表",
                        subtitle = nextCourse?.let {
                            "${it.courseName} · ${it.classroom.ifBlank { "教室待补充" }}"
                        } ?: "打开今日课程与考试周",
                        accent = ForestGreen,
                        icon = PrimarySection.Schedule.icon,
                        onClick = onOpenScheduleDay,
                    )
                    HomeQuickEntry(
                        title = "待办清单",
                        subtitle = priorityTodo?.title ?: "打开待办查看当前优先项",
                        accent = Ginkgo,
                        icon = PrimarySection.Todo.icon,
                        onClick = onOpenTodoPending,
                    )
                    HomeQuickEntry(
                        title = "复习计划",
                        subtitle = urgentReviewItem?.noteTitle ?: "查看今天应推进的复习项",
                        accent = TeaGreen,
                        icon = Icons.Rounded.AutoAwesome,
                        onClick = onOpenReviewPlanner,
                    )
                    HomeQuickEntry(
                        title = "番茄钟",
                        subtitle = boundTask.ifBlank {
                            topFocusTask?.let { "${it.minutes} 分钟累计" } ?: "开始一轮专注"
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
                                        "今日复习" -> onOpenReviewPlanner()
                                        "高优先待办" -> onOpenTodoPending()
                                        "下一门课" -> onOpenScheduleDay()
                                        "专注目标" -> onOpenPomodoro()
                                    }
                                },
                            )
                            Text(
                                if (homeEditMode) "首页编排中" else "今日总览",
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
                                HomeHeroStat("今日课表", "${todayClassCount} 课次", ForestGreen, modifier = Modifier.weight(1f))
                                HomeHeroStat("待办待推", "${pendingTodoCount} 项", Ginkgo, modifier = Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                HomeHeroStat("专注累计", "${focusedMinutes} 分钟", MossGreen, modifier = Modifier.weight(1f))
                                HomeHeroStat("复习待办", "${pendingReviewItems.size} 项", TeaGreen, modifier = Modifier.weight(1f))
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
                                            Text("接下来先做什么", style = MaterialTheme.typography.titleMedium, color = PineInk)
                                            Text("把今天最值得先处理的事项压到前三条。", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.64f))
                                        }
                                        Text(
                                            "${todayTimeline.size} 条",
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
                                                    "今日课程" -> onOpenScheduleDay()
                                                    "考试周" -> onOpenScheduleExamWeek()
                                                    "今日复习" -> onOpenReviewPlanner()
                                                    "待办优先" -> onOpenTodoPending()
                                                    "专注目标" -> onOpenTodoPending()
                                                    "专注绑定" -> onOpenPomodoro()
                                                    "专注趋势" -> onOpenPomodoro()
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
                        Text("快速检索", style = MaterialTheme.typography.titleLarge, color = PineInk)
                        Text(
                            "课程、待办、成绩和教学楼统一搜索，不用再来回翻页面。",
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
                            text = if (searchQuery.isBlank()) "就绪" else "${(localSearchResults.size + gradeSearchResults.size).coerceAtMost(8)} 条",
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
                    label = { Text("搜索课程、待办、成绩、教学楼") },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (searchQuery.isBlank()) {
                    if (searchHistory.isNotEmpty()) {
                        Text("最近搜索", style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            searchHistory.take(8).forEach { history ->
                                ActionPill(history, WarmMist) { searchQuery = history }
                            }
                            ActionPill("清空", CloudWhite) {
                                searchHistory.clear()
                                saveStringList(homePrefs, "search_history", searchHistory)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Text("快捷词", style = MaterialTheme.typography.titleMedium, color = PineInk)
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
                            text = if (gradeSearchLoading) "正在整理搜索结果..." else gradeSearchStatus.ifBlank { "当前没有匹配结果。" },
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
                    Text("工作台模块", style = MaterialTheme.typography.titleLarge, color = PineInk)
                    Text(
                        if (homeEditMode) {
                            "当前可直接调整模块显隐、顺序和尺寸。"
                        } else {
                            "下面开始是首页的可编排模块，按你的学习节奏自由组合。"
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
                        text = "${visibleModules.size} 个模块",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = PineInk,
                    )
                }
            }
        }
        if (homeEditMode) item {
            GlassCard {
                Text("工作台编排", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                Text("选择首页要保留哪些模块，按自己的使用节奏裁剪工作台。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
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
                Text("长按下面的模块卡可直接拖动排序。", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.62f))
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
                                    Text(if (isDragging) "拖动中" else "长按后上下拖动", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.62f))
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
                                Text("≡", style = MaterialTheme.typography.headlineSmall, color = ForestDeep.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp, start = 12.dp))
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

@Composable
private fun HomeLine(
    time: String,
    title: String,
    body: String,
    sizePreset: HomeModuleSize = HomeModuleSize.Standard,
    modifier: Modifier = Modifier,
) {
    val titleStyle = when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.bodyLarge
        HomeModuleSize.Standard -> MaterialTheme.typography.titleMedium
        HomeModuleSize.Hero -> MaterialTheme.typography.titleLarge
    }
    val bodyStyle = when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.bodySmall
        HomeModuleSize.Standard -> MaterialTheme.typography.bodyMedium
        HomeModuleSize.Hero -> MaterialTheme.typography.bodyLarge
    }
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(if (sizePreset == HomeModuleSize.Hero) 14.dp else 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(if (sizePreset == HomeModuleSize.Hero) 12.dp else if (sizePreset == HomeModuleSize.Compact) 8.dp else 10.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(TeaGreen, ForestGreen))),
        )
        Column {
            Text("$time  $title", style = titleStyle, color = PineInk)
            Text(body, style = bodyStyle, color = ForestDeep.copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun HomeHeroStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = accent)
            Text(value, style = MaterialTheme.typography.titleMedium, color = PineInk)
        }
    }
}

@Composable
private fun HomeQuickEntry(
    title: String,
    subtitle: String,
    accent: Color,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(156.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = PineInk,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.68f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeModuleHeader(
    title: String,
    collapsed: Boolean,
    collapsible: Boolean,
    sizePreset: HomeModuleSize,
    onToggleCollapsed: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = homeSectionTitleStyle(sizePreset), color = PineInk)
        if (collapsible) {
            ActionPill(
                text = if (collapsed) "展开" else "收起",
                background = WarmMist,
                onClick = onToggleCollapsed,
            )
        }
    }
}
@Composable
private fun ScheduleScreen(
    repository: HitaScheduleRepository,
    initialMode: ScheduleMode = ScheduleMode.Week,
    initialWorkbench: ScheduleWorkbench = ScheduleWorkbench.Timetable,
    onOpenAcademicAccount: () -> Unit = {},
    onOpenCourseNotes: (CourseNoteSeed) -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("schedule_auth", 0) }
    val todoPrefs = remember { context.getSharedPreferences("todo_board", Context.MODE_PRIVATE) }
    val focusBridgePrefs = remember { context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE) }
    val examWeekPrefs = remember { context.getSharedPreferences("schedule_exam_week", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val uiState by repository.observeUiState().collectAsState()
    val savedStudentId = remember { SecurePrefs.getString(prefs, "student_id_secure", "student_id") }
    val savedPassword = remember { SecurePrefs.getString(prefs, "password_secure", "password") }
    var restored by remember { mutableStateOf(false) }
    var mode by remember(initialMode) { mutableStateOf(initialMode) }
    var workbench by remember(initialWorkbench) { mutableStateOf(initialWorkbench) }
    var selectedCourse by remember { mutableStateOf<HitaCourseBlock?>(null) }
    var lastSyncTime by remember { mutableStateOf(prefs.getString("schedule_last_sync", "").orEmpty()) }
    var exportHint by remember { mutableStateOf("") }
    val extraEvents = remember { mutableStateListOf<ScheduleExtraEvent>().apply { addAll(loadScheduleExtraEvents(prefs)) } }
    var gradeTrend by remember { mutableStateOf<List<GradeTrendPoint>>(emptyList()) }
    var gradeTrendLoading by remember { mutableStateOf(false) }
    var gradeTrendStatus by remember { mutableStateOf("登录后可查看成绩趋势。") }
    var selectedTrendTerm by remember { mutableStateOf<String?>(null) }
    val completedExamWeekIds = remember { mutableStateListOf<String>().apply { addAll(loadStringList(examWeekPrefs, "completed_ids")) } }
    var selectedExtraEventId by remember(uiState.selectedDate) { mutableStateOf<String?>(null) }
    val selectedDateEvents = extraEvents
        .filter { it.date == uiState.selectedDate }
        .sortedBy { eventSortKey(it.time) }
    val examWeekItems = remember(uiState.weekSchedule, extraEvents) {
        buildExamWeekItems(uiState.weekSchedule, extraEvents, completedExamWeekIds)
    }
    val month = remember(uiState.selectedDate) {
        runCatching { YearMonth.from(LocalDate.parse(uiState.selectedDate)) }.getOrElse { YearMonth.now() }
    }

    LaunchedEffect(Unit) {
        if (!restored) {
            restored = true
            loadCachedScheduleUiState(prefs)?.let { repository.restoreCachedState(it) }
            if (savedStudentId.isNotBlank() && savedPassword.isNotBlank()) {
                repository.connectAndLoad(savedStudentId, savedPassword)
            }
        }
    }

    LaunchedEffect(uiState.loggedIn, uiState.terms, uiState.studentId) {
        val currentStudentId = SecurePrefs.getString(prefs, "student_id_secure", "student_id")
        val currentPassword = SecurePrefs.getString(prefs, "password_secure", "password")
        val academicGateway = if (currentStudentId.isNotBlank() && currentPassword.isNotBlank()) {
            HitaAcademicGateway(currentStudentId, currentPassword)
        } else {
            null
        }
        if (!uiState.loggedIn || academicGateway == null) {
            gradeTrend = emptyList()
            gradeTrendStatus = "登录后可查看成绩趋势。"
            return@LaunchedEffect
        }
        gradeTrendLoading = true
        gradeTrendStatus = "正在整理各学期成绩趋势..."
        runCatching {
            val cardsByTerm = uiState.terms.mapNotNull { term ->
                val cards = academicGateway.fetchGradesForTerm(term)
                if (cards.isEmpty()) null else term.name to cards
            }
            buildGradeTrendPoints(cardsByTerm)
        }.onSuccess { points ->
            gradeTrend = points
            selectedTrendTerm = selectedTrendTerm?.takeIf { target -> points.any { it.termName == target } }
                ?: points.firstOrNull()?.termName
            gradeTrendStatus = if (points.isEmpty()) "当前还没有可用于分析的成绩记录。" else "已生成 ${points.size} 个学期的成绩趋势。"
        }.onFailure {
            gradeTrend = emptyList()
            gradeTrendStatus = it.message ?: "成绩趋势加载失败。"
        }
        gradeTrendLoading = false
    }

    LaunchedEffect(
        uiState.loggedIn,
        uiState.loading,
        uiState.currentTerm,
        uiState.currentWeek,
        uiState.selectedDate,
        uiState.weekSchedule,
        uiState.selectedDateCourses,
    ) {
        if (uiState.loggedIn && !uiState.loading) {
            saveCachedScheduleUiState(prefs, uiState)
            val syncText = formatSyncTime(LocalDateTime.now())
            lastSyncTime = syncText
            prefs.edit().putString("schedule_last_sync", syncText).apply()
        }
    }

    LaunchedEffect(
        uiState.weekSchedule.week.title,
        uiState.selectedDate,
        extraEvents.joinToString("|") { "${it.id}:${it.date}:${it.time}:${it.title}:${it.type}" },
        completedExamWeekIds.joinToString("|"),
    ) {
        refreshLocalReminderSchedule(context)
    }

    ScreenColumn {
        item {
            GlassCard {
                Text("课程中心", style = MaterialTheme.typography.headlineMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.status, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                if (lastSyncTime.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("上次同步：$lastSyncTime", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.64f))
                }
                if (uiState.authExpired) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "当前登录会话可能已失效，请重新连接教务系统后再刷新。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A5B34),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { scope.launch { repository.refreshCurrent() } },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MossGreen),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.loggedIn && !uiState.loading,
                ) {
                    Text(if (uiState.loading) "正在刷新..." else "立即刷新课表")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ActionPill("账号", WarmMist, onClick = onOpenAcademicAccount)
                    ActionPill("复制摘要", MossGreen) {
                        val text = buildScheduleShareText(uiState)
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(ClipData.newPlainText("课表摘要", text))
                        exportHint = "已复制当前周课表摘要。"
                    }
                    ActionPill("系统分享", Ginkgo) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, buildScheduleShareText(uiState))
                        }
                        context.startActivity(Intent.createChooser(intent, "分享课表摘要"))
                        exportHint = "已打开系统分享面板。"
                    }
                }
                if (exportHint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(exportHint, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.68f))
                }
            }
        }
        item {
            GlassCard {
                Text(uiState.currentTerm.name, style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(
                    options = ScheduleWorkbench.entries.toList(),
                    selected = workbench,
                    label = { it.title },
                    onSelect = { workbench = it },
                )
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(
                    options = uiState.terms,
                    selected = uiState.currentTerm,
                    label = { it.name },
                    onSelect = { scope.launch { repository.selectTerm(it) } },
                )
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(
                    options = uiState.weeks,
                    selected = uiState.currentWeek,
                    label = { it.title },
                    onSelect = { scope.launch { repository.selectWeek(it) } },
                )
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(
                    options = ScheduleMode.entries.toList(),
                    selected = mode,
                    label = { it.label },
                    onSelect = { mode = it },
                )
            }
        }
        item {
            when (workbench) {
                ScheduleWorkbench.Timetable -> when (mode) {
                    ScheduleMode.Week -> WeekScheduleCard(
                        title = uiState.currentWeek.title,
                        slots = uiState.weekSchedule.timeSlots,
                        days = uiState.weekSchedule.days,
                        courses = uiState.weekSchedule.courses,
                        selectedCourse = selectedCourse,
                        onSelectCourse = { selectedCourse = it },
                    )
                    ScheduleMode.Day -> DayScheduleCard(
                        dates = uiState.weekSchedule.days.map { it.fullDate },
                        selectedDate = uiState.selectedDate,
                        courses = uiState.selectedDateCourses,
                        events = selectedDateEvents,
                        selectedCourse = selectedCourse,
                        selectedEventId = selectedExtraEventId,
                        onSelectDate = {
                            selectedCourse = null
                            selectedExtraEventId = null
                            scope.launch { repository.selectDate(it) }
                        },
                        onSelectCourse = {
                            selectedExtraEventId = null
                            selectedCourse = it
                        },
                        onSelectEvent = { event ->
                            selectedCourse = null
                            selectedExtraEventId = event.id
                        },
                        onAddEvent = { event ->
                            extraEvents.add(0, event)
                            selectedExtraEventId = event.id
                            selectedCourse = null
                            saveScheduleExtraEvents(prefs, extraEvents)
                        },
                        onUpdateEvent = { event ->
                            val index = extraEvents.indexOfFirst { it.id == event.id }
                            if (index >= 0) {
                                extraEvents[index] = event
                                selectedExtraEventId = event.id
                            }
                            saveScheduleExtraEvents(prefs, extraEvents)
                        },
                        onDeleteEvent = { eventId ->
                            extraEvents.removeAll { it.id == eventId }
                            if (selectedExtraEventId == eventId) selectedExtraEventId = null
                            saveScheduleExtraEvents(prefs, extraEvents)
                        },
                    )
                    ScheduleMode.Month -> MonthScheduleCard(
                        month = month,
                        selectedDate = uiState.selectedDate,
                        activeDates = (uiState.weekSchedule.days.map { it.fullDate } + extraEvents.map { it.date }).distinct(),
                        selectedCourses = uiState.selectedDateCourses,
                        selectedEvents = selectedDateEvents,
                        selectedCourse = selectedCourse,
                        onSelectDate = {
                            selectedCourse = null
                            scope.launch { repository.selectDate(it) }
                        },
                        onSelectCourse = { selectedCourse = it },
                    )
                }
                ScheduleWorkbench.Grades -> GradeTrendCard(
                    loading = gradeTrendLoading,
                    status = gradeTrendStatus,
                    points = gradeTrend,
                    selectedTerm = selectedTrendTerm,
                    onSelectTerm = { selectedTrendTerm = it },
                )
                ScheduleWorkbench.ExamWeek -> ExamWeekModeCard(
                    weekTitle = uiState.currentWeek.title,
                    selectedDate = uiState.selectedDate,
                    items = examWeekItems,
                    onCreateTodo = { item ->
                        val tasks = loadTodoTasks(todoPrefs).toMutableList()
                        tasks.add(
                            0,
                            TodoTask(
                                id = "exam-${System.currentTimeMillis()}",
                                title = item.title,
                                note = item.detail,
                                quadrant = if (item.priority == 0) TodoQuadrant.ImportantUrgent else TodoQuadrant.ImportantNotUrgent,
                                priority = when (item.priority) {
                                    0 -> TodoPriority.High
                                    1 -> TodoPriority.Medium
                                    else -> TodoPriority.Low
                                },
                                dueText = "${item.date} ${item.countdownLabel}",
                                tags = listOf("考试周"),
                                listName = "学习",
                                reminderText = if (item.priority <= 1) "今天 20:00" else "",
                                repeatText = "不重复",
                            ),
                        )
                        saveTodoTasks(todoPrefs, tasks)
                    },
                    onBindFocus = { item ->
                        focusBridgePrefs.edit()
                            .remove("bound_task_title")
                            .remove("bound_task_list")
                            .apply()
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_title_secure", item.title)
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_list_secure", "考试周")
                    },
                    onBindFocusGroup = { date, groupedItems ->
                        val headline = groupedItems.firstOrNull()?.title ?: "考试周冲刺"
                        focusBridgePrefs.edit()
                            .remove("bound_task_title")
                            .remove("bound_task_list")
                            .apply()
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_title_secure", "$headline 等 ${groupedItems.size} 项")
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_list_secure", "考试周 ${date.substringAfterLast("-")}")
                    },
                    onCreateTodoGroup = { date, groupedItems ->
                        val tasks = loadTodoTasks(todoPrefs).toMutableList()
                        val newTasks = groupedItems.mapIndexed { index, item ->
                            TodoTask(
                                id = "exam-group-${date}-${index}-${System.currentTimeMillis()}",
                                title = item.title,
                                note = item.detail,
                                quadrant = if (item.priority == 0) TodoQuadrant.ImportantUrgent else TodoQuadrant.ImportantNotUrgent,
                                priority = when (item.priority) {
                                    0 -> TodoPriority.High
                                    1 -> TodoPriority.Medium
                                    else -> TodoPriority.Low
                                },
                                dueText = "${item.date} ${item.countdownLabel}",
                                tags = listOf("考试周", date),
                                listName = "考试周 ${date.substringAfterLast("-")}",
                                reminderText = if (item.priority <= 1) "今天 20:00" else "",
                                repeatText = "不重复",
                            )
                        }
                        tasks.addAll(0, newTasks)
                        saveTodoTasks(todoPrefs, tasks)
                    },
                    onToggleFinishedGroup = { groupedItems, finished ->
                        groupedItems.forEach { item ->
                            if (finished) {
                                if (item.id !in completedExamWeekIds) completedExamWeekIds.add(item.id)
                            } else {
                                completedExamWeekIds.remove(item.id)
                            }
                        }
                        saveStringList(examWeekPrefs, "completed_ids", completedExamWeekIds)
                    },
                    onToggleFinished = { item ->
                        if (item.finished) {
                            completedExamWeekIds.remove(item.id)
                        } else {
                            if (item.id !in completedExamWeekIds) completedExamWeekIds.add(item.id)
                        }
                        saveStringList(examWeekPrefs, "completed_ids", completedExamWeekIds)
                    },
                    onClearFinished = {
                        completedExamWeekIds.removeAll { id ->
                            examWeekItems.any { it.id == id && it.finished && (it.countdownLabel == "已结束" || it.finished) }
                        }
                        saveStringList(examWeekPrefs, "completed_ids", completedExamWeekIds)
                    },
                )
            }
        }
        if (workbench == ScheduleWorkbench.Timetable && selectedCourse != null) {
            item {
                AnimatedVisibility(
                    visible = selectedCourse != null,
                    enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.96f),
                    exit = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.98f),
                ) {
                    selectedCourse?.let { course ->
                        CourseDetailCard(
                            course = course,
                            weekTitle = uiState.currentWeek.title,
                            dayLabel = uiState.weekSchedule.days.firstOrNull { it.weekDay == course.dayOfWeek }?.label.orEmpty(),
                            onOpenNotes = {
                                onOpenCourseNotes(
                                    CourseNoteSeed(
                                        courseName = course.courseName,
                                        teacher = course.teacher,
                                        classroom = course.classroom,
                                        courseLabel = "第 ${course.majorIndex} 大节",
                                    ),
                                )
                            },
                            onClose = { selectedCourse = null },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekScheduleCard(
    title: String,
    slots: List<HitaTimeSlot>,
    days: List<HitaWeekDay>,
    courses: List<HitaCourseBlock>,
    selectedCourse: HitaCourseBlock?,
    onSelectCourse: (HitaCourseBlock) -> Unit,
) {
    val headerHeight = 60.dp
    val rowHeight = 108.dp
    val freeTimeSummary = remember(slots, days, courses) { weeklyFreeTimeSummary(slots, days, courses) }
    val weeklyAnalysis = remember(days, courses) { weeklyCourseAnalysis(days, courses) }
    GlassCard {
        Text(title, style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Spacer(modifier = Modifier.height(headerHeight))
                slots.forEach { slot ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.24f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.16f)),
                        modifier = Modifier
                            .width(94.dp)
                            .requiredHeight(rowHeight),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
                            Text(slot.label, style = MaterialTheme.typography.labelLarge, color = PineInk)
                            Text(slot.timeRange, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                        }
                    }
                }
            }
            days.forEach { day ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.width(132.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selectedCourse?.dayOfWeek == day.weekDay) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.26f),
                        border = BorderStroke(
                            1.dp,
                            if (selectedCourse?.dayOfWeek == day.weekDay) Color.White.copy(alpha = 0.24f) else BambooStroke.copy(alpha = 0.16f),
                        ),
                        modifier = Modifier.requiredHeight(headerHeight),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(day.label, style = MaterialTheme.typography.titleMedium, color = PineInk)
                            Text(day.date, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                        }
                    }
                    slots.forEach { slot ->
                        val matches = courses.filter { it.dayOfWeek == day.weekDay && it.majorIndex == slot.majorIndex }
                        CourseCell(
                            courses = matches,
                            selectedCourse = selectedCourse,
                            onClick = { course -> onSelectCourse(course) },
                        )
                    }
                }
            }
        }
        if (weeklyAnalysis.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text("本周课表分析", style = MaterialTheme.typography.titleMedium, color = PineInk)
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                weeklyAnalysis.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.14f)),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(item.dayLabel, style = MaterialTheme.typography.titleSmall, color = PineInk)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item.highlights.forEach { label ->
                                    SelectionChip(text = label, chosen = false, onClick = {})
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("本周空闲时段", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            freeTimeSummary.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.14f)),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(item.dayLabel, style = MaterialTheme.typography.titleSmall, color = PineInk)
                            Text("${item.freeCount} 个空闲大节", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.7f))
                        }
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item.labels.forEach { label ->
                                SelectionChip(text = label, chosen = false, onClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCell(
    courses: List<HitaCourseBlock>,
    selectedCourse: HitaCourseBlock?,
    onClick: (HitaCourseBlock) -> Unit,
) {
    val primaryCourse = courses.firstOrNull()
    val hasConflict = courses.size > 1
    val isSelected = primaryCourse != null && selectedCourse?.let {
        courses.any { course ->
            it.courseName == course.courseName && it.dayOfWeek == course.dayOfWeek && it.majorIndex == course.majorIndex
        }
    } == true
    val isRelated = primaryCourse != null && selectedCourse != null && courses.any { selectedCourse.courseName == it.courseName }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = when {
            hasConflict -> Color(0xFFF6E0D2)
            isSelected -> Color.White.copy(alpha = 0.34f)
            isRelated -> Color.White.copy(alpha = 0.28f)
            else -> Color.White.copy(alpha = 0.22f)
        },
        border = BorderStroke(
            1.dp,
            when {
                hasConflict -> Color(0xFFD39B74)
                isSelected -> Color.White.copy(alpha = 0.28f)
                isRelated -> BambooStroke.copy(alpha = 0.2f)
                else -> BambooStroke.copy(alpha = 0.14f)
            },
        ),
        modifier = Modifier
            .requiredHeight(108.dp)
            .clickable(enabled = primaryCourse != null) { primaryCourse?.let(onClick) },
    ) {
        if (primaryCourse == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("留白", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.35f))
            }
        } else {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(primaryCourse.accent)),
                )
                Text(
                    if (hasConflict) "发生冲突" else primaryCourse.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = PineInk,
                )
                if (hasConflict) {
                    Text("${courses.size} 门课程落在同一大节", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9A5B34))
                    Text(courses.joinToString(" / ") { it.courseName }, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.74f), maxLines = 2)
                } else {
                    Text(primaryCourse.classroom, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                    Text(primaryCourse.teacher.ifBlank { "教师待补充" }, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                }
            }
        }
    }
}

@Composable
private fun DayScheduleCard(
    dates: List<String>,
    selectedDate: String,
    courses: List<HitaCourseBlock>,
    events: List<ScheduleExtraEvent>,
    selectedCourse: HitaCourseBlock?,
    selectedEventId: String?,
    onSelectDate: (String) -> Unit,
    onSelectCourse: (HitaCourseBlock) -> Unit,
    onSelectEvent: (ScheduleExtraEvent) -> Unit,
    onAddEvent: (ScheduleExtraEvent) -> Unit,
    onUpdateEvent: (ScheduleExtraEvent) -> Unit,
    onDeleteEvent: (String) -> Unit,
) {
    val context = LocalContext.current
    val draftPrefs = remember { context.getSharedPreferences("schedule_event_draft", Context.MODE_PRIVATE) }
    var draftTitle by remember(selectedDate) { mutableStateOf("") }
    var draftTime by remember(selectedDate) { mutableStateOf("19:30") }
    var draftType by remember(selectedDate) { mutableStateOf("作业") }
    var draftNote by remember(selectedDate) { mutableStateOf("") }
    var eventHint by remember(selectedDate) { mutableStateOf("") }
    var draftReady by remember(selectedDate) { mutableStateOf(false) }
    val timelineEntries = buildDayTimelineEntries(courses, events)
    val editingEvent = events.firstOrNull { it.id == selectedEventId }

    LaunchedEffect(selectedDate, editingEvent?.id) {
        val restoredDraft = loadScheduleEventDraft(draftPrefs)
        if (editingEvent != null) {
            if (restoredDraft != null && restoredDraft.eventId == editingEvent.id) {
                draftTitle = restoredDraft.title
                draftTime = restoredDraft.time.ifBlank { editingEvent.time }
                draftType = restoredDraft.type.ifBlank { editingEvent.type }
                draftNote = restoredDraft.note
                eventHint = "已恢复 ${editingEvent.title} 的未完成编辑。"
            } else {
                draftTitle = editingEvent.title
                draftTime = editingEvent.time
                draftType = editingEvent.type
                draftNote = editingEvent.note
                eventHint = "正在编辑 ${editingEvent.title}"
            }
        } else {
            if (restoredDraft != null && restoredDraft.date == selectedDate && restoredDraft.eventId == null) {
                draftTitle = restoredDraft.title
                draftTime = restoredDraft.time.ifBlank { "19:30" }
                draftType = restoredDraft.type.ifBlank { "作业" }
                draftNote = restoredDraft.note
                eventHint = if (restoredDraft.title.isNotBlank() || restoredDraft.note.isNotBlank()) {
                    "已恢复 $selectedDate 的未完成事件草稿。"
                } else {
                    ""
                }
            } else {
                draftTitle = ""
                draftTime = "19:30"
                draftType = "作业"
                draftNote = ""
            }
        }
        draftReady = true
    }

    LaunchedEffect(selectedDate, draftTitle, draftTime, draftType, draftNote, editingEvent?.id, draftReady) {
        if (!draftReady) return@LaunchedEffect
        if (draftTitle.isBlank() && draftNote.isBlank() && draftTime == "19:30" && draftType == "作业") {
            clearScheduleEventDraft(draftPrefs)
        } else {
            saveScheduleEventDraft(
                draftPrefs,
                ScheduleEventDraft(
                    eventId = editingEvent?.id,
                    date = selectedDate,
                    title = draftTitle,
                    time = draftTime,
                    type = draftType,
                    note = draftNote,
                ),
            )
        }
    }
    GlassCard {
        Text("当天安排", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        SelectionRow(options = dates, selected = selectedDate, label = { it.substringAfterLast("-") }, onSelect = onSelectDate)
        Spacer(modifier = Modifier.height(12.dp))
        if (timelineEntries.isEmpty()) {
            Text("当天暂时没有课程。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.7f))
        } else {
            timelineEntries.forEachIndexed { index, entry ->
                val course = entry.selectableCourse
                val extraEvent = entry.extraEvent
                val isSelected = course != null && selectedCourse?.let {
                    it.courseName == course.courseName && it.dayOfWeek == course.dayOfWeek && it.majorIndex == course.majorIndex
                } == true
                val isEventSelected = extraEvent?.id == selectedEventId
                val relationTags = if (course != null) remember(courses, course) { dayCourseTags(courses, course) } else entry.tags
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (isSelected || isEventSelected) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.24f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected || isEventSelected) Color.White.copy(alpha = 0.26f) else BambooStroke.copy(alpha = 0.16f),
                    ),
                    modifier = Modifier.clickable(enabled = course != null || extraEvent != null) {
                        when {
                            course != null -> onSelectCourse(course)
                            extraEvent != null -> onSelectEvent(extraEvent)
                        }
                    },
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(64.dp)
                                .clip(CircleShape)
                                .background(entry.accent),
                        )
                        Column {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                            Text(entry.subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                            Text(entry.detail, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                            if (relationTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    relationTags.forEach { tag ->
                                        SelectionChip(text = tag, chosen = false, onClick = {})
                                    }
                                }
                            }
                        }
                    }
                }
                if (index != timelineEntries.lastIndex) Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(if (editingEvent == null) "添加学习事件" else "编辑学习事件", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = draftTitle, onValueChange = { draftTitle = it }, label = { Text("事件标题") }, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = draftTime, onValueChange = { draftTime = it }, label = { Text("时间") }, shape = RoundedCornerShape(20.dp), modifier = Modifier.weight(1f))
            OutlinedTextField(value = draftType, onValueChange = { draftType = it }, label = { Text("类型") }, shape = RoundedCornerShape(20.dp), modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = draftNote, onValueChange = { draftNote = it }, label = { Text("说明") }, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (draftTitle.isNotBlank()) {
                        val updatedEvent = ScheduleExtraEvent(
                            id = editingEvent?.id ?: "event-${System.currentTimeMillis()}",
                            date = selectedDate,
                            title = draftTitle,
                            time = draftTime,
                            type = draftType.ifBlank { "事件" },
                            note = draftNote,
                        )
                        if (editingEvent == null) {
                            onAddEvent(updatedEvent)
                            eventHint = "已加入 $selectedDate $draftTime 的${draftType.ifBlank { "学习" }}事件"
                            clearScheduleEventDraft(draftPrefs)
                        } else {
                            onUpdateEvent(updatedEvent)
                            eventHint = "已更新 ${updatedEvent.title}"
                            clearScheduleEventDraft(draftPrefs)
                        }
                        draftTitle = ""
                        draftTime = "19:30"
                        draftType = "作业"
                        draftNote = ""
                    } else {
                        eventHint = "请先填写事件标题"
                    }
                },
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.weight(1f),
            ) {
                Text(if (editingEvent == null) "加入当天时间线" else "保存修改")
            }
            if (editingEvent != null) {
                OutlinedButton(
                    onClick = {
                        onDeleteEvent(editingEvent.id)
                        clearScheduleEventDraft(draftPrefs)
                        draftTitle = ""
                        draftTime = "19:30"
                        draftType = "作业"
                        draftNote = ""
                        eventHint = "已删除 ${editingEvent.title}"
                    },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("删除事件")
                }
            }
        }
        if (eventHint.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = eventHint,
                style = MaterialTheme.typography.bodyMedium,
                color = ForestDeep.copy(alpha = 0.76f),
            )
        }
    }
}

@Composable
private fun GradeTrendCard(
    loading: Boolean,
    status: String,
    points: List<GradeTrendPoint>,
    selectedTerm: String?,
    onSelectTerm: (String) -> Unit,
) {
    val selectedPoint = points.firstOrNull { it.termName == selectedTerm } ?: points.firstOrNull()
    var courseKeyword by remember(selectedPoint?.termName) { mutableStateOf("") }
    val bestPoint = points.maxByOrNull { it.averageScore }
    val riskyPoint = points.maxByOrNull {
        val total = it.courseCount.takeIf { count -> count > 0 } ?: 1
        (it.warningCount.toDouble() / total) + if (it.warningCount > 0) 1.0 else 0.0
    }
    var detailExpanded by remember(selectedPoint?.termName) { mutableStateOf(false) }
    val visibleCards = remember(selectedPoint?.termName, selectedPoint?.rawCards, courseKeyword) {
        val cards = selectedPoint?.rawCards.orEmpty()
        if (courseKeyword.isBlank()) cards else cards.filter {
            listOf(it.title, it.source, it.description).any { text -> text.contains(courseKeyword, ignoreCase = true) }
        }
    }
    GlassCard {
        Text("成绩趋势", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Text(status, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
        if (loading) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("正在整理平均分、绩点和预警课程。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
            return@GlassCard
        }
        if (points.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("当前还没有可用于分析的成绩记录。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.7f))
            return@GlassCard
        }
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(options = points, selected = selectedPoint ?: points.first(), label = { it.termName }, onSelect = { onSelectTerm(it.termName) })
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            MetricCard("平均分", formatTrendValue(selectedPoint?.averageScore), ForestGreen)
            MetricCard("平均绩点", formatTrendValue(selectedPoint?.averageGradePoint), Ginkgo)
            MetricCard("学分", formatTrendValue(selectedPoint?.credits), MossGreen)
            MetricCard("优秀率", formatPercent(selectedPoint?.excellentCount, selectedPoint?.courseCount), TeaGreen)
            MetricCard("预警率", formatPercent(selectedPoint?.warningCount, selectedPoint?.courseCount), WarmMist)
        }
        if (bestPoint != null || riskyPoint != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                bestPoint?.let { point ->
                    TrendInsightCard(
                        title = "最佳学期",
                        headline = point.termName,
                        body = "平均分 ${formatTrendValue(point.averageScore)} · 绩点 ${formatTrendValue(point.averageGradePoint)}",
                        accent = ForestGreen,
                    )
                }
                riskyPoint?.let { point ->
                    TrendInsightCard(
                        title = "风险提醒",
                        headline = point.termName,
                        body = if (point.warningCount > 0) "共有 ${point.warningCount} 门预警课程，建议优先复盘。" else "当前没有明显预警课程，可继续保持。",
                        accent = WarmMist,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("学期对比", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        val maxScore = points.maxOfOrNull { it.averageScore }?.takeIf { it > 0.0 } ?: 100.0
        points.forEachIndexed { index, point ->
            val scoreRatio = (point.averageScore / maxScore).coerceIn(0.08, 1.0).toFloat()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(point.termName, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                    Text(formatTrendValue(point.averageScore), style = MaterialTheme.typography.labelLarge, color = PineInk)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(scoreRatio)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(if (point.termName == selectedPoint?.termName) ForestGreen else MossGreen),
                    )
                }
            }
            if (index != points.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "课程 ${selectedPoint?.courseCount ?: 0} 门 · 优秀 ${selectedPoint?.excellentCount ?: 0} 门 · 预警 ${selectedPoint?.warningCount ?: 0} 门",
            style = MaterialTheme.typography.bodyLarge,
            color = ForestDeep.copy(alpha = 0.76f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ActionPill(if (detailExpanded) "收起详情" else "展开单科", if (detailExpanded) MossGreen else ForestGreen) {
                detailExpanded = !detailExpanded
            }
        }
        if (detailExpanded && selectedPoint != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = courseKeyword,
                onValueChange = { courseKeyword = it },
                label = { Text("搜索当前学期课程") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (visibleCards.isEmpty()) {
                Text("当前搜索条件下没有匹配课程。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.7f))
            }
            visibleCards.forEachIndexed { index, card ->
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.26f)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(card.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Text(card.source, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                        Text(card.description, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
                    }
                }
                if (index != visibleCards.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        points.forEachIndexed { index, point ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (point.termName == selectedPoint?.termName) Color.White.copy(alpha = 0.38f) else Color.White.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, if (point.termName == selectedPoint?.termName) ForestGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(point.termName, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Text("平均分 ${formatTrendValue(point.averageScore)} · 绩点 ${formatTrendValue(point.averageGradePoint)}", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                    }
                    Text("${point.warningCount} 项预警", style = MaterialTheme.typography.labelLarge, color = if (point.warningCount > 0) Color(0xFF9A5B34) else ForestDeep.copy(alpha = 0.68f))
                }
            }
            if (index != points.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExamWeekModeCard(
    weekTitle: String,
    selectedDate: String,
    items: List<ExamWeekItem>,
    onCreateTodo: (ExamWeekItem) -> Unit,
    onBindFocus: (ExamWeekItem) -> Unit,
    onBindFocusGroup: (String, List<ExamWeekItem>) -> Unit,
    onCreateTodoGroup: (String, List<ExamWeekItem>) -> Unit,
    onToggleFinishedGroup: (List<ExamWeekItem>, Boolean) -> Unit,
    onToggleFinished: (ExamWeekItem) -> Unit,
    onClearFinished: () -> Unit,
) {
    var filter by remember { mutableStateOf(ExamWeekFilter.All) }
    var typeFilter by remember { mutableStateOf(ExamWeekTypeFilter.All) }
    val collapsedDates = remember { mutableStateListOf<String>() }
    val filteredItems = remember(items, filter, typeFilter) {
        val base = when (filter) {
            ExamWeekFilter.All -> items
            ExamWeekFilter.Pending -> items.filterNot { it.finished }
            ExamWeekFilter.Urgent -> items.filter { !it.finished && (it.priority <= 1 || it.countdownLabel == "今天" || it.countdownLabel == "明天") }
            ExamWeekFilter.Finished -> items.filter { it.finished }
        }
        base.filter { item ->
            when (typeFilter) {
                ExamWeekTypeFilter.All -> true
                ExamWeekTypeFilter.Exam -> item.subtitle.contains("考试") || item.title.contains("考试")
                ExamWeekTypeFilter.Assignment -> item.subtitle.contains("作业") || item.title.contains("作业")
                ExamWeekTypeFilter.Review -> item.subtitle.contains("复习") || item.title.contains("复习")
            }
        }
    }
    val grouped = filteredItems
        .sortedWith(compareBy<ExamWeekItem> { it.priority }.thenBy { it.date }.thenBy { it.subtitle })
        .groupBy { it.date }
    val urgentPending = items.firstOrNull { !it.finished && (it.priority == 0 || it.countdownLabel == "今天" || it.countdownLabel == "明天") }
    GlassCard {
        Text("考试周模式", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Text("把考试、作业和本周课程压成一条冲刺视图，便于临近考试时快速排程。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            MetricCard("当前周", weekTitle, ForestGreen)
            MetricCard("冲刺项", items.size.toString(), Ginkgo)
            MetricCard("今日", selectedDate.substringAfterLast("-"), MossGreen)
            MetricCard("已完成", items.count { it.finished }.toString(), TeaGreen)
        }
        urgentPending?.let { item ->
            Spacer(modifier = Modifier.height(14.dp))
            TrendInsightCard(
                title = "临近事项",
                headline = item.title,
                body = "${item.countdownLabel} · ${item.subtitle}",
                accent = item.accent,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(
            options = ExamWeekFilter.entries.toList(),
            selected = filter,
            label = { it.title },
            onSelect = { filter = it },
        )
        Spacer(modifier = Modifier.height(10.dp))
        SelectionRow(
            options = ExamWeekTypeFilter.entries.toList(),
            selected = typeFilter,
            label = { it.title },
            onSelect = { typeFilter = it },
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (items.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                ActionPill("清空已完成", WarmMist, onClick = onClearFinished)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (filteredItems.isEmpty()) {
            Text("当前还没有考试、作业或复习事件。可以先在日视图里加入“考试”或“作业”事件。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.72f))
        } else {
            grouped.entries.forEachIndexed { index, entry ->
                val collapsed = entry.key in collapsedDates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        entry.key,
                        modifier = Modifier.clickable {
                            if (collapsed) collapsedDates.remove(entry.key) else collapsedDates.add(entry.key)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = PineInk,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (collapsed) "展开 ${entry.value.size} 项" else "收起 ${entry.value.size} 项",
                            modifier = Modifier.clickable {
                                if (collapsed) collapsedDates.remove(entry.key) else collapsedDates.add(entry.key)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestDeep.copy(alpha = 0.72f),
                        )
                        Text(
                            "整组转待办",
                            modifier = Modifier.clickable { onCreateTodoGroup(entry.key, entry.value) },
                            style = MaterialTheme.typography.labelMedium,
                            color = PineInk,
                        )
                        Text(
                            "整组绑定专注",
                            modifier = Modifier.clickable { onBindFocusGroup(entry.key, entry.value) },
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestDeep.copy(alpha = 0.82f),
                        )
                        Text(
                            if (entry.value.all { it.finished }) "整组恢复" else "整组完成",
                            modifier = Modifier.clickable {
                                onToggleFinishedGroup(entry.value, !entry.value.all { it.finished })
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (entry.value.all { it.finished }) ForestDeep.copy(alpha = 0.74f) else TeaGreen.copy(alpha = 0.92f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (!collapsed) {
                    entry.value.forEachIndexed { itemIndex, item ->
                        Surface(shape = RoundedCornerShape(22.dp), color = if (item.finished) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.34f)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.width(24.dp).height(4.dp).clip(CircleShape).background(item.accent))
                                        Text(item.title, style = MaterialTheme.typography.titleMedium, color = if (item.finished) ForestDeep.copy(alpha = 0.58f) else PineInk)
                                    }
                                    SelectionChip(text = if (item.finished) "已完成" else item.countdownLabel, chosen = item.priority == 0 || item.finished, onClick = {})
                                }
                                Text(
                                    when (item.priority) {
                                        0 -> "最高优先"
                                        1 -> "临近处理"
                                        else -> "常规安排"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ForestDeep.copy(alpha = 0.68f),
                                )
                                Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                                Text(item.detail, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    ActionPill(if (item.finished) "恢复" else "完成", if (item.finished) WarmMist else TeaGreen) { onToggleFinished(item) }
                                    ActionPill("转待办", MossGreen) { onCreateTodo(item) }
                                    ActionPill("绑定专注", ForestGreen) { onBindFocus(item) }
                                }
                            }
                        }
                        if (itemIndex != entry.value.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.18f)) {
                        Text(
                            "当前分组已折叠，保留 ${entry.value.size} 项摘要。",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestDeep.copy(alpha = 0.7f),
                        )
                    }
                }
                if (index != grouped.entries.size - 1) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private fun formatTrendValue(value: Double?): String {
    return if (value == null || value == 0.0) "--" else String.format("%.2f", value)
}

private fun formatPercent(count: Int?, total: Int?): String {
    if (count == null || total == null || total <= 0) return "--"
    return "${(count * 100 / total)}%"
}

@Composable
private fun TrendInsightCard(
    title: String,
    headline: String,
    body: String,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        modifier = Modifier.width(228.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SelectionChip(text = title, chosen = true, onClick = {})
            Text(headline, style = MaterialTheme.typography.titleMedium, color = PineInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(body, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.74f), maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MonthScheduleCard(
    month: YearMonth,
    selectedDate: String,
    activeDates: List<String>,
    selectedCourses: List<HitaCourseBlock>,
    selectedEvents: List<ScheduleExtraEvent>,
    selectedCourse: HitaCourseBlock?,
    onSelectDate: (String) -> Unit,
    onSelectCourse: (HitaCourseBlock) -> Unit,
) {
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value % 7
    val cells = remember(month) {
        val values = mutableListOf<LocalDate?>()
        repeat(leading) { values.add(null) }
        for (day in 1..month.lengthOfMonth()) {
            values.add(month.atDay(day))
        }
        values
    }
    val rows = remember(cells) { cells.chunked(7) }

    GlassCard {
        Text("${month.year} 年 ${month.monthValue} 月", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { label ->
                Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = ForestDeep.copy(alpha = 0.68f))
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        rows.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { date ->
                    val iso = date?.toString().orEmpty()
                    val selected = iso == selectedDate
                    val active = iso in activeDates
                    val isToday = iso == LocalDate.now().toString()
                    MonthCell(date = date, selected = selected, active = active, isToday = isToday, onClick = {
                        if (date != null) onSelectDate(date.toString())
                    })
                }
                repeat(7 - row.size) {
                    Spacer(modifier = Modifier.width(40.dp).height(52.dp))
                }
            }
            if (rowIndex != rows.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("当日详情", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (selectedCourses.isEmpty() && selectedEvents.isEmpty()) {
                DaySummaryCard(title = "暂无课程", body = "选中的日期当前没有返回课程安排。")
            } else {
                selectedCourses.forEach { course ->
                    DaySummaryCard(
                        title = course.courseName,
                        body = "${course.classroom} · ${course.teacher.ifBlank { "教师待补充" }}",
                        selected = selectedCourse?.let {
                            it.courseName == course.courseName && it.dayOfWeek == course.dayOfWeek && it.majorIndex == course.majorIndex
                        } == true,
                        onClick = { onSelectCourse(course) },
                    )
                }
                selectedEvents.forEach { event ->
                    DaySummaryCard(
                        title = event.title,
                        body = "${event.time} · ${event.type}${if (event.note.isBlank()) "" else " · ${event.note}"}",
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCell(
    date: LocalDate?,
    selected: Boolean,
    active: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "month-cell",
    )
    val background = when {
        selected -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.72f), BambooGlass))
        active -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.46f), Color.White.copy(alpha = 0.26f)))
        else -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.12f)))
    }

    Box(
        modifier = Modifier
            .width(40.dp)
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(BorderStroke(1.dp, if (selected) BambooStroke else Color.White.copy(alpha = 0.16f)), RoundedCornerShape(16.dp))
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Ginkgo, Color.Transparent))),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium, color = if (selected) PineInk else ForestDeep)
                if (active) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(MossGreen, TeaGreen))),
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySummaryCard(
    title: String,
    body: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (selected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, if (selected) Color.White.copy(alpha = 0.24f) else BambooStroke.copy(alpha = 0.16f)),
        modifier = Modifier
            .width(220.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = PineInk)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
        }
    }
}

@Composable
private fun CourseDetailCard(
    course: HitaCourseBlock,
    weekTitle: String,
    dayLabel: String,
    onOpenNotes: () -> Unit,
    onClose: () -> Unit,
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("课程详情", style = MaterialTheme.typography.titleMedium, color = PineInk)
                Text(
                    if (dayLabel.isBlank()) weekTitle else "$weekTitle · $dayLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.7f),
                )
            }
            SelectionChip(text = "收起", chosen = false, onClick = onClose)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(course.courseName, style = MaterialTheme.typography.headlineSmall, color = PineInk)
        Spacer(modifier = Modifier.height(12.dp))
        CourseMetaRow("教师", course.teacher.ifBlank { "教师待补充" })
        CourseMetaRow("教室", course.classroom.ifBlank { "教室待补充" })
        CourseMetaRow("时段", "第 ${course.majorIndex} 大节")
        CourseMetaRow("周内", if (dayLabel.isBlank()) "待补充" else dayLabel)
        CourseMetaRow("课程状态", "已接入教务周课表")
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionChip(text = "课程笔记", chosen = true, onClick = onOpenNotes)
            SelectionChip(text = "收起详情", chosen = false, onClick = onClose)
        }
    }
}

@Composable
private fun CourseMetaRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = PineInk,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(0.7f),
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

private data class FreeTimeDaySummary(
    val dayLabel: String,
    val freeCount: Int,
    val labels: List<String>,
)

private data class DayAnalysisSummary(
    val dayLabel: String,
    val highlights: List<String>,
)

private fun weeklyFreeTimeSummary(
    slots: List<HitaTimeSlot>,
    days: List<HitaWeekDay>,
    courses: List<HitaCourseBlock>,
): List<FreeTimeDaySummary> {
    return days.map { day ->
        val occupied = courses.filter { it.dayOfWeek == day.weekDay }.map { it.majorIndex }.toSet()
        val freeSlots = slots.filter { it.majorIndex !in occupied }
        FreeTimeDaySummary(
            dayLabel = day.label,
            freeCount = freeSlots.size,
            labels = if (freeSlots.isEmpty()) listOf("当天课程已满") else freeSlots.map { it.label },
        )
    }
}

private fun weeklyCourseAnalysis(
    days: List<HitaWeekDay>,
    courses: List<HitaCourseBlock>,
): List<DayAnalysisSummary> {
    return days.mapNotNull { day ->
        val dayCourses = courses.filter { it.dayOfWeek == day.weekDay }.sortedBy { it.majorIndex }
        if (dayCourses.isEmpty()) return@mapNotNull null
        val conflictTags = dayCourses.groupBy { it.majorIndex }
            .filterValues { it.size > 1 }
            .map { (majorIndex, items) -> "冲突 第 ${majorIndex} 大节 ${items.size} 门" }
        val contiguousTags = dayCourses.zipWithNext()
            .filter { (left, right) -> right.majorIndex - left.majorIndex == 1 }
            .map { (left, right) -> "连堂 ${left.courseName} → ${right.courseName}" }
        val tags = (conflictTags + contiguousTags).distinct()
        if (tags.isEmpty()) null else DayAnalysisSummary(day.label, tags)
    }
}

private fun dayCourseTags(
    courses: List<HitaCourseBlock>,
    course: HitaCourseBlock,
): List<String> {
    val sameSlotCount = courses.count { it.majorIndex == course.majorIndex }
    val hasPrev = courses.any { it.majorIndex == course.majorIndex - 1 }
    val hasNext = courses.any { it.majorIndex == course.majorIndex + 1 }
    val tags = mutableListOf<String>()
    if (sameSlotCount > 1) tags += "冲突 ${sameSlotCount} 门"
    if (hasPrev || hasNext) tags += "连堂"
    return tags
}

private fun buildDayTimelineEntries(
    courses: List<HitaCourseBlock>,
    events: List<ScheduleExtraEvent>,
): List<DayTimelineEntry> {
    val courseEntries = courses.map { course ->
        DayTimelineEntry(
            sortKey = course.majorIndex * 100,
            title = course.courseName,
            subtitle = "第 ${course.majorIndex} 时段",
            detail = "${course.classroom.ifBlank { "教室待补充" }} · ${course.teacher.ifBlank { "教师待补充" }}",
            accent = Color(course.accent),
            tags = emptyList(),
            selectableCourse = course,
        )
    }
    val eventEntries = events.map { event ->
        DayTimelineEntry(
            sortKey = eventSortKey(event.time),
            title = event.title,
            subtitle = "${event.time} · ${event.type}",
            detail = event.note.ifBlank { "已加入当天学习时间线" },
            accent = when (event.type) {
                "考试" -> Ginkgo
                "作业" -> MossGreen
                else -> ForestGreen
            },
            tags = listOf(event.type),
            extraEvent = event,
        )
    }
    return (courseEntries + eventEntries).sortedBy { it.sortKey }
}

private fun buildGradeTrendPoints(cardsByTerm: List<Pair<String, List<FeedCard>>>): List<GradeTrendPoint> {
    return cardsByTerm.map { (termName, cards) ->
        val parsed = cards.map { parseGradeCard(it) }
        val scores = parsed.mapNotNull { it.score }
        val gradePoints = parsed.mapNotNull { it.gradePoint }
        val credits = parsed.mapNotNull { it.credits }
        GradeTrendPoint(
            termName = termName,
            averageScore = if (scores.isEmpty()) 0.0 else scores.average(),
            averageGradePoint = if (gradePoints.isEmpty()) 0.0 else gradePoints.average(),
            credits = credits.sum(),
            excellentCount = parsed.count { (it.score ?: 0.0) >= 90.0 },
            warningCount = parsed.count {
                val score = it.score
                score == null || score < 60.0
            },
            courseCount = cards.size,
            rawCards = cards,
        )
    }
}

private fun buildExamWeekItems(
    schedule: HitaWeekSchedule,
    events: List<ScheduleExtraEvent>,
    completedIds: List<String>,
): List<ExamWeekItem> {
    val scheduleItems = schedule.days.flatMap { day ->
        schedule.courses
            .filter { it.dayOfWeek == day.weekDay }
            .sortedBy { it.majorIndex }
            .map { course ->
                ExamWeekItem(
                    id = "course-${day.fullDate}-${course.courseName}-${course.majorIndex}",
                    date = day.fullDate,
                    title = course.courseName,
                    subtitle = "${day.label} · 第 ${course.majorIndex} 大节",
                    detail = buildString {
                        append(course.classroom.ifBlank { "教室待补充" })
                        append(" · ")
                        append(course.teacher.ifBlank { "教师待补充" })
                    },
                    accent = Color(course.accent),
                    priority = countdownPriority(day.fullDate),
                    countdownLabel = countdownLabel(day.fullDate),
                    finished = "course-${day.fullDate}-${course.courseName}-${course.majorIndex}" in completedIds,
                )
            }
    }
    val eventItems = events
        .filter { it.type == "考试" || it.type == "作业" || it.type == "复习" }
        .sortedWith(compareBy<ScheduleExtraEvent> { it.date }.thenBy { eventSortKey(it.time) })
        .map { event ->
            ExamWeekItem(
                id = event.id,
                date = event.date,
                title = event.title,
                subtitle = "${event.date.substringAfterLast("-")} · ${event.time} · ${event.type}",
                detail = event.note.ifBlank { "已加入考试周冲刺列表" },
                accent = when (event.type) {
                    "考试" -> Ginkgo
                    "作业" -> MossGreen
                    else -> ForestGreen
                },
                priority = countdownPriority(event.date, event.type),
                countdownLabel = countdownLabel(event.date),
                finished = event.id in completedIds,
            )
        }
    return (eventItems + scheduleItems)
        .sortedWith(compareBy<ExamWeekItem> { it.priority }.thenBy { it.date }.thenBy { it.subtitle })
}

private data class ParsedGradeCard(
    val score: Double?,
    val gradePoint: Double?,
    val credits: Double?,
)

private fun parseGradeCard(card: FeedCard): ParsedGradeCard {
    return ParsedGradeCard(
        score = extractNumericValue(card.source, "总评"),
        gradePoint = extractNumericValue(card.source, "绩点"),
        credits = extractNumericValue(card.description, "学分"),
    )
}

private fun extractNumericValue(text: String, key: String): Double? {
    val afterKey = text.substringAfter(key, "").trim()
    if (afterKey.isBlank()) return null
    val token = afterKey.takeWhile { it.isDigit() || it == '.' }
    return token.toDoubleOrNull()
}

private fun countdownLabel(date: String): String {
    val target = runCatching { LocalDate.parse(date) }.getOrNull() ?: return "待定"
    val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), target).toInt()
    return when {
        days < 0 -> "已结束"
        days == 0 -> "今天"
        days == 1 -> "明天"
        else -> "还有 ${days} 天"
    }
}

private fun countdownPriority(date: String, type: String = ""): Int {
    val target = runCatching { LocalDate.parse(date) }.getOrNull() ?: return 2
    val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), target).toInt()
    return when {
        type == "考试" && days <= 1 -> 0
        days <= 2 -> 1
        else -> 2
    }
}

private fun eventSortKey(time: String): Int {
    val parts = time.split(":")
    if (parts.size != 2) return Int.MAX_VALUE / 2
    val hour = parts[0].toIntOrNull() ?: return Int.MAX_VALUE / 2
    val minute = parts[1].toIntOrNull() ?: 0
    return hour * 60 + minute
}

private fun loadScheduleExtraEvents(
    prefs: android.content.SharedPreferences,
): List<ScheduleExtraEvent> {
    val raw = prefs.getString("schedule_extra_events_v1", "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val json = array.getJSONObject(index)
                add(
                    ScheduleExtraEvent(
                        id = json.optString("id"),
                        date = json.optString("date"),
                        title = json.optString("title"),
                        time = json.optString("time"),
                        type = json.optString("type"),
                        note = json.optString("note"),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun saveScheduleExtraEvents(
    prefs: android.content.SharedPreferences,
    events: List<ScheduleExtraEvent>,
) {
    val array = JSONArray().apply {
        events.forEach { event ->
            put(
                JSONObject().apply {
                    put("id", event.id)
                    put("date", event.date)
                    put("title", event.title)
                    put("time", event.time)
                    put("type", event.type)
                    put("note", event.note)
                },
            )
        }
    }
    prefs.edit().putString("schedule_extra_events_v1", array.toString()).apply()
}

private fun loadScheduleEventDraft(
    prefs: android.content.SharedPreferences,
): ScheduleEventDraft? {
    val raw = prefs.getString("schedule_event_draft_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val json = JSONObject(raw)
        ScheduleEventDraft(
            eventId = json.optString("eventId").ifBlank { null },
            date = json.optString("date"),
            title = json.optString("title"),
            time = json.optString("time", "19:30"),
            type = json.optString("type", "作业"),
            note = json.optString("note"),
        )
    }.getOrNull()
}

private fun saveScheduleEventDraft(
    prefs: android.content.SharedPreferences,
    draft: ScheduleEventDraft,
) {
    prefs.edit()
        .putString(
            "schedule_event_draft_v1",
            JSONObject().apply {
                put("eventId", draft.eventId)
                put("date", draft.date)
                put("title", draft.title)
                put("time", draft.time)
                put("type", draft.type)
                put("note", draft.note)
            }.toString(),
        )
        .apply()
}

private fun clearScheduleEventDraft(prefs: android.content.SharedPreferences) {
    prefs.edit().remove("schedule_event_draft_v1").apply()
}

private fun buildScheduleShareText(
    state: HitaScheduleUiState,
): String {
    val courseLines = state.weekSchedule.days.joinToString("\n") { day ->
        val dayCourses = state.weekSchedule.courses
            .filter { it.dayOfWeek == day.weekDay }
            .sortedBy { it.majorIndex }
        if (dayCourses.isEmpty()) {
            "${day.label} ${day.date}：无课程"
        } else {
            val body = dayCourses.joinToString("；") { course ->
                "${course.courseName}（第${course.majorIndex}大节 ${course.classroom.ifBlank { "教室待补充" }} ${course.teacher.ifBlank { "教师待补充" }}）"
            }
            "${day.label} ${day.date}：$body"
        }
    }
    val freeLines = weeklyFreeTimeSummary(
        slots = state.weekSchedule.timeSlots,
        days = state.weekSchedule.days,
        courses = state.weekSchedule.courses,
    ).joinToString("\n") { item ->
        "${item.dayLabel}：${item.labels.joinToString("、")}"
    }
    return buildString {
        appendLine("${state.currentTerm.name} ${state.currentWeek.title} 课表摘要")
        appendLine()
        appendLine("课程安排")
        appendLine(courseLines)
        appendLine()
        appendLine("空闲时段")
        appendLine(freeLines)
    }.trim()
}

private fun saveCachedScheduleUiState(
    prefs: android.content.SharedPreferences,
    state: HitaScheduleUiState,
) {
    val root = JSONObject().apply {
        put("loggedIn", state.loggedIn)
        put("studentId", state.studentId)
        put("studentName", state.studentName)
        put("selectedDate", state.selectedDate)
        put("terms", JSONArray().apply { state.terms.forEach { put(termToJson(it)) } })
        put("currentTerm", termToJson(state.currentTerm))
        put("weeks", JSONArray().apply { state.weeks.forEach { put(weekToJson(it)) } })
        put("currentWeek", weekToJson(state.currentWeek))
        put("weekSchedule", weekScheduleToJson(state.weekSchedule))
        put("selectedDateCourses", JSONArray().apply { state.selectedDateCourses.forEach { put(courseToJson(it)) } })
    }
    prefs.edit().putString("schedule_cache_v1", root.toString()).apply()
}

private fun loadCachedScheduleUiState(
    prefs: android.content.SharedPreferences,
): HitaScheduleUiState? {
    val raw = prefs.getString("schedule_cache_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val root = JSONObject(raw)
        val terms = root.optJSONArray("terms")?.let(::termsFromJson).orEmpty()
        val weeks = root.optJSONArray("weeks")?.let(::weeksFromJson).orEmpty()
        val weekSchedule = root.optJSONObject("weekSchedule")?.let(::weekScheduleFromJson) ?: return null
        val currentTerm = root.optJSONObject("currentTerm")?.let(::termFromJson) ?: weekSchedule.term
        val currentWeek = root.optJSONObject("currentWeek")?.let(::weekFromJson) ?: weekSchedule.week
        val selectedDate = root.optString("selectedDate", weekSchedule.days.firstOrNull()?.fullDate.orEmpty())
        val selectedDateCourses = root.optJSONArray("selectedDateCourses")?.let(::coursesFromJson).orEmpty()
        HitaScheduleUiState(
            loading = false,
            loggedIn = root.optBoolean("loggedIn", false),
            studentId = root.optString("studentId", ""),
            studentName = root.optString("studentName", ""),
            terms = if (terms.isEmpty()) listOf(currentTerm) else terms,
            currentTerm = currentTerm,
            weeks = if (weeks.isEmpty()) listOf(currentWeek) else weeks,
            currentWeek = currentWeek,
            weekSchedule = weekSchedule,
            selectedDate = selectedDate,
            selectedDateCourses = if (selectedDateCourses.isEmpty()) {
                val day = weekSchedule.days.firstOrNull { it.fullDate == selectedDate }?.weekDay ?: 1
                weekSchedule.courses.filter { it.dayOfWeek == day }
            } else {
                selectedDateCourses
            },
            status = "已恢复上次缓存课表。",
        )
    }.getOrNull()
}

private fun formatSyncTime(time: LocalDateTime): String {
    return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}

private fun termToJson(term: HitaTerm): JSONObject {
    return JSONObject().apply {
        put("year", term.year)
        put("term", term.term)
        put("name", term.name)
        put("isCurrent", term.isCurrent)
    }
}

private fun termFromJson(json: JSONObject): HitaTerm {
    return HitaTerm(
        year = json.optString("year"),
        term = json.optString("term"),
        name = json.optString("name"),
        isCurrent = json.optBoolean("isCurrent"),
    )
}

private fun weekToJson(week: HitaWeek): JSONObject {
    return JSONObject().apply {
        put("index", week.index)
        put("title", week.title)
    }
}

private fun weekFromJson(json: JSONObject): HitaWeek {
    return HitaWeek(
        index = json.optInt("index"),
        title = json.optString("title"),
    )
}

private fun weekDayToJson(day: HitaWeekDay): JSONObject {
    return JSONObject().apply {
        put("weekDay", day.weekDay)
        put("label", day.label)
        put("date", day.date)
        put("fullDate", day.fullDate)
    }
}

private fun weekDayFromJson(json: JSONObject): HitaWeekDay {
    return HitaWeekDay(
        weekDay = json.optInt("weekDay"),
        label = json.optString("label"),
        date = json.optString("date"),
        fullDate = json.optString("fullDate"),
    )
}

private fun timeSlotToJson(slot: HitaTimeSlot): JSONObject {
    return JSONObject().apply {
        put("majorIndex", slot.majorIndex)
        put("label", slot.label)
        put("timeRange", slot.timeRange)
        put("startSection", slot.startSection)
        put("endSection", slot.endSection)
    }
}

private fun timeSlotFromJson(json: JSONObject): HitaTimeSlot {
    return HitaTimeSlot(
        majorIndex = json.optInt("majorIndex"),
        label = json.optString("label"),
        timeRange = json.optString("timeRange"),
        startSection = json.optInt("startSection"),
        endSection = json.optInt("endSection"),
    )
}

private fun courseToJson(course: HitaCourseBlock): JSONObject {
    return JSONObject().apply {
        put("courseName", course.courseName)
        put("classroom", course.classroom)
        put("teacher", course.teacher)
        put("dayOfWeek", course.dayOfWeek)
        put("majorIndex", course.majorIndex)
        put("accent", course.accent)
    }
}

private fun courseFromJson(json: JSONObject): HitaCourseBlock {
    return HitaCourseBlock(
        courseName = json.optString("courseName"),
        classroom = json.optString("classroom"),
        teacher = json.optString("teacher"),
        dayOfWeek = json.optInt("dayOfWeek"),
        majorIndex = json.optInt("majorIndex"),
        accent = json.optLong("accent"),
    )
}

private fun weekScheduleToJson(schedule: HitaWeekSchedule): JSONObject {
    return JSONObject().apply {
        put("term", termToJson(schedule.term))
        put("week", weekToJson(schedule.week))
        put("days", JSONArray().apply { schedule.days.forEach { put(weekDayToJson(it)) } })
        put("timeSlots", JSONArray().apply { schedule.timeSlots.forEach { put(timeSlotToJson(it)) } })
        put("courses", JSONArray().apply { schedule.courses.forEach { put(courseToJson(it)) } })
    }
}

private fun weekScheduleFromJson(json: JSONObject): HitaWeekSchedule {
    return HitaWeekSchedule(
        term = termFromJson(json.getJSONObject("term")),
        week = weekFromJson(json.getJSONObject("week")),
        days = weekDaysFromJson(json.getJSONArray("days")),
        timeSlots = timeSlotsFromJson(json.getJSONArray("timeSlots")),
        courses = coursesFromJson(json.getJSONArray("courses")),
    )
}

private fun termsFromJson(array: JSONArray): List<HitaTerm> {
    return buildList {
        for (index in 0 until array.length()) add(termFromJson(array.getJSONObject(index)))
    }
}

private fun weeksFromJson(array: JSONArray): List<HitaWeek> {
    return buildList {
        for (index in 0 until array.length()) add(weekFromJson(array.getJSONObject(index)))
    }
}

private fun weekDaysFromJson(array: JSONArray): List<HitaWeekDay> {
    return buildList {
        for (index in 0 until array.length()) add(weekDayFromJson(array.getJSONObject(index)))
    }
}

private fun timeSlotsFromJson(array: JSONArray): List<HitaTimeSlot> {
    return buildList {
        for (index in 0 until array.length()) add(timeSlotFromJson(array.getJSONObject(index)))
    }
}

private fun coursesFromJson(array: JSONArray): List<HitaCourseBlock> {
    return buildList {
        for (index in 0 until array.length()) add(courseFromJson(array.getJSONObject(index)))
    }
}

@Composable
private fun TodoScreen(initialFilter: TodoFilter = TodoFilter.All) {
    var filter by remember(initialFilter) { mutableStateOf(initialFilter) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("todo_board", android.content.Context.MODE_PRIVATE) }
    val draftPrefs = remember { context.getSharedPreferences("todo_draft", android.content.Context.MODE_PRIVATE) }
    val tasks = remember { mutableStateListOf<TodoTask>().apply { addAll(loadTodoTasks(prefs)) } }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("今天 21:00") }
    var duePreset by remember { mutableStateOf(TodoDuePreset.Today) }
    var dueClock by remember { mutableStateOf("21:00") }
    var listName by remember { mutableStateOf("收集箱") }
    var reminder by remember { mutableStateOf("提前 30 分钟") }
    var reminderPreset by remember { mutableStateOf(TodoReminderPreset.Before30Min) }
    var repeatText by remember { mutableStateOf("不重复") }
    var quadrant by remember { mutableStateOf(TodoQuadrant.ImportantNotUrgent) }
    var priority by remember { mutableStateOf(TodoPriority.Medium) }
    var focusGoal by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var tagDraft by remember { mutableStateOf("") }
    val draftTags = remember { mutableStateListOf<String>() }
    val selectedTags = remember { mutableStateListOf<String>() }
    var subtaskDraft by remember { mutableStateOf("") }
    val draftSubtasks = remember { mutableStateListOf<TodoSubtask>() }
    val restoredTodoDraft = remember { loadTodoDraft(draftPrefs) }
    var editingTaskId by remember { mutableStateOf(restoredTodoDraft?.editingTaskId) }
    var todoHint by remember { mutableStateOf("") }
    val focusPrefs = remember { context.getSharedPreferences("focus_bridge", android.content.Context.MODE_PRIVATE) }
    var viewMode by remember { mutableStateOf(TodoViewMode.Flat) }
    val editingTask = tasks.firstOrNull { it.id == editingTaskId }
    val quadrantCounts = TodoQuadrant.entries.associateWith { item -> tasks.count { !it.done && it.quadrant == item } }
    val listOptions = remember(tasks.toList()) {
        listOf("全部清单") + tasks.map { it.listName }.filter { it.isNotBlank() }.distinct()
    }
    val allTags = remember(tasks.toList()) { tasks.flatMap { it.tags }.distinct() }
    var listFilter by remember { mutableStateOf("全部清单") }
    var focusGoalFilter by remember { mutableStateOf(TodoFocusGoalFilter.All) }
    var draftReady by remember { mutableStateOf(false) }
    var draftRestored by remember { mutableStateOf(false) }

    val visibleTasks = when (filter) {
        TodoFilter.All -> tasks.toList()
        TodoFilter.Focus -> tasks.filter { !it.done && it.quadrant != TodoQuadrant.Neither }
        TodoFilter.Today -> tasks.filter { it.dueText.contains("今天") || it.dueText.contains("今晚") }
        TodoFilter.Done -> tasks.filter { it.done }
    }
    val finalVisibleTasks = visibleTasks.filter { task ->
        (listFilter == "全部清单" || task.listName == listFilter) &&
            (searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true) || task.note.contains(searchQuery, ignoreCase = true)) &&
            (selectedTags.isEmpty() || selectedTags.all { it in task.tags }) &&
            when (focusGoalFilter) {
                TodoFocusGoalFilter.All -> true
                TodoFocusGoalFilter.WithGoal -> task.focusGoal > 0
                TodoFocusGoalFilter.Pending -> task.focusGoal > 0 && task.focusCount < task.focusGoal
                TodoFocusGoalFilter.Reached -> task.focusGoal > 0 && task.focusCount >= task.focusGoal
            }
    }
    val groupedTasks = finalVisibleTasks.groupBy { it.listName.ifBlank { "未分组" } }
    val completedCount = tasks.count { it.done }
    val focusCount = tasks.map { it.focusCount }.sum()
    val archivedTasks = tasks.filter { it.done }
    var archiveExpanded by remember { mutableStateOf(false) }
    val resetEditorForm = {
        title = ""
        note = ""
        due = "今天 21:00"
        duePreset = TodoDuePreset.Today
        dueClock = "21:00"
        listName = "收集箱"
        reminder = "提前 30 分钟"
        reminderPreset = TodoReminderPreset.Before30Min
        repeatText = "不重复"
        quadrant = TodoQuadrant.ImportantNotUrgent
        priority = TodoPriority.Medium
        focusGoal = 0
        tagDraft = ""
        draftTags.clear()
        subtaskDraft = ""
        draftSubtasks.clear()
    }
    LaunchedEffect(editingTaskId) {
        val persistedDraft = if (!draftRestored) loadTodoDraft(draftPrefs) else null
        if (persistedDraft != null && persistedDraft.editingTaskId == editingTaskId) {
            title = persistedDraft.title
            note = persistedDraft.note
            duePreset = persistedDraft.duePreset
            dueClock = persistedDraft.dueClock
            due = buildTodoDueText(duePreset, dueClock)
            listName = persistedDraft.listName
            reminderPreset = persistedDraft.reminderPreset
            reminder = reminderPreset.title
            repeatText = persistedDraft.repeatText
            quadrant = persistedDraft.quadrant
            priority = persistedDraft.priority
            focusGoal = persistedDraft.focusGoal
            tagDraft = persistedDraft.tagDraft
            draftTags.clear()
            draftTags.addAll(persistedDraft.tags)
            subtaskDraft = persistedDraft.subtaskDraft
            draftSubtasks.clear()
            draftSubtasks.addAll(persistedDraft.subtasks)
            if (
                persistedDraft.title.isNotBlank() ||
                persistedDraft.note.isNotBlank() ||
                persistedDraft.tags.isNotEmpty() ||
                persistedDraft.subtasks.isNotEmpty()
            ) {
                todoHint = "已恢复上次未提交的待办草稿。"
            }
            draftRestored = true
        } else if (editingTask != null) {
            title = editingTask.title
            note = editingTask.note
            due = editingTask.dueText
            duePreset = inferTodoDuePreset(editingTask.dueText)
            dueClock = extractTodoDueClock(editingTask.dueText)
            listName = editingTask.listName
            reminder = editingTask.reminderText.ifBlank { "不提醒" }
            reminderPreset = inferTodoReminderPreset(editingTask.reminderText)
            repeatText = editingTask.repeatText
            quadrant = editingTask.quadrant
            priority = editingTask.priority
            focusGoal = editingTask.focusGoal
            draftTags.clear()
            draftTags.addAll(editingTask.tags)
            draftSubtasks.clear()
            draftSubtasks.addAll(editingTask.subtasks)
            todoHint = "正在编辑 ${editingTask.title}"
        } else if (!draftRestored) {
            val freshDraft = loadTodoDraft(draftPrefs)
            if (freshDraft != null && freshDraft.editingTaskId.isNullOrBlank()) {
                title = freshDraft.title
                note = freshDraft.note
                duePreset = freshDraft.duePreset
                dueClock = freshDraft.dueClock
                due = buildTodoDueText(duePreset, dueClock)
                listName = freshDraft.listName
                reminderPreset = freshDraft.reminderPreset
                reminder = reminderPreset.title
                repeatText = freshDraft.repeatText
                quadrant = freshDraft.quadrant
                priority = freshDraft.priority
                focusGoal = freshDraft.focusGoal
                tagDraft = freshDraft.tagDraft
                draftTags.clear()
                draftTags.addAll(freshDraft.tags)
                subtaskDraft = freshDraft.subtaskDraft
                draftSubtasks.clear()
                draftSubtasks.addAll(freshDraft.subtasks)
                if (freshDraft.title.isNotBlank() || freshDraft.note.isNotBlank() || freshDraft.tags.isNotEmpty() || freshDraft.subtasks.isNotEmpty()) {
                    todoHint = "已恢复上次未提交的待办草稿。"
                }
                draftRestored = true
            } else {
                resetEditorForm()
            }
        }
        draftReady = true
    }

    LaunchedEffect(
        editingTaskId,
        title,
        note,
        duePreset,
        dueClock,
        listName,
        reminderPreset,
        repeatText,
        quadrant,
        priority,
        focusGoal,
        tagDraft,
        draftTags.joinToString("|"),
        subtaskDraft,
        draftSubtasks.joinToString("|") { "${it.title}:${it.done}" },
        draftReady,
    ) {
        if (!draftReady) return@LaunchedEffect
        val hasDraftContent =
            title.isNotBlank() ||
                note.isNotBlank() ||
                listName != "收集箱" ||
                repeatText != "不重复" ||
                focusGoal > 0 ||
                duePreset != TodoDuePreset.Today ||
                dueClock != "21:00" ||
                reminderPreset != TodoReminderPreset.Before30Min ||
                quadrant != TodoQuadrant.ImportantNotUrgent ||
                priority != TodoPriority.Medium ||
                tagDraft.isNotBlank() ||
                draftTags.isNotEmpty() ||
                subtaskDraft.isNotBlank() ||
                draftSubtasks.isNotEmpty()
        if (!hasDraftContent) {
            clearTodoDraft(draftPrefs)
        } else {
            saveTodoDraft(
                draftPrefs,
                TodoDraft(
                    editingTaskId = editingTaskId,
                    title = title,
                    note = note,
                    duePreset = duePreset,
                    dueClock = dueClock,
                    listName = listName,
                    reminderPreset = reminderPreset,
                    repeatText = repeatText,
                    quadrant = quadrant,
                    priority = priority,
                    focusGoal = focusGoal,
                    tagDraft = tagDraft,
                    tags = draftTags.toList(),
                    subtaskDraft = subtaskDraft,
                    subtasks = draftSubtasks.toList(),
                ),
            )
        }
    }

    LaunchedEffect(
        tasks.joinToString("|") {
            "${it.id}:${it.title}:${it.dueText}:${it.reminderText}:${it.done}:${it.focusGoal}:${it.focusCount}"
        },
    ) {
        refreshLocalReminderSchedule(context)
    }

    CompositionLocalProvider(LocalStaticGlassMode provides true) {
        ScreenColumn {
        item {
            GlassCard {
                Text("待办工作台", style = MaterialTheme.typography.headlineMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                Text("支持四象限、自定义任务、提醒、重复与清单归类。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.78f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    MetricCard("总任务", tasks.size.toString(), ForestGreen)
                    MetricCard("已完成", completedCount.toString(), MossGreen)
                    MetricCard("专注回写", "${focusCount} 次", Ginkgo)
                    MetricCard("目标达成", tasks.count { it.focusGoal > 0 && it.focusCount >= it.focusGoal }.toString(), TeaGreen)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    TodoQuadrant.entries.forEach { item ->
                        Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.54f), modifier = Modifier.width(164.dp)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.title, style = MaterialTheme.typography.titleMedium, color = PineInk, modifier = Modifier.weight(1f, fill = false))
                                    Text("${quadrantCounts[item] ?: 0}", style = MaterialTheme.typography.titleMedium, color = ForestGreen)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.7f), maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassCard {
                Text(if (editingTask == null) "新建任务" else "编辑任务", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("任务标题") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("补充说明") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(options = TodoDuePreset.entries.toList(), selected = duePreset, label = { it.title }, onSelect = {
                    duePreset = it
                    due = buildTodoDueText(it, dueClock)
                })
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = dueClock,
                    onValueChange = {
                        dueClock = it
                        due = buildTodoDueText(duePreset, dueClock)
                    },
                    label = { Text("时间") },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = listName, onValueChange = { listName = it }, label = { Text("所属清单") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(options = TodoReminderPreset.entries.toList(), selected = reminderPreset, label = { it.title }, onSelect = {
                    reminderPreset = it
                    reminder = it.title
                })
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = repeatText, onValueChange = { repeatText = it }, label = { Text("重复") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(options = TodoQuadrant.entries.toList(), selected = quadrant, label = { it.title }, onSelect = { quadrant = it })
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(options = TodoPriority.entries.toList(), selected = priority, label = { it.title }, onSelect = { priority = it })
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(
                    options = TodoFocusGoalOptions,
                    selected = focusGoal,
                    label = { if (it == 0) "无专注目标" else "目标 ${it} 轮" },
                    onSelect = { focusGoal = it },
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tagDraft,
                        onValueChange = { tagDraft = it },
                        label = { Text("标签") },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.weight(1f),
                    )
                    ActionPill("加入", Ginkgo) {
                        val normalized = tagDraft.trim()
                        if (normalized.isNotBlank() && normalized !in draftTags) {
                            draftTags.add(normalized)
                            tagDraft = ""
                        }
                    }
                }
                if (draftTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        draftTags.forEachIndexed { index, tag ->
                            ActionPill(tag, WarmMist) { draftTags.removeAt(index) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = subtaskDraft,
                        onValueChange = { subtaskDraft = it },
                        label = { Text("子任务") },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.weight(1f),
                    )
                    ActionPill(
                        text = "加入",
                        background = ForestGreen,
                        onClick = {
                            if (subtaskDraft.isNotBlank()) {
                                draftSubtasks.add(TodoSubtask(subtaskDraft))
                                subtaskDraft = ""
                            }
                        },
                    )
                }
                if (draftSubtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        draftSubtasks.forEachIndexed { index, subtask ->
                            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.4f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(subtask.title, style = MaterialTheme.typography.bodyMedium, color = PineInk, modifier = Modifier.weight(1f))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ActionPill(
                                            text = if (subtask.done) "已完成" else "待完成",
                                            background = if (subtask.done) MossGreen else TeaGreen,
                                            onClick = {
                                                draftSubtasks[index] = draftSubtasks[index].copy(done = !draftSubtasks[index].done)
                                            },
                                        )
                                        ActionPill(
                                            text = "删除",
                                            background = WarmMist,
                                            onClick = { draftSubtasks.removeAt(index) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val updatedTask = TodoTask(
                                    id = editingTask?.id ?: "todo-${System.currentTimeMillis()}",
                                    title = title,
                                    note = note,
                                    quadrant = quadrant,
                                    priority = priority,
                                    dueText = buildTodoDueText(duePreset, dueClock),
                                    tags = draftTags.toList(),
                                    listName = listName,
                                    reminderText = reminderPreset.title,
                                    repeatText = repeatText,
                                    subtasks = draftSubtasks.toList(),
                                    focusCount = editingTask?.focusCount ?: 0,
                                    focusGoal = focusGoal,
                                    done = editingTask?.done ?: false,
                                )
                                if (editingTask == null) {
                                    tasks.add(0, updatedTask)
                                    todoHint = "已加入待办：${updatedTask.title}"
                                } else {
                                    val index = tasks.indexOfFirst { it.id == updatedTask.id }
                                    if (index >= 0) tasks[index] = updatedTask
                                    todoHint = "已更新任务：${updatedTask.title}"
                                }
                                saveTodoTasks(prefs, tasks)
                                clearTodoDraft(draftPrefs)
                                editingTaskId = null
                                draftRestored = false
                                resetEditorForm()
                            } else {
                                todoHint = "请先填写任务标题"
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (editingTask == null) "加入待办" else "保存修改")
                    }
                    if (editingTask != null) {
                        OutlinedButton(
                            onClick = {
                                tasks.removeAll { it.id == editingTask.id }
                                saveTodoTasks(prefs, tasks)
                                clearTodoDraft(draftPrefs)
                                todoHint = "已删除任务：${editingTask.title}"
                                editingTaskId = null
                                draftRestored = false
                                resetEditorForm()
                            },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("删除任务")
                        }
                    }
                }
                if (todoHint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(todoHint, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                }
            }
        }
        item {
            GlassCard {
                SelectionRow(options = TodoFilter.entries.toList(), selected = filter, label = { it.title }, onSelect = { filter = it })
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(options = TodoViewMode.entries.toList(), selected = viewMode, label = { it.title }, onSelect = { viewMode = it })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("搜索任务") },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (allTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        allTags.forEach { tag ->
                            SelectionChip(
                                text = tag,
                                chosen = tag in selectedTags,
                                onClick = {
                                    if (tag in selectedTags) selectedTags.remove(tag) else selectedTags.add(tag)
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(options = listOptions, selected = listFilter, label = { it }, onSelect = { listFilter = it })
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(
                    options = TodoFocusGoalFilter.entries.toList(),
                    selected = focusGoalFilter,
                    label = { it.title },
                    onSelect = { focusGoalFilter = it },
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (viewMode == TodoViewMode.Flat) {
                    finalVisibleTasks.forEachIndexed { index, task ->
                        val taskIndex = tasks.indexOfFirst { it.id == task.id }
                        TodoTaskCard(
                            task = task,
                            onToggle = {
                                if (taskIndex >= 0) {
                                    todoHint = toggleTodoTask(tasks, taskIndex)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            onPostpone = {
                                if (taskIndex >= 0) {
                                    todoHint = postponeTodoTask(tasks, taskIndex)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            onEdit = {
                                editingTaskId = task.id
                                todoHint = "正在编辑 ${task.title}"
                            },
                            onBindPomodoro = {
                                focusPrefs.edit()
                                    .remove("bound_task_title")
                                    .remove("bound_task_list")
                                    .apply()
                                SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                                SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                todoHint = "已绑定到番茄钟：${task.title}"
                            },
                            onNotify = {
                                sendAppNotification(context, "待办提醒", "${task.title} · ${task.dueText}")
                                todoHint = "已发送提醒：${task.title}"
                            },
                            onToggleSubtask = { subtaskIndex ->
                                if (taskIndex >= 0 && subtaskIndex in tasks[taskIndex].subtasks.indices) {
                                    val subtasks = tasks[taskIndex].subtasks.toMutableList()
                                    subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
                                    tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            onMoveUp = {
                                if (taskIndex > 0) {
                                    tasks.swap(taskIndex, taskIndex - 1)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            onMoveDown = {
                                if (taskIndex >= 0 && taskIndex < tasks.lastIndex) {
                                    tasks.swap(taskIndex, taskIndex + 1)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            canMoveUp = taskIndex > 0,
                            canMoveDown = taskIndex in 0 until tasks.lastIndex,
                        )
                        if (index != finalVisibleTasks.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                    }
                } else if (viewMode == TodoViewMode.Grouped) {
                    groupedTasks.entries.forEachIndexed { groupIndex, entry ->
                        Text(entry.key, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Spacer(modifier = Modifier.height(8.dp))
                        entry.value.forEachIndexed { index, task ->
                            val taskIndex = tasks.indexOfFirst { it.id == task.id }
                            TodoTaskCard(
                                task = task,
                                onToggle = {
                                    if (taskIndex >= 0) {
                                        todoHint = toggleTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onPostpone = {
                                    if (taskIndex >= 0) {
                                        todoHint = postponeTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onEdit = {
                                    editingTaskId = task.id
                                    todoHint = "正在编辑 ${task.title}"
                                },
                                onBindPomodoro = {
                                    focusPrefs.edit()
                                        .remove("bound_task_title")
                                        .remove("bound_task_list")
                                        .apply()
                                    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                    todoHint = "已绑定到番茄钟：${task.title}"
                                },
                                onNotify = {
                                    sendAppNotification(context, "待办提醒", "${task.title} · ${task.dueText}")
                                    todoHint = "已发送提醒：${task.title}"
                                },
                                onToggleSubtask = { subtaskIndex ->
                                    if (taskIndex >= 0 && subtaskIndex in tasks[taskIndex].subtasks.indices) {
                                        val subtasks = tasks[taskIndex].subtasks.toMutableList()
                                        subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
                                        tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onMoveUp = {
                                    if (taskIndex > 0) {
                                        tasks.swap(taskIndex, taskIndex - 1)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onMoveDown = {
                                    if (taskIndex >= 0 && taskIndex < tasks.lastIndex) {
                                        tasks.swap(taskIndex, taskIndex + 1)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                canMoveUp = taskIndex > 0,
                                canMoveDown = taskIndex in 0 until tasks.lastIndex,
                            )
                            if (index != entry.value.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (groupIndex != groupedTasks.entries.size - 1) Spacer(modifier = Modifier.height(14.dp))
                    }
                } else {
                    val calendarGroups = finalVisibleTasks.groupBy { it.dueText }.toList()
                    calendarGroups.forEachIndexed { groupIndex, entry ->
                        Text(entry.first, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Spacer(modifier = Modifier.height(8.dp))
                        entry.second.forEachIndexed { index, task ->
                            val taskIndex = tasks.indexOfFirst { it.id == task.id }
                            TodoTaskCard(
                                task = task,
                                onToggle = {
                                    if (taskIndex >= 0) {
                                        todoHint = toggleTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onPostpone = {
                                    if (taskIndex >= 0) {
                                        todoHint = postponeTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onEdit = {
                                    editingTaskId = task.id
                                    todoHint = "正在编辑 ${task.title}"
                                },
                                onBindPomodoro = {
                                    focusPrefs.edit()
                                        .remove("bound_task_title")
                                        .remove("bound_task_list")
                                        .apply()
                                    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                    todoHint = "已绑定到番茄钟：${task.title}"
                                },
                                onNotify = {
                                    sendAppNotification(context, "待办提醒", "${task.title} · ${task.dueText}")
                                    todoHint = "已发送提醒：${task.title}"
                                },
                                onToggleSubtask = { subtaskIndex ->
                                    if (taskIndex >= 0 && subtaskIndex in tasks[taskIndex].subtasks.indices) {
                                        val subtasks = tasks[taskIndex].subtasks.toMutableList()
                                        subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
                                        tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onMoveUp = {},
                                onMoveDown = {},
                                canMoveUp = false,
                                canMoveDown = false,
                            )
                            if (index != entry.second.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (groupIndex != calendarGroups.lastIndex) Spacer(modifier = Modifier.height(14.dp))
                    }
                }
                if (archivedTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("完成归档", style = MaterialTheme.typography.titleMedium, color = PineInk)
                        ActionPill("${archivedTasks.size}", MossGreen, onClick = {})
                        ActionPill(if (archiveExpanded) "收起" else "展开", WarmMist) { archiveExpanded = !archiveExpanded }
                        ActionPill("清空已完成", CloudWhite) {
                            val before = tasks.size
                            tasks.removeAll { it.done }
                            saveTodoTasks(prefs, tasks)
                            archiveExpanded = false
                            todoHint = "已清空 ${before - tasks.size} 条已完成任务。"
                        }
                    }
                    if (archiveExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        archivedTasks.forEachIndexed { index, task ->
                            TodoTaskCard(
                                task = task,
                                onToggle = {
                                    val taskIndex = tasks.indexOfFirst { it.id == task.id }
                                    if (taskIndex >= 0) {
                                        todoHint = toggleTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onPostpone = {},
                                onEdit = {
                                    editingTaskId = task.id
                                    todoHint = "正在编辑 ${task.title}"
                                },
                                onBindPomodoro = {
                                    focusPrefs.edit()
                                        .remove("bound_task_title")
                                        .remove("bound_task_list")
                                        .apply()
                                    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                    todoHint = "已绑定到番茄钟：${task.title}"
                                },
                                onNotify = {
                                    sendAppNotification(context, "待办提醒", "${task.title} · ${task.dueText}")
                                    todoHint = "已发送提醒：${task.title}"
                                },
                                onToggleSubtask = { subtaskIndex ->
                                    val taskIndex = tasks.indexOfFirst { it.id == task.id }
                                    if (taskIndex >= 0 && subtaskIndex in tasks[taskIndex].subtasks.indices) {
                                        val subtasks = tasks[taskIndex].subtasks.toMutableList()
                                        subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
                                        tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onMoveUp = {},
                                onMoveDown = {},
                                canMoveUp = false,
                                canMoveDown = false,
                            )
                            if (index != archivedTasks.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("清单统计", style = MaterialTheme.typography.titleMedium, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    tasks.groupBy { it.listName.ifBlank { "未分组" } }.forEach { (name, grouped) ->
                        MetricCard(name, "${grouped.count { !it.done }}/${grouped.size}", if (grouped.any { !it.done }) ForestGreen else MossGreen)
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun TodoTaskCard(
    task: TodoTask,
    onToggle: () -> Unit,
    onPostpone: () -> Unit,
    onEdit: () -> Unit,
    onBindPomodoro: () -> Unit,
    onNotify: () -> Unit,
    onToggleSubtask: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
) {
    val dueStatus = remember(task.dueText, task.done) { todoDueStatus(task) }
    val focusProgressLabel = if (task.focusGoal > 0) "专注 ${task.focusCount}/${task.focusGoal} 轮" else "专注 ${task.focusCount} 轮"
    val focusGoalReached = task.focusGoal > 0 && task.focusCount >= task.focusGoal
    Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.56f)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.fillMaxWidth(0.78f)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Text(task.note.ifBlank { "无补充说明" }, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                }
                ActionPill(if (task.done) "已完成" else "完成", if (task.done) MossGreen else ForestGreen, onClick = onToggle)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${task.listName} · ${task.dueText}", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
                ActionPill(dueStatus.label, dueStatus.color, onClick = {})
            }
            Text("${task.reminderText.ifBlank { "不提醒" }} · ${task.repeatText}", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionPill(task.priority.title, when (task.priority) {
                    TodoPriority.High -> Ginkgo
                    TodoPriority.Medium -> TeaGreen
                    TodoPriority.Low -> WarmMist
                }, onClick = {})
                ActionPill(task.quadrant.title, TeaGreen, onClick = {})
                ActionPill(focusProgressLabel, if (focusGoalReached) MossGreen else BambooGlass, onClick = {})
                if (!task.done) ActionPill("顺延", CloudWhite, onClick = onPostpone)
                ActionPill("编辑", WarmMist, onClick = onEdit)
                ActionPill("绑定专注", ForestGreen, onClick = onBindPomodoro)
                ActionPill("提醒", Ginkgo, onClick = onNotify)
                if (canMoveUp) ActionPill("上移", CloudWhite, onClick = onMoveUp)
                if (canMoveDown) ActionPill("下移", CloudWhite, onClick = onMoveDown)
            }
            if (task.focusGoal > 0) {
                Text(
                    if (focusGoalReached) "本任务的专注目标已完成。" else "距离专注目标还差 ${task.focusGoal - task.focusCount} 轮。",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.72f),
                )
            }
            if (task.tags.isNotEmpty()) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    task.tags.forEach { tag ->
                        ActionPill(tag, Ginkgo, onClick = {})
                    }
                }
            }
            if (task.subtasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    task.subtasks.forEachIndexed { index, subtask ->
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.34f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ForestDeep.copy(alpha = if (subtask.done) 0.56f else 0.82f),
                                    modifier = Modifier.weight(1f),
                                )
                                ActionPill(
                                    text = if (subtask.done) "已完成" else "完成",
                                    background = if (subtask.done) MossGreen else ForestGreen,
                                    onClick = { onToggleSubtask(index) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun loadTodoTasks(prefs: android.content.SharedPreferences): List<TodoTask> {
    val raw = prefs.getString("todo_tasks", null) ?: return emptyList()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    TodoTask(
                        id = item.optString("id").ifBlank { "todo-$index" },
                        title = item.optString("title").ifBlank { "未命名任务" },
                        note = item.optString("note"),
                        quadrant = runCatching {
                            TodoQuadrant.valueOf(item.optString("quadrant", TodoQuadrant.ImportantNotUrgent.name))
                        }.getOrDefault(TodoQuadrant.ImportantNotUrgent),
                        priority = runCatching {
                            TodoPriority.valueOf(item.optString("priority", TodoPriority.Medium.name))
                        }.getOrDefault(TodoPriority.Medium),
                        dueText = item.optString("dueText"),
                        tags = buildList {
                            val tagsArray = item.optJSONArray("tags") ?: JSONArray()
                            for (tagIndex in 0 until tagsArray.length()) {
                                tagsArray.optString(tagIndex).takeIf(String::isNotBlank)?.let(::add)
                            }
                        },
                        listName = item.optString("listName", "收集箱"),
                        reminderText = item.optString("reminderText"),
                        repeatText = item.optString("repeatText", "不重复"),
                        subtasks = buildList {
                            val subtasksArray = item.optJSONArray("subtasks") ?: JSONArray()
                            for (subtaskIndex in 0 until subtasksArray.length()) {
                                val subtaskItem = subtasksArray.optJSONObject(subtaskIndex) ?: continue
                                val title = subtaskItem.optString("title").trim()
                                if (title.isBlank()) continue
                                add(
                                    TodoSubtask(
                                        title = title,
                                        done = subtaskItem.optBoolean("done"),
                                    ),
                                )
                            }
                        },
                        done = item.optBoolean("done"),
                        focusCount = item.optInt("focusCount"),
                        focusGoal = item.optInt("focusGoal"),
                    ),
                )
            }
        }
    }.getOrElse { emptyList() }
}

private fun saveTodoTasks(
    prefs: android.content.SharedPreferences,
    tasks: List<TodoTask>,
) {
    val array = JSONArray()
    tasks.forEach { task ->
        array.put(
            JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("note", task.note)
                put("quadrant", task.quadrant.name)
                put("priority", task.priority.name)
                put("dueText", task.dueText)
                put("tags", JSONArray(task.tags))
                put("listName", task.listName)
                put("reminderText", task.reminderText)
                put("repeatText", task.repeatText)
                put(
                    "subtasks",
                    JSONArray().apply {
                        task.subtasks.forEach { subtask ->
                            put(
                                JSONObject().apply {
                                    put("title", subtask.title)
                                    put("done", subtask.done)
                                },
                            )
                        }
                    },
                )
                put("focusCount", task.focusCount)
                put("focusGoal", task.focusGoal)
                put("done", task.done)
            },
        )
    }
    prefs.edit().putString("todo_tasks", array.toString()).apply()
}

private fun loadTodoDraft(prefs: android.content.SharedPreferences): TodoDraft? {
    val raw = prefs.getString("todo_draft_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val item = JSONObject(raw)
        TodoDraft(
            editingTaskId = item.optString("editingTaskId").ifBlank { null },
            title = item.optString("title"),
            note = item.optString("note"),
            duePreset = runCatching { TodoDuePreset.valueOf(item.optString("duePreset", TodoDuePreset.Today.name)) }.getOrDefault(TodoDuePreset.Today),
            dueClock = item.optString("dueClock", "21:00"),
            listName = item.optString("listName", "收集箱"),
            reminderPreset = runCatching {
                TodoReminderPreset.valueOf(item.optString("reminderPreset", TodoReminderPreset.Before30Min.name))
            }.getOrDefault(TodoReminderPreset.Before30Min),
            repeatText = item.optString("repeatText", "不重复"),
            quadrant = runCatching {
                TodoQuadrant.valueOf(item.optString("quadrant", TodoQuadrant.ImportantNotUrgent.name))
            }.getOrDefault(TodoQuadrant.ImportantNotUrgent),
            priority = runCatching {
                TodoPriority.valueOf(item.optString("priority", TodoPriority.Medium.name))
            }.getOrDefault(TodoPriority.Medium),
            focusGoal = item.optInt("focusGoal"),
            tagDraft = item.optString("tagDraft"),
            tags = buildList {
                val tagsArray = item.optJSONArray("tags") ?: JSONArray()
                for (index in 0 until tagsArray.length()) add(tagsArray.optString(index))
            },
            subtaskDraft = item.optString("subtaskDraft"),
            subtasks = buildList {
                val subtasksArray = item.optJSONArray("subtasks") ?: JSONArray()
                for (index in 0 until subtasksArray.length()) {
                    val subtask = subtasksArray.optJSONObject(index) ?: continue
                    add(TodoSubtask(subtask.optString("title"), subtask.optBoolean("done")))
                }
            },
        )
    }.getOrNull()
}

private fun saveTodoDraft(
    prefs: android.content.SharedPreferences,
    draft: TodoDraft,
) {
    prefs.edit()
        .putString(
            "todo_draft_v1",
            JSONObject().apply {
                put("editingTaskId", draft.editingTaskId)
                put("title", draft.title)
                put("note", draft.note)
                put("duePreset", draft.duePreset.name)
                put("dueClock", draft.dueClock)
                put("listName", draft.listName)
                put("reminderPreset", draft.reminderPreset.name)
                put("repeatText", draft.repeatText)
                put("quadrant", draft.quadrant.name)
                put("priority", draft.priority.name)
                put("focusGoal", draft.focusGoal)
                put("tagDraft", draft.tagDraft)
                put("tags", JSONArray(draft.tags))
                put("subtaskDraft", draft.subtaskDraft)
                put(
                    "subtasks",
                    JSONArray().apply {
                        draft.subtasks.forEach { subtask ->
                            put(
                                JSONObject().apply {
                                    put("title", subtask.title)
                                    put("done", subtask.done)
                                },
                            )
                        }
                    },
                )
            }.toString(),
        )
        .apply()
}

private fun clearTodoDraft(prefs: android.content.SharedPreferences) {
    prefs.edit().remove("todo_draft_v1").apply()
}

private fun <T> MutableList<T>.swap(left: Int, right: Int) {
    if (left !in indices || right !in indices || left == right) return
    val temp = this[left]
    this[left] = this[right]
    this[right] = temp
}

private data class TodoDueStatus(
    val label: String,
    val color: Color,
)

private val TodoDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private fun buildTodoDueText(
    preset: TodoDuePreset,
    clock: String,
): String {
    val normalizedClock = clock.trim()
    return if (normalizedClock.isBlank()) preset.title else "${preset.title} $normalizedClock"
}

private fun inferTodoDuePreset(dueText: String): TodoDuePreset {
    return when {
        dueText.contains("今晚") -> TodoDuePreset.Tonight
        dueText.contains("明天") -> TodoDuePreset.Tomorrow
        dueText.contains("本周") -> TodoDuePreset.ThisWeek
        dueText.contains("下周") -> TodoDuePreset.NextWeek
        else -> TodoDuePreset.Today
    }
}

private fun extractTodoDueClock(dueText: String): String {
    val parts = dueText.split(" ")
    return parts.getOrNull(1) ?: "21:00"
}

private fun inferTodoReminderPreset(reminderText: String): TodoReminderPreset {
    return TodoReminderPreset.entries.firstOrNull { it.title == reminderText } ?: TodoReminderPreset.Before30Min
}

private fun todoDueStatus(task: TodoTask): TodoDueStatus {
    if (task.done) return TodoDueStatus("已完成", MossGreen)
    val now = LocalDateTime.now()
    val dueAt = parseTodoDateTime(task.dueText, now)
    val isGoalTask = task.focusGoal > 0 && task.focusCount < task.focusGoal
    if (dueAt != null) {
        val today = now.toLocalDate()
        val dueDate = dueAt.toLocalDate()
        val thisWeekEnd = today.plusDays((7 - today.dayOfWeek.value).coerceAtLeast(0).toLong())
        return when {
            dueAt.isBefore(now.minusMinutes(1)) -> if (isGoalTask) TodoDueStatus("目标超期", Ginkgo) else TodoDueStatus("已逾期", Ginkgo)
            dueDate == today -> if (isGoalTask) TodoDueStatus("目标临期", Ginkgo) else TodoDueStatus("临近截止", Ginkgo)
            dueDate == today.plusDays(1) -> if (isGoalTask) TodoDueStatus("目标将至", TeaGreen) else TodoDueStatus("即将到期", TeaGreen)
            !dueDate.isAfter(thisWeekEnd) -> if (isGoalTask) TodoDueStatus("目标推进中", ForestGreen) else TodoDueStatus("本周处理", ForestGreen)
            else -> if (isGoalTask) TodoDueStatus("目标推进中", ForestGreen) else TodoDueStatus("已安排", WarmMist)
        }
    }
    if (isGoalTask) {
        return when {
            task.dueText.contains("今天") || task.dueText.contains("今晚") -> TodoDueStatus("目标临期", Ginkgo)
            task.dueText.contains("明天") || task.dueText.contains("明晚") -> TodoDueStatus("目标将至", TeaGreen)
            else -> TodoDueStatus("目标推进中", ForestGreen)
        }
    }
    return when {
        task.dueText.contains("今天") || task.dueText.contains("今晚") -> TodoDueStatus("临近截止", Ginkgo)
        task.dueText.contains("明天") || task.dueText.contains("明晚") -> TodoDueStatus("即将到期", TeaGreen)
        task.dueText.contains("周") || task.dueText.contains("本周") -> TodoDueStatus("本周处理", ForestGreen)
        else -> TodoDueStatus("已安排", WarmMist)
    }
}

private fun isRepeatingTodo(repeatText: String): Boolean {
    val normalized = repeatText.trim()
    return normalized.isNotBlank() && normalized != "不重复"
}

private fun formatTodoDateTime(dateTime: LocalDateTime): String = dateTime.format(TodoDateTimeFormatter)

private fun nextRepeatedDueAt(
    repeatText: String,
    currentDueAt: LocalDateTime,
): LocalDateTime? {
    val normalized = repeatText.trim()
    return when {
        normalized == "不重复" || normalized.isBlank() -> null
        normalized.contains("工作日") -> {
            var candidate = currentDueAt.plusDays(1)
            while (candidate.dayOfWeek.value >= 6) {
                candidate = candidate.plusDays(1)
            }
            candidate
        }
        normalized.contains("每2天") || normalized.contains("每两天") -> currentDueAt.plusDays(2)
        normalized.contains("每天") || normalized.contains("每日") -> currentDueAt.plusDays(1)
        normalized.contains("每2周") || normalized.contains("每两周") -> currentDueAt.plusWeeks(2)
        normalized.contains("每周") -> currentDueAt.plusWeeks(1)
        normalized.contains("每月") -> currentDueAt.plusMonths(1)
        normalized.contains("每年") -> currentDueAt.plusYears(1)
        else -> null
    }
}

private fun advanceTodoReminderText(
    reminderText: String,
    currentDueAt: LocalDateTime,
    nextDueAt: LocalDateTime,
    now: LocalDateTime,
): String {
    val trimmed = reminderText.trim()
    if (trimmed.isBlank() || trimmed == TodoReminderPreset.None.title) return trimmed
    if (TodoReminderPreset.entries.any { it.title == trimmed }) return trimmed
    val currentReminderAt = parseReminderDateTime(trimmed, currentDueAt, now) ?: return trimmed
    val offset = java.time.Duration.between(currentReminderAt, currentDueAt)
    if (offset.isNegative) return trimmed
    return formatTodoDateTime(nextDueAt.minus(offset))
}

private fun advanceRepeatingTodoTask(
    task: TodoTask,
    now: LocalDateTime = LocalDateTime.now(),
): TodoTask? {
    if (!isRepeatingTodo(task.repeatText)) return null
    val currentDueAt = parseTodoDateTime(task.dueText, now)
        ?: LocalDateTime.of(
            now.toLocalDate(),
            parseClock(Regex("""(\d{1,2}:\d{2})""").find(task.dueText)?.groupValues?.getOrNull(1) ?: extractTodoClockFallback(task.dueText))
                ?: LocalTime.of(21, 0),
        )
    val nextDueAt = nextRepeatedDueAt(task.repeatText, currentDueAt) ?: return null
    return task.copy(
        dueText = formatTodoDateTime(nextDueAt),
        reminderText = advanceTodoReminderText(task.reminderText, currentDueAt, nextDueAt, now),
        subtasks = task.subtasks.map { it.copy(done = false) },
        focusCount = 0,
        done = false,
    )
}

private fun postponeTodoTask(
    tasks: MutableList<TodoTask>,
    taskIndex: Int,
    now: LocalDateTime = LocalDateTime.now(),
): String {
    val task = tasks.getOrNull(taskIndex) ?: return ""
    if (task.done) return "已完成任务无需顺延：${task.title}"
    val currentDueAt = parseTodoDateTime(task.dueText, now)
        ?: LocalDateTime.of(
            now.toLocalDate(),
            parseClock(
                Regex("""(\d{1,2}:\d{2})""").find(task.dueText)?.groupValues?.getOrNull(1)
                    ?: extractTodoClockFallback(task.dueText),
            ) ?: LocalTime.of(21, 0),
        )
    val nextDueAt = currentDueAt.plusDays(1)
    tasks[taskIndex] = task.copy(
        dueText = formatTodoDateTime(nextDueAt),
        reminderText = advanceTodoReminderText(task.reminderText, currentDueAt, nextDueAt, now),
    )
    return "已顺延任务：${task.title}"
}

private fun toggleTodoTask(
    tasks: MutableList<TodoTask>,
    taskIndex: Int,
    now: LocalDateTime = LocalDateTime.now(),
): String {
    val task = tasks.getOrNull(taskIndex) ?: return ""
    if (task.done) {
        tasks[taskIndex] = task.copy(done = false)
        return "已恢复任务：${task.title}"
    }
    val repeatedTask = advanceRepeatingTodoTask(task, now)
    return if (repeatedTask != null) {
        tasks[taskIndex] = repeatedTask
        "已续期重复任务：${task.title}"
    } else {
        tasks[taskIndex] = task.copy(done = true)
        "已完成任务：${task.title}"
    }
}

private fun recordTodoFocusProgress(
    prefs: android.content.SharedPreferences,
    boundTaskTitle: String,
) {
    if (boundTaskTitle.isBlank()) return
    val tasks = loadTodoTasks(prefs).toMutableList()
    val taskIndex = tasks.indexOfFirst { it.title == boundTaskTitle }
    if (taskIndex < 0) return
    val task = tasks[taskIndex]
    val subtasks = task.subtasks.toMutableList()
    val pendingIndex = subtasks.indexOfFirst { !it.done }
    if (pendingIndex >= 0) {
        subtasks[pendingIndex] = subtasks[pendingIndex].copy(done = true)
    }
    val updatedFocusCount = task.focusCount + 1
    val focusGoalReached = task.focusGoal <= 0 || updatedFocusCount >= task.focusGoal
    val updatedTask = task.copy(
        subtasks = subtasks,
        focusCount = updatedFocusCount,
        done = if (subtasks.isNotEmpty()) subtasks.all { it.done } && focusGoalReached else (task.done || focusGoalReached && task.focusGoal > 0),
    )
    tasks[taskIndex] = if (updatedTask.done) advanceRepeatingTodoTask(updatedTask) ?: updatedTask else updatedTask
    saveTodoTasks(prefs, tasks)
}

private fun loadStringList(
    prefs: android.content.SharedPreferences,
    key: String,
): List<String> {
    val raw = prefs.getString(key, "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}

private fun saveStringList(
    prefs: android.content.SharedPreferences,
    key: String,
    items: List<String>,
) {
    prefs.edit().putString(key, items.joinToString("|")).apply()
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

@Composable
private fun SearchResultRow(
    result: HomeSearchResult,
    onClick: () -> Unit,
) {
    val accent = when (result.category) {
        HomeSearchCategory.Course -> ForestGreen
        HomeSearchCategory.Note -> PineInk
        HomeSearchCategory.Todo -> Ginkgo
        HomeSearchCategory.Grade -> MossGreen
        HomeSearchCategory.Building -> TeaGreen
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PineInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${result.category.title} · ${result.subtitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForestDeep.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = result.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.64f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ActionPill("进入", accent, onClick = onClick)
        }
    }
}

private fun homeMetricSpacing(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 10.dp
        HomeModuleSize.Standard -> 12.dp
        HomeModuleSize.Hero -> 14.dp
    }

private fun homeSectionSpacing(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 8.dp
        HomeModuleSize.Standard -> 12.dp
        HomeModuleSize.Hero -> 14.dp
    }

private fun homeSecondarySpacing(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 6.dp
        HomeModuleSize.Standard -> 8.dp
        HomeModuleSize.Hero -> 10.dp
    }

private fun homeLineGap(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 8.dp
        HomeModuleSize.Standard -> 10.dp
        HomeModuleSize.Hero -> 12.dp
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

@Composable
private fun homeSectionTitleStyle(sizePreset: HomeModuleSize) =
    when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.titleMedium
        HomeModuleSize.Standard -> MaterialTheme.typography.titleLarge
        HomeModuleSize.Hero -> MaterialTheme.typography.headlineSmall
    }

@Composable
private fun homeSectionBodyStyle(sizePreset: HomeModuleSize) =
    when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.bodyMedium
        HomeModuleSize.Standard -> MaterialTheme.typography.bodyLarge
        HomeModuleSize.Hero -> MaterialTheme.typography.titleMedium
    }

private fun buildFocusDayStats(records: List<FocusRecord>): List<FocusDayStat> {
    val today = LocalDate.now()
    return (6 downTo 0).map { offset ->
        val target = today.minusDays(offset.toLong())
        val label = "${target.monthValue}.${target.dayOfMonth}"
        val minutes = records.filter { parseFocusRecordDate(it.finishedAt) == target }.map { it.seconds }.sum() / 60
        FocusDayStat(label = label, minutes = minutes)
    }
}

private fun buildFocusTaskStats(records: List<FocusRecord>): List<FocusTaskStat> {
    return records
        .groupBy { it.taskTitle.ifBlank { "未命名专注" } }
        .map { (title, items) ->
            FocusTaskStat(
                title = title,
                minutes = items.map { it.seconds }.sum() / 60,
                count = items.size,
            )
        }
        .sortedWith(compareByDescending<FocusTaskStat> { it.minutes }.thenByDescending { it.count })
}

private fun parseFocusRecordDate(raw: String): LocalDate? {
    if (raw.isBlank()) return null
    return runCatching {
        java.time.LocalDateTime.parse(raw, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
    }.recoverCatching {
        val currentYear = LocalDate.now().year
        java.time.LocalDateTime.parse(
            "$currentYear-$raw",
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        ).toLocalDate()
    }.getOrNull()
}

private fun loadFocusRecords(prefs: android.content.SharedPreferences): List<FocusRecord> {
    val raw = prefs.getString("focus_records", null) ?: return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    FocusRecord(
                        taskTitle = item.optString("taskTitle"),
                        modeTitle = item.optString("modeTitle"),
                        seconds = item.optInt("seconds"),
                        finishedAt = item.optString("finishedAt"),
                    ),
                )
            }
        }
    }.getOrElse { emptyList() }
}

private fun saveFocusRecords(
    prefs: android.content.SharedPreferences,
    records: List<FocusRecord>,
) {
    val array = JSONArray()
    records.forEach { record ->
        array.put(
            JSONObject().apply {
                put("taskTitle", record.taskTitle)
                put("modeTitle", record.modeTitle)
                put("seconds", record.seconds)
                put("finishedAt", record.finishedAt)
            },
        )
    }
    prefs.edit().putString("focus_records", array.toString()).apply()
}

private fun loadPomodoroSession(prefs: android.content.SharedPreferences): PomodoroSession? {
    val raw = prefs.getString("pomodoro_session_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val item = JSONObject(raw)
        val originalRunning = item.optBoolean("running")
        val savedAt = item.optLong("savedAt")
        val savedLeft = item.optInt("leftSeconds")
        val elapsedSeconds = if (originalRunning && savedAt > 0L) {
            ((System.currentTimeMillis() - savedAt) / 1000L).toInt().coerceAtLeast(0)
        } else {
            0
        }
        val restoredLeft = (savedLeft - elapsedSeconds).coerceAtLeast(0)
        PomodoroSession(
            mode = runCatching { PomodoroMode.valueOf(item.optString("mode", PomodoroMode.Focus.name)) }.getOrDefault(PomodoroMode.Focus),
            presetTitle = item.optString("presetTitle", "25 分钟"),
            presetSeconds = item.optInt("presetSeconds", 25 * 60).coerceAtLeast(1),
            leftSeconds = restoredLeft,
            running = originalRunning && restoredLeft > 0,
            autoNext = item.optBoolean("autoNext", true),
            strictMode = item.optBoolean("strictMode"),
            boundTask = item.optString("boundTask"),
            sound = item.optString("sound", "山风"),
            ambientOn = item.optBoolean("ambientOn"),
            focusMinutes = item.optInt("focusMinutes", 125),
            cycles = item.optInt("cycles", 4),
            customMinutes = item.optInt("customMinutes", 25),
            customSeconds = item.optInt("customSeconds"),
            onlyPendingGoalTasks = item.optBoolean("onlyPendingGoalTasks", true),
        )
    }.getOrNull()
}

private fun savePomodoroSession(
    prefs: android.content.SharedPreferences,
    session: PomodoroSession,
) {
    prefs.edit()
        .putString(
            "pomodoro_session_v1",
            JSONObject().apply {
                put("mode", session.mode.name)
                put("presetTitle", session.presetTitle)
                put("presetSeconds", session.presetSeconds)
                put("leftSeconds", session.leftSeconds)
                put("running", session.running)
                put("autoNext", session.autoNext)
                put("strictMode", session.strictMode)
                put("boundTask", session.boundTask)
                put("sound", session.sound)
                put("ambientOn", session.ambientOn)
                put("focusMinutes", session.focusMinutes)
                put("cycles", session.cycles)
                put("customMinutes", session.customMinutes)
                put("customSeconds", session.customSeconds)
                put("onlyPendingGoalTasks", session.onlyPendingGoalTasks)
                put("savedAt", System.currentTimeMillis())
            }.toString(),
        )
        .apply()
}

fun sendAppNotification(
    context: Context,
    title: String,
    content: String,
) {
    val channelId = "poxiao_local_notice"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "本地提醒", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
}

private data class ScheduledReminderPlan(
    val id: String,
    val title: String,
    val body: String,
    val triggerAtMillis: Long,
)

fun refreshLocalReminderSchedule(context: Context) {
    val schedulerPrefs = context.getSharedPreferences("reminder_scheduler", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val scheduleCachePrefs = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)
    val scheduleAuthPrefs = context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE)
    val examWeekPrefs = context.getSharedPreferences("schedule_exam_week", Context.MODE_PRIVATE)
    val preferenceState = loadNotificationPreferenceState(context)
    val now = System.currentTimeMillis()
    val plans = buildTodoReminderPlans(todoPrefs, preferenceState)
        .plus(buildCourseReminderPlans(scheduleAuthPrefs, scheduleCachePrefs, preferenceState))
        .plus(buildExamWeekReminderPlans(scheduleCachePrefs, scheduleAuthPrefs, examWeekPrefs, preferenceState))
        .filter { it.triggerAtMillis > now }
        .sortedBy { it.triggerAtMillis }
        .distinctBy { it.id }
        .take(48)
    val nextIds = plans.map { it.id }
    val nextSnapshot = plans.joinToString("|") { plan ->
        "${plan.id}@${plan.triggerAtMillis}@${plan.title.hashCode()}@${plan.body.hashCode()}"
    }
    val previousIds = loadStringList(schedulerPrefs, "scheduled_ids")
    val previousSnapshot = schedulerPrefs.getString("scheduled_snapshot", "").orEmpty()
    if (nextIds == previousIds && nextSnapshot == previousSnapshot) return
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    previousIds.forEach { id ->
        alarmManager.cancel(buildReminderPendingIntent(context, id, "", ""))
    }
    plans.forEach { plan ->
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            plan.triggerAtMillis,
            buildReminderPendingIntent(context, plan.id, plan.title, plan.body),
        )
    }
    saveStringList(schedulerPrefs, "scheduled_ids", nextIds)
    schedulerPrefs.edit().putString("scheduled_snapshot", nextSnapshot).apply()
}

private fun loadPrimaryScheduleState(
    primaryPrefs: android.content.SharedPreferences,
    fallbackPrefs: android.content.SharedPreferences,
): HitaScheduleUiState? {
    return loadCachedScheduleUiState(primaryPrefs) ?: loadCachedScheduleUiState(fallbackPrefs)
}

private fun loadPrimaryScheduleEvents(
    primaryPrefs: android.content.SharedPreferences,
    fallbackPrefs: android.content.SharedPreferences,
): List<ScheduleExtraEvent> {
    val primary = loadScheduleExtraEvents(primaryPrefs)
    return if (primary.isNotEmpty()) primary else loadScheduleExtraEvents(fallbackPrefs)
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

private data class PendingReviewBridgeItem(
    val id: String,
    val courseName: String,
    val noteTitle: String,
    val sourceTitle: String,
    val recommendedMinutes: Int,
    val nextReviewAt: Long,
)

private data class PendingReviewBridgePayload(
    val reason: String,
    val prompt: String,
    val items: List<PendingReviewBridgeItem>,
)

private data class ReviewBridgeExecutionSummary(
    val createdTaskIds: List<String>,
    val createdTaskTitles: List<String>,
    val linkedReviewItemIds: List<String>,
    val boundTaskTitle: String,
    val summary: String,
    val executedAt: Long,
    val replaySourceExecutedAt: Long = 0L,
    val diffSummary: String = "",
)

private fun loadPendingReviewBridge(
    prefs: android.content.SharedPreferences,
): PendingReviewBridgePayload? {
    val raw = prefs.getString("pending_review_plan_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val json = JSONObject(raw)
        val items = buildList {
            val array = json.optJSONArray("items") ?: JSONArray()
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    PendingReviewBridgeItem(
                        id = item.optString("id"),
                        courseName = item.optString("courseName"),
                        noteTitle = item.optString("noteTitle"),
                        sourceTitle = item.optString("sourceTitle"),
                        recommendedMinutes = item.optInt("recommendedMinutes", 25),
                        nextReviewAt = item.optLong("nextReviewAt"),
                    ),
                )
            }
        }
        PendingReviewBridgePayload(
            reason = json.optString("reason"),
            prompt = json.optString("prompt"),
            items = items,
        )
    }.getOrNull()
}

private fun shouldExecuteReviewBridge(
    prompt: String,
    summaries: List<AssistantContextSummary>,
): Boolean {
    val normalized = prompt.lowercase()
    val hasReviewBridge = summaries.any { it.id == "review_bridge" }
    return hasReviewBridge && (
        normalized.contains("接管复习") ||
            normalized.contains("安排今天复习") ||
            normalized.contains("执行复习") ||
            normalized.contains("开始复习计划") ||
            normalized.contains("把复习计划落地")
        )
}

private fun buildAssistantRequestPrompt(
    userInput: String,
    summaries: List<AssistantContextSummary>,
    availableTools: List<AssistantToolDefinition>,
    toolCall: AssistantToolCall?,
): String {
    return buildString {
        appendLine("你是“破晓”校园学习效率应用内的智能助理。")
        appendLine("请使用简体中文回答，优先基于给定的应用上下文提供直接、可执行的建议。")
        if (summaries.isNotEmpty()) {
            appendLine("【当前本地上下文】")
            summaries.sortedByDescending { it.priority }.take(6).forEach { summary ->
                appendLine("- ${summary.source}｜${summary.title}：${summary.body}")
            }
        }
        if (availableTools.isNotEmpty()) {
            appendLine("【当前可用的本地能力】")
            appendLine(availableTools.joinToString("、") { it.title })
        }
        toolCall?.let {
            appendLine("【刚执行的本地动作】")
            appendLine("- ${it.title}：${it.summary}")
        }
        appendLine("【用户问题】")
        append(userInput.trim())
    }.trim()
}

private fun resolveAssistantReply(
    remoteReply: String,
    fallback: String,
    toolCall: AssistantToolCall?,
): String {
    val trimmed = remoteReply.trim()
    if (trimmed.isBlank()) return fallback
    val remoteUnavailable = trimmed.startsWith("星火助手暂时不可用")
    return if (remoteUnavailable && toolCall != null) {
        "$fallback\n\n$trimmed"
    } else {
        trimmed
    }
}

private fun applyReviewBridgeExecution(
    context: Context,
): String {
    val bridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE)
    val payload = loadPendingReviewBridge(bridgePrefs) ?: return "当前没有可执行的复习桥接计划。"
    val tasks = loadTodoTasks(todoPrefs).toMutableList()
    val createdTaskIds = mutableListOf<String>()
    val createdTaskTitles = mutableListOf<String>()
    val executedAt = System.currentTimeMillis()
    payload.items.take(3).forEach { item ->
        val dueDate = runCatching {
            Instant.ofEpochMilli(item.nextReviewAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        }.getOrNull()
        val dueText = dueDate?.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) ?: "今天 20:00"
        val task = TodoTask(
            id = "assistant-review-${item.id}",
            title = "复习：${item.noteTitle}",
            note = "${item.courseName} · 来源《${item.sourceTitle}》\n由智能体接管复习计划生成。",
            quadrant = TodoQuadrant.ImportantNotUrgent,
            priority = TodoPriority.High,
            dueText = dueText,
            tags = listOf("复习", "智能排程", item.courseName),
            listName = "复习计划",
            reminderText = "提前 30 分钟",
            repeatText = "不重复",
            subtasks = listOf(
                TodoSubtask("回看知识点"),
                TodoSubtask("完成一轮口述或默写"),
            ),
            focusGoal = when {
                item.recommendedMinutes >= 50 -> 3
                item.recommendedMinutes >= 30 -> 2
                else -> 1
            },
        )
        if (tasks.none { it.id == task.id }) {
            tasks.add(0, task)
            createdTaskIds += task.id
            createdTaskTitles += task.title
        }
    }
    saveTodoTasks(todoPrefs, tasks)
    val boundTaskTitle = payload.items.firstOrNull()?.let { "复习：${it.noteTitle}" }.orEmpty()
    payload.items.firstOrNull()?.let { first ->
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "复习：${first.noteTitle}")
        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "复习计划")
    }
    val summaryText = "已生成 ${payload.items.take(3).size} 条复习待办，并把首项绑定到番茄钟。"
    updateReviewExecutionLinks(
        context = context,
        reviewItemIds = payload.items.take(3).map { it.id },
        executedAt = executedAt,
    )
    val executionJson = JSONObject().apply {
        put("createdTaskIds", JSONArray(createdTaskIds))
        put("createdTaskTitles", JSONArray(createdTaskTitles))
        put("linkedReviewItemIds", JSONArray(payload.items.take(3).map { it.id }))
        put("boundTaskTitle", boundTaskTitle)
        put("summary", summaryText)
        put("executedAt", executedAt)
    }
    val historyArray = runCatching {
        JSONArray(bridgePrefs.getString("execution_history_v1", "").orEmpty().ifBlank { "[]" })
    }.getOrDefault(JSONArray())
    historyArray.put(0, executionJson)
    while (historyArray.length() > 8) {
        historyArray.remove(historyArray.length() - 1)
    }
    bridgePrefs.edit()
        .putString("last_executed_review_plan_v1", payload.prompt)
        .putString("last_execution_summary_v1", executionJson.toString())
        .putString("execution_history_v1", historyArray.toString())
        .putLong("last_executed_at_v1", System.currentTimeMillis())
        .apply()
    return summaryText
}

private fun replayReviewBridgeExecution(
    context: Context,
    execution: ReviewBridgeExecutionSummary,
): String {
    val bridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE)
    if (execution.createdTaskTitles.isEmpty()) return "这条历史没有可回放的复习待办。"
    val tasks = loadTodoTasks(todoPrefs).toMutableList()
    val replayTaskIds = mutableListOf<String>()
    val replayTaskTitles = mutableListOf<String>()
    val replayReviewItemIds = resolveReviewExecutionItemIds(context, execution)
    val replayExecutedAt = System.currentTimeMillis()
    execution.createdTaskTitles.forEachIndexed { index, title ->
        val replayId = "assistant-review-replay-${execution.executedAt}-$index"
        if (tasks.none { it.id == replayId }) {
            val task = TodoTask(
                id = replayId,
                title = title,
                note = "根据历史接管记录回放生成。\n来源执行时间：${
                    formatSyncTime(
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(execution.executedAt),
                            java.time.ZoneId.systemDefault(),
                        ),
                    )
                }",
                quadrant = TodoQuadrant.ImportantNotUrgent,
                priority = TodoPriority.High,
                dueText = "今天 20:00",
                tags = listOf("复习", "历史回放"),
                listName = "复习计划",
                reminderText = "提前 30 分钟",
                repeatText = "不重复",
                subtasks = listOf(
                    TodoSubtask("回看知识点"),
                    TodoSubtask("完成一轮口述或默写"),
                ),
                focusGoal = 1,
            )
            tasks.add(0, task)
            replayTaskIds += task.id
            replayTaskTitles += task.title
        }
    }
    saveTodoTasks(todoPrefs, tasks)
    if (execution.boundTaskTitle.isNotBlank()) {
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", execution.boundTaskTitle)
        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "复习计划")
    }
    val replaySummaryText = "已从历史回放 ${replayTaskTitles.size} 条复习待办，并恢复番茄绑定。"
    updateReviewExecutionLinks(
        context = context,
        reviewItemIds = replayReviewItemIds,
        executedAt = replayExecutedAt,
    )
    val diffSummary = buildReviewReplayDiffSummary(
        sourceTitles = execution.createdTaskTitles,
        replayTitles = replayTaskTitles,
        sourceBoundTitle = execution.boundTaskTitle,
        replayBoundTitle = execution.boundTaskTitle,
    )
    val replayJson = JSONObject().apply {
        put("createdTaskIds", JSONArray(replayTaskIds))
        put("createdTaskTitles", JSONArray(replayTaskTitles))
        put("linkedReviewItemIds", JSONArray(replayReviewItemIds))
        put("boundTaskTitle", execution.boundTaskTitle)
        put("summary", replaySummaryText)
        put("executedAt", replayExecutedAt)
        put("replaySourceExecutedAt", execution.executedAt)
        put("diffSummary", diffSummary)
    }
    val historyArray = runCatching {
        JSONArray(bridgePrefs.getString("execution_history_v1", "").orEmpty().ifBlank { "[]" })
    }.getOrDefault(JSONArray())
    historyArray.put(0, replayJson)
    while (historyArray.length() > 8) {
        historyArray.remove(historyArray.length() - 1)
    }
    bridgePrefs.edit()
        .putString("last_execution_summary_v1", replayJson.toString())
        .putString("execution_history_v1", historyArray.toString())
        .putLong("last_executed_at_v1", System.currentTimeMillis())
        .apply()
    return replaySummaryText
}

private fun loadReviewBridgeExecutionSummary(
    prefs: android.content.SharedPreferences,
): ReviewBridgeExecutionSummary? {
    val raw = prefs.getString("last_execution_summary_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val json = JSONObject(raw)
        ReviewBridgeExecutionSummary(
            createdTaskIds = buildList {
                val array = json.optJSONArray("createdTaskIds") ?: JSONArray()
                for (index in 0 until array.length()) {
                    val id = array.optString(index)
                    if (id.isNotBlank()) add(id)
                }
            },
            createdTaskTitles = buildList {
                val array = json.optJSONArray("createdTaskTitles") ?: JSONArray()
                for (index in 0 until array.length()) {
                    val title = array.optString(index)
                    if (title.isNotBlank()) add(title)
                }
            },
            linkedReviewItemIds = buildList {
                val array = json.optJSONArray("linkedReviewItemIds") ?: JSONArray()
                for (index in 0 until array.length()) {
                    val id = array.optString(index)
                    if (id.isNotBlank()) add(id)
                }
            },
            boundTaskTitle = json.optString("boundTaskTitle"),
            summary = json.optString("summary"),
            executedAt = json.optLong("executedAt"),
            replaySourceExecutedAt = json.optLong("replaySourceExecutedAt"),
            diffSummary = json.optString("diffSummary"),
        )
    }.getOrNull()
}

private fun undoReviewBridgeExecution(
    context: Context,
): String {
    val bridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE)
    val summary = loadReviewBridgeExecutionSummary(bridgePrefs) ?: return "当前没有可撤销的复习计划执行结果。"
    if (summary.createdTaskIds.isEmpty()) return "当前没有可撤销的复习待办。"
    val tasks = loadTodoTasks(todoPrefs).filterNot { it.id in summary.createdTaskIds }
    saveTodoTasks(todoPrefs, tasks)
    val currentBound = SecurePrefs.getString(focusPrefs, "bound_task_title_secure", "bound_task_title")
    if (summary.boundTaskTitle.isNotBlank() && currentBound == summary.boundTaskTitle) {
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "")
        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "")
    }
    clearReviewExecutionLinks(context, summary.executedAt)
    bridgePrefs.edit()
        .remove("last_execution_summary_v1")
        .apply()
    return "已撤销上一次复习计划执行，移除了 ${summary.createdTaskIds.size} 条待办，并清除了对应番茄绑定。"
}

private fun loadReviewBridgeExecutionHistory(
    prefs: android.content.SharedPreferences,
): List<ReviewBridgeExecutionSummary> {
    val raw = prefs.getString("execution_history_v1", "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                add(
                    ReviewBridgeExecutionSummary(
                        createdTaskIds = buildList {
                            val ids = json.optJSONArray("createdTaskIds") ?: JSONArray()
                            for (idIndex in 0 until ids.length()) {
                                val id = ids.optString(idIndex)
                                if (id.isNotBlank()) add(id)
                            }
                        },
                        createdTaskTitles = buildList {
                            val titles = json.optJSONArray("createdTaskTitles") ?: JSONArray()
                            for (titleIndex in 0 until titles.length()) {
                                val title = titles.optString(titleIndex)
                                if (title.isNotBlank()) add(title)
                            }
                        },
                        linkedReviewItemIds = buildList {
                            val ids = json.optJSONArray("linkedReviewItemIds") ?: JSONArray()
                            for (idIndex in 0 until ids.length()) {
                                val id = ids.optString(idIndex)
                                if (id.isNotBlank()) add(id)
                            }
                        },
                        boundTaskTitle = json.optString("boundTaskTitle"),
                        summary = json.optString("summary"),
                        executedAt = json.optLong("executedAt"),
                        replaySourceExecutedAt = json.optLong("replaySourceExecutedAt"),
                        diffSummary = json.optString("diffSummary"),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun buildReviewReplayDiffSummary(
    sourceTitles: List<String>,
    replayTitles: List<String>,
    sourceBoundTitle: String,
    replayBoundTitle: String,
): String {
    val sameTasks = sourceTitles.intersect(replayTitles.toSet()).size
    val missingTasks = sourceTitles.size - sameTasks
    val addedTasks = replayTitles.size - sameTasks
    val bindingText = when {
        sourceBoundTitle.isBlank() && replayBoundTitle.isBlank() -> "未恢复番茄绑定"
        sourceBoundTitle == replayBoundTitle -> "已恢复原番茄绑定"
        else -> "番茄绑定已调整"
    }
    return buildString {
        append("与原方案相比：")
        append("复用了 $sameTasks 项")
        if (addedTasks > 0) append("，新增 $addedTasks 项")
        if (missingTasks > 0) append("，缺少 $missingTasks 项")
        append("，$bindingText。")
    }
}

private fun updateReviewExecutionLinks(
    context: Context,
    reviewItemIds: List<String>,
    executedAt: Long,
) {
    if (reviewItemIds.isEmpty() || executedAt <= 0L) return
    val store = ReviewPlannerStore(context)
    val updated = store.loadItems().map { item ->
        if (item.id in reviewItemIds) {
            item.copy(assistantExecutionAt = executedAt)
        } else {
            item
        }
    }
    store.saveItems(updated)
}

private fun clearReviewExecutionLinks(
    context: Context,
    executedAt: Long,
) {
    if (executedAt <= 0L) return
    val store = ReviewPlannerStore(context)
    val updated = store.loadItems().map { item ->
        if (item.assistantExecutionAt == executedAt) item.copy(assistantExecutionAt = 0L) else item
    }
    store.saveItems(updated)
}

private fun resolveReviewExecutionItemIds(
    context: Context,
    execution: ReviewBridgeExecutionSummary,
): List<String> {
    if (execution.linkedReviewItemIds.isNotEmpty()) return execution.linkedReviewItemIds
    val reviewItems = ReviewPlannerStore(context).loadItems()
    val titles = execution.createdTaskTitles.map { it.removePrefix("复习：").trim() }.filter { it.isNotBlank() }.toSet()
    return reviewItems.filter { it.noteTitle in titles }.map { it.id }
}

private fun buildReminderPendingIntent(
    context: Context,
    reminderId: String,
    title: String,
    body: String,
): PendingIntent {
    val intent = Intent(context, LocalReminderReceiver::class.java).apply {
        putExtra("id", reminderId)
        putExtra("title", title)
        putExtra("body", body)
    }
    return PendingIntent.getBroadcast(
        context,
        reminderId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun buildTodoReminderPlans(
    prefs: android.content.SharedPreferences,
    preferenceState: com.poxiao.app.settings.NotificationPreferenceState,
): List<ScheduledReminderPlan> {
    if (!preferenceState.todoEnabled) return emptyList()
    if (!prefs.contains("todo_tasks")) return emptyList()
    val now = LocalDateTime.now()
    return loadTodoTasks(prefs)
        .filterNot { it.done }
        .mapNotNull { task ->
            val dueAt = parseTodoDateTime(task.dueText, now) ?: return@mapNotNull null
            val remindAt = parseReminderDateTime(task.reminderText, dueAt, now) ?: return@mapNotNull null
            ScheduledReminderPlan(
                id = "todo_${task.id}",
                title = "待办提醒",
                body = "${task.title} · ${task.dueText}",
                triggerAtMillis = remindAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            )
        }
}

private fun buildCourseReminderPlans(
    authPrefs: android.content.SharedPreferences,
    fallbackPrefs: android.content.SharedPreferences,
    preferenceState: com.poxiao.app.settings.NotificationPreferenceState,
): List<ScheduledReminderPlan> {
    if (!preferenceState.courseEnabled) return emptyList()
    val state = loadPrimaryScheduleState(authPrefs, fallbackPrefs) ?: return emptyList()
    val now = LocalDateTime.now()
    return state.weekSchedule.courses.mapNotNull { course ->
        val day = state.weekSchedule.days.firstOrNull { it.weekDay == course.dayOfWeek } ?: return@mapNotNull null
        val slot = state.weekSchedule.timeSlots.firstOrNull { it.majorIndex == course.majorIndex } ?: return@mapNotNull null
        val classDate = runCatching { LocalDate.parse(day.fullDate.ifBlank { day.date }) }.getOrNull() ?: return@mapNotNull null
        val startClock = slot.timeRange.substringBefore("-").trim()
        val startTime = parseClock(startClock) ?: return@mapNotNull null
        val classTime = LocalDateTime.of(classDate, startTime)
        val remindAt = classTime.minusMinutes(preferenceState.courseLeadMinutes.toLong())
        if (remindAt.isBefore(now)) return@mapNotNull null
        ScheduledReminderPlan(
            id = "course_${day.fullDate}_${course.dayOfWeek}_${course.majorIndex}_${course.courseName.hashCode()}",
            title = "课前提醒",
            body = "${course.courseName} · ${slot.label} · ${course.classroom.ifBlank { "教室待补充" }}",
            triggerAtMillis = remindAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
    }
}

private fun buildExamWeekReminderPlans(
    scheduleCachePrefs: android.content.SharedPreferences,
    scheduleAuthPrefs: android.content.SharedPreferences,
    examWeekPrefs: android.content.SharedPreferences,
    preferenceState: com.poxiao.app.settings.NotificationPreferenceState,
): List<ScheduledReminderPlan> {
    if (!preferenceState.examEnabled) return emptyList()
    val state = loadPrimaryScheduleState(scheduleAuthPrefs, scheduleCachePrefs) ?: return emptyList()
    val items = buildExamWeekItems(
        state.weekSchedule,
        loadPrimaryScheduleEvents(scheduleAuthPrefs, scheduleCachePrefs),
        loadStringList(examWeekPrefs, "completed_ids"),
    )
    val now = LocalDateTime.now()
    return items.filterNot { it.finished }.mapNotNull { item ->
        val date = runCatching { LocalDate.parse(item.date) }.getOrNull() ?: return@mapNotNull null
        val remindAt = when (preferenceState.examPreset) {
            "当天早 08:00" -> LocalDateTime.of(date, LocalTime.of(8, 0))
            "提前 3 小时" -> LocalDateTime.of(date, LocalTime.of(9, 0))
            else -> LocalDateTime.of(date.minusDays(1), LocalTime.of(20, 0))
        }
        if (remindAt.isBefore(now)) return@mapNotNull null
        ScheduledReminderPlan(
            id = "exam_${item.id}",
            title = "考试周提醒",
            body = "${item.title} · ${item.countdownLabel}",
            triggerAtMillis = remindAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
    }
}

private fun parseTodoDateTime(
    text: String,
    now: LocalDateTime,
): LocalDateTime? {
    val trimmed = text.trim()
    val explicitDate = Regex("""(\d{4}-\d{2}-\d{2})""").find(trimmed)?.groupValues?.getOrNull(1)
    val clock = parseClock(Regex("""(\d{1,2}:\d{2})""").find(trimmed)?.groupValues?.getOrNull(1) ?: extractTodoClockFallback(trimmed))
        ?: LocalTime.of(21, 0)
    val date = when {
        explicitDate != null -> runCatching { LocalDate.parse(explicitDate) }.getOrNull()
        trimmed.contains("今晚") || trimmed.contains("今天") -> now.toLocalDate()
        trimmed.contains("明晚") || trimmed.contains("明天") -> now.toLocalDate().plusDays(1)
        trimmed.contains("本周") -> now.toLocalDate().plusDays((7 - now.dayOfWeek.value).coerceAtLeast(0).toLong())
        trimmed.contains("下周") -> now.toLocalDate().plusDays(7)
        trimmed.contains("周末") -> now.toLocalDate().plusDays((6 - now.dayOfWeek.value).coerceAtLeast(0).toLong())
        else -> now.toLocalDate()
    } ?: return null
    return LocalDateTime.of(date, clock)
}

private fun parseReminderDateTime(
    reminderText: String,
    dueAt: LocalDateTime,
    now: LocalDateTime,
): LocalDateTime? {
    val trimmed = reminderText.trim()
    if (trimmed.isBlank() || trimmed == TodoReminderPreset.None.title) return null
    val reminderTime = when (trimmed) {
        TodoReminderPreset.Before10Min.title -> dueAt.minusMinutes(10)
        TodoReminderPreset.Before30Min.title -> dueAt.minusMinutes(30)
        TodoReminderPreset.Before1Hour.title -> dueAt.minusHours(1)
        TodoReminderPreset.PreviousNight.title -> LocalDateTime.of(dueAt.toLocalDate().minusDays(1), LocalTime.of(20, 0))
        else -> parseTodoDateTime(trimmed, now)
    } ?: return null
    return if (reminderTime.isAfter(now)) reminderTime else null
}

private fun extractTodoClockFallback(text: String): String {
    return when {
        text.contains("今晚") -> "20:00"
        text.contains("明晚") -> "20:00"
        else -> "21:00"
    }
}

private fun parseClock(raw: String): LocalTime? {
    val normalized = raw.trim()
    val parts = normalized.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)) }.getOrNull()
}

@Composable
private fun PomodoroScreen(active: Boolean) {
    val context = LocalContext.current
    val focusPrefs = remember { context.getSharedPreferences("focus_bridge", android.content.Context.MODE_PRIVATE) }
    val todoPrefs = remember { context.getSharedPreferences("todo_board", android.content.Context.MODE_PRIVATE) }
    val focusRecordPrefs = remember { context.getSharedPreferences("focus_records", android.content.Context.MODE_PRIVATE) }
    val sessionPrefs = remember { context.getSharedPreferences("pomodoro_session", android.content.Context.MODE_PRIVATE) }
    val noisePlayer = remember { NoisePlayer(context) }
    val presets = listOf(
        PomodoroPreset("25 分钟", 25 * 60),
        PomodoroPreset("45 分钟", 45 * 60),
        PomodoroPreset("60 分钟", 60 * 60),
    )
    val soundPresets = remember {
        listOf("山风", "溪流", "雨声", "海浪", "篝火", "白噪音", "粉噪")
    }
    val restoredSession = remember { loadPomodoroSession(sessionPrefs) }
    val restoredPreset = restoredSession?.let { PomodoroPreset(it.presetTitle, it.presetSeconds) } ?: presets.first()
    var mode by remember { mutableStateOf(restoredSession?.mode ?: PomodoroMode.Focus) }
    var preset by remember { mutableStateOf(restoredPreset) }
    var leftSeconds by remember { mutableIntStateOf(restoredSession?.leftSeconds ?: restoredPreset.seconds) }
    var running by remember { mutableStateOf(restoredSession?.running ?: false) }
    var autoNext by remember { mutableStateOf(restoredSession?.autoNext ?: true) }
    var strictMode by remember { mutableStateOf(restoredSession?.strictMode ?: false) }
    var boundTask by remember {
        mutableStateOf(
            restoredSession?.boundTask?.ifBlank {
                SecurePrefs.getString(focusPrefs, "bound_task_title_secure", "bound_task_title")
            } ?: SecurePrefs.getString(focusPrefs, "bound_task_title_secure", "bound_task_title").ifBlank { "完成机器学习实验报告" },
        )
    }
    var sound by remember { mutableStateOf(restoredSession?.sound ?: "山风") }
    var ambientOn by remember { mutableStateOf(restoredSession?.ambientOn ?: false) }
    var focusMinutes by remember { mutableIntStateOf(restoredSession?.focusMinutes ?: 125) }
    var cycles by remember { mutableIntStateOf(restoredSession?.cycles ?: 4) }
    var customMinutes by remember { mutableIntStateOf(restoredSession?.customMinutes ?: (restoredPreset.seconds / 60)) }
    var customSeconds by remember { mutableIntStateOf(restoredSession?.customSeconds ?: 0) }
    var onlyPendingGoalTasks by remember { mutableStateOf(restoredSession?.onlyPendingGoalTasks ?: true) }
    var sessionHint by remember {
        mutableStateOf(
            if (restoredSession != null) "已恢复上次未完成的专注会话。"
            else "",
        )
    }
    var ambientHint by remember { mutableStateOf("首次播放请等待 3-5 秒完成加载。") }
    val latestNoiseFailureHandler = rememberUpdatedState {
        ambientOn = false
        ambientHint = "当前设备未能启动环境声，请检查网络后重试。"
    }
    val latestNoiseReadyHandler = rememberUpdatedState {
        ambientHint = if (sound == "粉噪") "粉噪音量较大，已自动降低音量。" else ""
    }
    noisePlayer.onFailure = latestNoiseFailureHandler.value
    noisePlayer.onReady = latestNoiseReadyHandler.value
    val focusRecords = remember { mutableStateListOf<FocusRecord>().apply { addAll(loadFocusRecords(focusRecordPrefs)) } }
    val todoTasks = remember { mutableStateListOf<TodoTask>().apply { addAll(loadTodoTasks(todoPrefs)) } }
    val focusDayStats = remember(focusRecords.size) { buildFocusDayStats(focusRecords) }
    val focusTaskStats = remember(focusRecords.size) { buildFocusTaskStats(focusRecords) }
    val focusModeStats = remember(focusRecords.size) {
        focusRecords.groupBy { it.modeTitle }.mapValues { entry -> entry.value.map { it.seconds }.sum() / 60 }
    }
    val goalCandidateTasks = todoTasks.filter { task ->
        !task.done && if (onlyPendingGoalTasks) task.focusGoal > 0 && task.focusCount < task.focusGoal else true
    }
    val boundTodoTask = todoTasks.firstOrNull { it.title == boundTask }
    val remainingFocusRounds = (boundTodoTask?.focusGoal ?: 0) - (boundTodoTask?.focusCount ?: 0)
    val tasksWithGoals = todoTasks.filter { it.focusGoal > 0 }
    val reachedGoalCount = tasksWithGoals.count { it.focusCount >= it.focusGoal }
    val goalAttainmentRate = if (tasksWithGoals.isEmpty()) "--" else "${(reachedGoalCount * 100 / tasksWithGoals.size)}%"
    val recommendedFocusSeconds = when {
        remainingFocusRounds >= 3 -> 60 * 60
        remainingFocusRounds == 2 -> 45 * 60
        remainingFocusRounds == 1 -> 25 * 60
        else -> 0
    }

    LaunchedEffect(running, leftSeconds, preset.seconds) {
        if (!running) return@LaunchedEffect
        if (leftSeconds <= 0) {
            running = false
            if (mode == PomodoroMode.Focus) {
                recordTodoFocusProgress(todoPrefs, boundTask)
                todoTasks.clear()
                todoTasks.addAll(loadTodoTasks(todoPrefs))
                focusRecords.add(
                    0,
                    FocusRecord(
                        taskTitle = boundTask.ifBlank { "未命名专注" },
                        modeTitle = mode.title,
                        seconds = preset.seconds,
                        finishedAt = formatSyncTime(LocalDateTime.now()),
                    ),
                )
                saveFocusRecords(focusRecordPrefs, focusRecords)
                if (loadNotificationPreferenceState(context).pomodoroEnabled) {
                    sendAppNotification(context, "专注已完成", if (boundTask.isBlank()) "本轮专注已结束" else "已完成：$boundTask")
                }
                val updatedTask = todoTasks.firstOrNull { it.title == boundTask }
                if (updatedTask != null && updatedTask.focusGoal > 0 && updatedTask.focusCount >= updatedTask.focusGoal) {
                    if (loadNotificationPreferenceState(context).pomodoroEnabled) {
                        sendAppNotification(context, "任务专注目标已达成", "${updatedTask.title} 已达到 ${updatedTask.focusGoal} 轮")
                    }
                }
                focusMinutes += preset.seconds / 60
                cycles += 1
                if (autoNext) {
                    mode = if (cycles % 4 == 0) PomodoroMode.LongBreak else PomodoroMode.ShortBreak
                    preset = if (mode == PomodoroMode.LongBreak) PomodoroPreset("20 分钟", 20 * 60) else PomodoroPreset("5 分钟", 5 * 60)
                    leftSeconds = preset.seconds
                    running = true
                }
            }
            return@LaunchedEffect
        }
        delay(1000)
        leftSeconds -= 1
    }

    LaunchedEffect(active, running, ambientOn, sound, mode) {
        if (active && ambientOn) {
            val started = noisePlayer.start(sound)
            if (!started) {
                ambientOn = false
                ambientHint = "当前设备未能启动环境声，请检查网络后重试。"
            } else {
                ambientHint = if (sound == "粉噪") {
                    when {
                        noisePlayer.isCached(sound) -> "粉噪音量较大，已自动降低音量。"
                        noisePlayer.isCaching(sound) -> "粉噪正在缓存中，已自动降低音量，请等待 3-5 秒。"
                        else -> "粉噪音量较大，已自动降低音量。首次播放请等待 3-5 秒完成加载。"
                    }
                } else if (noisePlayer.isCached(sound)) {
                    ""
                } else if (noisePlayer.isCaching(sound)) {
                    "当前音色正在缓存中，请等待 3-5 秒。"
                } else {
                    "首次播放请等待 3-5 秒完成加载。"
                }
            }
        } else {
            noisePlayer.stop()
        }
    }

    LaunchedEffect(active) {
        if (active) {
            noisePlayer.warmUp()
        }
    }

    DisposableEffect(Unit) {
        onDispose { noisePlayer.stop() }
    }

    LaunchedEffect(boundTask) {
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", boundTask)
    }

    LaunchedEffect(
        mode,
        preset.title,
        preset.seconds,
        leftSeconds,
        running,
        autoNext,
        strictMode,
        boundTask,
        sound,
        ambientOn,
        focusMinutes,
        cycles,
        customMinutes,
        customSeconds,
        onlyPendingGoalTasks,
    ) {
        savePomodoroSession(
            sessionPrefs,
            PomodoroSession(
                mode = mode,
                presetTitle = preset.title,
                presetSeconds = preset.seconds,
                leftSeconds = leftSeconds,
                running = running,
                autoNext = autoNext,
                strictMode = strictMode,
                boundTask = boundTask,
                sound = sound,
                ambientOn = ambientOn,
                focusMinutes = focusMinutes,
                cycles = cycles,
                customMinutes = customMinutes,
                customSeconds = customSeconds,
                onlyPendingGoalTasks = onlyPendingGoalTasks,
            ),
        )
    }

    ScreenColumn {
        item {
            GlassCard {
                Text("专注台", style = MaterialTheme.typography.headlineMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                Text("支持任务绑定、自动流转、严格模式与白噪音选择。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.78f))
                if (sessionHint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(sessionHint, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(options = PomodoroMode.entries.toList(), selected = mode, label = { it.title }) {
                    mode = it
                    val seconds = when (it) {
                        PomodoroMode.Focus -> 25 * 60
                        PomodoroMode.ShortBreak -> 5 * 60
                        PomodoroMode.LongBreak -> 20 * 60
                    }
                    preset = PomodoroPreset("${seconds / 60} 分钟", seconds)
                    leftSeconds = seconds
                    running = false
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(formatSeconds(leftSeconds), style = MaterialTheme.typography.headlineLarge, color = PineInk)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    presets.forEach { item ->
                        ActionPill(item.title, if (item == preset) ForestGreen else MossGreen) {
                            preset = item
                            leftSeconds = item.seconds
                            running = false
                        }
                    }
                    if (recommendedFocusSeconds > 0) {
                        ActionPill("推荐 ${recommendedFocusSeconds / 60} 分钟", TeaGreen) {
                            preset = PomodoroPreset("目标推荐", recommendedFocusSeconds)
                            leftSeconds = recommendedFocusSeconds
                            running = false
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionPill(if (running) "暂停" else "开始", ForestGreen) { running = !running }
                    ActionPill("重置", Ginkgo) {
                        running = false
                        leftSeconds = preset.seconds
                    }
                }
            }
        }
        item {
            GlassCard {
                Text("自定义时长", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                Text("滚轮选择分钟与秒数，再应用到本次专注。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    WheelPicker(
                        title = "分钟",
                        values = (0..180).toList(),
                        selected = customMinutes,
                        label = { "${it} 分" },
                        onSelect = { customMinutes = it },
                        modifier = Modifier.weight(1f),
                    )
                    WheelPicker(
                        title = "秒",
                        values = (0..59).toList(),
                        selected = customSeconds,
                        label = { "%02d 秒".format(it) },
                        onSelect = { customSeconds = it },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                ActionPill("应用 ${customMinutes}分 ${"%02d".format(customSeconds)}秒", ForestGreen) {
                    val totalSeconds = customMinutes * 60 + customSeconds
                    if (totalSeconds > 0) {
                        preset = PomodoroPreset("自定义", totalSeconds)
                        leftSeconds = totalSeconds
                        running = false
                    }
                }
            }
        }
        item {
            GlassCard {
                Text("会话设置", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = boundTask, onValueChange = { boundTask = it }, label = { Text("绑定任务") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                val boundList = SecurePrefs.getString(focusPrefs, "bound_task_list_secure", "bound_task_list")
                if (boundList.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("来自清单：$boundList", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                ToggleLine("仅看未达成目标任务", onlyPendingGoalTasks) { onlyPendingGoalTasks = it }
                if (goalCandidateTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        goalCandidateTasks.take(8).forEach { task ->
                            SelectionChip(
                                text = if (task.focusGoal > 0) "${task.title} ${task.focusCount}/${task.focusGoal}" else task.title,
                                chosen = task.title == boundTask,
                                onClick = {
                                    boundTask = task.title
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                },
                            )
                        }
                    }
                }
                boundTodoTask?.let { task ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (task.focusGoal > 0) {
                            if (remainingFocusRounds <= 0) "专注目标已完成：${task.focusCount}/${task.focusGoal} 轮"
                            else "当前进度：${task.focusCount}/${task.focusGoal} 轮，还差 $remainingFocusRounds 轮"
                        } else {
                            "当前进度：已完成 ${task.focusCount} 轮，可回到待办设置专注目标"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForestDeep.copy(alpha = 0.72f),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("环境声", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                ) {
                    Text(
                        text = sound,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = PineInk,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "切换音色请点下方预设。首次播放请等待 3-5 秒完成加载。",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.66f),
                )
                if (ambientHint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        ambientHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestDeep.copy(alpha = 0.72f),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    soundPresets.forEach { presetName ->
                        SelectionChip(
                            text = presetName,
                            chosen = presetName == sound,
                            onClick = {
                                sound = presetName
                                ambientHint = if (presetName == "粉噪") {
                                    when {
                                        noisePlayer.isCached(presetName) -> "粉噪音量较大，已自动降低音量。"
                                        noisePlayer.isCaching(presetName) -> "粉噪正在缓存中，已自动降低音量，请等待 3-5 秒。"
                                        else -> "粉噪音量较大，已自动降低音量。首次播放请等待 3-5 秒完成加载。"
                                    }
                                } else if (noisePlayer.isCached(presetName)) {
                                    ""
                                } else if (noisePlayer.isCaching(presetName)) {
                                    "当前音色正在缓存中，请等待 3-5 秒。"
                                } else {
                                    "首次播放请等待 3-5 秒完成加载。"
                                }
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ToggleLine("播放白噪音", ambientOn) { ambientOn = it }
                Spacer(modifier = Modifier.height(8.dp))
                ToggleLine("自动进入下一阶段", autoNext) { autoNext = it }
                Spacer(modifier = Modifier.height(8.dp))
                ToggleLine("严格模式", strictMode) { strictMode = it }
            }
        }
        item {
            GlassCard {
                Text("今日统计", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    MetricCard("专注时长", "$focusMinutes 分钟", ForestGreen)
                    MetricCard("完成轮次", cycles.toString(), Ginkgo)
                }
            }
        }
        item {
            GlassCard {
                Text("专注分析", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                Text("按天、按任务和按模式回看最近的投入分布。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                Spacer(modifier = Modifier.height(12.dp))
                if (focusRecords.isEmpty()) {
                    Text("完成几轮专注后，这里会自动生成分析结果。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.68f))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        MetricCard("目标达成率", goalAttainmentRate, TeaGreen)
                        MetricCard("已达成目标", reachedGoalCount.toString(), MossGreen)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("近 7 天", style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Spacer(modifier = Modifier.height(10.dp))
                    val maxDaily = focusDayStats.maxOfOrNull { it.minutes }?.coerceAtLeast(1) ?: 1
                    focusDayStats.forEachIndexed { index, stat ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(stat.label, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                                Text("${stat.minutes} 分钟", style = MaterialTheme.typography.labelLarge, color = PineInk)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((stat.minutes.toFloat() / maxDaily).coerceIn(0.08f, 1f))
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreen),
                                )
                            }
                        }
                        if (index != focusDayStats.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("任务排行", style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Spacer(modifier = Modifier.height(10.dp))
                    focusTaskStats.take(5).forEachIndexed { index, stat ->
                        Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.26f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                    Text(stat.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                                    Text("${stat.count} 轮 · ${stat.minutes} 分钟", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                                }
                                SelectionChip(text = "${index + 1}", chosen = index == 0, onClick = {})
                            }
                        }
                        if (index != minOf(focusTaskStats.size, 5) - 1) Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("模式占比", style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        focusModeStats.forEach { (modeTitle, minutes) ->
                            MetricCard(modeTitle, "${minutes} 分钟", if (modeTitle == PomodoroMode.Focus.title) ForestGreen else Ginkgo)
                        }
                    }
                }
            }
        }
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("专注记录", style = MaterialTheme.typography.titleLarge, color = PineInk)
                    if (focusRecords.isNotEmpty()) {
                        ActionPill("清空", WarmMist) {
                            focusRecords.clear()
                            saveFocusRecords(focusRecordPrefs, focusRecords)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (focusRecords.isEmpty()) {
                    Text("还没有完成的专注记录。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.7f))
                } else {
                    focusRecords.take(12).forEachIndexed { index, record ->
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.36f)) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(record.taskTitle, style = MaterialTheme.typography.titleMedium, color = PineInk)
                                Text("${record.modeTitle} · ${formatSeconds(record.seconds)}", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
                                Text(record.finishedAt, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
                            }
                        }
                        if (index != minOf(focusRecords.size, 12) - 1) Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}
@Composable
internal fun ToggleLine(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.56f)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = PineInk)
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun AssistantPermissionScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val store = remember { AssistantPermissionStore(context) }
    val toolkit = remember { AssistantToolKit() }
    var permissionState by remember { mutableStateOf(store.load()) }
    val tools = remember(permissionState) { toolkit.availableTools(permissionState) }
    val palette = PoxiaoThemeState.palette
    Box(modifier = modifier) {
        ScreenColumn {
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                            Text("智能体权限", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
                            Text("控制智能体可读取哪些数据，以及可触发哪些本地 mock 工具。", style = MaterialTheme.typography.bodyLarge, color = palette.softText)
                        }
                        ActionPill("返回", WarmMist, onClick = onBack)
                    }
                }
            }
            item {
                GlassCard {
                    Text("数据读取", style = MaterialTheme.typography.titleLarge, color = PineInk)
                    Spacer(modifier = Modifier.height(12.dp))
                    ToggleLine("允许读取课表与考试周", permissionState.readSchedule) {
                        permissionState = permissionState.copy(readSchedule = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许读取待办与目标", permissionState.readTodo) {
                        permissionState = permissionState.copy(readTodo = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许读取专注记录", permissionState.readFocus) {
                        permissionState = permissionState.copy(readFocus = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许读取成绩缓存", permissionState.readGrades) {
                        permissionState = permissionState.copy(readGrades = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许读取地图点位", permissionState.readMap) {
                        permissionState = permissionState.copy(readMap = it)
                        store.save(permissionState)
                    }
                }
            }
            item {
                GlassCard {
                    Text("动作能力", style = MaterialTheme.typography.titleLarge, color = PineInk)
                    Spacer(modifier = Modifier.height(12.dp))
                    ToggleLine("允许生成待办建议", permissionState.createTodo) {
                        permissionState = permissionState.copy(createTodo = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许生成专注绑定建议", permissionState.bindPomodoro) {
                        permissionState = permissionState.copy(bindPomodoro = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许生成地图跳转建议", permissionState.openCampusMap) {
                        permissionState = permissionState.copy(openCampusMap = it)
                        store.save(permissionState)
                    }
                }
            }
            item {
                GlassCard {
                    Text("当前工具清单", style = MaterialTheme.typography.titleLarge, color = PineInk)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (tools.isEmpty()) {
                        Text("当前所有工具都已关闭，智能体只会保留纯文本对话。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.72f))
                    } else {
                        tools.forEachIndexed { index, tool ->
                            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.34f)) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(tool.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                                    Text(tool.description, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                                }
                            }
                            if (index != tools.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomDock(
    current: PrimarySection,
    onSelect: (PrimarySection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val densityPreset = LocalUiDensityPreset.current
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val dockHeight = 80.dp
    val dockCorner = 40.dp
    val selectionCorner = 32.dp
    val dockTint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.16f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.1f)
    }
    val dockBorder = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.24f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.34f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.24f)
    }
    val dockGlow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.03f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.1f)
    }
    val items = remember {
        listOf(
            PrimarySection.Schedule,
            PrimarySection.Todo,
            PrimarySection.Home,
            PrimarySection.Pomodoro,
            PrimarySection.More,
        )
    }
    var visualCurrent by remember { mutableStateOf(current) }
    var transitionFrom by remember { mutableStateOf(current) }
    val itemWeights = remember { listOf(1f, 1f, 1.42f, 1f, 1f) }
    LaunchedEffect(current) {
        if (visualCurrent != current) {
            visualCurrent = current
        }
        transitionFrom = current
    }
    val selectedIndex = remember(visualCurrent, items) { items.indexOf(visualCurrent).coerceAtLeast(0) }
    val originIndex = remember(transitionFrom, items) { items.indexOf(transitionFrom).coerceAtLeast(0) }
    val bookTurnEasing = remember { CubicBezierEasing(0.12f, 0.86f, 0.18f, 1f) }
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = 360, easing = bookTurnEasing),
        label = "dock-book-index",
    )
    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(dockHeight * densityPreset.scale),
        cornerRadius = dockCorner * densityPreset.scale,
        shapeOverride = CircleShape,
        contentPadding = PaddingValues(
            horizontal = 8.dp * densityPreset.scale,
            vertical = 8.dp * densityPreset.scale,
        ),
        tint = dockTint,
        borderColor = dockBorder,
        glowColor = dockGlow,
        blurRadius = if (stylePreset == LiquidGlassStylePreset.Hyper) 13.dp else 14.dp,
        refractionHeight = 10.dp,
        refractionAmount = 14.dp,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val slotSpacing = 4.dp * densityPreset.scale
            val availableWidth = maxWidth - slotSpacing * items.lastIndex.toFloat()
            val totalWeight = itemWeights.sum()
            val slotWidths = itemWeights.map { availableWidth * (it / totalWeight) }
            fun slotStart(index: Int): Dp {
                var result = 0.dp
                for (slotIndex in 0 until index) {
                    result += slotWidths[slotIndex] + slotSpacing
                }
                return result
            }

            val leadingIndex = floor(animatedIndex).toInt().coerceIn(0, items.lastIndex)
            val trailingIndex = ceil(animatedIndex).toInt().coerceIn(0, items.lastIndex)
            val travelFraction = (animatedIndex - leadingIndex).coerceIn(0f, 1f)
            val turnArch = sin(travelFraction * PI).toFloat()
            val routeSpan = abs(selectedIndex - originIndex).coerceAtLeast(1).toFloat()
            val routeProgress = if (selectedIndex == originIndex) {
                1f
            } else {
                (abs(animatedIndex - originIndex) / routeSpan).coerceIn(0f, 1f)
            }
            val pillLeft = slotStart(leadingIndex) + (slotStart(trailingIndex) - slotStart(leadingIndex)) * travelFraction
            val pillWidth =
                slotWidths[leadingIndex] +
                    (slotWidths[trailingIndex] - slotWidths[leadingIndex]) * travelFraction +
                    (12.dp * densityPreset.scale) * turnArch
            val pillGlow = when (stylePreset) {
                LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.08f + turnArch * 0.05f)
                LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.1f + turnArch * 0.06f)
                LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.18f + turnArch * 0.12f)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassSurface(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .offset(x = pillLeft)
                        .requiredWidth(pillWidth),
                    cornerRadius = selectionCorner * densityPreset.scale,
                    shapeOverride = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    tint = palette.card.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.18f
                            LiquidGlassStylePreset.IOS -> 0.06f
                            LiquidGlassStylePreset.Hyper -> 0.2f
                        },
                    ),
                    borderColor = when (stylePreset) {
                        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.2f + turnArch * 0.04f)
                        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.28f + turnArch * 0.08f)
                        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.2f + turnArch * 0.08f)
                    },
                    glowColor = pillGlow,
                    blurRadius = when (stylePreset) {
                        LiquidGlassStylePreset.Harmony -> 11.dp
                        LiquidGlassStylePreset.IOS -> 13.dp
                        LiquidGlassStylePreset.Hyper -> 12.dp
                    },
                    refractionHeight = 8.dp,
                    refractionAmount = when (stylePreset) {
                        LiquidGlassStylePreset.Harmony -> 7.dp + 3.dp * turnArch
                        LiquidGlassStylePreset.IOS -> 9.dp + 5.dp * turnArch
                        LiquidGlassStylePreset.Hyper -> 10.dp + 6.dp * turnArch
                    },
                ) {}

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(slotSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEachIndexed { index, item ->
                        val selectionProgress = when {
                            selectedIndex == originIndex && index == selectedIndex -> 1f
                            index == originIndex -> 1f - routeProgress
                            index == selectedIndex -> routeProgress
                            else -> 0f
                        }
                        DockNavItem(
                            item = item,
                            active = visualCurrent == item,
                            selectedProgress = selectionProgress,
                            onPreviewSelect = { target ->
                                if (target != visualCurrent) {
                                    transitionFrom = visualCurrent
                                    visualCurrent = target
                                }
                            },
                            onCancelPreview = {
                                if (visualCurrent != current) {
                                    transitionFrom = visualCurrent
                                    visualCurrent = current
                                }
                            },
                            onSelect = { target ->
                                if (target != current) {
                                    onSelect(target)
                                }
                            },
                            emphasized = item == PrimarySection.Home,
                            modifier = Modifier.weight(itemWeights[index]),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockNavItem(
    item: PrimarySection,
    active: Boolean,
    selectedProgress: Float,
    onPreviewSelect: (PrimarySection) -> Unit,
    onCancelPreview: () -> Unit,
    onSelect: (PrimarySection) -> Unit,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val stylePreset = LocalLiquidGlassStylePreset.current
    val contentScale = if (emphasized) 1.02f + 0.12f * selectedProgress else 1f + 0.07f * selectedProgress
    val iconScale = if (emphasized) 1.02f + 0.16f * selectedProgress else 1f + 0.1f * selectedProgress
    val contentOffset = (-1.5f).dp * selectedProgress * densityPreset.scale
    val baseTint = if (emphasized) {
        palette.pillOn.copy(alpha = 0.95f)
    } else {
        palette.pillOn.copy(alpha = 0.8f)
    }
    val contentTint = lerpColor(baseTint, Color.White, selectedProgress)
    val captionAlpha = if (emphasized) 0.96f else 0.78f + 0.22f * selectedProgress
    val emphasisGlow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.92f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.9f)
        LiquidGlassStylePreset.Hyper -> palette.secondary
    }
    val contentWidth = if (emphasized) 72.dp * densityPreset.scale else 54.dp * densityPreset.scale
    val iconShellSize = if (emphasized) 38.dp * densityPreset.scale else 30.dp * densityPreset.scale
    val iconSize = (if (emphasized) 24.dp else 18.dp) * densityPreset.scale
    val labelLift = if (emphasized) (-1.5).dp * densityPreset.scale else 0.dp
    val latestPreviewSelect = rememberUpdatedState(onPreviewSelect)
    val latestCancelPreview = rememberUpdatedState(onCancelPreview)
    val latestSelect = rememberUpdatedState(onSelect)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .pointerInput(item) {
                detectTapGestures(
                    onPress = {
                        latestPreviewSelect.value(item)
                        val released = tryAwaitRelease()
                        if (released) {
                            latestSelect.value(item)
                        } else {
                            latestCancelPreview.value()
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (emphasized) {
            CenterDockAuraLayer(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                selected = active,
                pulse = 0.96f + 0.1f * selectedProgress,
                auraAlpha = 0.2f + 0.28f * selectedProgress,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(contentWidth)
                .offset(y = contentOffset)
                .scale(contentScale)
                .padding(horizontal = 2.dp * densityPreset.scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(iconShellSize)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                emphasisGlow.copy(alpha = if (emphasized) 0.12f + 0.18f * selectedProgress else 0.05f * selectedProgress),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.navLabel,
                    tint = contentTint,
                    modifier = Modifier
                        .size(iconSize)
                        .scale(iconScale),
                )
            }
            Spacer(modifier = Modifier.height(2.dp * densityPreset.scale))
            Text(
                text = item.navLabel,
                style = if (emphasized) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                color = contentTint.copy(alpha = captionAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = labelLift),
            )
        }
    }
}

@Composable
private fun CenterDockAuraLayer(
    selected: Boolean,
    pulse: Float,
    auraAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val transition = rememberInfiniteTransition(label = "dock-center-aura")
    val orbitDegrees by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (selected) 4800 else 6200,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dock-center-orbit",
    )
    val shimmer by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dock-center-shimmer",
    )
    val particleAlpha by animateFloatAsState(
        targetValue = if (selected) 0.94f else 0.56f,
        animationSpec = tween(durationMillis = 260, easing = CubicBezierEasing(0.18f, 0.9f, 0.24f, 1f)),
        label = "dock-center-particles",
    )
    val baseColor = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White
        LiquidGlassStylePreset.IOS -> Color.White
        LiquidGlassStylePreset.Hyper -> palette.secondary
    }
    Canvas(modifier = modifier) {
        val coreCenter = center
        val coreRadius = size.minDimension * 0.46f * pulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = auraAlpha * 0.52f * shimmer),
                    baseColor.copy(alpha = auraAlpha * 0.68f),
                    Color.Transparent,
                ),
                center = coreCenter,
                radius = coreRadius,
            ),
            radius = coreRadius,
            center = coreCenter,
        )
        drawCircle(
            color = baseColor.copy(alpha = auraAlpha * 0.18f),
            radius = size.minDimension * 0.31f,
            center = coreCenter,
            style = Stroke(width = size.minDimension * 0.026f),
        )

        val particles = listOf(
            Triple(0f, 0.28f, 0.09f),
            Triple(128f, 0.36f, 0.075f),
            Triple(248f, 0.23f, 0.065f),
        )
        particles.forEachIndexed { index, (offsetDegrees, orbitFactor, radiusFactor) ->
            val angle = Math.toRadians((orbitDegrees + offsetDegrees).toDouble())
            val particleCenter = Offset(
                x = coreCenter.x + cos(angle).toFloat() * size.minDimension * orbitFactor,
                y = coreCenter.y + sin(angle).toFloat() * size.minDimension * orbitFactor * 0.72f,
            )
            val glowRadius = size.minDimension * radiusFactor * shimmer
            val particleColor = if (index == 0) {
                Color.White
            } else {
                baseColor
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        particleColor.copy(alpha = particleAlpha * (0.62f - index * 0.12f)),
                        baseColor.copy(alpha = particleAlpha * (0.34f - index * 0.06f)),
                        Color.Transparent,
                    ),
                    center = particleCenter,
                    radius = glowRadius * 2.7f,
                ),
                radius = glowRadius * 2.7f,
                center = particleCenter,
            )
        }
    }
}

@Composable
internal fun ScreenColumn(
    content: LazyListScope.() -> Unit,
) {
    val densityPreset = LocalUiDensityPreset.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 18.dp * densityPreset.scale,
            end = 18.dp * densityPreset.scale,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 60.dp * densityPreset.scale,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp * densityPreset.scale,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp * densityPreset.scale),
        content = content,
    )
}

@Composable
internal fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val glassStrength = LocalGlassStrengthPreset.current
    val staticGlass = LocalStaticGlassMode.current
    if (staticGlass) {
        Surface(
            shape = RoundedCornerShape(30.dp * densityPreset.scale),
            color = palette.card.copy(alpha = (palette.card.alpha * glassStrength.cardAlpha).coerceIn(0f, 1f)),
            border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.76f)),
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp * densityPreset.scale),
                content = content,
            )
        }
    } else {
        LiquidGlassCard(
            modifier = modifier.fillMaxWidth(),
            cornerRadius = 30.dp * densityPreset.scale,
            contentPadding = PaddingValues(20.dp * densityPreset.scale),
            tint = palette.card.copy(alpha = palette.card.alpha * glassStrength.cardAlpha),
            borderColor = palette.cardBorder.copy(alpha = 0.82f),
            glowColor = palette.cardGlow.copy(alpha = palette.cardGlow.alpha * glassStrength.glowScale),
            blurRadius = 12.dp,
            refractionHeight = 12.dp,
            refractionAmount = 18.dp,
            content = content,
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    accent: Color,
    sizePreset: HomeModuleSize = HomeModuleSize.Standard,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val glassStrength = LocalGlassStrengthPreset.current
    val staticGlass = LocalStaticGlassMode.current
    val cardWidth = when (sizePreset) {
        HomeModuleSize.Compact -> 96.dp
        HomeModuleSize.Standard -> 106.dp
        HomeModuleSize.Hero -> 124.dp
    }
    val valueStyle = when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.titleSmall
        HomeModuleSize.Standard -> MaterialTheme.typography.titleMedium
        HomeModuleSize.Hero -> MaterialTheme.typography.titleLarge
    }
    val metricContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                    ),
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp * densityPreset.scale),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp * densityPreset.scale),
                color = accent.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 10.dp * densityPreset.scale, vertical = 6.dp * densityPreset.scale),
                )
            }
            Text(value, style = valueStyle, color = palette.ink)
        }
    }
    if (staticGlass) {
        Surface(
            shape = RoundedCornerShape(22.dp * densityPreset.scale),
            color = palette.card.copy(alpha = (0.3f * glassStrength.cardAlpha).coerceAtLeast(0.18f)),
            border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.72f)),
            modifier = modifier.width(cardWidth * densityPreset.scale),
        ) {
            Column(
                modifier = Modifier.padding(14.dp * densityPreset.scale),
            ) {
                metricContent()
            }
        }
    } else {
        LiquidGlassSurface(
            modifier = modifier.width(cardWidth * densityPreset.scale),
            cornerRadius = 22.dp * densityPreset.scale,
            contentPadding = PaddingValues(14.dp * densityPreset.scale),
            tint = palette.card.copy(alpha = 0.3f * glassStrength.cardAlpha),
            borderColor = palette.cardBorder.copy(alpha = 0.78f),
            glowColor = accent.copy(alpha = 0.22f * glassStrength.glowScale),
            blurRadius = 10.dp,
            refractionHeight = 10.dp,
            refractionAmount = 14.dp,
        ) {
            metricContent()
        }
    }
}

@Composable
private fun ReviewExecutionDetailBlock(
    execution: ReviewBridgeExecutionSummary,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (execution.replaySourceExecutedAt > 0L) {
            Text(
                "回放自 ${formatSyncTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(execution.replaySourceExecutedAt), java.time.ZoneId.systemDefault()))}",
                style = MaterialTheme.typography.labelMedium,
                color = ForestGreen,
            )
        }
        if (execution.diffSummary.isNotBlank()) {
            Text(
                execution.diffSummary,
                style = MaterialTheme.typography.bodySmall,
                color = ForestDeep.copy(alpha = 0.78f),
            )
        }
        if (execution.createdTaskTitles.isNotEmpty()) {
            Text("生成待办", style = MaterialTheme.typography.labelMedium, color = ForestGreen)
            execution.createdTaskTitles.take(3).forEach { title ->
                Text("• $title", style = MaterialTheme.typography.bodySmall, color = PineInk)
            }
        }
        if (execution.boundTaskTitle.isNotBlank()) {
            Text(
                "已绑定专注：${execution.boundTaskTitle}",
                style = MaterialTheme.typography.bodySmall,
                color = ForestDeep.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
internal fun ActionPill(
    text: String,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val staticGlass = LocalStaticGlassMode.current
    val textColor = if (background.red * 0.299f + background.green * 0.587f + background.blue * 0.114f > 0.62f) palette.ink else palette.pillOn
    if (staticGlass) {
        Surface(
            shape = RoundedCornerShape(22.dp * densityPreset.scale),
            color = background.copy(alpha = 0.22f),
            border = BorderStroke(1.dp, background.copy(alpha = 0.28f)),
            modifier = modifier.clickable(onClick = onClick),
        ) {
            Box(
                modifier = Modifier.padding(
                    horizontal = 14.dp * densityPreset.scale,
                    vertical = 10.dp * densityPreset.scale,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LiquidGlassSurface(
            modifier = modifier.clickable(onClick = onClick),
            cornerRadius = 22.dp * densityPreset.scale,
            contentPadding = PaddingValues(
                horizontal = 14.dp * densityPreset.scale,
                vertical = 10.dp * densityPreset.scale,
            ),
            tint = background.copy(alpha = 0.3f),
            borderColor = background.copy(alpha = 0.36f),
            glowColor = background.copy(alpha = 0.28f),
            blurRadius = 8.dp,
            refractionHeight = 8.dp,
            refractionAmount = 12.dp,
        ) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun WheelPicker(
    title: String,
    values: List<Int>,
    selected: Int,
    label: (Int) -> String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val glassStrength = LocalGlassStrengthPreset.current
    Surface(
        shape = RoundedCornerShape(22.dp * densityPreset.scale),
        color = palette.card.copy(alpha = palette.card.alpha * glassStrength.cardAlpha),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.72f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp * densityPreset.scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp * densityPreset.scale),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = palette.softText)
            LazyColumn(
                modifier = Modifier.height(172.dp * densityPreset.scale).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 18.dp * densityPreset.scale),
                verticalArrangement = Arrangement.spacedBy(8.dp * densityPreset.scale),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(values) { value ->
                    val active = value == selected
                    Surface(
                        shape = RoundedCornerShape(18.dp * densityPreset.scale),
                        color = if (active) palette.primary else palette.card.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, if (active) palette.primary.copy(alpha = 0.24f) else palette.cardBorder.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .padding(horizontal = 12.dp * densityPreset.scale)
                            .clickable { onSelect(value) },
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp * densityPreset.scale, vertical = 10.dp * densityPreset.scale),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label(value),
                                style = if (active) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                                color = if (active) palette.pillOn else palette.ink,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionChip(
    text: String,
    chosen: Boolean,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    Surface(
        shape = RoundedCornerShape(16.dp * densityPreset.scale),
        color = if (chosen) palette.primary else palette.card.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, if (chosen) palette.primary.copy(alpha = 0.24f) else palette.cardBorder.copy(alpha = 0.54f)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.padding(horizontal = 10.dp * densityPreset.scale, vertical = 6.dp * densityPreset.scale), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = if (chosen) palette.pillOn else palette.ink,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun <T> SelectionRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp * densityPreset.scale)) {
        options.forEach { item ->
            val chosen = item == selected
            Surface(
                shape = RoundedCornerShape(18.dp * densityPreset.scale),
                color = if (chosen) palette.primary else palette.card.copy(alpha = 0.86f),
                border = BorderStroke(1.dp, if (chosen) palette.primary.copy(alpha = 0.26f) else palette.cardBorder.copy(alpha = 0.58f)),
                modifier = Modifier.clickable { onSelect(item) },
            ) {
                Box(modifier = Modifier.padding(horizontal = 12.dp * densityPreset.scale, vertical = 9.dp * densityPreset.scale), contentAlignment = Alignment.Center) {
                    Text(
                        label(item),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (chosen) palette.pillOn else palette.ink,
                    )
                }
            }
        }
    }
}

private fun formatSeconds(seconds: Int): String {
    val minute = seconds / 60
    val second = seconds % 60
    return "%02d:%02d".format(minute, second)
}
