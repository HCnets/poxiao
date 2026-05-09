package com.poxiao.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.WarmMist

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
