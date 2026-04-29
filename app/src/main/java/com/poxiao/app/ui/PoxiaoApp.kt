
package com.poxiao.app.ui

import android.Manifest
import android.app.AlarmManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewKanban
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.poxiao.app.campus.CampusMapScreen
import com.poxiao.app.campus.CampusServicesScreen
import com.poxiao.app.campus.HitaAcademicGateway
import com.poxiao.app.calculator.ScientificCalculatorScreen
import com.poxiao.app.data.ChatMessage
import com.poxiao.app.data.FeedCard
import com.poxiao.app.data.AssistantContextSummary
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantMockExecution
import com.poxiao.app.data.AssistantPermissionState
import com.poxiao.app.data.AssistantPermissionStore
import com.poxiao.app.data.AssistantSessionStore
import com.poxiao.app.data.AssistantToolCall
import com.poxiao.app.data.AssistantToolKit
import com.poxiao.app.data.AppSummaryProvider
import com.poxiao.app.data.AssistantToolDefinition
import com.poxiao.app.data.AssistantGatewayFactory
import com.poxiao.app.insights.LearningDashboardScreen
import com.poxiao.app.notes.CourseNoteSeed
import com.poxiao.app.notes.CourseNoteStore
import com.poxiao.app.notes.CourseNotesScreen
import com.poxiao.app.pomodoro.NoisePlayer
import com.poxiao.app.notifications.LocalReminderReceiver
import com.poxiao.app.reports.ExportCenterScreen
import com.poxiao.app.review.ReviewPlannerScreen
import com.poxiao.app.review.ReviewPlannerSeed
import com.poxiao.app.review.ReviewPlannerStore
import com.poxiao.app.settings.NotificationPreferencesScreen
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.schedule.HitaScheduleUiState
import com.poxiao.app.schedule.HitaScheduleRepository
import com.poxiao.app.schedule.HitaTerm
import com.poxiao.app.schedule.HitaTimeSlot
import com.poxiao.app.schedule.HitaWeek
import com.poxiao.app.schedule.HitaWeekDay
import com.poxiao.app.schedule.HitaWeekSchedule
import com.poxiao.app.settings.loadNotificationPreferenceState
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.ui.theme.BambooGlass
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.CloudWhite
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.PoxiaoTheme
import com.poxiao.app.ui.theme.PoxiaoThemePreset
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Instant
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

}


internal data class FocusDayStat(
    val label: String,
    val minutes: Int,
)

internal data class FocusTaskStat(
    val title: String,
    val minutes: Int,
    val count: Int,
)


internal enum class HomeModuleSize(
    val title: String,
) {
    Compact("绱у噾"),
    Standard("鏍囧噯"),
    Hero("寮鸿皟"),
}

internal enum class OverlayPage(val label: String) {
    CampusServices("鏍″洯鏈嶅姟"),
    CampusMap("鏍″洯鍦板浘"),
    AcademicAccount("鏁欏姟璐﹀彿"),
    Calculator("绉戝璁＄畻锟?),
    CourseNotes("璇剧▼绗旇"),
    ReviewPlanner("澶嶄範璁″垝"),
    NotificationPreferences("閫氱煡鍋忓ソ"),
    LearningDashboard("瀛︿範鏁版嵁"),
    ExportCenter("瀵煎嚭涓績"),
    Preferences("鐣岄潰鍋忓ソ"),
    AssistantPermissions("鏅鸿兘浣撴潈锟?),
}

internal enum class UiDensityPreset(
    val title: String,
    val scale: Float,
) {
    Compact("绱у噾", 0.92f),
    Comfortable("鍧囪　", 1f),
    Relaxed("鑸掑睍", 1.08f),
}

internal enum class GlassStrengthPreset(
    val title: String,
    val cardAlpha: Float,
    val glowScale: Float,
) {
    Crisp("娓咃拷?, 0.88f, 0.72f),
    Balanced("鏌旈浘", 1f, 1f),
    Lush("鏅舵鼎", 1.14f, 1.24f),
}

internal val LocalUiDensityPreset = staticCompositionLocalOf { UiDensityPreset.Comfortable }
internal val LocalGlassStrengthPreset = staticCompositionLocalOf { GlassStrengthPreset.Balanced }
internal val LocalStaticGlassMode = staticCompositionLocalOf { false }

internal data class ScheduleExtraEvent(
    val id: String,
    val date: String,
    val title: String,
    val time: String,
    val type: String,
    val note: String,
)

@Composable
fun PoxiaoApp() {
    val context = LocalContext.current
    val uiPrefs = remember { context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE) }
    var themePreset by remember {
        mutableStateOf(
            runCatching {
                PoxiaoThemePreset.valueOf(uiPrefs.getString("theme_preset", PoxiaoThemePreset.Forest.name).orEmpty())
            }.getOrDefault(PoxiaoThemePreset.Forest),
        )
    }
    var densityPreset by remember {
        mutableStateOf(
            runCatching {
                UiDensityPreset.valueOf(uiPrefs.getString("density_preset", UiDensityPreset.Comfortable.name).orEmpty())
            }.getOrDefault(UiDensityPreset.Comfortable),
        )
    }
    var glassStrengthPreset by remember {
        mutableStateOf(
            runCatching {
                GlassStrengthPreset.valueOf(uiPrefs.getString("glass_preset", GlassStrengthPreset.Balanced.name).orEmpty())
            }.getOrDefault(GlassStrengthPreset.Balanced),
        )
    }
    var liquidGlassStylePreset by remember {
        mutableStateOf(
            runCatching {
                LiquidGlassStylePreset.valueOf(
                    uiPrefs.getString("liquid_glass_style", LiquidGlassStylePreset.IOS.name).orEmpty(),
                )
            }.getOrDefault(LiquidGlassStylePreset.IOS),
        )
    }

    LaunchedEffect(Unit) {
        refreshLocalReminderSchedule(context)
    }

    PoxiaoTheme(preset = themePreset) {
        CompositionLocalProvider(
            LocalUiDensityPreset provides densityPreset,
            LocalGlassStrengthPreset provides glassStrengthPreset,
            LocalLiquidGlassStylePreset provides liquidGlassStylePreset,
        ) {
            PoxiaoAppScaffold(
                themePreset = themePreset,
                densityPreset = densityPreset,
                glassStrengthPreset = glassStrengthPreset,
                liquidGlassStylePreset = liquidGlassStylePreset,
                onThemePresetChange = {
                    themePreset = it
                    uiPrefs.edit().putString("theme_preset", it.name).apply()
                },
                onDensityPresetChange = {
                    densityPreset = it
                    uiPrefs.edit().putString("density_preset", it.name).apply()
                },
                onGlassStrengthChange = {
                    glassStrengthPreset = it
                    uiPrefs.edit().putString("glass_preset", it.name).apply()
                },
                onLiquidGlassStyleChange = {
                    liquidGlassStylePreset = it
                    uiPrefs.edit().putString("liquid_glass_style", it.name).apply()
                },
            )
        }
    }
}



@Composable

internal fun loadStringList(
    prefs: android.content.SharedPreferences,
    key: String,
): List<String> {
    val raw = prefs.getString(key, "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}

internal fun saveStringList(
    prefs: android.content.SharedPreferences,
    key: String,
    items: List<String>,
) {
    prefs.edit().putString(key, items.joinToString("|")).apply()
}


internal fun buildFocusDayStats(records: List<FocusRecord>): List<FocusDayStat> {
    val today = LocalDate.now()
    return (6 downTo 0).map { offset ->
        val target = today.minusDays(offset.toLong())
        val label = "${target.monthValue}.${target.dayOfMonth}"
        val minutes = records.filter { parseFocusRecordDate(it.finishedAt) == target }.map { it.seconds }.sum() / 60
        FocusDayStat(label = label, minutes = minutes)
    }
}

internal fun buildFocusTaskStats(records: List<FocusRecord>): List<FocusTaskStat> {
    return records
        .groupBy { it.taskTitle.ifBlank { "鏈懡鍚嶄笓锟? } }
        .map { (title, items) ->
            FocusTaskStat(
                title = title,
                minutes = items.map { it.seconds }.sum() / 60,
                count = items.size,
            )
        }
        .sortedWith(compareByDescending<FocusTaskStat> { it.minutes }.thenByDescending { it.count })
}

private fun parseFocusRecordDate(raw: String): LocalDate? {
    if (raw.isBlank()) return null
    return runCatching {
        java.time.LocalDateTime.parse(raw, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
    }.recoverCatching {
        val currentYear = LocalDate.now().year
        java.time.LocalDateTime.parse(
            "$currentYear-$raw",
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        ).toLocalDate()
    }.getOrNull()
}

internal fun loadFocusRecords(prefs: android.content.SharedPreferences): List<FocusRecord> {
    val raw = prefs.getString("focus_records", null) ?: return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    FocusRecord(
                        taskTitle = item.optString("taskTitle"),
                        modeTitle = item.optString("modeTitle"),
                        seconds = item.optInt("seconds"),
                        finishedAt = item.optString("finishedAt"),
                    ),
                )
            }
        }
    }.getOrElse { emptyList() }
}

internal fun saveFocusRecords(
    prefs: android.content.SharedPreferences,
    records: List<FocusRecord>,
) {
    val array = JSONArray()
    records.forEach { record ->
        array.put(
            JSONObject().apply {
                put("taskTitle", record.taskTitle)
                put("modeTitle", record.modeTitle)
                put("seconds", record.seconds)
                put("finishedAt", record.finishedAt)
            },
        )
    }
    prefs.edit().putString("focus_records", array.toString()).apply()
}

fun sendAppNotification(
    context: Context,
    title: String,
    content: String,
) {
    val channelId = "poxiao_local_notice"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "鏈湴鎻愰啋", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
}

fun refreshLocalReminderSchedule(context: Context) {
    val schedulerPrefs = context.getSharedPreferences("reminder_scheduler", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val scheduleCachePrefs = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)
    val scheduleAuthPrefs = context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE)
    val examWeekPrefs = context.getSharedPreferences("schedule_exam_week", Context.MODE_PRIVATE)
    val preferenceState = loadNotificationPreferenceState(context)
    val now = System.currentTimeMillis()
    val plans = buildTodoReminderPlans(todoPrefs, preferenceState)
        .plus(buildCourseReminderPlans(scheduleAuthPrefs, scheduleCachePrefs, preferenceState))
        .plus(buildExamWeekReminderPlans(scheduleCachePrefs, scheduleAuthPrefs, examWeekPrefs, preferenceState))
        .filter { it.triggerAtMillis > now }
        .sortedBy { it.triggerAtMillis }
        .distinctBy { it.id }
        .take(48)
    val nextIds = plans.map { it.id }
    val nextSnapshot = plans.joinToString("|") { plan ->
        "${plan.id}@${plan.triggerAtMillis}@${plan.title.hashCode()}@${plan.body.hashCode()}"
    }
    val previousIds = loadStringList(schedulerPrefs, "scheduled_ids")
    val previousSnapshot = schedulerPrefs.getString("scheduled_snapshot", "").orEmpty()
    if (nextIds == previousIds && nextSnapshot == previousSnapshot) return
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    previousIds.forEach { id ->
        alarmManager.cancel(buildReminderPendingIntent(context, id, "", ""))
    }
    plans.forEach { plan ->
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            plan.triggerAtMillis,
            buildReminderPendingIntent(context, plan.id, plan.title, plan.body),
        )
    }
    saveStringList(schedulerPrefs, "scheduled_ids", nextIds)
    schedulerPrefs.edit().putString("scheduled_snapshot", nextSnapshot).apply()
}

internal fun loadPrimaryScheduleState(
    primaryPrefs: android.content.SharedPreferences,
    fallbackPrefs: android.content.SharedPreferences,
): HitaScheduleUiState? {
    return loadCachedScheduleUiState(primaryPrefs) ?: loadCachedScheduleUiState(fallbackPrefs)
}

internal fun loadPrimaryScheduleEvents(
    primaryPrefs: android.content.SharedPreferences,
    fallbackPrefs: android.content.SharedPreferences,
): List<ScheduleExtraEvent> {
    val primary = loadScheduleExtraEvents(primaryPrefs)
    return if (primary.isNotEmpty()) primary else loadScheduleExtraEvents(fallbackPrefs)
}




