package com.poxiao.app.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poxiao.app.schedule.HitaScheduleRepository
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
    val accountSummary = remember(schedulePrefs, uiState, palette) {
        buildMoreAccountSummary(
            schedulePrefs = schedulePrefs,
            uiState = uiState,
            palette = palette,
        )
    }
    val navigationContent = remember(
        palette,
        onOpenCampusServices,
        onOpenCalculator,
        onOpenCourseNotes,
        onOpenReviewPlanner,
        onOpenNotificationPreferences,
        onOpenLearningDashboard,
        onOpenExportCenter,
        onOpenPreferences,
    ) {
        buildMoreNavigationContent(
            palette = palette,
            onOpenCampusServices = onOpenCampusServices,
            onOpenCalculator = onOpenCalculator,
            onOpenCourseNotes = onOpenCourseNotes,
            onOpenReviewPlanner = onOpenReviewPlanner,
            onOpenNotificationPreferences = onOpenNotificationPreferences,
            onOpenLearningDashboard = onOpenLearningDashboard,
            onOpenExportCenter = onOpenExportCenter,
            onOpenPreferences = onOpenPreferences,
        )
    }
    ScreenColumn {
        item {
            GlassCard {
                Text("更多", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
            }
        }
        item {
            MoreAccountCard(summary = accountSummary, onClick = onOpenAcademicAccount)
        }
        item {
            MoreNavigationSections(
                learningEntries = navigationContent.learningEntries,
                toolEntries = navigationContent.toolEntries,
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
                        val result = copyLocalBackupPayload(context, clipboard)
                        backupText = result.text
                        backupStatus = result.status
                    }
                    ActionPill("从剪贴板粘贴", WarmMist) {
                        val result = pasteLocalBackupPayload(context, clipboard)
                        backupText = result.text
                        backupStatus = result.status
                    }
                    ActionPill("执行恢复", Ginkgo) {
                        backupStatus = restoreLocalBackup(context, backupText)
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
