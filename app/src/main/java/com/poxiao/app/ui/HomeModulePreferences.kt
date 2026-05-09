package com.poxiao.app.ui

import android.content.SharedPreferences

private const val VisibleModulesKey = "visible_modules"
private const val ModuleSizesKey = "module_sizes"
private const val CollapsedModulesKey = "collapsed_modules"

internal fun loadHomeModules(
    prefs: SharedPreferences,
): List<HomeModule> {
    val stored = loadStringList(prefs, VisibleModulesKey)
    if (stored.isEmpty()) return HomeModule.entries.toList()
    val parsed = stored.mapNotNull { raw -> runCatching { HomeModule.valueOf(raw) }.getOrNull() }
    if (parsed.isEmpty()) return HomeModule.entries.toList()
    val missing = HomeModule.entries.filterNot { it in parsed }
    return parsed + missing
}

internal fun defaultHomeModuleSize(module: HomeModule): HomeModuleSize =
    when (module) {
        HomeModule.Metrics -> HomeModuleSize.Compact
        HomeModule.Rhythm -> HomeModuleSize.Standard
        HomeModule.Learning -> HomeModuleSize.Hero
        HomeModule.QuickPoints -> HomeModuleSize.Compact
        HomeModule.RecentPoints -> HomeModuleSize.Compact
        HomeModule.Assistant -> HomeModuleSize.Hero
    }

internal fun loadHomeModuleSizes(
    prefs: SharedPreferences,
): Map<HomeModule, HomeModuleSize> {
    val stored = loadStringList(prefs, ModuleSizesKey)
    val parsed = stored.mapNotNull { raw ->
        val parts = raw.split(":")
        if (parts.size != 2) return@mapNotNull null
        val module = runCatching { HomeModule.valueOf(parts[0]) }.getOrNull() ?: return@mapNotNull null
        val size = runCatching { HomeModuleSize.valueOf(parts[1]) }.getOrNull() ?: return@mapNotNull null
        module to size
    }.toMap()
    return HomeModule.entries.associateWith { module -> parsed[module] ?: defaultHomeModuleSize(module) }
}

internal fun saveHomeModuleSizes(
    prefs: SharedPreferences,
    moduleSizes: Map<HomeModule, HomeModuleSize>,
) {
    saveStringList(
        prefs,
        ModuleSizesKey,
        moduleSizes.entries.map { (module, size) -> "${module.name}:${size.name}" },
    )
}

internal fun saveHomeModules(
    prefs: SharedPreferences,
    modules: List<HomeModule>,
) {
    saveStringList(prefs, VisibleModulesKey, modules.map { it.name })
}

internal fun loadCollapsedHomeModules(
    prefs: SharedPreferences,
): List<HomeModule> {
    val stored = loadStringList(prefs, CollapsedModulesKey)
    return stored.mapNotNull { raw -> runCatching { HomeModule.valueOf(raw) }.getOrNull() }
        .filter { isHomeModuleCollapsible(it) }
}

internal fun saveCollapsedHomeModules(
    prefs: SharedPreferences,
    modules: List<HomeModule>,
) {
    saveStringList(
        prefs,
        CollapsedModulesKey,
        modules.distinct().filter { isHomeModuleCollapsible(it) }.map { it.name },
    )
}

internal fun rememberSearchTerm(
    prefs: SharedPreferences,
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

internal fun isHomeModuleCollapsible(module: HomeModule): Boolean =
    module in setOf(
        HomeModule.Rhythm,
        HomeModule.Learning,
    )
