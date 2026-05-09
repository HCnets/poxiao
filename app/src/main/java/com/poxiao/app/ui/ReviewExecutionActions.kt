package com.poxiao.app.ui

import android.content.Context
import com.poxiao.app.security.SecurePrefs
import java.time.Instant
import java.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject

internal fun applyReviewBridgeExecution(
    context: Context,
): String {
    val bridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE)
    val payload = loadPendingReviewBridge(bridgePrefs) ?: return "当前没有可执行的复习桥接计划。"
    val tasks = loadTodoTasks(todoPrefs).toMutableList()
    val createdTaskIds = mutableListOf<String>()
    val createdTaskTitles = mutableListOf<String>()
    val executedAt = System.currentTimeMillis()
    payload.items.take(3).forEach { item ->
        val task = buildReviewBridgeTodoTask(item)
        if (tasks.none { it.id == task.id }) {
            tasks.add(0, task)
            createdTaskIds += task.id
            createdTaskTitles += task.title
        }
    }
    saveTodoTasks(todoPrefs, tasks)
    val boundTaskTitle = payload.items.firstOrNull()?.let { "复习：${it.noteTitle}" }.orEmpty()
    payload.items.firstOrNull()?.let { first ->
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "复习：${first.noteTitle}")
        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "复习计划")
    }
    val summaryText = "已生成 ${payload.items.take(3).size} 条复习待办，并按复习时间自动匹配提醒，首项已绑定到番茄钟。"
    updateReviewExecutionLinks(
        context = context,
        reviewItemIds = payload.items.take(3).map { it.id },
        executedAt = executedAt,
    )
    val executionJson = JSONObject().apply {
        put("createdTaskIds", JSONArray(createdTaskIds))
        put("createdTaskTitles", JSONArray(createdTaskTitles))
        put("linkedReviewItemIds", JSONArray(payload.items.take(3).map { it.id }))
        put("boundTaskTitle", boundTaskTitle)
        put("summary", summaryText)
        put("executedAt", executedAt)
    }
    val historyArray = runCatching {
        JSONArray(bridgePrefs.getString("execution_history_v1", "").orEmpty().ifBlank { "[]" })
    }.getOrDefault(JSONArray())
    historyArray.put(0, executionJson)
    while (historyArray.length() > 8) {
        historyArray.remove(historyArray.length() - 1)
    }
    bridgePrefs.edit()
        .putString("last_executed_review_plan_v1", payload.prompt)
        .putString("last_execution_summary_v1", executionJson.toString())
        .putString("execution_history_v1", historyArray.toString())
        .putLong("last_executed_at_v1", System.currentTimeMillis())
        .apply()
    return summaryText
}

internal fun replayReviewBridgeExecution(
    context: Context,
    execution: ReviewBridgeExecutionSummary,
): String {
    val bridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE)
    if (execution.createdTaskTitles.isEmpty()) return "这条历史没有可回放的复习待办。"
    val tasks = loadTodoTasks(todoPrefs).toMutableList()
    val replayTaskIds = mutableListOf<String>()
    val replayTaskTitles = mutableListOf<String>()
    val replayReviewItemIds = resolveReviewExecutionItemIds(context, execution)
    val replayExecutedAt = System.currentTimeMillis()
    execution.createdTaskTitles.forEachIndexed { index, title ->
        val replayId = "assistant-review-replay-${execution.executedAt}-$index"
        if (tasks.none { it.id == replayId }) {
            val task = buildReviewReplayTodoTask(
                replayId = replayId,
                title = title,
                sourceExecutedAt = execution.executedAt,
            )
            tasks.add(0, task)
            replayTaskIds += task.id
            replayTaskTitles += task.title
        }
    }
    saveTodoTasks(todoPrefs, tasks)
    if (execution.boundTaskTitle.isNotBlank()) {
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", execution.boundTaskTitle)
        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "复习计划")
    }
    val replaySummaryText = "已从历史回放 ${replayTaskTitles.size} 条复习待办，并重新匹配提醒，恢复番茄绑定。"
    updateReviewExecutionLinks(
        context = context,
        reviewItemIds = replayReviewItemIds,
        executedAt = replayExecutedAt,
    )
    val diffSummary = buildReviewReplayDiffSummary(
        sourceTitles = execution.createdTaskTitles,
        replayTitles = replayTaskTitles,
        sourceBoundTitle = execution.boundTaskTitle,
        replayBoundTitle = execution.boundTaskTitle,
    )
    val replayJson = JSONObject().apply {
        put("createdTaskIds", JSONArray(replayTaskIds))
        put("createdTaskTitles", JSONArray(replayTaskTitles))
        put("linkedReviewItemIds", JSONArray(replayReviewItemIds))
        put("boundTaskTitle", execution.boundTaskTitle)
        put("summary", replaySummaryText)
        put("executedAt", replayExecutedAt)
        put("replaySourceExecutedAt", execution.executedAt)
        put("diffSummary", diffSummary)
    }
    val historyArray = runCatching {
        JSONArray(bridgePrefs.getString("execution_history_v1", "").orEmpty().ifBlank { "[]" })
    }.getOrDefault(JSONArray())
    historyArray.put(0, replayJson)
    while (historyArray.length() > 8) {
        historyArray.remove(historyArray.length() - 1)
    }
    bridgePrefs.edit()
        .putString("last_execution_summary_v1", replayJson.toString())
        .putString("execution_history_v1", historyArray.toString())
        .putLong("last_executed_at_v1", System.currentTimeMillis())
        .apply()
    return replaySummaryText
}

internal fun undoReviewBridgeExecution(
    context: Context,
): String {
    val bridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE)
    val summary = loadReviewBridgeExecutionSummary(bridgePrefs) ?: return "当前没有可撤销的复习计划执行结果。"
    if (summary.createdTaskIds.isEmpty()) return "当前没有可撤销的复习待办。"
    val tasks = loadTodoTasks(todoPrefs).filterNot { it.id in summary.createdTaskIds }
    saveTodoTasks(todoPrefs, tasks)
    val currentBound = SecurePrefs.getString(focusPrefs, "bound_task_title_secure", "bound_task_title")
    if (summary.boundTaskTitle.isNotBlank() && currentBound == summary.boundTaskTitle) {
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "")
        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "")
    }
    clearReviewExecutionLinks(context, summary.executedAt)
    bridgePrefs.edit()
        .remove("last_execution_summary_v1")
        .apply()
    return "已撤销上一次复习计划执行，移除了 ${summary.createdTaskIds.size} 条待办，并清除了对应番茄绑定。"
}
