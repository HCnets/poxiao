package com.poxiao.app.ui

import androidx.compose.runtime.Composable
import com.poxiao.app.ui.theme.PoxiaoThemePreset

@Composable
internal fun PoxiaoAppScaffold(
    themePreset: PoxiaoThemePreset,
    densityPreset: UiDensityPreset,
    glassStrengthPreset: GlassStrengthPreset,
    liquidGlassStylePreset: LiquidGlassStylePreset,
    onThemePresetChange: (PoxiaoThemePreset) -> Unit,
    onDensityPresetChange: (UiDensityPreset) -> Unit,
    onGlassStrengthChange: (GlassStrengthPreset) -> Unit,
    onLiquidGlassStyleChange: (LiquidGlassStylePreset) -> Unit,
) {
    val scaffoldState = rememberPoxiaoAppScaffoldState()
    PoxiaoAppScaffoldEffects(scaffoldState)
    PoxiaoAppScaffoldScene(
        scaffoldState = scaffoldState,
        themePreset = themePreset,
        densityPreset = densityPreset,
        glassStrengthPreset = glassStrengthPreset,
        liquidGlassStylePreset = liquidGlassStylePreset,
        onThemePresetChange = onThemePresetChange,
        onDensityPresetChange = onDensityPresetChange,
        onGlassStrengthChange = onGlassStrengthChange,
        onLiquidGlassStyleChange = onLiquidGlassStyleChange,
    )
}
