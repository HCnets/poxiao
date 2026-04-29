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

@Composable
internal fun PoxiaoAppScaffold(
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
