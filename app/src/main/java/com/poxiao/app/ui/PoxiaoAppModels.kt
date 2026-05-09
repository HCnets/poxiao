package com.poxiao.app.ui

import com.poxiao.app.ui.theme.PoxiaoThemePreset

internal data class PoxiaoAppUiState(
    val themePreset: PoxiaoThemePreset,
    val densityPreset: UiDensityPreset,
    val glassStrengthPreset: GlassStrengthPreset,
    val liquidGlassStylePreset: LiquidGlassStylePreset,
    val customBlur: Float,
    val customGlow: Float,
    val customAlpha: Float,
    val customHueOffset: Float,
    val customSaturation: Float,
    val onThemePresetChange: (PoxiaoThemePreset) -> Unit,
    val onDensityPresetChange: (UiDensityPreset) -> Unit,
    val onGlassStrengthChange: (GlassStrengthPreset) -> Unit,
    val onLiquidGlassStyleChange: (LiquidGlassStylePreset) -> Unit,
    val onCustomBlurChange: (Float) -> Unit,
    val onCustomGlowChange: (Float) -> Unit,
    val onCustomAlphaChange: (Float) -> Unit,
    val onCustomHueOffsetChange: (Float) -> Unit,
    val onCustomSaturationChange: (Float) -> Unit,
)
