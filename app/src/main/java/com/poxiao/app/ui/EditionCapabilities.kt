package com.poxiao.app.ui

import com.poxiao.app.BuildConfig

public enum class AppEdition(val id: String, val label: String) {
    Hitsz("hitsz", "HITSZ"),
    Academic("academic", "Academic"),
    Lite("lite", "Lite");

    companion object {
        fun from(raw: String): AppEdition =
            entries.firstOrNull { it.id.equals(raw.trim(), ignoreCase = true) } ?: Hitsz
    }
}

public data class EditionCapabilities(
    val edition: AppEdition,
    val editionLabel: String,
    val supportsAcademic: Boolean,
    val supportsCampusServices: Boolean,
    val supportsCampusMap: Boolean,
    val supportsGradeSearch: Boolean,
    val availableSections: List<PrimarySection>,
    val availableOverlays: Set<OverlayPage>,
    val availableHomeModules: Set<HomeModule>,
) {
    fun supports(section: PrimarySection): Boolean = section in availableSections
    fun supports(overlay: OverlayPage): Boolean = overlay in availableOverlays
}

public val EditionCapabilities.canShowAcademic: Boolean get() = supportsAcademic
public val EditionCapabilities.canShowSchedule: Boolean get() = supportsAcademic // 课表能力目前与教务能力挂钩
public val EditionCapabilities.canShowCampus: Boolean get() = supportsCampusServices || supportsCampusMap
public val EditionCapabilities.canShowGradeSearch: Boolean get() = supportsGradeSearch
public val EditionCapabilities.canShowInsights: Boolean get() = supportsAcademic
public val EditionCapabilities.canShowTodo: Boolean get() = true // 待办是基础功能，全版本开启

public val LocalEditionCapabilities = androidx.compose.runtime.staticCompositionLocalOf {
    EditionCapabilities(
        edition = AppEdition.Hitsz,
        editionLabel = "HITSZ",
        supportsAcademic = true,
        supportsCampusServices = true,
        supportsCampusMap = true,
        supportsGradeSearch = true,
        availableSections = emptyList(),
        availableOverlays = emptySet(),
        availableHomeModules = emptySet(),
    )
}

public fun editionCapabilitiesFromBuildConfig(): EditionCapabilities {
    val edition = AppEdition.from(BuildConfig.APP_EDITION)
    val availableSections = buildList {
        add(PrimarySection.Home)
        if (BuildConfig.ENABLE_ACADEMIC) add(PrimarySection.Schedule)
        add(PrimarySection.Todo)
        add(PrimarySection.Pomodoro)
        add(PrimarySection.More)
    }
    val availableOverlays = buildSet {
        if (BuildConfig.ENABLE_CAMPUS_SERVICES) add(OverlayPage.CampusServices)
        if (BuildConfig.ENABLE_CAMPUS_MAP) add(OverlayPage.CampusMap)
        if (BuildConfig.ENABLE_ACADEMIC) add(OverlayPage.AcademicAccount)
        add(OverlayPage.Calculator)
        add(OverlayPage.CourseNotes)
        add(OverlayPage.ReviewPlanner)
        add(OverlayPage.NotificationPreferences)
        add(OverlayPage.LearningDashboard)
        add(OverlayPage.ExportCenter)
        add(OverlayPage.Preferences)
        add(OverlayPage.AssistantPermissions)
    }
    val availableHomeModules = buildSet {
        add(HomeModule.Metrics)
        add(HomeModule.Rhythm)
        add(HomeModule.Learning)
        add(HomeModule.Assistant)
        if (BuildConfig.ENABLE_CAMPUS_MAP) {
            add(HomeModule.QuickPoints)
            add(HomeModule.RecentPoints)
        }
    }
    return EditionCapabilities(
        edition = edition,
        editionLabel = edition.label,
        supportsAcademic = BuildConfig.ENABLE_ACADEMIC,
        supportsCampusServices = BuildConfig.ENABLE_CAMPUS_SERVICES,
        supportsCampusMap = BuildConfig.ENABLE_CAMPUS_MAP,
        supportsGradeSearch = BuildConfig.ENABLE_GRADE_SEARCH,
        availableSections = availableSections,
        availableOverlays = availableOverlays,
        availableHomeModules = availableHomeModules,
    )
}

internal fun buildBottomDockSections(
    sections: List<PrimarySection>,
): List<PrimarySection> {
    val preferredOrder = listOf(
        PrimarySection.Schedule,
        PrimarySection.Todo,
        PrimarySection.Home,
        PrimarySection.Pomodoro,
        PrimarySection.More,
    )
    return preferredOrder.filter { it in sections }
}
