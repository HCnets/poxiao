package com.poxiao.app.ui

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

private const val PendingReviewPlanKey = "pending_review_plan_v1"

internal fun loadPendingReviewBridge(
    prefs: SharedPreferences,
): PendingReviewBridgePayload? {
    val raw = prefs.getString(PendingReviewPlanKey, "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val json = JSONObject(raw)
        val items = buildList {
            val array = json.optJSONArray("items") ?: JSONArray()
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    PendingReviewBridgeItem(
                        id = item.optString("id"),
                        courseName = item.optString("courseName"),
                        noteTitle = item.optString("noteTitle"),
                        sourceTitle = item.optString("sourceTitle"),
                        recommendedMinutes = item.optInt("recommendedMinutes", 25),
                        nextReviewAt = item.optLong("nextReviewAt"),
                        mastery = item.optDouble("mastery", 0.0).toFloat(),
                        importanceScore = item.optInt("importanceScore", 0),
                    ),
                )
            }
        }
        PendingReviewBridgePayload(
            reason = json.optString("reason"),
            prompt = json.optString("prompt"),
            items = items,
        )
    }.getOrNull()
}
