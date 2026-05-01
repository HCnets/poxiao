package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.data.AssistantContextSummary
import com.poxiao.app.data.FeedCard
import com.poxiao.app.schedule.AcademicRepository
import com.poxiao.app.schedule.AcademicUiState
import com.poxiao.app.schedule.HitaScheduleRepository
import com.poxiao.app.todo.TodoTask
import org.json.JSONArray

private const val GradeCacheKey = "grade_cache_v1"

internal fun loadPrimaryScheduleState(
    primaryPrefs: SharedPreferences,
    fallbackPrefs: SharedPreferences,
): HitaScheduleUiState? {
    return loadCachedScheduleUiState(primaryPrefs) ?: loadCachedScheduleUiState(fallbackPrefs)
}

internal fun loadPrimaryScheduleEvents(
    primaryPrefs: SharedPreferences,
    fallbackPrefs: SharedPreferences,
): List<ScheduleExtraEvent> {
    val primary = loadScheduleExtraEvents(primaryPrefs)
    return if (primary.isNotEmpty()) primary else loadScheduleExtraEvents(fallbackPrefs)
}

internal fun loadHomeGradeCache(
    prefs: SharedPreferences,
): List<FeedCard> {
    val raw = prefs.getString(GradeCacheKey, "").orEmpty()
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

internal fun loadHomeInitialAcademicState(
    prefs: SharedPreferences,
    repository: AcademicRepository,
): AcademicUiState {
    val cached = loadCachedScheduleUiState(prefs)
    if (cached != null) {
        if (repository is HitaScheduleRepository) {
            repository.restoreCachedState(cached)
        }
        return cached
    }
    return AcademicUiState()
}
