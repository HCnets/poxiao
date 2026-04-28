package com.poxiao.app.settings

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.refreshLocalReminderSchedule
import com.poxiao.app.ui.sendAppNotification
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.theme.WarmMist

private const val NotificationPrefsName = "notification_preferences"
private const val TodoEnabledKey = "todo_enabled"
private const val CourseEnabledKey = "course_enabled"
private const val ExamEnabledKey = "exam_enabled"
private const val PomodoroEnabledKey = "pomodoro_enabled"
private const val CourseLeadKey = "course_lead_minutes"
private const val ExamPresetKey = "exam_preset"

data class NotificationPreferenceState(
    val todoEnabled: Boolean = true,
    val courseEnabled: Boolean = true,
    val examEnabled: Boolean = true,
    val pomodoroEnabled: Boolean = true,
    val courseLeadMinutes: Int = 30,
    val examPreset: String = "前一天晚 20:00",
)

private fun normalizeExamPreset(raw: String): String {
    val trimmed = raw.trim()
    return when {
        trimmed.isBlank() -> "前一天晚 20:00"
        trimmed.contains("当天") || trimmed.contains("08:00") -> "当天早 08:00"
        trimmed.contains("3") && (trimmed.contains("小时") || trimmed.contains("灏忔椂")) -> "提前 3 小时"
        trimmed.contains("前一天") || trimmed.contains("20:00") || trimmed.contains("鍓嶄竴澶") -> "前一天晚 20:00"
        else -> "前一天晚 20:00"
    }
}

fun loadNotificationPreferenceState(context: Context): NotificationPreferenceState {
    val prefs = context.getSharedPreferences(NotificationPrefsName, Context.MODE_PRIVATE)
    return NotificationPreferenceState(
        todoEnabled = prefs.getBoolean(TodoEnabledKey, true),
        courseEnabled = prefs.getBoolean(CourseEnabledKey, true),
        examEnabled = prefs.getBoolean(ExamEnabledKey, true),
        pomodoroEnabled = prefs.getBoolean(PomodoroEnabledKey, true),
        courseLeadMinutes = prefs.getInt(CourseLeadKey, 30),
        examPreset = normalizeExamPreset(prefs.getString(ExamPresetKey, "前一天晚 20:00").orEmpty()),
    )
}

fun saveNotificationPreferenceState(
    context: Context,
    state: NotificationPreferenceState,
) {
    context.getSharedPreferences(NotificationPrefsName, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(TodoEnabledKey, state.todoEnabled)
        .putBoolean(CourseEnabledKey, state.courseEnabled)
        .putBoolean(ExamEnabledKey, state.examEnabled)
        .putBoolean(PomodoroEnabledKey, state.pomodoroEnabled)
        .putInt(CourseLeadKey, state.courseLeadMinutes)
        .putString(ExamPresetKey, normalizeExamPreset(state.examPreset))
        .apply()
}

private fun readScheduledReminderCount(context: Context): Int {
    val raw = context.getSharedPreferences("reminder_scheduler", Context.MODE_PRIVATE)
        .getString("scheduled_ids", "")
        .orEmpty()
    if (raw.isBlank()) return 0
    return raw.split("|").count { it.isNotBlank() }
}

private fun buildReminderStatusText(context: Context, prefix: String? = null): String {
    val count = readScheduledReminderCount(context)
    val suffix = if (count > 0) "当前已排入 $count 条本地提醒。" else "当前没有待触发的本地提醒。"
    return if (prefix.isNullOrBlank()) suffix else "$prefix$suffix"
}

@Composable
fun NotificationPreferencesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val palette = PoxiaoThemeState.palette
    var state by remember { mutableStateOf(loadNotificationPreferenceState(context)) }
    var status by remember { mutableStateOf(buildReminderStatusText(context, "修改后会立即重排提醒计划。")) }

    fun updateState(transform: (NotificationPreferenceState) -> NotificationPreferenceState) {
        state = transform(state)
        saveNotificationPreferenceState(context, state)
        refreshLocalReminderSchedule(context)
        status = buildReminderStatusText(context, "已更新提醒偏好。")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        palette.backgroundTop.copy(alpha = 0.94f),
                        palette.backgroundBottom.copy(alpha = 0.98f),
                    ),
                ),
            ),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PreferenceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth(0.78f)) {
                        Text("通知偏好", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
                        Text("统一管理待办、课前、考试周和番茄钟完成提醒。", style = MaterialTheme.typography.bodyLarge, color = palette.softText)
                    }
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMist, contentColor = palette.ink),
                    ) {
                        Text("返回")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(status, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            refreshLocalReminderSchedule(context)
                            status = buildReminderStatusText(context, "已立即重排提醒计划。")
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.pillOn),
                    ) {
                        Text("立即重排")
                    }
                    Button(
                        onClick = {
                            sendAppNotification(context, "测试通知", "本地提醒链路正常，可继续测试系统通知。")
                            status = buildReminderStatusText(context, "已发送测试通知。")
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = palette.ink),
                    ) {
                        Text("测试通知")
                    }
                }
            }
        }
        item {
            PreferenceCard {
                Text("提醒开关", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                PreferenceToggle("待办提醒", "按任务提醒时间与截止时间触发。", state.todoEnabled) {
                    updateState { current -> current.copy(todoEnabled = it) }
                }
                PreferenceToggle("课前提醒", "根据周课表自动在开课前提醒。", state.courseEnabled) {
                    updateState { current -> current.copy(courseEnabled = it) }
                }
                PreferenceToggle("考试周提醒", "为考试、作业和复习事项生成定时提醒。", state.examEnabled) {
                    updateState { current -> current.copy(examEnabled = it) }
                }
                PreferenceToggle("番茄完成提醒", "专注结束后发送系统通知。", state.pomodoroEnabled) {
                    updateState { current -> current.copy(pomodoroEnabled = it) }
                }
            }
        }
        item {
            PreferenceCard {
                Text("课前提醒时间", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(10, 20, 30, 60).forEach { minutes ->
                        PreferenceChip(
                            title = "提前 ${minutes} 分钟",
                            selected = state.courseLeadMinutes == minutes,
                            onClick = { updateState { current -> current.copy(courseLeadMinutes = minutes) } },
                        )
                    }
                }
            }
        }
        item {
            PreferenceCard {
                Text("考试周提醒策略", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("前一天晚 20:00", "当天早 08:00", "提前 3 小时").forEach { preset ->
                        PreferenceChip(
                            title = preset,
                            selected = state.examPreset == preset,
                            onClick = { updateState { current -> current.copy(examPreset = preset) } },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceCard(content: @Composable ColumnScope.() -> Unit) {
    val palette = PoxiaoThemeState.palette
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = palette.card.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, palette.cardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.08f)),
                    ),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = content,
        )
    }
}

@Composable
private fun PreferenceToggle(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.22f)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.78f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun PreferenceChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) palette.primary.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, if (selected) palette.primary.copy(alpha = 0.16f) else palette.cardBorder),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            color = if (selected) palette.pillOn else palette.ink,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
}
