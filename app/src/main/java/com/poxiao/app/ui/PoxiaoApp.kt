
package com.poxiao.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.poxiao.app.ui.theme.PoxiaoTheme

@Composable
fun PoxiaoApp() {
    val context = LocalContext.current
    val uiState = rememberPoxiaoAppUiState(context)

    LaunchedEffect(Unit) {
        refreshLocalReminderSchedule(context)
    }

    PoxiaoTheme(preset = uiState.themePreset) {
        CompositionLocalProvider(
            LocalUiDensityPreset provides uiState.densityPreset,
            LocalGlassStrengthPreset provides uiState.glassStrengthPreset,
            LocalLiquidGlassStylePreset provides uiState.liquidGlassStylePreset,
        ) {
            PoxiaoAppScaffold(
                themePreset = uiState.themePreset,
                densityPreset = uiState.densityPreset,
                glassStrengthPreset = uiState.glassStrengthPreset,
                liquidGlassStylePreset = uiState.liquidGlassStylePreset,
                onThemePresetChange = uiState.onThemePresetChange,
                onDensityPresetChange = uiState.onDensityPresetChange,
                onGlassStrengthChange = uiState.onGlassStrengthChange,
                onLiquidGlassStyleChange = uiState.onLiquidGlassStyleChange,
            )
        }
    }
}
