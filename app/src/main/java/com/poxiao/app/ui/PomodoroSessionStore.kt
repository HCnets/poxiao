package com.poxiao.app.ui

import android.content.SharedPreferences
import org.json.JSONObject

private const val PomodoroSessionKey = "pomodoro_session_v1"

internal fun loadPomodoroSession(prefs: SharedPreferences): PomodoroSession? {
    val raw = prefs.getString(PomodoroSessionKey, "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val item = JSONObject(raw)
        val originalRunning = item.optBoolean("running")
        val savedAt = item.optLong("savedAt")
        val savedLeft = item.optInt("leftSeconds")
        val elapsedSeconds = if (originalRunning && savedAt > 0L) {
            ((System.currentTimeMillis() - savedAt) / 1000L).toInt().coerceAtLeast(0)
        } else {
            0
        }
        val restoredLeft = (savedLeft - elapsedSeconds).coerceAtLeast(0)
        PomodoroSession(
            mode = runCatching { PomodoroMode.valueOf(item.optString("mode", PomodoroMode.Focus.name)) }
                .getOrDefault(PomodoroMode.Focus),
            presetTitle = item.optString("presetTitle", "25 分钟"),
            presetSeconds = item.optInt("presetSeconds", 25 * 60).coerceAtLeast(1),
            leftSeconds = restoredLeft,
            running = originalRunning && restoredLeft > 0,
            autoNext = item.optBoolean("autoNext", true),
            strictMode = item.optBoolean("strictMode"),
            boundTask = item.optString("boundTask"),
            sound = item.optString("sound", "山风"),
            ambientOn = item.optBoolean("ambientOn"),
            focusMinutes = item.optInt("focusMinutes", 125),
            cycles = item.optInt("cycles", 4),
            customMinutes = item.optInt("customMinutes", 25),
            customSeconds = item.optInt("customSeconds"),
            onlyPendingGoalTasks = item.optBoolean("onlyPendingGoalTasks", true),
        )
    }.getOrNull()
}

internal fun savePomodoroSession(
    prefs: SharedPreferences,
    session: PomodoroSession,
) {
    prefs.edit()
        .putString(
            PomodoroSessionKey,
            JSONObject().apply {
                put("mode", session.mode.name)
                put("presetTitle", session.presetTitle)
                put("presetSeconds", session.presetSeconds)
                put("leftSeconds", session.leftSeconds)
                put("running", session.running)
                put("autoNext", session.autoNext)
                put("strictMode", session.strictMode)
                put("boundTask", session.boundTask)
                put("sound", session.sound)
                put("ambientOn", session.ambientOn)
                put("focusMinutes", session.focusMinutes)
                put("cycles", session.cycles)
                put("customMinutes", session.customMinutes)
                put("customSeconds", session.customSeconds)
                put("onlyPendingGoalTasks", session.onlyPendingGoalTasks)
                put("savedAt", System.currentTimeMillis())
            }.toString(),
        )
        .apply()
}
