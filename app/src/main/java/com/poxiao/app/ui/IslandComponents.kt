package com.poxiao.app.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoThemeState

@Composable
internal fun IslandHint(
    text: String,
    icon: ImageVector,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val islandEasing = remember { CubicBezierEasing(0.2f, 0.92f, 0.22f, 1f) }
    val islandCloseEasing = remember { CubicBezierEasing(0.32f, 0f, 0.2f, 1f) }
    val expandedWidth = remember(text) {
        when (text.length) {
            0, 1 -> 102.dp
            2 -> 114.dp
            3 -> 126.dp
            else -> 138.dp
        }
    }
    val islandTint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.18f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.12f)
    }
    val islandBorder = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.2f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.36f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.28f)
    }
    val islandGlow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.04f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.03f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.24f)
    }
    val islandBlur = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> 6.dp
        LiquidGlassStylePreset.IOS -> 7.dp
        LiquidGlassStylePreset.Hyper -> 8.dp
    }
    val islandRefraction = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> 4.dp
        LiquidGlassStylePreset.IOS -> 7.dp
        LiquidGlassStylePreset.Hyper -> 10.dp
    }
    val transition = updateTransition(targetState = visible, label = "island-shell")
    val shellProgress by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = if (targetState) 400 else 300,
                easing = if (targetState) islandEasing else islandCloseEasing,
            )
        },
        label = "island-progress",
    ) { shown ->
        if (shown) 1f else 0f
    }
    val collapsedWidth = 38.dp
    val shellWidth = collapsedWidth + (expandedWidth - collapsedWidth) * shellProgress
    val shellAlpha = shellProgress.coerceIn(0f, 1f)
    val contentAlpha = if (visible) {
        ((shellProgress - 0.34f) / 0.66f).coerceIn(0f, 1f)
    } else {
        (shellProgress / 0.42f).coerceIn(0f, 1f)
    }
    val sheenWidthFraction = 0.1f + (0.34f - 0.1f) * shellProgress

    Box(
        modifier = modifier
            .requiredWidth(shellWidth)
            .requiredHeight(36.dp)
            .alpha(shellAlpha),
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxSize(),
            cornerRadius = 18.dp,
            shapeOverride = CircleShape,
            contentPadding = PaddingValues(0.dp),
            tint = islandTint,
            borderColor = islandBorder,
            glowColor = islandGlow,
            shadowColor = Color.Transparent,
            blurRadius = islandBlur,
            refractionHeight = islandBlur,
            refractionAmount = islandRefraction,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 3.dp)
                    .fillMaxWidth(sheenWidthFraction)
                    .requiredHeight(7.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(
                                    alpha = when (stylePreset) {
                                        LiquidGlassStylePreset.Harmony -> 0.12f
                                        LiquidGlassStylePreset.IOS -> 0.32f
                                        LiquidGlassStylePreset.Hyper -> 0.16f
                                    },
                                ),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .alpha(contentAlpha),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.pillOn.copy(alpha = 0.96f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(12.dp),
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.pillOn,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
        }
    }
}
