package com.poxiao.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poxiao.app.calculator.ScientificCalculatorScreen
import com.poxiao.app.campus.CampusMapScreen
import com.poxiao.app.campus.CampusServicesScreen
import com.poxiao.app.insights.LearningDashboardScreen
import com.poxiao.app.notes.CourseNotesScreen
import com.poxiao.app.reports.ExportCenterScreen
import com.poxiao.app.review.ReviewPlannerScreen
import com.poxiao.app.settings.NotificationPreferencesScreen
import com.poxiao.app.ui.theme.PoxiaoThemePreset
import com.poxiao.app.ui.EditionCapabilities
import com.poxiao.app.ui.LocalEditionCapabilities

@Composable
internal fun PoxiaoAppScaffoldScene(
    scaffoldState: PoxiaoAppScaffoldState,
    themePreset: PoxiaoThemePreset,
    densityPreset: UiDensityPreset,
    glassStrengthPreset: GlassStrengthPreset,
    liquidGlassStylePreset: LiquidGlassStylePreset,
    customBlur: Float,
    customGlow: Float,
    customAlpha: Float,
    customHueOffset: Float,
    customSaturation: Float,
    onThemePresetChange: (PoxiaoThemePreset) -> Unit,
    onDensityPresetChange: (UiDensityPreset) -> Unit,
    onGlassStrengthChange: (GlassStrengthPreset) -> Unit,
    onLiquidGlassStyleChange: (LiquidGlassStylePreset) -> Unit,
    onCustomBlurChange: (Float) -> Unit,
    onCustomGlowChange: (Float) -> Unit,
    onCustomAlphaChange: (Float) -> Unit,
    onCustomHueOffsetChange: (Float) -> Unit,
    onCustomSaturationChange: (Float) -> Unit,
    capabilities: EditionCapabilities = LocalEditionCapabilities.current,
) {
    with(scaffoldState) {
        LiquidGlassScene(modifier = Modifier.fillMaxSize()) {
            if (overlayPage != null) {
                PoxiaoOverlayHost(
                    scaffoldState = scaffoldState,
                    themePreset = themePreset,
                    densityPreset = densityPreset,
                    glassStrengthPreset = glassStrengthPreset,
                    liquidGlassStylePreset = liquidGlassStylePreset,
                    customBlur = customBlur,
                    customGlow = customGlow,
                    customAlpha = customAlpha,
                    customHueOffset = customHueOffset,
                    customSaturation = customSaturation,
                    onThemePresetChange = onThemePresetChange,
                    onDensityPresetChange = onDensityPresetChange,
                    onGlassStrengthChange = onGlassStrengthChange,
                    onLiquidGlassStyleChange = onLiquidGlassStyleChange,
                    onCustomBlurChange = onCustomBlurChange,
                    onCustomGlowChange = onCustomGlowChange,
                    onCustomAlphaChange = onCustomAlphaChange,
                    onCustomHueOffsetChange = onCustomHueOffsetChange,
                    onCustomSaturationChange = onCustomSaturationChange,
                    capabilities = capabilities,
                )
            } else {
                PoxiaoSectionHost(scaffoldState)
            }

            PoxiaoScaffoldIslandHint(scaffoldState)

            if (overlayPage == null) {
                PoxiaoAppScaffoldControls(scaffoldState)
            }
        }
    }
}

@Composable
private fun BoxScope.PoxiaoScaffoldIslandHint(
    scaffoldState: PoxiaoAppScaffoldState,
) {
    with(scaffoldState) {
        IslandHint(
            text = overlayPage?.label ?: section.label,
            icon = section.icon,
            visible = islandVisible && overlayPage == null && !sideNavExpanded,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp),
        )
    }
}

@Composable
internal fun PoxiaoOverlayHost(
    scaffoldState: PoxiaoAppScaffoldState,
    themePreset: PoxiaoThemePreset,
    densityPreset: UiDensityPreset,
    glassStrengthPreset: GlassStrengthPreset,
    liquidGlassStylePreset: LiquidGlassStylePreset,
    customBlur: Float,
    customGlow: Float,
    customAlpha: Float,
    customHueOffset: Float,
    customSaturation: Float,
    onThemePresetChange: (PoxiaoThemePreset) -> Unit,
    onDensityPresetChange: (UiDensityPreset) -> Unit,
    onGlassStrengthChange: (GlassStrengthPreset) -> Unit,
    onLiquidGlassStyleChange: (LiquidGlassStylePreset) -> Unit,
    onCustomBlurChange: (Float) -> Unit,
    onCustomGlowChange: (Float) -> Unit,
    onCustomAlphaChange: (Float) -> Unit,
    onCustomHueOffsetChange: (Float) -> Unit,
    onCustomSaturationChange: (Float) -> Unit,
    capabilities: EditionCapabilities = LocalEditionCapabilities.current,
) {
    with(scaffoldState) {
        when (overlayPage) {
            OverlayPage.CampusServices -> if (capabilities.canShowCampus) {
                CampusServicesScreen(
                    modifier = Modifier.fillMaxSize(),
                    onBack = scaffoldState::closeOverlay,
                    onOpenMap = scaffoldState::openCampusMap,
                )
            }
            OverlayPage.CampusMap -> if (capabilities.canShowCampus) {
                CampusMapScreen(
                    modifier = Modifier.fillMaxSize(),
                    onBack = scaffoldState::closeOverlay,
                )
            }
            OverlayPage.AcademicAccount -> if (capabilities.canShowAcademic) {
                AcademicAccountScreen(
                    repository = repository,
                    modifier = Modifier.fillMaxSize(),
                    onBack = scaffoldState::closeOverlay,
                )
            }
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
            OverlayPage.LearningDashboard -> if (capabilities.canShowInsights) {
                LearningDashboardScreen(
                    modifier = Modifier.fillMaxSize(),
                    onOpenExportCenter = scaffoldState::openExportCenter,
                    onBack = scaffoldState::closeOverlay,
                )
            }
            OverlayPage.ExportCenter -> ExportCenterScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = scaffoldState::closeOverlay,
                capabilities = capabilities,
            )
            OverlayPage.Preferences -> PreferencesScreen(
                modifier = Modifier.fillMaxSize(),
                currentPreset = themePreset,
                currentDensity = densityPreset,
                currentGlassStrength = glassStrengthPreset,
                currentGlassStyle = liquidGlassStylePreset,
                customBlur = customBlur,
                customGlow = customGlow,
                customAlpha = customAlpha,
                customHueOffset = customHueOffset,
                customSaturation = customSaturation,
                onSelectPreset = onThemePresetChange,
                onSelectDensity = onDensityPresetChange,
                onSelectGlassStrength = onGlassStrengthChange,
                onSelectGlassStyle = onLiquidGlassStyleChange,
                onCustomBlurChange = onCustomBlurChange,
                onCustomGlowChange = onCustomGlowChange,
                onCustomAlphaChange = onCustomAlphaChange,
                onCustomHueOffsetChange = onCustomHueOffsetChange,
                onCustomSaturationChange = onCustomSaturationChange,
                onBack = scaffoldState::closeOverlay,
            )
            OverlayPage.AssistantPermissions -> AssistantPermissionScreen(
                repository = repository,
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
                                active = isCurrentSection,
                                repository = repository,
                                initialAssistantHistoryFocusAt = assistantHistoryFocusAt,
                                onAssistantHistoryFocusConsumed = scaffoldState::consumeAssistantHistoryFocus,
                                onOpenMap = scaffoldState::openCampusMap,
                                onOpenScheduleDay = scaffoldState::openScheduleDay,
                                onOpenScheduleExamWeek = scaffoldState::openScheduleExamWeek,
                                onOpenCampusServices = scaffoldState::openCampusServices,
                                onOpenTodoPending = { scaffoldState.openTodoPending(it) },
                                onOpenPomodoro = scaffoldState::openPomodoro,
                                onOpenReviewPlanner = scaffoldState::openReviewPlanner,
                                onOpenReviewPlannerSeeded = scaffoldState::openReviewPlanner,
                                onOpenAssistantPermissions = scaffoldState::openAssistantPermissions,
                                onOpenCourseNotes = scaffoldState::openCourseNotes,
                            )
                            PrimarySection.Schedule -> ScheduleScreen(
                                repository,
                                active = isCurrentSection,
                                initialMode = scheduleEntryMode,
                                initialWorkbench = scheduleEntryWorkbench,
                                onOpenAcademicAccount = scaffoldState::openMoreSection,
                                onOpenCourseNotes = scaffoldState::openCourseNotes,
                            )
                            PrimarySection.Todo -> TodoScreen(
                                active = isCurrentSection,
                                initialFilter = todoEntryFilter
                            )
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
