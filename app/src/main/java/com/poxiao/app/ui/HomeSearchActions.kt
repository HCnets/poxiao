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
    onOpenTodoPending: () -> Unit,
    onOpenCampusServices: () -> Unit,
    onOpenMap: () -> Unit,
) {
    rememberSearchTerm(homePrefs, searchHistory, searchQuery)
    when (result.category) {
        HomeSearchCategory.Course -> onOpenScheduleDay()
        HomeSearchCategory.Note -> onOpenCourseNotes(CourseNoteSeed(courseName = result.title))
        HomeSearchCategory.Todo -> onOpenTodoPending()
        HomeSearchCategory.Grade -> onOpenCampusServices()
        HomeSearchCategory.Building -> onOpenMap()
    }
}
