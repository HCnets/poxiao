package com.poxiao.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoPalette
import com.poxiao.app.ui.theme.PoxiaoThemeState

@Composable
internal fun MoreAccountCard(
    summary: MoreAccountSummary,
    onClick: () -> Unit,
) {
    val palette: PoxiaoPalette = PoxiaoThemeState.palette
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AcademicAvatarBadge(
                displayName = summary.displayName,
                studentId = if (summary.hasBoundAccount) summary.summaryStudentId else "",
                avatarUri = if (summary.hasBoundAccount) summary.accountProfile.avatarUri else "",
                accent = palette.primary,
                size = 64.dp,
            )
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(summary.displayName, style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Text(
                    text = if (summary.hasBoundAccount) "学号 ${summary.summaryStudentId}" else "未登录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.softText,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = summary.accountStatusAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, summary.accountStatusAccent.copy(alpha = 0.18f)),
                ) {
                    Text(
                        text = summary.accountStatusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = summary.accountStatusAccent,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "进入",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.primary,
                    )
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.softText.copy(alpha = 0.72f),
                    )
                }
            }
        }
    }
}
