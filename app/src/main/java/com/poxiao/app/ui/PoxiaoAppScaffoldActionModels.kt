package com.poxiao.app.ui

internal data class PoxiaoScaffoldControlActions(
    val onToggleSideNav: () -> Unit,
    val onDismissDrawer: () -> Unit,
    val onSelectSection: (PrimarySection) -> Unit,
    val onOpenOverlay: (OverlayPage) -> Unit,
    val onSelectBottomSection: (PrimarySection) -> Unit,
)
