package com.poxiao.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

internal data class MoreNavigationItem(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val actionLabel: String,
    val onClick: () -> Unit,
)
