package com.poxiao.app.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateBottomPadding
import androidx.compose.foundation.layout.calculateTopPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.BambooGlass
import com.poxiao.app.ui.theme.CloudWhite
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.requiredWidthIn

@Composable
internal fun SideNavToggleButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val tint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.2f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.14f)
    }
    val border = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.24f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.34f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.28f)
    }
    val glow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.04f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.03f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.12f)
    }
    val lineColor = if (expanded) palette.primary else palette.pillOn
    val interactionSource = remember { MutableInteractionSource() }

    LiquidGlassSurface(
        modifier = modifier
            .size(36.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        cornerRadius = 18.dp,
        shapeOverride = CircleShape,
        contentPadding = PaddingValues(0.dp),
        tint = tint,
        borderColor = border,
        glowColor = glow,
        shadowColor = Color.Transparent,
        blurRadius = 10.dp,
        refractionHeight = 8.dp,
        refractionAmount = 10.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.requiredWidth(16.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                listOf(14.dp, if (expanded) 10.dp else 14.dp, 14.dp).forEach { width ->
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(lineColor.copy(alpha = 0.96f)),
                    )
                }
            }
        }
    }
}

@Composable
internal fun SideNavigationDrawer(
    expanded: Boolean,
    currentSection: PrimarySection,
    currentOverlay: OverlayPage?,
    onDismiss: () -> Unit,
    onSelectSection: (PrimarySection) -> Unit,
    onOpenOverlay: (OverlayPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val panelEasing = remember { CubicBezierEasing(0.2f, 0.92f, 0.22f, 1f) }
    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 340, easing = panelEasing),
        label = "side-nav-progress",
    )

    val scrimAlpha = 0.28f * progress
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val panelTint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.2f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.03f)
        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.12f)
    }
    val panelBorder = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.24f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.34f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.28f)
    }
    val panelGlow = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.04f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.025f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.14f)
    }
    Box(
        modifier = modifier.drawWithContent {
            if (progress > 0.001f) {
                drawContent()
            }
        },
    ) {
        val panelModifier = if (progress > 0.001f || expanded) {
            Modifier
                .align(Alignment.CenterStart)
                .padding(
                    start = 12.dp,
                    top = statusTop + 8.dp,
                    bottom = navigationBottom + 8.dp,
                )
                .fillMaxHeight()
                .requiredWidth(308.dp)
                .offset(x = (-28.dp) * (1f - progress))
                .alpha((0.001f + 0.999f * progress).coerceIn(0f, 1f))
                .scale(0.96f + 0.04f * progress)
        } else {
            Modifier
                .requiredWidth(1.dp)
                .requiredHeight(1.dp)
                .offset(x = (-640).dp, y = (-640).dp)
                .alpha(0f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .then(
                    if (progress > 0.001f) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(onTap = { onDismiss() })
                        }
                    } else {
                        Modifier
                    },
                ),
        )

        LiquidGlassSurface(
            modifier = panelModifier,
            cornerRadius = 34.dp,
            shapeOverride = RoundedCornerShape(34.dp),
            contentPadding = PaddingValues(0.dp),
            tint = panelTint,
            borderColor = panelBorder,
            glowColor = panelGlow,
            blurRadius = 16.dp,
            refractionHeight = 12.dp,
            refractionAmount = 16.dp,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SideNavHeader(
                        currentLabel = currentOverlay?.label ?: sectionDisplayTitle(currentSection),
                    )
                }
                item {
                    SideNavGroupTitle(title = "主分区", detail = "5 项")
                }
                items(
                    listOf(
                        PrimarySection.Home,
                        PrimarySection.Schedule,
                        PrimarySection.Todo,
                        PrimarySection.Pomodoro,
                        PrimarySection.More,
                    ),
                ) { item ->
                    SideNavEntry(
                        title = sectionDisplayTitle(item),
                        icon = item.icon,
                        accent = sideNavAccentForSection(item),
                        active = currentOverlay == null && currentSection == item,
                        onClick = { onSelectSection(item) },
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    SideNavGroupTitle(title = "快捷入口", detail = "8 项")
                }
                item {
                    SideNavEntry(
                        title = "课程笔记",
                        accent = MossGreen,
                        active = currentOverlay == OverlayPage.CourseNotes,
                        onClick = { onOpenOverlay(OverlayPage.CourseNotes) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "复习计划",
                        accent = TeaGreen,
                        active = currentOverlay == OverlayPage.ReviewPlanner,
                        onClick = { onOpenOverlay(OverlayPage.ReviewPlanner) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "学习数据",
                        accent = Ginkgo,
                        active = currentOverlay == OverlayPage.LearningDashboard,
                        onClick = { onOpenOverlay(OverlayPage.LearningDashboard) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "导出中心",
                        accent = WarmMist,
                        active = currentOverlay == OverlayPage.ExportCenter,
                        onClick = { onOpenOverlay(OverlayPage.ExportCenter) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "校园服务",
                        accent = BambooGlass,
                        active = currentOverlay == OverlayPage.CampusServices || currentOverlay == OverlayPage.CampusMap,
                        onClick = { onOpenOverlay(OverlayPage.CampusServices) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "科学计算器",
                        accent = CloudWhite,
                        active = currentOverlay == OverlayPage.Calculator,
                        onClick = { onOpenOverlay(OverlayPage.Calculator) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "通知偏好",
                        accent = ForestGreen,
                        active = currentOverlay == OverlayPage.NotificationPreferences,
                        onClick = { onOpenOverlay(OverlayPage.NotificationPreferences) },
                    )
                }
                item {
                    SideNavEntry(
                        title = "界面偏好",
                        accent = palette.primary,
                        active = currentOverlay == OverlayPage.Preferences,
                        onClick = { onOpenOverlay(OverlayPage.Preferences) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SideNavGroupTitle(
    title: String,
    detail: String,
) {
    val palette = PoxiaoThemeState.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = palette.softText,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = palette.softText.copy(alpha = 0.68f),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            palette.cardBorder.copy(alpha = 0.4f),
                            palette.cardBorder.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun SideNavHeader(
    currentLabel: String,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = palette.card.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.62f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.primary.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.primary.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.2f)),
                ) {
                    Text(
                        text = "全局导航",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(palette.primary.copy(alpha = 0.88f)),
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = currentLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.ink,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SideNavHeaderChip(
                    title = "主分区",
                    value = "5",
                )
                SideNavHeaderChip(
                    title = "快捷入口",
                    value = "8",
                )
            }
        }
    }
}

@Composable
private fun SideNavHeaderChip(
    title: String,
    value: String,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = palette.ink,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = palette.softText,
            )
        }
    }
}

@Composable
private fun SideNavEntry(
    title: String,
    subtitle: String = "",
    accent: Color,
    active: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val palette = PoxiaoThemeState.palette
    val shape = RoundedCornerShape(26.dp)
    val backgroundBrush = if (active) {
        Brush.linearGradient(
            listOf(
                accent.copy(alpha = 0.16f),
                accent.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.1f),
            ),
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.1f),
                palette.card.copy(alpha = 0.28f),
            ),
        )
    }
    val border = if (active) accent.copy(alpha = 0.3f) else palette.cardBorder.copy(alpha = 0.48f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = if (active) 0.24f else 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = if (active) 0.24f else 0.16f)),
                ) {
                    Box(
                        modifier = Modifier.size(if (active) 38.dp else 34.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (active) accent else palette.ink.copy(alpha = 0.78f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accent.copy(alpha = 0.88f),
                                    accent.copy(alpha = 0.42f),
                                ),
                            ),
                        ),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.ink,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.softText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (active) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (active) accent.copy(alpha = 0.18f) else palette.cardBorder.copy(alpha = 0.4f)),
            ) {
                Text(
                    text = if (active) "当前" else "进入",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) accent else palette.softText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                )
            }
        }
    }
}

internal fun sectionDisplayTitle(section: PrimarySection): String =
    when (section) {
        PrimarySection.Home -> "智能体"
        else -> section.label
    }

internal fun sideNavAccentForSection(section: PrimarySection): Color =
    when (section) {
        PrimarySection.Home -> ForestGreen
        PrimarySection.Schedule -> TeaGreen
        PrimarySection.Todo -> WarmMist
        PrimarySection.Pomodoro -> Ginkgo
        PrimarySection.More -> BambooGlass
    }
