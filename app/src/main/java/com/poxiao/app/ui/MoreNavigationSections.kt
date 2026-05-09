package com.poxiao.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoThemeState

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
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
                modifier = Modifier.padding(bottom = 2.dp),
            )
            items.forEachIndexed { index, item ->
                MoreNavigationRow(item = item)
                if (index != items.lastIndex) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .height(0.5.dp)
                            .background(palette.cardBorder.copy(alpha = 0.14f)),
                    )
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
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = item.onClick,
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            item.accent.copy(alpha = 0.16f),
                            item.accent.copy(alpha = 0.06f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.accent,
                modifier = Modifier.size(21.dp),
            )
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = palette.ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.actionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = item.accent.copy(alpha = 0.78f),
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleMedium,
            color = palette.softText.copy(alpha = 0.36f),
        )
    }
}
