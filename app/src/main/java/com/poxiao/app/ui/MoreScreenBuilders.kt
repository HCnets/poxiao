package com.poxiao.app.ui

import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor
import com.poxiao.app.schedule.AcademicUiState
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.ui.theme.PoxiaoPalette

internal data class MoreAccountSummary(
    val displayName: String,
    val summaryStudentId: String,
    val accountProfile: AcademicAccountProfile,
    val hasBoundAccount: Boolean,
    val accountStatusText: String,
    val accountStatusAccent: Color,
)

internal data class MoreNavigationContent(
    val learningEntries: List<MoreNavigationItem>,
    val toolEntries: List<MoreNavigationItem>,
)

internal fun buildMoreAccountSummary(
    schedulePrefs: SharedPreferences,
    uiState: AcademicUiState,
    palette: PoxiaoPalette,
): MoreAccountSummary {
    val sessionStudentId = uiState.studentId.trim()
    val persistedStudentId = getPersistedAcademicStudentId(schedulePrefs)
    val persistedPassword = SecurePrefs.getString(schedulePrefs, "password_secure", "password").trim()
    val liveBoundStudentId = sessionStudentId.takeIf {
        uiState.loggedIn && isLikelyCompleteAcademicStudentId(it)
    }.orEmpty()
    val storedBoundStudentId = persistedStudentId.takeIf {
        isLikelyCompleteAcademicStudentId(it) && persistedPassword.isNotBlank()
    }.orEmpty()
    val summaryStudentId = liveBoundStudentId.ifBlank { storedBoundStudentId }
    val accountProfile = if (isLikelyCompleteAcademicStudentId(summaryStudentId)) {
        resolveAcademicAccountProfile(schedulePrefs, summaryStudentId)
    } else {
        AcademicAccountProfile("", "", "")
    }
    val realName = accountProfile.realName.ifBlank {
        if (uiState.loggedIn && sessionStudentId == summaryStudentId) uiState.studentName else ""
    }
    val hasBoundAccount = summaryStudentId.isNotBlank() && (realName.isNotBlank() || storedBoundStudentId.isNotBlank())
    val displayName = if (hasBoundAccount) {
        accountProfile.nickname.ifBlank {
            realName.ifBlank { summaryStudentId }
        }
    } else {
        "教务账号"
    }
    val accountStatusText = when {
        uiState.loading -> "同步中"
        uiState.authExpired && hasBoundAccount -> "会话失效"
        uiState.loggedIn && hasBoundAccount -> "已连接"
        else -> "未登录"
    }
    val accountStatusAccent = when {
        uiState.loading -> com.poxiao.app.ui.theme.Ginkgo
        uiState.authExpired && hasBoundAccount -> com.poxiao.app.ui.theme.WarmMist
        uiState.loggedIn && hasBoundAccount -> com.poxiao.app.ui.theme.ForestGreen
        else -> palette.softText
    }
    return MoreAccountSummary(
        displayName = displayName,
        summaryStudentId = summaryStudentId,
        accountProfile = accountProfile,
        hasBoundAccount = hasBoundAccount,
        accountStatusText = accountStatusText,
        accountStatusAccent = accountStatusAccent,
    )
}

internal fun buildMoreNavigationContent(
    palette: PoxiaoPalette,
    onOpenCampusServices: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenCourseNotes: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenLearningDashboard: () -> Unit,
    onOpenExportCenter: () -> Unit,
    onOpenPreferences: () -> Unit,
): MoreNavigationContent {
    return MoreNavigationContent(
        learningEntries = listOf(
            MoreNavigationItem(
                title = "校园服务",
                icon = Icons.Rounded.Apartment,
                accent = palette.primary,
                actionLabel = "进入",
                onClick = onOpenCampusServices,
            ),
            MoreNavigationItem(
                title = "课程笔记",
                icon = Icons.Rounded.MenuBook,
                accent = lerpColor(palette.primary, palette.secondary, 0.28f),
                actionLabel = "进入",
                onClick = onOpenCourseNotes,
            ),
            MoreNavigationItem(
                title = "复习计划",
                icon = Icons.Rounded.AssignmentTurnedIn,
                accent = lerpColor(palette.primary, palette.secondary, 0.52f),
                actionLabel = "进入",
                onClick = onOpenReviewPlanner,
            ),
            MoreNavigationItem(
                title = "学习数据",
                icon = Icons.Rounded.Insights,
                accent = palette.secondary,
                actionLabel = "进入",
                onClick = onOpenLearningDashboard,
            ),
        ),
        toolEntries = listOf(
            MoreNavigationItem(
                title = "科学计算器",
                icon = Icons.Rounded.Calculate,
                accent = palette.secondary,
                actionLabel = "进入",
                onClick = onOpenCalculator,
            ),
            MoreNavigationItem(
                title = "导出中心",
                icon = Icons.Rounded.Share,
                accent = lerpColor(palette.primary, palette.secondary, 0.7f),
                actionLabel = "进入",
                onClick = onOpenExportCenter,
            ),
            MoreNavigationItem(
                title = "通知偏好",
                icon = Icons.Rounded.Notifications,
                accent = lerpColor(palette.secondary, palette.primary, 0.18f),
                actionLabel = "设置",
                onClick = onOpenNotificationPreferences,
            ),
            MoreNavigationItem(
                title = "界面偏好",
                icon = Icons.Rounded.Tune,
                accent = palette.primary,
                actionLabel = "设置",
                onClick = onOpenPreferences,
            ),
        ),
    )
}
