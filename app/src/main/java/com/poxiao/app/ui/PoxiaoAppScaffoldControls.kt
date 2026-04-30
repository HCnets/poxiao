package com.poxiao.app.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
internal fun BoxScope.PoxiaoAppScaffoldControls(
    scaffoldState: PoxiaoAppScaffoldState,
) {
    val controlActions = buildPoxiaoScaffoldControlActions(scaffoldState)

    with(scaffoldState) {
        SideNavToggleButton(
            expanded = sideNavExpanded,
            onClick = controlActions.onToggleSideNav,
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(5f)
                .padding(
                    start = 16.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp,
                ),
        )

        SideNavigationDrawer(
            expanded = sideNavExpanded,
            currentSection = section,
            currentOverlay = overlayPage,
            onDismiss = controlActions.onDismissDrawer,
            onSelectSection = controlActions.onSelectSection,
            onOpenOverlay = controlActions.onOpenOverlay,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(4f),
        )

        BottomDock(
            current = section,
            onSelect = controlActions.onSelectBottomSection,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 10.dp,
                ),
        )
    }
}
