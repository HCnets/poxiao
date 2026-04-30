package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
import com.poxiao.app.review.ReviewPlannerStore
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
        val json = JSONObject(raw)
        ReviewBridgeExecutionSummary(
            createdTaskIds = buildJsonStringList(json.optJSONArray("createdTaskIds")),
            createdTaskTitles = buildJsonStringList(json.optJSONArray("createdTaskTitles")),
            linkedReviewItemIds = buildJsonStringList(json.optJSONArray("linkedReviewItemIds")),
            boundTaskTitle = json.optString("boundTaskTitle"),
            summary = json.optString("summary"),
            executedAt = json.optLong("executedAt"),
            replaySourceExecutedAt = json.optLong("replaySourceExecutedAt"),
            diffSummary = json.optString("diffSummary"),
        )
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
                add(
                    ReviewBridgeExecutionSummary(
                        createdTaskIds = buildJsonStringList(json.optJSONArray("createdTaskIds")),
                        createdTaskTitles = buildJsonStringList(json.optJSONArray("createdTaskTitles")),
                        linkedReviewItemIds = buildJsonStringList(json.optJSONArray("linkedReviewItemIds")),
                        boundTaskTitle = json.optString("boundTaskTitle"),
                        summary = json.optString("summary"),
                        executedAt = json.optLong("executedAt"),
                        replaySourceExecutedAt = json.optLong("replaySourceExecutedAt"),
                        diffSummary = json.optString("diffSummary"),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}

internal fun buildReviewReplayDiffSummary(
    sourceTitles: List<String>,
    replayTitles: List<String>,
    sourceBoundTitle: String,
    replayBoundTitle: String,
): String {
    val sameTasks = sourceTitles.intersect(replayTitles.toSet()).size
    val missingTasks = sourceTitles.size - sameTasks
    val addedTasks = replayTitles.size - sameTasks
    val bindingText = when {
        sourceBoundTitle.isBlank() && replayBoundTitle.isBlank() -> "未恢复番茄绑定"
        sourceBoundTitle == replayBoundTitle -> "已恢复原番茄绑定"
        else -> "番茄绑定已调整"
    }
    return buildString {
        append("与原方案相比：")
        append("复用了 $sameTasks 项")
        if (addedTasks > 0) append("，新增 $addedTasks 项")
        if (missingTasks > 0) append("，缺少 $missingTasks 项")
        append("，$bindingText。")
    }
}

internal fun updateReviewExecutionLinks(
    context: Context,
    reviewItemIds: List<String>,
    executedAt: Long,
) {
    if (reviewItemIds.isEmpty() || executedAt <= 0L) return
    val store = ReviewPlannerStore(context)
    val updated = store.loadItems().map { item ->
        if (item.id in reviewItemIds) {
            item.copy(assistantExecutionAt = executedAt)
        } else {
            item
        }
    }
    store.saveItems(updated)
}

internal fun clearReviewExecutionLinks(
    context: Context,
    executedAt: Long,
) {
    if (executedAt <= 0L) return
    val store = ReviewPlannerStore(context)
    val updated = store.loadItems().map { item ->
        if (item.assistantExecutionAt == executedAt) item.copy(assistantExecutionAt = 0L) else item
    }
    store.saveItems(updated)
}

internal fun resolveReviewExecutionItemIds(
    context: Context,
    execution: ReviewBridgeExecutionSummary,
): List<String> {
    if (execution.linkedReviewItemIds.isNotEmpty()) return execution.linkedReviewItemIds
    val reviewItems = ReviewPlannerStore(context).loadItems()
    val titles = execution.createdTaskTitles.map { it.removePrefix("复习：").trim() }.filter { it.isNotBlank() }.toSet()
    return reviewItems.filter { it.noteTitle in titles }.map { it.id }
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
