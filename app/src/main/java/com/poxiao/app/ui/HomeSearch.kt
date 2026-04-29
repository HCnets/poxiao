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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen

internal enum class HomeSearchCategory(val title: String) {
    Course("课程"),
    Note("笔记"),
    Todo("待办"),
    Grade("成绩"),
    Building("楼栋"),
}

internal data class HomeSearchResult(
    val id: String,
    val category: HomeSearchCategory,
    val title: String,
    val subtitle: String,
    val detail: String,
)

@Composable
internal fun SearchResultRow(
    result: HomeSearchResult,
    onClick: () -> Unit,
) {
    val accent = when (result.category) {
        HomeSearchCategory.Course -> ForestGreen
        HomeSearchCategory.Note -> PineInk
        HomeSearchCategory.Todo -> Ginkgo
        HomeSearchCategory.Grade -> MossGreen
        HomeSearchCategory.Building -> TeaGreen
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PineInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${result.category.title} · ${result.subtitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForestDeep.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = result.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.64f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ActionPill("进入", accent, onClick = onClick)
        }
    }
}
