
package com.poxiao.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.poxiao.app.ui.theme.PoxiaoTheme
import com.poxiao.app.ui.interactions.rememberHapticManager
import com.poxiao.app.ui.EditionCapabilities
import com.poxiao.app.ui.editionCapabilitiesFromBuildConfig

@Composable
fun PoxiaoApp() {
    val context = LocalContext.current
    val uiState = rememberPoxiaoAppUiState(context)
    val editionCapabilities = editionCapabilitiesFromBuildConfig()

    LaunchedEffect(Unit) {
        refreshLocalReminderSchedule(context)
    }

    PoxiaoTheme(
        preset = uiState.themePreset,
        customHueOffset = uiState.customHueOffset,
        customSaturation = uiState.customSaturation,
    ) {
        GyroScopeProvider {
            CompositionLocalProvider(
                LocalUiDensityPreset provides uiState.densityPreset,
                LocalGlassStrengthPreset provides uiState.glassStrengthPreset,
                LocalLiquidGlassStylePreset provides uiState.liquidGlassStylePreset,
                LocalCustomBlur provides uiState.customBlur,
                LocalCustomGlow provides uiState.customGlow,
                LocalCustomAlpha provides uiState.customAlpha,
            ) {
                PoxiaoAppScaffold(
                    editionCapabilities = editionCapabilities,
                    themePreset = uiState.themePreset,
                    densityPreset = uiState.densityPreset,
                    glassStrengthPreset = uiState.glassStrengthPreset,
                    liquidGlassStylePreset = uiState.liquidGlassStylePreset,
                    customBlur = uiState.customBlur,
                    customGlow = uiState.customGlow,
                    customAlpha = uiState.customAlpha,
                    customHueOffset = uiState.customHueOffset,
                    customSaturation = uiState.customSaturation,
                    onThemePresetChange = uiState.onThemePresetChange,
                    onDensityPresetChange = uiState.onDensityPresetChange,
                    onGlassStrengthChange = uiState.onGlassStrengthChange,
                    onLiquidGlassStyleChange = uiState.onLiquidGlassStyleChange,
                    onCustomBlurChange = uiState.onCustomBlurChange,
                    onCustomGlowChange = uiState.onCustomGlowChange,
                    onCustomAlphaChange = uiState.onCustomAlphaChange,
                    onCustomHueOffsetChange = uiState.onCustomHueOffsetChange,
                    onCustomSaturationChange = uiState.onCustomSaturationChange,
                )
            }
        }
    }
}
