package com.poxiao.app.ui

import androidx.compose.runtime.Composable
import com.poxiao.app.ui.theme.PoxiaoThemePreset

@Composable
internal fun PoxiaoAppScaffold(
    editionCapabilities: EditionCapabilities,
    themePreset: PoxiaoThemePreset,
    densityPreset: UiDensityPreset,
    glassStrengthPreset: GlassStrengthPreset,
    liquidGlassStylePreset: LiquidGlassStylePreset,
    customBlur: Float,
    customGlow: Float,
    customAlpha: Float,
    onThemePresetChange: (PoxiaoThemePreset) -> Unit,
    onDensityPresetChange: (UiDensityPreset) -> Unit,
    onGlassStrengthChange: (GlassStrengthPreset) -> Unit,
    onLiquidGlassStyleChange: (LiquidGlassStylePreset) -> Unit,
    onCustomBlurChange: (Float) -> Unit,
    onCustomGlowChange: (Float) -> Unit,
    onCustomAlphaChange: (Float) -> Unit,
) {
    val scaffoldState = rememberPoxiaoAppScaffoldState(editionCapabilities)
    PoxiaoAppScaffoldEffects(scaffoldState)
    PoxiaoAppScaffoldScene(
        scaffoldState = scaffoldState,
        themePreset = themePreset,
        densityPreset = densityPreset,
        glassStrengthPreset = glassStrengthPreset,
        liquidGlassStylePreset = liquidGlassStylePreset,
        customBlur = customBlur,
        customGlow = customGlow,
        customAlpha = customAlpha,
        onThemePresetChange = onThemePresetChange,
        onDensityPresetChange = onDensityPresetChange,
        onGlassStrengthChange = onGlassStrengthChange,
        onLiquidGlassStyleChange = onLiquidGlassStyleChange,
        onCustomBlurChange = onCustomBlurChange,
        onCustomGlowChange = onCustomGlowChange,
        onCustomAlphaChange = onCustomAlphaChange,
    )
}
