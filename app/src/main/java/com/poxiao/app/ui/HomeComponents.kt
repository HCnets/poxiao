package com.poxiao.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist

@Composable
internal fun HomeLine(
    time: String,
    title: String,
    body: String,
    sizePreset: HomeModuleSize = HomeModuleSize.Standard,
    modifier: Modifier = Modifier,
) {
    val titleStyle = when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.bodyLarge
        HomeModuleSize.Standard -> MaterialTheme.typography.titleMedium
        HomeModuleSize.Hero -> MaterialTheme.typography.titleLarge
    }
    val bodyStyle = when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.bodySmall
        HomeModuleSize.Standard -> MaterialTheme.typography.bodyMedium
        HomeModuleSize.Hero -> MaterialTheme.typography.bodyLarge
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (sizePreset == HomeModuleSize.Hero) 14.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(if (sizePreset == HomeModuleSize.Hero) 12.dp else if (sizePreset == HomeModuleSize.Compact) 8.dp else 10.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(TeaGreen, ForestGreen))),
        )
        Column {
            Text("$time  $title", style = titleStyle, color = PineInk)
            Text(body, style = bodyStyle, color = ForestDeep.copy(alpha = 0.72f))
        }
    }
}

@Composable
internal fun HomeHeroStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = accent)
            Text(value, style = MaterialTheme.typography.titleMedium, color = PineInk)
        }
    }
}

@Composable
internal fun HomeQuickEntry(
    title: String,
    subtitle: String,
    accent: Color,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(156.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = PineInk,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.68f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun HomeModuleHeader(
    title: String,
    collapsed: Boolean,
    collapsible: Boolean,
    sizePreset: HomeModuleSize,
    onToggleCollapsed: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = homeSectionTitleStyle(sizePreset), color = PineInk)
        if (collapsible) {
            ActionPill(
                text = if (collapsed) "展开" else "收起",
                background = WarmMist,
                onClick = onToggleCollapsed,
            )
        }
    }
}

internal fun homeMetricSpacing(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 10.dp
        HomeModuleSize.Standard -> 12.dp
        HomeModuleSize.Hero -> 14.dp
    }

internal fun homeSectionSpacing(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 8.dp
        HomeModuleSize.Standard -> 12.dp
        HomeModuleSize.Hero -> 14.dp
    }

internal fun homeSecondarySpacing(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 6.dp
        HomeModuleSize.Standard -> 8.dp
        HomeModuleSize.Hero -> 10.dp
    }

internal fun homeLineGap(sizePreset: HomeModuleSize): Dp =
    when (sizePreset) {
        HomeModuleSize.Compact -> 8.dp
        HomeModuleSize.Standard -> 10.dp
        HomeModuleSize.Hero -> 12.dp
    }

@Composable
internal fun homeSectionTitleStyle(sizePreset: HomeModuleSize) =
    when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.titleMedium
        HomeModuleSize.Standard -> MaterialTheme.typography.titleLarge
        HomeModuleSize.Hero -> MaterialTheme.typography.headlineSmall
    }

@Composable
internal fun homeSectionBodyStyle(sizePreset: HomeModuleSize) =
    when (sizePreset) {
        HomeModuleSize.Compact -> MaterialTheme.typography.bodyMedium
        HomeModuleSize.Standard -> MaterialTheme.typography.bodyLarge
        HomeModuleSize.Hero -> MaterialTheme.typography.titleMedium
    }
