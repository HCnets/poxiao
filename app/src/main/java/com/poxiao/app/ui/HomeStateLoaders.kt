package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.campus.HitaAcademicGateway
import com.poxiao.app.data.AssistantSessionStore
import com.poxiao.app.security.SecurePrefs

internal fun bootstrapHomeAssistantSession(
    assistantStore: AssistantSessionStore,
): HomeAssistantSessionBootstrap {
    val initialConversations = assistantStore.loadConversations()
        .ifEmpty { listOf(assistantStore.defaultConversation()) }
    val activeConversationId = assistantStore.loadActiveConversationId()
        .takeIf { target -> initialConversations.any { it.id == target } }
        ?: initialConversations.first().id
    val initialPrompt = initialConversations.firstOrNull { it.id == activeConversationId }?.draftInput.orEmpty()
    return HomeAssistantSessionBootstrap(
        initialConversations = initialConversations,
        activeConversationId = activeConversationId,
        initialPrompt = initialPrompt,
    )
}

internal fun loadHomeQuickKeywords(
    homePrefs: SharedPreferences,
): List<String> {
    val stored = loadStringList(homePrefs, "search_keywords")
    return if (stored.isNotEmpty()) {
        stored
    } else {
        listOf("机器学习", "实验报告", "A栋", "考试", "高优先")
    }
}

internal fun loadHomeFavoritePoints(
    mapPrefs: SharedPreferences,
): List<String> {
    val homeQuick = mapPrefs.getString("home_quick_points", "").orEmpty()
    val fallback = mapPrefs.getString("favorite_points", "").orEmpty()
    val raw = if (homeQuick.isNotBlank()) homeQuick else fallback
    return raw.split("|").filter { it.isNotBlank() }
}

internal fun loadHomeRecentPoints(
    mapPrefs: SharedPreferences,
): List<String> {
    return mapPrefs.getString("recent_points", "").orEmpty()
        .split("|")
        .filter { it.isNotBlank() }
}

internal fun loadHomeBuildingCandidates(
    mapPrefs: SharedPreferences,
): List<String> {
    return buildList {
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

internal fun bootstrapHomeWorkbench(
    homePrefs: SharedPreferences,
): HomeWorkbenchBootstrap {
    val visibleModules = loadHomeModules(homePrefs)
    val collapsedModules = loadCollapsedHomeModules(homePrefs)
    val moduleSizes = loadHomeModuleSizes(homePrefs).toMutableMap().apply {
        HomeModule.entries.forEach { module ->
            putIfAbsent(module, defaultHomeModuleSize(module))
        }
    }
    return HomeWorkbenchBootstrap(
        visibleModules = visibleModules,
        collapsedModules = collapsedModules,
        moduleSizes = moduleSizes,
    )
}

internal suspend fun refreshHomeGradeSearch(
    searchQuery: String,
    authPrefs: SharedPreferences,
    campusPrefs: SharedPreferences,
): HomeGradeSearchRefresh {
    if (searchQuery.isBlank()) {
        return HomeGradeSearchRefresh(
            cards = emptyList(),
            status = "",
            loading = false,
        )
    }
    val studentId = SecurePrefs.getString(authPrefs, "student_id_secure", "student_id")
    val password = SecurePrefs.getString(authPrefs, "password_secure", "password")
    if (studentId.isBlank() || password.isBlank()) {
        val cachedCards = loadHomeGradeCache(campusPrefs)
        return HomeGradeSearchRefresh(
            cards = cachedCards,
            status = if (cachedCards.isEmpty()) "登录教务后可搜索真实成绩。" else "",
            loading = false,
        )
    }
    return runCatching {
        val gateway = HitaAcademicGateway(studentId, password)
        gateway.fetchTerms()
            .take(3)
            .flatMap { term -> gateway.fetchGradesForTerm(term) }
    }.fold(
        onSuccess = { cards ->
            if (cards.isNotEmpty()) {
                HomeGradeSearchRefresh(
                    cards = cards,
                    status = "",
                    loading = false,
                )
            } else {
                val cachedCards = loadHomeGradeCache(campusPrefs)
                HomeGradeSearchRefresh(
                    cards = cachedCards,
                    status = if (cachedCards.isEmpty()) "当前没有可搜索的成绩记录。" else "当前显示的是最近同步的成绩缓存。",
                    loading = false,
                )
            }
        },
        onFailure = { error ->
            val cachedCards = loadHomeGradeCache(campusPrefs)
            HomeGradeSearchRefresh(
                cards = cachedCards,
                status = if (cachedCards.isEmpty()) (error.message ?: "成绩检索失败。") else "当前显示的是最近同步的成绩缓存。",
                loading = false,
            )
        },
    )
}
