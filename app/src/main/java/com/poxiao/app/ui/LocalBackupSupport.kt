package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

internal val LocalBackupPreferenceNames = listOf(
    "ui_prefs",
    "notification_preferences",
    "campus_map_prefs",
    "home_workbench",
    "course_notes",
    "review_planner",
    "todo_board",
    "focus_bridge",
    "focus_records",
    "schedule_cache",
    "schedule_exam_week",
    "schedule_auth",
    "campus_services_prefs",
    "calculator_prefs",
)

internal fun buildLocalBackupPayload(context: Context): String {
    val root = JSONObject().apply {
        put("app", "poxiao")
        put("version", 1)
        put("exported_at", System.currentTimeMillis())
        put("prefs", JSONObject().apply {
            LocalBackupPreferenceNames.forEach { name ->
                put(name, sharedPreferencesToJson(context.getSharedPreferences(name, Context.MODE_PRIVATE)))
            }
        })
    }
    return root.toString()
}

internal fun restoreLocalBackupPayload(context: Context, raw: String): String {
    if (raw.isBlank()) return "请先粘贴备份文本。"
    return try {
        val root = JSONObject(raw)
        if (root.optString("app") != "poxiao") return "备份文本不属于当前应用。"
        val prefsRoot = root.optJSONObject("prefs") ?: return "备份文本缺少本地数据内容。"
        LocalBackupPreferenceNames.forEach { name ->
            val prefsJson = prefsRoot.optJSONObject(name) ?: JSONObject()
            restoreSharedPreferencesFromJson(context.getSharedPreferences(name, Context.MODE_PRIVATE), prefsJson)
        }
        refreshLocalReminderSchedule(context)
        "已恢复本地数据。为确保所有页面状态刷新，建议返回首页或重启应用。"
    } catch (_: Throwable) {
        "恢复失败，请确认备份文本完整且未被改动。"
    }
}

private fun sharedPreferencesToJson(prefs: SharedPreferences): JSONObject {
    return JSONObject().apply {
        prefs.all.forEach { (key, value) ->
            put(key, preferenceValueToJson(value))
        }
    }
}

private fun preferenceValueToJson(value: Any?): JSONObject {
    return JSONObject().apply {
        when (value) {
            is String -> {
                put("type", "string")
                put("value", value)
            }
            is Int -> {
                put("type", "int")
                put("value", value)
            }
            is Long -> {
                put("type", "long")
                put("value", value)
            }
            is Float -> {
                put("type", "float")
                put("value", value.toDouble())
            }
            is Boolean -> {
                put("type", "boolean")
                put("value", value)
            }
            is Set<*> -> {
                put("type", "string_set")
                put("value", JSONArray(value.filterIsInstance<String>()))
            }
            else -> {
                put("type", "string")
                put("value", value?.toString().orEmpty())
            }
        }
    }
}

private fun restoreSharedPreferencesFromJson(
    prefs: SharedPreferences,
    json: JSONObject,
) {
    val editor = prefs.edit().clear()
    val keys = json.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val item = json.optJSONObject(key) ?: continue
        when (item.optString("type")) {
            "string" -> editor.putString(key, item.optString("value"))
            "int" -> editor.putInt(key, item.optInt("value"))
            "long" -> editor.putLong(key, item.optLong("value"))
            "float" -> editor.putFloat(key, item.optDouble("value").toFloat())
            "boolean" -> editor.putBoolean(key, item.optBoolean("value"))
            "string_set" -> {
                val array = item.optJSONArray("value") ?: JSONArray()
                val values = mutableSetOf<String>()
                for (index in 0 until array.length()) {
                    values.add(array.optString(index))
                }
                editor.putStringSet(key, values)
            }
        }
    }
    editor.apply()
}
