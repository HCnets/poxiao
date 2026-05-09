package com.poxiao.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay

@Composable
internal fun PoxiaoAppScaffoldEffects(
    scaffoldState: PoxiaoAppScaffoldState,
) {
    with(scaffoldState) {
        LaunchedEffect(section, overlayPage) {
            islandVisible = true
            delay(2200)
            islandVisible = false
        }
        LaunchedEffect(overlayPage) {
            if (overlayPage != null && sideNavExpanded) {
                sideNavExpanded = false
            }
        }
        LaunchedEffect(section) {
            if (section !in residentSections) {
                residentSections += section
            }
        }
        LaunchedEffect(section) {
            if (section != previousSection) {
                val from = previousSection
                sectionTransitionDirection = if (sectionOrder.indexOf(section) >= sectionOrder.indexOf(from)) 1 else -1
                previousSection = section
                sectionSweepProgress.snapTo(0f)
                sectionSweepProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 900,
                        easing = CubicBezierEasing(0.14f, 0.98f, 0.22f, 1f),
                    ),
                )
            }
        }
        LaunchedEffect(Unit) {
            withFrameNanos { }
            withFrameNanos { }
            delay(520)
            sectionOrder.filter { it != PrimarySection.Home }.forEach { target ->
                if (target !in residentSections) {
                    residentSections += target
                }
                delay(220)
            }
        }
        BackHandler(enabled = sideNavExpanded || overlayPage != null || section != PrimarySection.Home) {
            when {
                sideNavExpanded -> {
                    pendingDrawerAction = null
                    sideNavExpanded = false
                }

                overlayPage != null -> {
                    overlayPage = null
                }

                section != PrimarySection.Home -> {
                    section = PrimarySection.Home
                }
            }
        }
        LaunchedEffect(sideNavExpanded, pendingDrawerAction) {
            val action = pendingDrawerAction ?: return@LaunchedEffect
            if (!sideNavExpanded) {
                withFrameNanos { }
                action()
                pendingDrawerAction = null
            }
        }
    }
}
