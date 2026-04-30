package com.poxiao.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal fun homeMetricSpacing(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 10.dp
        HomeModuleSize.Standard -> 12.dp
        HomeModuleSize.Hero -> 14.dp
    }

internal fun homeSectionSpacing(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 8.dp
        HomeModuleSize.Standard -> 12.dp
        HomeModuleSize.Hero -> 14.dp
    }

internal fun homeSecondarySpacing(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 6.dp
        HomeModuleSize.Standard -> 8.dp
        HomeModuleSize.Hero -> 10.dp
    }

internal fun homeLineGap(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 8.dp
        HomeModuleSize.Standard -> 10.dp
        HomeModuleSize.Hero -> 12.dp
    }

internal fun buildHomeModuleRows(
    modules: List<HomeModule>,
    moduleSizes: Map<HomeModule, HomeModuleSize>,
): List<List<HomeModule>> {
    val rows = mutableListOf<List<HomeModule>>()
    var index = 0
    while (index < modules.size) {
        val current = modules[index]
        val currentCompact = isHomeModulePairable(current, moduleSizes[current] ?: defaultHomeModuleSize(current))
        val next = modules.getOrNull(index + 1)
        val nextCompact = next != null && isHomeModulePairable(next, moduleSizes[next] ?: defaultHomeModuleSize(next))
        if (currentCompact && nextCompact) {
            rows += listOf(current, next!!)
            index += 2
        } else {
            rows += listOf(current)
            index += 1
        }
    }
    return rows
}

internal fun isHomeModulePairable(
    module: HomeModule,
    sizePreset: HomeModuleSize,
): Boolean {
    if (sizePreset != HomeModuleSize.Compact) return false
    return module in setOf(
        HomeModule.QuickPoints,
        HomeModule.RecentPoints,
        HomeModule.Metrics,
    )
}

@Composable
internal fun homeSectionTitleStyle(sizePreset: HomeModuleSize) =
    when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.titleMedium
        HomeModuleSize.Standard -> MaterialTheme.typography.titleLarge
        HomeModuleSize.Hero -> MaterialTheme.typography.headlineSmall
    }

@Composable
internal fun homeSectionBodyStyle(sizePreset: HomeModuleSize) =
    when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.bodyMedium
        HomeModuleSize.Standard -> MaterialTheme.typography.bodyLarge
        HomeModuleSize.Hero -> MaterialTheme.typography.titleMedium
    }
