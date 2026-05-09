package com.poxiao.app.ui

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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.interactions.bouncyClick
import com.poxiao.app.ui.interactions.rememberHapticManager
import com.poxiao.app.ui.theme.PoxiaoThemeState

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
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp * densityPreset.scale,
        contentPadding = PaddingValues(24.dp * densityPreset.scale),
        tint = palette.card,
        borderColor = palette.cardBorder,
        glowColor = palette.cardGlow,
        blurRadius = 6.dp,
        refractionHeight = 6.dp,
        refractionAmount = 8.dp,
        highlightAlpha = 0.5f,
        content = content,
    )
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
                border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    modifier = Modifier.padding(
                        horizontal = 10.dp * densityPreset.scale,
                        vertical = 6.dp * densityPreset.scale,
                    ),
                )
            }
            Text(value, style = valueStyle, color = palette.ink)
        }
    }
    if (staticGlass) {
        Surface(
            shape = RoundedCornerShape(22.dp * densityPreset.scale),
            color = palette.card.copy(alpha = (0.3f * glassStrength.cardAlpha).coerceAtLeast(0.18f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.72f)),
            modifier = modifier.width(cardWidth * densityPreset.scale),
        ) {
            Column(modifier = Modifier.padding(14.dp * densityPreset.scale)) {
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
    val hapticManager = rememberHapticManager()
    val staticGlass = LocalStaticGlassMode.current
    val isLightAccent = background.luminance() > 0.72f
    val textColor = if (isLightAccent) palette.ink.copy(alpha = 0.84f) else background.copy(alpha = 0.96f)
    val staticTint = if (isLightAccent) Color.White.copy(alpha = 0.54f) else background.copy(alpha = 0.12f)
    val staticBorder = if (isLightAccent) palette.cardBorder.copy(alpha = 0.18f) else background.copy(alpha = 0.24f)
    val glassTint = if (isLightAccent) Color.White.copy(alpha = 0.22f) else background.copy(alpha = 0.14f)
    val glassBorder = if (isLightAccent) palette.cardBorder.copy(alpha = 0.26f) else background.copy(alpha = 0.18f)
    val glassGlow = if (isLightAccent) Color.Transparent else background.copy(alpha = 0.08f)
    if (staticGlass) {
        Surface(
            shape = RoundedCornerShape(22.dp * densityPreset.scale),
            color = staticTint,
            border = androidx.compose.foundation.BorderStroke(0.75.dp, staticBorder),
            shadowElevation = 1.dp,
            modifier = modifier.bouncyClick(hapticManager = hapticManager, onClick = onClick),
        ) {
            Box(
                modifier = Modifier.padding(
                    horizontal = 14.dp * densityPreset.scale,
                    vertical = 8.dp * densityPreset.scale,
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
            modifier = modifier.bouncyClick(hapticManager = hapticManager, onClick = onClick),
            cornerRadius = 22.dp * densityPreset.scale,
            contentPadding = PaddingValues(
                horizontal = 14.dp * densityPreset.scale,
                vertical = 8.dp * densityPreset.scale,
            ),
            tint = glassTint,
            borderColor = glassBorder,
            glowColor = glassGlow,
            blurRadius = 5.dp,
            refractionHeight = 5.dp,
            refractionAmount = 6.dp,
            highlightAlpha = 0.22f,
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
internal fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = PoxiaoThemeState.palette.primary,
    height: Dp = 52.dp,
) {
    val densityPreset = LocalUiDensityPreset.current
    val hapticManager = rememberHapticManager()
    val staticGlass = LocalStaticGlassMode.current
    val shape = RoundedCornerShape(22.dp * densityPreset.scale)
    val contentModifier = modifier
        .fillMaxWidth()
        .height(height * densityPreset.scale)
    val textColor = if (enabled) accent.copy(alpha = 0.98f) else accent.copy(alpha = 0.42f)
    val tint = if (enabled) accent.copy(alpha = 0.16f) else accent.copy(alpha = 0.08f)
    val borderColor = if (enabled) accent.copy(alpha = 0.18f) else accent.copy(alpha = 0.1f)
    val glowColor = if (enabled) accent.copy(alpha = 0.08f) else Color.Transparent

    if (staticGlass) {
        Surface(
            shape = shape,
            color = tint,
            border = androidx.compose.foundation.BorderStroke(0.9.dp, borderColor),
            modifier = contentModifier.then(
                if (enabled) Modifier.bouncyClick(hapticManager = hapticManager, onClick = onClick) else Modifier
            ),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = text, style = MaterialTheme.typography.labelLarge, color = textColor)
            }
        }
    } else {
        LiquidGlassSurface(
            modifier = contentModifier.then(
                if (enabled) Modifier.bouncyClick(hapticManager = hapticManager, onClick = onClick) else Modifier
            ),
            cornerRadius = 22.dp * densityPreset.scale,
            tint = tint,
            borderColor = borderColor,
            glowColor = glowColor,
            blurRadius = 5.dp,
            refractionHeight = 5.dp,
            refractionAmount = 6.dp,
            highlightAlpha = 0.22f,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
internal fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = PoxiaoThemeState.palette.ink,
    height: Dp = 52.dp,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val hapticManager = rememberHapticManager()
    val staticGlass = LocalStaticGlassMode.current
    val shape = RoundedCornerShape(22.dp * densityPreset.scale)
    val contentModifier = modifier
        .fillMaxWidth()
        .height(height * densityPreset.scale)
    val textColor = if (enabled) accent.copy(alpha = 0.8f) else accent.copy(alpha = 0.36f)
    val tint = if (enabled) palette.card.copy(alpha = 0.42f) else palette.card.copy(alpha = 0.24f)
    val borderColor = if (enabled) palette.cardBorder.copy(alpha = 0.38f) else palette.cardBorder.copy(alpha = 0.18f)

    Surface(
        shape = shape,
        color = tint,
        border = androidx.compose.foundation.BorderStroke(0.9.dp, borderColor),
        modifier = contentModifier.then(
            if (enabled) Modifier.bouncyClick(hapticManager = hapticManager, onClick = onClick) else Modifier
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = textColor)
        }
    }
}

@Composable
internal fun WheelPicker(
    title: String,
    values: List<Int>,
    selected: Int,
    label: (Int) -> String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val glassStrength = LocalGlassStrengthPreset.current
    Surface(
        shape = RoundedCornerShape(22.dp * densityPreset.scale),
        color = palette.card.copy(alpha = palette.card.alpha * glassStrength.cardAlpha),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.72f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp * densityPreset.scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp * densityPreset.scale),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = palette.softText)
            LazyColumn(
                modifier = Modifier
                    .height(172.dp * densityPreset.scale)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 18.dp * densityPreset.scale),
                verticalArrangement = Arrangement.spacedBy(8.dp * densityPreset.scale),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(values) { value ->
                    val active = value == selected
                    val hapticManager = rememberHapticManager()
                    Surface(
                        shape = RoundedCornerShape(18.dp * densityPreset.scale),
                        color = if (active) palette.primary else palette.card.copy(alpha = 0.72f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (active) palette.primary.copy(alpha = 0.24f) else palette.cardBorder.copy(alpha = 0.5f),
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp * densityPreset.scale)
                            .bouncyClick(hapticManager = hapticManager) { onSelect(value) },
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp * densityPreset.scale, vertical = 10.dp * densityPreset.scale),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label(value),
                                style = if (active) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                                color = if (active) palette.pillOn else palette.ink,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ToggleLine(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(shape = RoundedCornerShape(18.dp), color = palette.card.copy(alpha = 0.56f), border = androidx.compose.foundation.BorderStroke(0.5.dp, palette.cardBorder.copy(alpha = 0.18f))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = PineInk)
            Switch(checked = checked, onCheckedChange = onChange)
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
    val hapticManager = rememberHapticManager()
    Surface(
        shape = RoundedCornerShape(16.dp * densityPreset.scale),
        color = if (chosen) palette.primary else palette.card.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (chosen) palette.primary.copy(alpha = 0.24f) else palette.cardBorder.copy(alpha = 0.54f),
        ),
        modifier = Modifier.bouncyClick(hapticManager = hapticManager, onClick = onClick),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp * densityPreset.scale, vertical = 6.dp * densityPreset.scale),
            contentAlignment = Alignment.Center,
        ) {
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
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp * densityPreset.scale),
    ) {
        options.forEach { item ->
            val chosen = item == selected
            Surface(
                shape = RoundedCornerShape(18.dp * densityPreset.scale),
                color = if (chosen) palette.primary else palette.card.copy(alpha = 0.86f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (chosen) palette.primary.copy(alpha = 0.26f) else palette.cardBorder.copy(alpha = 0.58f),
                ),
                modifier = Modifier.clickable { onSelect(item) },
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp * densityPreset.scale, vertical = 9.dp * densityPreset.scale),
                    contentAlignment = Alignment.Center,
                ) {
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
