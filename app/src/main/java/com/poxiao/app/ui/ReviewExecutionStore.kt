package com.poxiao.app.ui

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

private const val LastExecutionSummaryKey = "last_execution_summary_v1"
private const val ExecutionHistoryKey = "execution_history_v1"

internal fun loadReviewBridgeExecutionSummary(
    prefs: SharedPreferences,
): ReviewBridgeExecutionSummary? {
    val raw = prefs.getString(LastExecutionSummaryKey, "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        buildReviewExecutionSummary(JSONObject(raw))
    }.getOrNull()
}

internal fun loadReviewBridgeExecutionHistory(
    prefs: SharedPreferences,
): List<ReviewBridgeExecutionSummary> {
    val raw = prefs.getString(ExecutionHistoryKey, "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                add(buildReviewExecutionSummary(json))
            }
        }
    }.getOrDefault(emptyList())
}

private fun buildReviewExecutionSummary(json: JSONObject): ReviewBridgeExecutionSummary {
    return ReviewBridgeExecutionSummary(
        createdTaskIds = buildJsonStringList(json.optJSONArray("createdTaskIds")),
        createdTaskTitles = buildJsonStringList(json.optJSONArray("createdTaskTitles")),
        linkedReviewItemIds = buildJsonStringList(json.optJSONArray("linkedReviewItemIds")),
        boundTaskTitle = json.optString("boundTaskTitle"),
        summary = json.optString("summary"),
        executedAt = json.optLong("executedAt"),
        replaySourceExecutedAt = json.optLong("replaySourceExecutedAt"),
        diffSummary = json.optString("diffSummary"),
    )
}

private fun buildJsonStringList(array: JSONArray?): List<String> {
    if (array == null) return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val value = array.optString(index)
            if (value.isNotBlank()) add(value)
        }
    }
}
