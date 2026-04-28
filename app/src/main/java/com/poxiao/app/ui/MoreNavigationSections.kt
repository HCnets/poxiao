package com.poxiao.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoThemeState

internal data class MoreNavigationItem(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val actionLabel: String,
    val onClick: () -> Unit,
)

@Composable
internal fun MoreNavigationSections(
    learningEntries: List<MoreNavigationItem>,
    toolEntries: List<MoreNavigationItem>,
) {
    MoreNavigationSectionCard(
        title = "学习与校园",
        items = learningEntries,
    )
    MoreNavigationSectionCard(
        title = "工具与偏好",
        items = toolEntries,
    )
}

@Composable
private fun MoreNavigationSectionCard(
    title: String,
    items: List<MoreNavigationItem>,
) {
    val palette = PoxiaoThemeState.palette
    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = palette.ink)
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.58f)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items.forEachIndexed { index, item ->
                        MoreNavigationRow(item = item)
                        if (index != items.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(palette.cardBorder.copy(alpha = 0.38f)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreNavigationRow(
    item: MoreNavigationItem,
) {
    val palette = PoxiaoThemeState.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(item.accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.accent,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = palette.ink,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = item.accent,
            )
            Text(
                text = "›",
                style = MaterialTheme.typography.titleMedium,
                color = palette.softText.copy(alpha = 0.72f),
            )
        }
    }
}
