package com.poxiao.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.poxiao.app.campus.CampusMapScreen
import com.poxiao.app.campus.CampusServicesScreen
import com.poxiao.app.calculator.ScientificCalculatorScreen
import com.poxiao.app.insights.LearningDashboardScreen
import com.poxiao.app.notes.CourseNotesScreen
import com.poxiao.app.reports.ExportCenterScreen
import com.poxiao.app.review.ReviewPlannerScreen
import com.poxiao.app.settings.NotificationPreferencesScreen
import com.poxiao.app.ui.theme.PoxiaoThemePreset

@Composable
internal fun PoxiaoOverlayHost(
    scaffoldState: PoxiaoAppScaffoldState,
    themePreset: PoxiaoThemePreset,
    densityPreset: UiDensityPreset,
    glassStrengthPreset: GlassStrengthPreset,
    liquidGlassStylePreset: LiquidGlassStylePreset,
    onThemePresetChange: (PoxiaoThemePreset) -> Unit,
    onDensityPresetChange: (UiDensityPreset) -> Unit,
    onGlassStrengthChange: (GlassStrengthPreset) -> Unit,
    onLiquidGlassStyleChange: (LiquidGlassStylePreset) -> Unit,
) {
    with(scaffoldState) {
        when (overlayPage) {
            OverlayPage.CampusServices -> CampusServicesScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = scaffoldState::closeOverlay,
                onOpenMap = scaffoldState::openCampusMap,
            )
            OverlayPage.CampusMap -> CampusMapScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = scaffoldState::closeOverlay,
            )
            OverlayPage.AcademicAccount -> AcademicAccountScreen(
                repository = repository,
                modifier = Modifier.fillMaxSize(),
                onBack = scaffoldState::closeOverlay,
            )
            OverlayPage.Calculator -> ScientificCalculatorScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = scaffoldState::closeOverlay,
            )
            OverlayPage.CourseNotes -> CourseNotesScreen(
                modifier = Modifier.fillMaxSize(),
                initialSeed = courseNoteSeed,
                onBack = scaffoldState::closeOverlay,
            )
            OverlayPage.ReviewPlanner -> ReviewPlannerScreen(
                modifier = Modifier.fillMaxSize(),
                initialSeed = reviewPlannerSeed,
                onOpenAssistantHistory = scaffoldState::focusAssistantHistory,
                onOpenCourseNoteSource = scaffoldState::openCourseNotes,
                onOpenExportCenter = scaffoldState::openExportCenter,
                onBack = scaffoldState::closeOverlay,
            )
            OverlayPage.NotificationPreferences -> NotificationPreferencesScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = scaffoldState::closeOverlay,
            )
            OverlayPage.LearningDashboard -> LearningDashboardScreen(
                modifier = Modifier.fillMaxSize(),
                onOpenExportCenter = scaffoldState::openExportCenter,
                onBack = scaffoldState::closeOverlay,
            )
            OverlayPage.ExportCenter -> ExportCenterScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = scaffoldState::closeOverlay,
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
                onBack = scaffoldState::closeOverlay,
            )
            OverlayPage.AssistantPermissions -> AssistantPermissionScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = scaffoldState::closeOverlay,
            )
            null -> Unit
        }
    }
}

@Composable
internal fun PoxiaoSectionHost(
    scaffoldState: PoxiaoAppScaffoldState,
) {
    with(scaffoldState) {
        val renderSections = sectionOrder.filter { it in residentSections }
        val transition = rememberPoxiaoSectionTransitionSnapshot(sectionSweepProgress.value)
        Box(modifier = Modifier.fillMaxSize()) {
            renderSections.forEach { residentSection ->
                key(residentSection) {
                    val isCurrentSection = residentSection == section
                    val sectionModifier = Modifier.poxiaoSectionHostModifier(
                        isCurrentSection = isCurrentSection,
                        transition = transition,
                    )
                    Box(modifier = sectionModifier) {
                        when (residentSection) {
                            PrimarySection.Home -> HomeScreen(
                                initialAssistantHistoryFocusAt = assistantHistoryFocusAt,
                                onAssistantHistoryFocusConsumed = scaffoldState::consumeAssistantHistoryFocus,
                                onOpenMap = scaffoldState::openCampusMap,
                                onOpenScheduleDay = scaffoldState::openScheduleDay,
                                onOpenScheduleExamWeek = scaffoldState::openScheduleExamWeek,
                                onOpenCampusServices = scaffoldState::openCampusServices,
                                onOpenTodoPending = scaffoldState::openTodoPending,
                                onOpenPomodoro = scaffoldState::openPomodoro,
                                onOpenReviewPlanner = scaffoldState::openReviewPlanner,
                                onOpenReviewPlannerSeeded = scaffoldState::openReviewPlanner,
                                onOpenAssistantPermissions = scaffoldState::openAssistantPermissions,
                                onOpenCourseNotes = scaffoldState::openCourseNotes,
                            )
                            PrimarySection.Schedule -> ScheduleScreen(
                                repository,
                                initialMode = scheduleEntryMode,
                                initialWorkbench = scheduleEntryWorkbench,
                                onOpenAcademicAccount = scaffoldState::openMoreSection,
                                onOpenCourseNotes = scaffoldState::openCourseNotes,
                            )
                            PrimarySection.Todo -> TodoScreen(initialFilter = todoEntryFilter)
                            PrimarySection.Pomodoro -> PomodoroScreen(active = isCurrentSection)
                            PrimarySection.More -> MoreScreen(
                                repository = repository,
                                onOpenAcademicAccount = scaffoldState::openAcademicAccount,
                                onOpenCampusServices = scaffoldState::openCampusServices,
                                onOpenCalculator = scaffoldState::openCalculator,
                                onOpenCourseNotes = scaffoldState::openCourseNotes,
                                onOpenReviewPlanner = scaffoldState::openReviewPlanner,
                                onOpenNotificationPreferences = scaffoldState::openNotificationPreferences,
                                onOpenLearningDashboard = scaffoldState::openLearningDashboard,
                                onOpenExportCenter = scaffoldState::openExportCenter,
                                onOpenPreferences = scaffoldState::openPreferences,
                            )
                        }
                    }
                }
            }
        }
    }
}
