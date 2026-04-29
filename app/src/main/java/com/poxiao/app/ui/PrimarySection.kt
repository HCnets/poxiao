package com.poxiao.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.ViewKanban
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class PrimarySection(
    val label: String,
    val navLabel: String,
    val icon: ImageVector,
) {
    Home("首页", "智能体", Icons.Rounded.AutoAwesome),
    Schedule("课表", "课表", Icons.Rounded.CalendarMonth),
    Todo("待办", "待办", Icons.Rounded.ViewKanban),
    Pomodoro("番茄钟", "番茄钟", Icons.Rounded.Timer),
    More("更多", "更多", Icons.Rounded.GridView),
}
