package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.notes.CourseNoteSeed

internal fun buildHomeContentActionPack(
    homePrefs: SharedPreferences,
    searchHistory: MutableList<String>,
    visibleModules: MutableList<HomeModule>,
    moduleSizes: MutableMap<HomeModule, HomeModuleSize>,
    searchQueryProvider: () -> String,
    draggingModuleProvider: () -> HomeModule?,
    dragOffsetYProvider: () -> Float,
    dragSwapThreshold: Float,
    onSearchQueryChange: (String) -> Unit,
    onDraggingModuleChange: (HomeModule?) -> Unit,
    onDragOffsetYChange: (Float) -> Unit,
    onHomeEditModeToggle: () -> Unit,
    onOpenScheduleDay: () -> Unit,
    onOpenCourseNotes: (CourseNoteSeed?) -> Unit,
    onOpenTodoPending: () -> Unit,
    onOpenCampusServices: () -> Unit,
    onOpenMap: () -> Unit,
): HomeContentActionPack {
    return HomeContentActionPack(
        onToggleEditMode = {
            onHomeEditModeToggle()
            onDraggingModuleChange(null)
            onDragOffsetYChange(0f)
        },
        onSearchQueryChange = onSearchQueryChange,
        onSelectHistory = onSearchQueryChange,
        onClearHistory = {
            searchHistory.clear()
            saveStringList(homePrefs, "search_history", searchHistory)
        },
        onSelectKeyword = onSearchQueryChange,
        onSearchResultClick = { result ->
            handleHomeSearchResult(
                result = result,
                homePrefs = homePrefs,
                searchHistory = searchHistory,
                searchQuery = searchQueryProvider(),
                onOpenScheduleDay = onOpenScheduleDay,
                onOpenCourseNotes = onOpenCourseNotes,
                onOpenTodoPending = onOpenTodoPending,
                onOpenCampusServices = onOpenCampusServices,
                onOpenMap = onOpenMap,
            )
        },
        onToggleModuleVisibility = { module ->
            if (module in visibleModules) {
                if (visibleModules.size > 1) visibleModules.remove(module)
            } else {
                visibleModules.add(module)
            }
            saveHomeModules(homePrefs, visibleModules)
        },
        onDragStart = { module ->
            onDraggingModuleChange(module)
            onDragOffsetYChange(0f)
        },
        onDragCancel = {
            onDraggingModuleChange(null)
            onDragOffsetYChange(0f)
        },
        onDragEnd = {
            onDraggingModuleChange(null)
            onDragOffsetYChange(0f)
            saveHomeModules(homePrefs, visibleModules)
        },
        onDragMove = fun(module: HomeModule, deltaY: Float) {
            if (draggingModuleProvider() != module) return
            val nextOffset = dragOffsetYProvider() + deltaY
            onDragOffsetYChange(nextOffset)
            val currentIndex = visibleModules.indexOf(module)
            if (nextOffset > dragSwapThreshold && currentIndex < visibleModules.lastIndex) {
                visibleModules.swap(currentIndex, currentIndex + 1)
                onDragOffsetYChange(nextOffset - dragSwapThreshold)
                saveHomeModules(homePrefs, visibleModules)
            } else if (nextOffset < -dragSwapThreshold && currentIndex > 0) {
                visibleModules.swap(currentIndex, currentIndex - 1)
                onDragOffsetYChange(nextOffset + dragSwapThreshold)
                saveHomeModules(homePrefs, visibleModules)
            }
        },
        onSelectModuleSize = { module, sizePreset ->
            moduleSizes[module] = sizePreset
            saveHomeModuleSizes(homePrefs, moduleSizes)
        },
    )
}
