package com.poxiao.app.ui

internal fun buildPoxiaoScaffoldControlActions(
    scaffoldState: PoxiaoAppScaffoldState,
): PoxiaoScaffoldControlActions =
    PoxiaoScaffoldControlActions(
        onToggleSideNav = scaffoldState::toggleSideNav,
        onDismissDrawer = scaffoldState::dismissDrawer,
        onSelectSection = scaffoldState::queueSectionSelection,
        onOpenOverlay = scaffoldState::queueOverlayOpen,
        onSelectBottomSection = scaffoldState::selectBottomSection,
    )
