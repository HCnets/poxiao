package com.poxiao.app.ui

import com.poxiao.app.ui.theme.PoxiaoThemePreset

internal data class PoxiaoAppUiState(
    val themePreset: PoxiaoThemePreset,
    val densityPreset: UiDensityPreset,
    val glassStrengthPreset: GlassStrengthPreset,
    val liquidGlassStylePreset: LiquidGlassStylePreset,
    val onThemePresetChange: (PoxiaoThemePreset) -> Unit,
    val onDensityPresetChange: (UiDensityPreset) -> Unit,
    val onGlassStrengthChange: (GlassStrengthPreset) -> Unit,
    val onLiquidGlassStyleChange: (LiquidGlassStylePreset) -> Unit,
)
