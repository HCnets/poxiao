package com.poxiao.app.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    with(scaffoldState) {
        LiquidGlassScene(modifier = Modifier.fillMaxSize()) {
            if (overlayPage != null) {
                PoxiaoOverlayHost(
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
            } else {
                PoxiaoSectionHost(scaffoldState)
            }

            IslandHint(
                text = overlayPage?.label ?: section.label,
                icon = section.icon,
                visible = islandVisible && overlayPage == null && !sideNavExpanded,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp),
            )

            if (overlayPage == null) {
                PoxiaoAppScaffoldControls(scaffoldState)
            }
        }
    }
}
