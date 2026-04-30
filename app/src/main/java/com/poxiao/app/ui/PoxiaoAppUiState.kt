package com.poxiao.app.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.poxiao.app.ui.theme.PoxiaoThemePreset

@Composable
internal fun rememberPoxiaoAppUiState(
    context: Context,
): PoxiaoAppUiState {
    val uiPrefs = remember { context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE) }
    var themePreset by remember {
        mutableStateOf(
            runCatching {
                PoxiaoThemePreset.valueOf(uiPrefs.getString("theme_preset", PoxiaoThemePreset.Forest.name).orEmpty())
            }.getOrDefault(PoxiaoThemePreset.Forest),
        )
    }
    var densityPreset by remember {
        mutableStateOf(
            runCatching {
                UiDensityPreset.valueOf(uiPrefs.getString("density_preset", UiDensityPreset.Comfortable.name).orEmpty())
            }.getOrDefault(UiDensityPreset.Comfortable),
        )
    }
    var glassStrengthPreset by remember {
        mutableStateOf(
            runCatching {
                GlassStrengthPreset.valueOf(uiPrefs.getString("glass_preset", GlassStrengthPreset.Balanced.name).orEmpty())
            }.getOrDefault(GlassStrengthPreset.Balanced),
        )
    }
    var liquidGlassStylePreset by remember {
        mutableStateOf(
            runCatching {
                LiquidGlassStylePreset.valueOf(
                    uiPrefs.getString("liquid_glass_style", LiquidGlassStylePreset.IOS.name).orEmpty(),
                )
            }.getOrDefault(LiquidGlassStylePreset.IOS),
        )
    }

    return PoxiaoAppUiState(
        themePreset = themePreset,
        densityPreset = densityPreset,
        glassStrengthPreset = glassStrengthPreset,
        liquidGlassStylePreset = liquidGlassStylePreset,
        onThemePresetChange = {
            themePreset = it
            uiPrefs.edit().putString("theme_preset", it.name).apply()
        },
        onDensityPresetChange = {
            densityPreset = it
            uiPrefs.edit().putString("density_preset", it.name).apply()
        },
        onGlassStrengthChange = {
            glassStrengthPreset = it
            uiPrefs.edit().putString("glass_preset", it.name).apply()
        },
        onLiquidGlassStyleChange = {
            liquidGlassStylePreset = it
            uiPrefs.edit().putString("liquid_glass_style", it.name).apply()
        },
    )
}
