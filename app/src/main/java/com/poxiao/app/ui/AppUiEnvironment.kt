package com.poxiao.app.ui

import androidx.compose.runtime.staticCompositionLocalOf

internal enum class UiDensityPreset(
    val title: String,
    val scale: Float,
) {
    Compact("紧凑", 0.92f),
    Comfortable("均衡", 1f),
    Relaxed("舒展", 1.08f),
}

internal enum class GlassStrengthPreset(
    val title: String,
    val cardAlpha: Float,
    val glowScale: Float,
) {
    Crisp("清透", 0.88f, 0.72f),
    Balanced("柔雾", 1f, 1f),
    Lush("晶润", 1.14f, 1.24f),
}

internal val LocalUiDensityPreset = staticCompositionLocalOf { UiDensityPreset.Comfortable }
internal val LocalGlassStrengthPreset = staticCompositionLocalOf { GlassStrengthPreset.Balanced }
internal val LocalStaticGlassMode = staticCompositionLocalOf { false }
