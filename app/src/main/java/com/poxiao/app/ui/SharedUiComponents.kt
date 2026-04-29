package com.poxiao.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoThemeState

@Composable
internal fun ToggleLine(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.56f)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = PineInk)
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
internal fun ScreenColumn(
    content: LazyListScope.() -> Unit,
) {
    val densityPreset = LocalUiDensityPreset.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 18.dp * densityPreset.scale,
            end = 18.dp * densityPreset.scale,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 60.dp * densityPreset.scale,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 110.dp * densityPreset.scale,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp * densityPreset.scale),
        content = content,
    )
}

@Composable
internal fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val glassStrength = LocalGlassStrengthPreset.current
    val staticGlass = LocalStaticGlassMode.current
    if (staticGlass) {
        Surface(
            shape = RoundedCornerShape(30.dp * densityPreset.scale),
            color = palette.card.copy(alpha = (palette.card.alpha * glassStrength.cardAlpha).coerceIn(0f, 1f)),
            border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.76f)),
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp * densityPreset.scale),
                content = content,
            )
        }
    } else {
        LiquidGlassCard(
            modifier = modifier.fillMaxWidth(),
            cornerRadius = 30.dp * densityPreset.scale,
            contentPadding = PaddingValues(20.dp * densityPreset.scale),
            tint = palette.card.copy(alpha = palette.card.alpha * glassStrength.cardAlpha),
            borderColor = palette.cardBorder.copy(alpha = 0.82f),
            glowColor = palette.cardGlow.copy(alpha = palette.cardGlow.alpha * glassStrength.glowScale),
            blurRadius = 12.dp,
            refractionHeight = 12.dp,
            refractionAmount = 18.dp,
            content = content,
        )
    }
}

@Composable
internal fun MetricCard(
    title: String,
    value: String,
    accent: Color,
    sizePreset: HomeModuleSize = HomeModuleSize.Standard,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val glassStrength = LocalGlassStrengthPreset.current
    val staticGlass = LocalStaticGlassMode.current
    val cardWidth = when (sizePreset) {
        HomeModuleSize.Compact -> 96.dp
        HomeModuleSize.Standard -> 106.dp
        HomeModuleSize.Hero -> 124.dp
    }
    val valueStyle = when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.titleSmall
        HomeModuleSize.Standard -> MaterialTheme.typography.titleMedium
        HomeModuleSize.Hero -> MaterialTheme.typography.titleLarge
    }
    val metricContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                    ),
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp * densityPreset.scale),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp * densityPreset.scale),
                color = accent.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 10.dp * densityPreset.scale, vertical = 6.dp * densityPreset.scale),
                )
            }
            Text(value, style = valueStyle, color = palette.ink)
        }
    }
    if (staticGlass) {
        Surface(
            shape = RoundedCornerShape(22.dp * densityPreset.scale),
            color = palette.card.copy(alpha = (0.3f * glassStrength.cardAlpha).coerceAtLeast(0.18f)),
            border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.72f)),
            modifier = modifier.width(cardWidth * densityPreset.scale),
        ) {
            Column(
                modifier = Modifier.padding(14.dp * densityPreset.scale),
            ) {
                metricContent()
            }
        }
    } else {
        LiquidGlassSurface(
            modifier = modifier.width(cardWidth * densityPreset.scale),
            cornerRadius = 22.dp * densityPreset.scale,
            contentPadding = PaddingValues(14.dp * densityPreset.scale),
            tint = palette.card.copy(alpha = 0.3f * glassStrength.cardAlpha),
            borderColor = palette.cardBorder.copy(alpha = 0.78f),
            glowColor = accent.copy(alpha = 0.22f * glassStrength.glowScale),
            blurRadius = 10.dp,
            refractionHeight = 10.dp,
            refractionAmount = 14.dp,
        ) {
            metricContent()
        }
    }
}

@Composable
internal fun ActionPill(
    text: String,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val staticGlass = LocalStaticGlassMode.current
    val textColor = if (background.red * 0.299f + background.green * 0.587f + background.blue * 0.114f > 0.62f) palette.ink else palette.pillOn
    if (staticGlass) {
        Surface(
            shape = RoundedCornerShape(22.dp * densityPreset.scale),
            color = background.copy(alpha = 0.22f),
            border = BorderStroke(1.dp, background.copy(alpha = 0.28f)),
            modifier = modifier.clickable(onClick = onClick),
        ) {
            Box(
                modifier = Modifier.padding(
                    horizontal = 14.dp * densityPreset.scale,
                    vertical = 10.dp * densityPreset.scale,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LiquidGlassSurface(
            modifier = modifier.clickable(onClick = onClick),
            cornerRadius = 22.dp * densityPreset.scale,
            contentPadding = PaddingValues(
                horizontal = 14.dp * densityPreset.scale,
                vertical = 10.dp * densityPreset.scale,
            ),
            tint = background.copy(alpha = 0.3f),
            borderColor = background.copy(alpha = 0.36f),
            glowColor = background.copy(alpha = 0.28f),
            blurRadius = 8.dp,
            refractionHeight = 8.dp,
            refractionAmount = 12.dp,
        ) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
internal fun SelectionChip(
    text: String,
    chosen: Boolean,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    Surface(
        shape = RoundedCornerShape(16.dp * densityPreset.scale),
        color = if (chosen) palette.primary else palette.card.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, if (chosen) palette.primary.copy(alpha = 0.24f) else palette.cardBorder.copy(alpha = 0.54f)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.padding(horizontal = 10.dp * densityPreset.scale, vertical = 6.dp * densityPreset.scale), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = if (chosen) palette.pillOn else palette.ink,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun <T> SelectionRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp * densityPreset.scale)) {
        options.forEach { item ->
            val chosen = item == selected
            Surface(
                shape = RoundedCornerShape(18.dp * densityPreset.scale),
                color = if (chosen) palette.primary else palette.card.copy(alpha = 0.86f),
                border = BorderStroke(1.dp, if (chosen) palette.primary.copy(alpha = 0.26f) else palette.cardBorder.copy(alpha = 0.58f)),
                modifier = Modifier.clickable { onSelect(item) },
            ) {
                Box(modifier = Modifier.padding(horizontal = 12.dp * densityPreset.scale, vertical = 9.dp * densityPreset.scale), contentAlignment = Alignment.Center) {
                    Text(
                        label(item),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (chosen) palette.pillOn else palette.ink,
                    )
                }
            }
        }
    }
}
