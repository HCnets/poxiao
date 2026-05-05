package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.notes.CourseNoteSeed

internal fun handleHomeSearchResult(
    result: HomeSearchResult,
    homePrefs: SharedPreferences,
    searchHistory: MutableList<String>,
    searchQuery: String,
    onOpenScheduleDay: () -> Unit,
    onOpenCourseNotes: (CourseNoteSeed?) -> Unit,
    onOpenTodoPending: (TodoFilter) -> Unit,
    onOpenCampusServices: () -> Unit,
    onOpenMap: () -> Unit,
    capabilities: EditionCapabilities = editionCapabilitiesFromBuildConfig(),
) {
    rememberSearchTerm(homePrefs, searchHistory, searchQuery)
    when (result.category) {
        HomeSearchCategory.Course -> if (capabilities.canShowSchedule) onOpenScheduleDay()
        HomeSearchCategory.Note -> onOpenCourseNotes(CourseNoteSeed(courseName = result.title))
        HomeSearchCategory.Todo -> onOpenTodoPending(TodoFilter.All)
        HomeSearchCategory.Grade -> if (capabilities.canShowAcademic) onOpenCampusServices()
        HomeSearchCategory.Building -> if (capabilities.canShowCampus) onOpenMap()
    }
}
