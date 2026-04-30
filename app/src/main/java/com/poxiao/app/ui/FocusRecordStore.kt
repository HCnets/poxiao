package com.poxiao.app.ui

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

private const val FocusRecordsKey = "focus_records"

internal fun loadFocusRecords(prefs: SharedPreferences): List<FocusRecord> {
    val raw = prefs.getString(FocusRecordsKey, null) ?: return emptyList()
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
    prefs: SharedPreferences,
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
    prefs.edit().putString(FocusRecordsKey, array.toString()).apply()
}
