package com.poxiao.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.unit.dp
import com.poxiao.app.schedule.HitaScheduleRepository
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.theme.WarmMist

@Composable
internal fun MoreScreen(
    repository: HitaScheduleRepository,
    onOpenAcademicAccount: () -> Unit,
    onOpenCampusServices: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenCourseNotes: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenLearningDashboard: () -> Unit,
    onOpenExportCenter: () -> Unit,
    onOpenPreferences: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val palette = PoxiaoThemeState.palette
    val schedulePrefs = remember { context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE) }
    val clipboard = remember { context.getSystemService(ClipboardManager::class.java) }
    val uiState by repository.observeUiState().collectAsState()
    var backupText by remember { mutableStateOf("") }
    var backupStatus by remember { mutableStateOf("") }
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
        uiState.loading -> Ginkgo
        uiState.authExpired && hasBoundAccount -> WarmMist
        uiState.loggedIn && hasBoundAccount -> ForestGreen
        else -> palette.softText
    }

    val learningEntries = listOf(
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
    )
    val toolEntries = listOf(
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
    )
    ScreenColumn {
        item {
            GlassCard {
                Text("更多", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
            }
        }
        item {
            GlassCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenAcademicAccount),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AcademicAvatarBadge(
                        displayName = displayName,
                        studentId = if (hasBoundAccount) summaryStudentId else "",
                        avatarUri = if (hasBoundAccount) accountProfile.avatarUri else "",
                        accent = palette.primary,
                        size = 64.dp,
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(displayName, style = MaterialTheme.typography.titleLarge, color = palette.ink)
                        Text(
                            text = if (hasBoundAccount) "学号 $summaryStudentId" else "未登录",
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
                            color = accountStatusAccent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, accountStatusAccent.copy(alpha = 0.18f)),
                        ) {
                            Text(
                                text = accountStatusText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = accountStatusAccent,
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
        item {
            MoreNavigationSections(
                learningEntries = learningEntries,
                toolEntries = toolEntries,
            )
        }
        item {
            GlassCard {
                Text("本地备份与恢复", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(8.dp))
                Text("恢复会覆盖当前本地数据。", style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionPill("复制备份", ForestGreen) {
                        val export = buildLocalBackupPayload(context)
                        backupText = export
                        clipboard?.setPrimaryClip(ClipData.newPlainText("破晓本地备份", export))
                        backupStatus = "已复制备份文本，可自行保存到任意位置。"
                    }
                    ActionPill("从剪贴板粘贴", WarmMist) {
                        backupText = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                        backupStatus = if (backupText.isBlank()) "剪贴板里没有可恢复的备份文本。" else "已读取剪贴板内容，可直接执行恢复。"
                    }
                    ActionPill("执行恢复", Ginkgo) {
                        backupStatus = restoreLocalBackupPayload(context, backupText)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = backupText,
                    onValueChange = { backupText = it },
                    label = { Text("备份文本") },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 9,
                )
                if (backupStatus.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(backupStatus, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                }
            }
        }
    }
}
