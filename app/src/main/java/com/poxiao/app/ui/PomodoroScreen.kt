package com.poxiao.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.poxiao.app.pomodoro.NoisePlayer
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.settings.loadNotificationPreferenceState
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import java.time.LocalDateTime
import kotlinx.coroutines.delay

@Composable
internal fun PomodoroScreen(active: Boolean) {
    val context = LocalContext.current
    val focusPrefs = remember { context.getSharedPreferences("focus_bridge", android.content.Context.MODE_PRIVATE) }
    val todoPrefs = remember { context.getSharedPreferences("todo_board", android.content.Context.MODE_PRIVATE) }
    val focusRecordPrefs = remember { context.getSharedPreferences("focus_records", android.content.Context.MODE_PRIVATE) }
    val sessionPrefs = remember { context.getSharedPreferences("pomodoro_session", android.content.Context.MODE_PRIVATE) }
    val noisePlayer = remember { NoisePlayer(context) }
    val presets = listOf(
        PomodoroPreset("25 分钟", 25 * 60),
        PomodoroPreset("45 分钟", 45 * 60),
        PomodoroPreset("60 分钟", 60 * 60),
    )
    val soundPresets = remember {
        listOf("山风", "溪流", "雨声", "海浪", "篝火", "白噪音", "粉噪")
    }
    val restoredSession = remember { loadPomodoroSession(sessionPrefs) }
    val restoredPreset = restoredSession?.let { PomodoroPreset(it.presetTitle, it.presetSeconds) } ?: presets.first()
    var mode by remember { mutableStateOf(restoredSession?.mode ?: PomodoroMode.Focus) }
    var preset by remember { mutableStateOf(restoredPreset) }
    var leftSeconds by remember { mutableIntStateOf(restoredSession?.leftSeconds ?: restoredPreset.seconds) }
    var running by remember { mutableStateOf(restoredSession?.running ?: false) }
    var autoNext by remember { mutableStateOf(restoredSession?.autoNext ?: true) }
    var strictMode by remember { mutableStateOf(restoredSession?.strictMode ?: false) }
    var boundTask by remember {
        mutableStateOf(
            restoredSession?.boundTask?.ifBlank {
                SecurePrefs.getString(focusPrefs, "bound_task_title_secure", "bound_task_title")
            } ?: SecurePrefs.getString(focusPrefs, "bound_task_title_secure", "bound_task_title").ifBlank { "完成机器学习实验报告" },
        )
    }
    var sound by remember { mutableStateOf(restoredSession?.sound ?: "山风") }
    var ambientOn by remember { mutableStateOf(restoredSession?.ambientOn ?: false) }
    var focusMinutes by remember { mutableIntStateOf(restoredSession?.focusMinutes ?: 125) }
    var cycles by remember { mutableIntStateOf(restoredSession?.cycles ?: 4) }
    var customMinutes by remember { mutableIntStateOf(restoredSession?.customMinutes ?: (restoredPreset.seconds / 60)) }
    var customSeconds by remember { mutableIntStateOf(restoredSession?.customSeconds ?: 0) }
    var onlyPendingGoalTasks by remember { mutableStateOf(restoredSession?.onlyPendingGoalTasks ?: true) }
    var sessionHint by remember {
        mutableStateOf(
            if (restoredSession != null) "已恢复上次未完成的专注会话。"
            else "",
        )
    }
    var ambientHint by remember { mutableStateOf("首次播放请等待 3-5 秒完成加载。") }
    val latestNoiseFailureHandler = rememberUpdatedState {
        ambientOn = false
        ambientHint = "当前设备未能启动环境声，请检查网络后重试。"
    }
    val latestNoiseReadyHandler = rememberUpdatedState {
        ambientHint = if (sound == "粉噪") "粉噪音量较大，已自动降低音量。" else ""
    }
    noisePlayer.onFailure = latestNoiseFailureHandler.value
    noisePlayer.onReady = latestNoiseReadyHandler.value
    val focusRecords = remember { loadFocusRecords(focusRecordPrefs).toMutableStateList() }
    val todoTasks = remember { loadTodoTasks(todoPrefs).toMutableStateList() }
    val focusDayStats = remember(focusRecords.size) { buildFocusDayStats(focusRecords) }
    val focusTaskStats = remember(focusRecords.size) { buildFocusTaskStats(focusRecords) }
    val focusModeStats = remember(focusRecords.size) {
        focusRecords.groupBy { it.modeTitle }.mapValues { entry -> entry.value.map { it.seconds }.sum() / 60 }
    }
    val goalCandidateTasks = todoTasks.filter { task ->
        !task.done && if (onlyPendingGoalTasks) task.focusGoal > 0 && task.focusCount < task.focusGoal else true
    }
    val boundTodoTask = todoTasks.firstOrNull { it.title == boundTask }
    val remainingFocusRounds = (boundTodoTask?.focusGoal ?: 0) - (boundTodoTask?.focusCount ?: 0)
    val tasksWithGoals = todoTasks.filter { it.focusGoal > 0 }
    val reachedGoalCount = tasksWithGoals.count { it.focusCount >= it.focusGoal }
    val goalAttainmentRate = if (tasksWithGoals.isEmpty()) "--" else "${(reachedGoalCount * 100 / tasksWithGoals.size)}%"
    val recommendedFocusSeconds = when {
        remainingFocusRounds >= 3 -> 60 * 60
        remainingFocusRounds == 2 -> 45 * 60
        remainingFocusRounds == 1 -> 25 * 60
        else -> 0
    }

    LaunchedEffect(running, leftSeconds, preset.seconds) {
        if (!running) return@LaunchedEffect
        if (leftSeconds <= 0) {
            running = false
            if (mode == PomodoroMode.Focus) {
                recordTodoFocusProgress(context, todoPrefs, boundTask)
                todoTasks.clear()
                todoTasks.addAll(loadTodoTasks(todoPrefs))
                focusRecords.add(
                    0,
                    FocusRecord(
                        taskTitle = boundTask.ifBlank { "未命名专注" },
                        modeTitle = mode.title,
                        seconds = preset.seconds,
                        finishedAt = formatSyncTime(LocalDateTime.now()),
                    ),
                )
                saveFocusRecords(focusRecordPrefs, focusRecords)
                if (loadNotificationPreferenceState(context).pomodoroEnabled) {
                    sendAppNotification(context, "专注已完成", if (boundTask.isBlank()) "本轮专注已结束" else "已完成：$boundTask")
                }
                val updatedTask = todoTasks.firstOrNull { it.title == boundTask }
                if (updatedTask != null && updatedTask.focusGoal > 0 && updatedTask.focusCount >= updatedTask.focusGoal) {
                    if (loadNotificationPreferenceState(context).pomodoroEnabled) {
                        sendAppNotification(context, "任务专注目标已达成", "${updatedTask.title} 已达到 ${updatedTask.focusGoal} 轮")
                    }
                }
                focusMinutes += preset.seconds / 60
                cycles += 1
                if (autoNext) {
                    mode = if (cycles % 4 == 0) PomodoroMode.LongBreak else PomodoroMode.ShortBreak
                    preset = if (mode == PomodoroMode.LongBreak) PomodoroPreset("20 分钟", 20 * 60) else PomodoroPreset("5 分钟", 5 * 60)
                    leftSeconds = preset.seconds
                    running = true
                }
            }
            return@LaunchedEffect
        }
        delay(1000)
        leftSeconds -= 1
    }

    LaunchedEffect(active, running, ambientOn, sound, mode) {
        if (active && ambientOn) {
            val started = noisePlayer.start(sound)
            if (!started) {
                ambientOn = false
                ambientHint = "当前设备未能启动环境声，请检查网络后重试。"
            } else {
                ambientHint = if (sound == "粉噪") {
                    when {
                        noisePlayer.isCached(sound) -> "粉噪音量较大，已自动降低音量。"
                        noisePlayer.isCaching(sound) -> "粉噪正在缓存中，已自动降低音量，请等待 3-5 秒。"
                        else -> "粉噪音量较大，已自动降低音量。首次播放请等待 3-5 秒完成加载。"
                    }
                } else if (noisePlayer.isCached(sound)) {
                    ""
                } else if (noisePlayer.isCaching(sound)) {
                    "当前音色正在缓存中，请等待 3-5 秒。"
                } else {
                    "首次播放请等待 3-5 秒完成加载。"
                }
            }
        } else {
            noisePlayer.stop()
        }
    }

    LaunchedEffect(active) {
        if (active) {
            noisePlayer.warmUp()
        }
    }

    DisposableEffect(Unit) {
        onDispose { noisePlayer.stop() }
    }

    LaunchedEffect(boundTask) {
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", boundTask)
    }

    LaunchedEffect(
        mode,
        preset.title,
        preset.seconds,
        leftSeconds,
        running,
        autoNext,
        strictMode,
        boundTask,
        sound,
        ambientOn,
        focusMinutes,
        cycles,
        customMinutes,
        customSeconds,
        onlyPendingGoalTasks,
    ) {
        savePomodoroSession(
            sessionPrefs,
            PomodoroSession(
                mode = mode,
                presetTitle = preset.title,
                presetSeconds = preset.seconds,
                leftSeconds = leftSeconds,
                running = running,
                autoNext = autoNext,
                strictMode = strictMode,
                boundTask = boundTask,
                sound = sound,
                ambientOn = ambientOn,
                focusMinutes = focusMinutes,
                cycles = cycles,
                customMinutes = customMinutes,
                customSeconds = customSeconds,
                onlyPendingGoalTasks = onlyPendingGoalTasks,
            ),
        )
    }

    ScreenColumn {
        item {
            GlassCard {
                Text("专注台", style = MaterialTheme.typography.headlineMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                Text("支持任务绑定、自动流转、严格模式与白噪音选择。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.78f))
                if (sessionHint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(sessionHint, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(options = PomodoroMode.entries.toList(), selected = mode, label = { it.title }) {
                    mode = it
                    val seconds = when (it) {
                        PomodoroMode.Focus -> 25 * 60
                        PomodoroMode.ShortBreak -> 5 * 60
                        PomodoroMode.LongBreak -> 20 * 60
                    }
                    preset = PomodoroPreset("${seconds / 60} 分钟", seconds)
                    leftSeconds = seconds
                    running = false
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(formatSeconds(leftSeconds), style = MaterialTheme.typography.headlineLarge, color = PineInk)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    presets.forEach { item ->
                        ActionPill(item.title, if (item == preset) ForestGreen else MossGreen) {
                            preset = item
                            leftSeconds = item.seconds
                            running = false
                        }
                    }
                    if (recommendedFocusSeconds > 0) {
                        ActionPill("推荐 ${recommendedFocusSeconds / 60} 分钟", TeaGreen) {
                            preset = PomodoroPreset("目标推荐", recommendedFocusSeconds)
                            leftSeconds = recommendedFocusSeconds
                            running = false
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionPill(if (running) "暂停" else "开始", ForestGreen) { running = !running }
                    ActionPill("重置", Ginkgo) {
                        running = false
                        leftSeconds = preset.seconds
                    }
                }
            }
        }
        item {
            GlassCard {
                Text("自定义时长", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                Text("滚轮选择分钟与秒数，再应用到本次专注。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    WheelPicker(
                        title = "分钟",
                        values = (0..180).toList(),
                        selected = customMinutes,
                        label = { "${it} 分" },
                        onSelect = { customMinutes = it },
                        modifier = Modifier.weight(1f),
                    )
                    WheelPicker(
                        title = "秒",
                        values = (0..59).toList(),
                        selected = customSeconds,
                        label = { "%02d 秒".format(it) },
                        onSelect = { customSeconds = it },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                ActionPill("应用 ${customMinutes}分 ${"%02d".format(customSeconds)}秒", ForestGreen) {
                    val totalSeconds = customMinutes * 60 + customSeconds
                    if (totalSeconds > 0) {
                        preset = PomodoroPreset("自定义", totalSeconds)
                        leftSeconds = totalSeconds
                        running = false
                    }
                }
            }
        }
        item {
            GlassCard {
                Text("会话设置", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = boundTask, onValueChange = { boundTask = it }, label = { Text("绑定任务") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                val boundList = SecurePrefs.getString(focusPrefs, "bound_task_list_secure", "bound_task_list")
                if (boundList.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("来自清单：$boundList", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                ToggleLine("仅看未达成目标任务", onlyPendingGoalTasks) { onlyPendingGoalTasks = it }
                if (goalCandidateTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        goalCandidateTasks.take(8).forEach { task ->
                            SelectionChip(
                                text = if (task.focusGoal > 0) "${task.title} ${task.focusCount}/${task.focusGoal}" else task.title,
                                chosen = task.title == boundTask,
                                onClick = {
                                    boundTask = task.title
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                },
                            )
                        }
                    }
                }
                boundTodoTask?.let { task ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (task.focusGoal > 0) {
                            if (remainingFocusRounds <= 0) "专注目标已完成：${task.focusCount}/${task.focusGoal} 轮"
                            else "当前进度：${task.focusCount}/${task.focusGoal} 轮，还差 $remainingFocusRounds 轮"
                        } else {
                            "当前进度：已完成 ${task.focusCount} 轮，可回到待办设置专注目标"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForestDeep.copy(alpha = 0.72f),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("环境声", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                ) {
                    Text(
                        text = sound,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = PineInk,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "切换音色请点下方预设。首次播放请等待 3-5 秒完成加载。",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.66f),
                )
                if (ambientHint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        ambientHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestDeep.copy(alpha = 0.72f),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    soundPresets.forEach { presetName ->
                        SelectionChip(
                            text = presetName,
                            chosen = presetName == sound,
                            onClick = {
                                sound = presetName
                                ambientHint = if (presetName == "粉噪") {
                                    when {
                                        noisePlayer.isCached(presetName) -> "粉噪音量较大，已自动降低音量。"
                                        noisePlayer.isCaching(presetName) -> "粉噪正在缓存中，已自动降低音量，请等待 3-5 秒。"
                                        else -> "粉噪音量较大，已自动降低音量。首次播放请等待 3-5 秒完成加载。"
                                    }
                                } else if (noisePlayer.isCached(presetName)) {
                                    ""
                                } else if (noisePlayer.isCaching(presetName)) {
                                    "当前音色正在缓存中，请等待 3-5 秒。"
                                } else {
                                    "首次播放请等待 3-5 秒完成加载。"
                                }
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ToggleLine("播放白噪音", ambientOn) { ambientOn = it }
                Spacer(modifier = Modifier.height(8.dp))
                ToggleLine("自动进入下一阶段", autoNext) { autoNext = it }
                Spacer(modifier = Modifier.height(8.dp))
                ToggleLine("严格模式", strictMode) { strictMode = it }
            }
        }
        item {
            GlassCard {
                Text("今日统计", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    MetricCard("专注时长", "$focusMinutes 分钟", ForestGreen)
                    MetricCard("完成轮次", cycles.toString(), Ginkgo)
                }
            }
        }
        item {
            GlassCard {
                Text("专注分析", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                Text("按天、按任务和按模式回看最近的投入分布。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                Spacer(modifier = Modifier.height(12.dp))
                if (focusRecords.isEmpty()) {
                    Text("完成几轮专注后，这里会自动生成分析结果。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.68f))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        MetricCard("目标达成率", goalAttainmentRate, TeaGreen)
                        MetricCard("已达成目标", reachedGoalCount.toString(), MossGreen)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("近 7 天", style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Spacer(modifier = Modifier.height(10.dp))
                    val maxDaily = focusDayStats.maxOfOrNull { it.minutes }?.coerceAtLeast(1) ?: 1
                    focusDayStats.forEachIndexed { index, stat ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(stat.label, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                                Text("${stat.minutes} 分钟", style = MaterialTheme.typography.labelLarge, color = PineInk)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((stat.minutes.toFloat() / maxDaily).coerceIn(0.08f, 1f))
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreen),
                                )
                            }
                        }
                        if (index != focusDayStats.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("任务排行", style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Spacer(modifier = Modifier.height(10.dp))
                    focusTaskStats.take(5).forEachIndexed { index, stat ->
                        Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.26f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                    Text(stat.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                                    Text("${stat.count} 轮 · ${stat.minutes} 分钟", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                                }
                                SelectionChip(text = "${index + 1}", chosen = index == 0, onClick = {})
                            }
                        }
                        if (index != minOf(focusTaskStats.size, 5) - 1) Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("模式占比", style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        focusModeStats.forEach { (modeTitle, minutes) ->
                            MetricCard(modeTitle, "${minutes} 分钟", if (modeTitle == PomodoroMode.Focus.title) ForestGreen else Ginkgo)
                        }
                    }
                }
            }
        }
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("专注记录", style = MaterialTheme.typography.titleLarge, color = PineInk)
                    if (focusRecords.isNotEmpty()) {
                        ActionPill("清空", WarmMist) {
                            focusRecords.clear()
                            saveFocusRecords(focusRecordPrefs, focusRecords)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (focusRecords.isEmpty()) {
                    Text("还没有完成的专注记录。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.7f))
                } else {
                    focusRecords.take(12).forEachIndexed { index, record ->
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.36f)) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(record.taskTitle, style = MaterialTheme.typography.titleMedium, color = PineInk)
                                Text("${record.modeTitle} · ${formatSeconds(record.seconds)}", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
                                Text(record.finishedAt, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
                            }
                        }
                        if (index != minOf(focusRecords.size, 12) - 1) Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}
