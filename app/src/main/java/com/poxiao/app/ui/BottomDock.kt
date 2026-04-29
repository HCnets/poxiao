package com.poxiao.app.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoThemeState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

@Composable
internal fun BottomDock(
    current: PrimarySection,
    onSelect: (PrimarySection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val densityPreset = LocalUiDensityPreset.current
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val dockHeight = 80.dp
    val dockCorner = 40.dp
    val selectionCorner = 32.dp
    val dockTint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.16f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.1f)
    }
    val dockBorder = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.24f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.34f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.24f)
    }
    val dockGlow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.03f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.1f)
    }
    val items = remember {
        listOf(
            PrimarySection.Schedule,
            PrimarySection.Todo,
            PrimarySection.Home,
            PrimarySection.Pomodoro,
            PrimarySection.More,
        )
    }
    var visualCurrent by remember { mutableStateOf(current) }
    var transitionFrom by remember { mutableStateOf(current) }
    val itemWeights = remember { listOf(1f, 1f, 1.42f, 1f, 1f) }
    LaunchedEffect(current) {
        if (visualCurrent != current) {
            visualCurrent = current
        }
        transitionFrom = current
    }
    val selectedIndex = remember(visualCurrent, items) { items.indexOf(visualCurrent).coerceAtLeast(0) }
    val originIndex = remember(transitionFrom, items) { items.indexOf(transitionFrom).coerceAtLeast(0) }
    val bookTurnEasing = remember { CubicBezierEasing(0.12f, 0.86f, 0.18f, 1f) }
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = 360, easing = bookTurnEasing),
        label = "dock-book-index",
    )
    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(dockHeight * densityPreset.scale),
        cornerRadius = dockCorner * densityPreset.scale,
        shapeOverride = CircleShape,
        contentPadding = PaddingValues(
            horizontal = 8.dp * densityPreset.scale,
            vertical = 8.dp * densityPreset.scale,
        ),
        tint = dockTint,
        borderColor = dockBorder,
        glowColor = dockGlow,
        blurRadius = if (stylePreset == LiquidGlassStylePreset.Hyper) 13.dp else 14.dp,
        refractionHeight = 10.dp,
        refractionAmount = 14.dp,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val slotSpacing = 4.dp * densityPreset.scale
            val availableWidth = maxWidth - slotSpacing * items.lastIndex.toFloat()
            val totalWeight = itemWeights.sum()
            val slotWidths = itemWeights.map { availableWidth * (it / totalWeight) }
            fun slotStart(index: Int): Dp {
                var result = 0.dp
                for (slotIndex in 0 until index) {
                    result += slotWidths[slotIndex] + slotSpacing
                }
                return result
            }

            val leadingIndex = floor(animatedIndex).toInt().coerceIn(0, items.lastIndex)
            val trailingIndex = ceil(animatedIndex).toInt().coerceIn(0, items.lastIndex)
            val travelFraction = (animatedIndex - leadingIndex).coerceIn(0f, 1f)
            val turnArch = sin(travelFraction * PI).toFloat()
            val routeSpan = abs(selectedIndex - originIndex).coerceAtLeast(1).toFloat()
            val routeProgress = if (selectedIndex == originIndex) {
                1f
            } else {
                (abs(animatedIndex - originIndex) / routeSpan).coerceIn(0f, 1f)
            }
            val pillLeft = slotStart(leadingIndex) + (slotStart(trailingIndex) - slotStart(leadingIndex)) * travelFraction
            val pillWidth =
                slotWidths[leadingIndex] +
                    (slotWidths[trailingIndex] - slotWidths[leadingIndex]) * travelFraction +
                    (12.dp * densityPreset.scale) * turnArch
            val pillGlow = when (stylePreset) {
                LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.08f + turnArch * 0.05f)
                LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.1f + turnArch * 0.06f)
                LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.18f + turnArch * 0.12f)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassSurface(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .offset(x = pillLeft)
                        .requiredWidth(pillWidth),
                    cornerRadius = selectionCorner * densityPreset.scale,
                    shapeOverride = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    tint = palette.card.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.18f
                            LiquidGlassStylePreset.IOS -> 0.06f
                            LiquidGlassStylePreset.Hyper -> 0.2f
                        },
                    ),
                    borderColor = when (stylePreset) {
                        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.2f + turnArch * 0.04f)
                        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.28f + turnArch * 0.08f)
                        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.2f + turnArch * 0.08f)
                    },
                    glowColor = pillGlow,
                    blurRadius = when (stylePreset) {
                        LiquidGlassStylePreset.Harmony -> 11.dp
                        LiquidGlassStylePreset.IOS -> 13.dp
                        LiquidGlassStylePreset.Hyper -> 12.dp
                    },
                    refractionHeight = 8.dp,
                    refractionAmount = when (stylePreset) {
                        LiquidGlassStylePreset.Harmony -> 7.dp + 3.dp * turnArch
                        LiquidGlassStylePreset.IOS -> 9.dp + 5.dp * turnArch
                        LiquidGlassStylePreset.Hyper -> 10.dp + 6.dp * turnArch
                    },
                ) {}

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(slotSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEachIndexed { index, item ->
                        val selectionProgress = when {
                            selectedIndex == originIndex && index == selectedIndex -> 1f
                            index == originIndex -> 1f - routeProgress
                            index == selectedIndex -> routeProgress
                            else -> 0f
                        }
                        DockNavItem(
                            item = item,
                            active = visualCurrent == item,
                            selectedProgress = selectionProgress,
                            onPreviewSelect = { target ->
                                if (target != visualCurrent) {
                                    transitionFrom = visualCurrent
                                    visualCurrent = target
                                }
                            },
                            onCancelPreview = {
                                if (visualCurrent != current) {
                                    transitionFrom = visualCurrent
                                    visualCurrent = current
                                }
                            },
                            onSelect = { target ->
                                if (target != current) {
                                    onSelect(target)
                                }
                            },
                            emphasized = item == PrimarySection.Home,
                            modifier = Modifier.weight(itemWeights[index]),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockNavItem(
    item: PrimarySection,
    active: Boolean,
    selectedProgress: Float,
    onPreviewSelect: (PrimarySection) -> Unit,
    onCancelPreview: () -> Unit,
    onSelect: (PrimarySection) -> Unit,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val densityPreset = LocalUiDensityPreset.current
    val stylePreset = LocalLiquidGlassStylePreset.current
    val contentScale = if (emphasized) 1.02f + 0.12f * selectedProgress else 1f + 0.07f * selectedProgress
    val iconScale = if (emphasized) 1.02f + 0.16f * selectedProgress else 1f + 0.1f * selectedProgress
    val contentOffset = (-1.5f).dp * selectedProgress * densityPreset.scale
    val baseTint = if (emphasized) {
        palette.pillOn.copy(alpha = 0.95f)
    } else {
        palette.pillOn.copy(alpha = 0.8f)
    }
    val contentTint = androidx.compose.ui.graphics.lerp(baseTint, Color.White, selectedProgress)
    val captionAlpha = if (emphasized) 0.96f else 0.78f + 0.22f * selectedProgress
    val emphasisGlow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.92f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.9f)
        LiquidGlassStylePreset.Hyper -> palette.secondary
    }
    val contentWidth = if (emphasized) 72.dp * densityPreset.scale else 54.dp * densityPreset.scale
    val iconShellSize = if (emphasized) 38.dp * densityPreset.scale else 30.dp * densityPreset.scale
    val iconSize = (if (emphasized) 24.dp else 18.dp) * densityPreset.scale
    val labelLift = if (emphasized) (-1.5).dp * densityPreset.scale else 0.dp
    val latestPreviewSelect = rememberUpdatedState(onPreviewSelect)
    val latestCancelPreview = rememberUpdatedState(onCancelPreview)
    val latestSelect = rememberUpdatedState(onSelect)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .pointerInput(item) {
                detectTapGestures(
                    onPress = {
                        latestPreviewSelect.value(item)
                        val released = tryAwaitRelease()
                        if (released) {
                            latestSelect.value(item)
                        } else {
                            latestCancelPreview.value()
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (emphasized) {
            CenterDockAuraLayer(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                selected = active,
                pulse = 0.96f + 0.1f * selectedProgress,
                auraAlpha = 0.2f + 0.28f * selectedProgress,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(contentWidth)
                .offset(y = contentOffset)
                .scale(contentScale)
                .padding(horizontal = 2.dp * densityPreset.scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(iconShellSize)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                emphasisGlow.copy(alpha = if (emphasized) 0.12f + 0.18f * selectedProgress else 0.05f * selectedProgress),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.navLabel,
                    tint = contentTint,
                    modifier = Modifier
                        .size(iconSize)
                        .scale(iconScale),
                )
            }
            Spacer(modifier = Modifier.height(2.dp * densityPreset.scale))
            Text(
                text = item.navLabel,
                style = if (emphasized) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                color = contentTint.copy(alpha = captionAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = labelLift),
            )
        }
    }
}

@Composable
private fun CenterDockAuraLayer(
    selected: Boolean,
    pulse: Float,
    auraAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val transition = rememberInfiniteTransition(label = "dock-center-aura")
    val orbitDegrees by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (selected) 4800 else 6200,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dock-center-orbit",
    )
    val shimmer by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dock-center-shimmer",
    )
    val particleAlpha by animateFloatAsState(
        targetValue = if (selected) 0.94f else 0.56f,
        animationSpec = tween(durationMillis = 260, easing = CubicBezierEasing(0.18f, 0.9f, 0.24f, 1f)),
        label = "dock-center-particles",
    )
    val baseColor = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White
        LiquidGlassStylePreset.IOS -> Color.White
        LiquidGlassStylePreset.Hyper -> palette.secondary
    }
    Canvas(modifier = modifier) {
        val coreCenter = center
        val coreRadius = size.minDimension * 0.46f * pulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = auraAlpha * 0.52f * shimmer),
                    baseColor.copy(alpha = auraAlpha * 0.68f),
                    Color.Transparent,
                ),
                center = coreCenter,
                radius = coreRadius,
            ),
            radius = coreRadius,
            center = coreCenter,
        )
        drawCircle(
            color = baseColor.copy(alpha = auraAlpha * 0.18f),
            radius = size.minDimension * 0.31f,
            center = coreCenter,
            style = Stroke(width = size.minDimension * 0.026f),
        )

        val particles = listOf(
            Triple(0f, 0.28f, 0.09f),
            Triple(128f, 0.36f, 0.075f),
            Triple(248f, 0.23f, 0.065f),
        )
        particles.forEachIndexed { index, (offsetDegrees, orbitFactor, radiusFactor) ->
            val angle = Math.toRadians((orbitDegrees + offsetDegrees).toDouble())
            val particleCenter = Offset(
                x = coreCenter.x + cos(angle).toFloat() * size.minDimension * orbitFactor,
                y = coreCenter.y + sin(angle).toFloat() * size.minDimension * orbitFactor * 0.72f,
            )
            val glowRadius = size.minDimension * radiusFactor * shimmer
            val particleColor = if (index == 0) {
                Color.White
            } else {
                baseColor
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        particleColor.copy(alpha = particleAlpha * (0.62f - index * 0.12f)),
                        baseColor.copy(alpha = particleAlpha * (0.34f - index * 0.06f)),
                        Color.Transparent,
                    ),
                    center = particleCenter,
                    radius = glowRadius * 2.7f,
                ),
                radius = glowRadius * 2.7f,
                center = particleCenter,
            )
        }
    }
}
