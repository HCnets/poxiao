package com.poxiao.app.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private data class PendingReviewBridgeItem(
    val id: String,
    val courseName: String,
    val noteTitle: String,
    val sourceTitle: String,
    val recommendedMinutes: Int,
    val nextReviewAt: Long,
)

private data class PendingReviewBridgePayload(
    val reason: String,
    val prompt: String,
    val items: List<PendingReviewBridgeItem>,
)

internal data class ReviewBridgeExecutionSummary(
    val createdTaskIds: List<String>,
    val createdTaskTitles: List<String>,
    val linkedReviewItemIds: List<String>,
    val boundTaskTitle: String,
    val summary: String,
    val executedAt: Long,
    val replaySourceExecutedAt: Long = 0L,
    val diffSummary: String = "",
)

private fun loadPendingReviewBridge(
    prefs: android.content.SharedPreferences,
): PendingReviewBridgePayload? {
    val raw = prefs.getString("pending_review_plan_v1", "").orEmpty()
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

internal fun shouldExecuteReviewBridge(
    prompt: String,
    summaries: List<AssistantContextSummary>,
): Boolean {
    val normalized = prompt.lowercase()
    val hasReviewBridge = summaries.any { it.id == "review_bridge" }
    return hasReviewBridge && (
        normalized.contains("鎺ョ澶嶄範") ||
            normalized.contains("瀹夋帓浠婂ぉ澶嶄範") ||
            normalized.contains("鎵ц澶嶄範") ||
            normalized.contains("寮€濮嬪涔犺锟?) ||
            normalized.contains("鎶婂涔犺鍒掕惤锟?)
        )
}

internal fun buildAssistantRequestPrompt(
    userInput: String,
    summaries: List<AssistantContextSummary>,
    availableTools: List<AssistantToolDefinition>,
    toolCall: AssistantToolCall?,
): String {
    return buildString {
        appendLine("浣犳槸鈥滅牬鏅撯€濇牎鍥涔犳晥鐜囧簲鐢ㄥ唴鐨勬櫤鑳藉姪鐞嗭拷?)
        appendLine("璇蜂娇鐢ㄧ畝浣撲腑鏂囧洖绛旓紝浼樺厛鍩轰簬缁欏畾鐨勫簲鐢ㄤ笂涓嬫枃鎻愪緵鐩存帴銆佸彲鎵ц鐨勫缓璁拷?)
        if (summaries.isNotEmpty()) {
            appendLine("銆愬綋鍓嶆湰鍦颁笂涓嬫枃锟?)
            summaries.sortedByDescending { it.priority }.take(6).forEach { summary ->
                appendLine("- ${summary.source}锟?{summary.title}锟?{summary.body}")
            }
        }
        if (availableTools.isNotEmpty()) {
            appendLine("銆愬綋鍓嶅彲鐢ㄧ殑鏈湴鑳藉姏锟?)
            appendLine(availableTools.joinToString("锟?) { it.title })
        }
        toolCall?.let {
            appendLine("銆愬垰鎵ц鐨勬湰鍦板姩浣滐拷?)
            appendLine("- ${it.title}锟?{it.summary}")
        }
        appendLine("銆愮敤鎴烽棶棰橈拷?)
        append(userInput.trim())
    }.trim()
}

internal fun resolveAssistantReply(
    remoteReply: String,
    fallback: String,
    toolCall: AssistantToolCall?,
): String {
    val trimmed = remoteReply.trim()
    if (trimmed.isBlank()) return fallback
    val remoteUnavailable = trimmed.startsWith("鏄熺伀鍔╂墜鏆傛椂涓嶅彲锟?)
    return if (remoteUnavailable && toolCall != null) {
        "$fallback\n\n$trimmed"
    } else {
        trimmed
    }
}

internal fun applyReviewBridgeExecution(
    context: Context,
): String {
    val bridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE)
    val payload = loadPendingReviewBridge(bridgePrefs) ?: return "褰撳墠娌℃湁鍙墽琛岀殑澶嶄範妗ユ帴璁″垝锟?
    val tasks = loadTodoTasks(todoPrefs).toMutableList()
    val createdTaskIds = mutableListOf<String>()
    val createdTaskTitles = mutableListOf<String>()
    val executedAt = System.currentTimeMillis()
    payload.items.take(3).forEach { item ->
        val dueDate = runCatching {
            Instant.ofEpochMilli(item.nextReviewAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        }.getOrNull()
        val dueText = dueDate?.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) ?: "浠婂ぉ 20:00"
        val task = TodoTask(
            id = "assistant-review-${item.id}",
            title = "澶嶄範锟?{item.noteTitle}",
            note = "${item.courseName} 路 鏉ユ簮锟?{item.sourceTitle}銆媆n鐢辨櫤鑳戒綋鎺ョ澶嶄範璁″垝鐢熸垚锟?,
            quadrant = TodoQuadrant.ImportantNotUrgent,
            priority = TodoPriority.High,
            dueText = dueText,
            tags = listOf("澶嶄範", "鏅鸿兘鎺掔▼", item.courseName),
            listName = "澶嶄範璁″垝",
            reminderText = "鎻愬墠 30 鍒嗛挓",
            repeatText = "涓嶉噸锟?,
            subtasks = listOf(
                TodoSubtask("鍥炵湅鐭ヨ瘑锟?),
                TodoSubtask("瀹屾垚涓€杞彛杩版垨榛樺啓"),
            ),
            focusGoal = when {
                item.recommendedMinutes >= 50 -> 3
                item.recommendedMinutes >= 30 -> 2
                else -> 1
            },
        )
        if (tasks.none { it.id == task.id }) {
            tasks.add(0, task)
            createdTaskIds += task.id
            createdTaskTitles += task.title
        }
    }
    saveTodoTasks(todoPrefs, tasks)
    val boundTaskTitle = payload.items.firstOrNull()?.let { "澶嶄範锟?{it.noteTitle}" }.orEmpty()
    payload.items.firstOrNull()?.let { first ->
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "澶嶄範锟?{first.noteTitle}")
        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "澶嶄範璁″垝")
    }
    val summaryText = "宸茬敓锟?${payload.items.take(3).size} 鏉″涔犲緟鍔烇紝骞舵妸棣栭」缁戝畾鍒扮暘鑼勯挓锟?
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
    if (execution.createdTaskTitles.isEmpty()) return "杩欐潯鍘嗗彶娌℃湁鍙洖鏀剧殑澶嶄範寰呭姙锟?
    val tasks = loadTodoTasks(todoPrefs).toMutableList()
    val replayTaskIds = mutableListOf<String>()
    val replayTaskTitles = mutableListOf<String>()
    val replayReviewItemIds = resolveReviewExecutionItemIds(context, execution)
    val replayExecutedAt = System.currentTimeMillis()
    execution.createdTaskTitles.forEachIndexed { index, title ->
        val replayId = "assistant-review-replay-${execution.executedAt}-$index"
        if (tasks.none { it.id == replayId }) {
            val task = TodoTask(
                id = replayId,
                title = title,
                note = "鏍规嵁鍘嗗彶鎺ョ璁板綍鍥炴斁鐢熸垚銆俓n鏉ユ簮鎵ц鏃堕棿锟?{
                    formatSyncTime(
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(execution.executedAt),
                            java.time.ZoneId.systemDefault(),
                        ),
                    )
                }",
                quadrant = TodoQuadrant.ImportantNotUrgent,
                priority = TodoPriority.High,
                dueText = "浠婂ぉ 20:00",
                tags = listOf("澶嶄範", "鍘嗗彶鍥炴斁"),
                listName = "澶嶄範璁″垝",
                reminderText = "鎻愬墠 30 鍒嗛挓",
                repeatText = "涓嶉噸锟?,
                subtasks = listOf(
                    TodoSubtask("鍥炵湅鐭ヨ瘑锟?),
                    TodoSubtask("瀹屾垚涓€杞彛杩版垨榛樺啓"),
                ),
                focusGoal = 1,
            )
            tasks.add(0, task)
            replayTaskIds += task.id
            replayTaskTitles += task.title
        }
    }
    saveTodoTasks(todoPrefs, tasks)
    if (execution.boundTaskTitle.isNotBlank()) {
        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", execution.boundTaskTitle)
        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "澶嶄範璁″垝")
    }
    val replaySummaryText = "宸蹭粠鍘嗗彶鍥炴斁 ${replayTaskTitles.size} 鏉″涔犲緟鍔烇紝骞舵仮澶嶇暘鑼勭粦瀹氾拷?
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

internal fun loadReviewBridgeExecutionSummary(
    prefs: android.content.SharedPreferences,
): ReviewBridgeExecutionSummary? {
    val raw = prefs.getString("last_execution_summary_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val json = JSONObject(raw)
        ReviewBridgeExecutionSummary(
            createdTaskIds = buildList {
                val array = json.optJSONArray("createdTaskIds") ?: JSONArray()
                for (index in 0 until array.length()) {
                    val id = array.optString(index)
                    if (id.isNotBlank()) add(id)
                }
            },
            createdTaskTitles = buildList {
                val array = json.optJSONArray("createdTaskTitles") ?: JSONArray()
                for (index in 0 until array.length()) {
                    val title = array.optString(index)
                    if (title.isNotBlank()) add(title)
                }
            },
            linkedReviewItemIds = buildList {
                val array = json.optJSONArray("linkedReviewItemIds") ?: JSONArray()
                for (index in 0 until array.length()) {
                    val id = array.optString(index)
                    if (id.isNotBlank()) add(id)
                }
            },
            boundTaskTitle = json.optString("boundTaskTitle"),
            summary = json.optString("summary"),
            executedAt = json.optLong("executedAt"),
            replaySourceExecutedAt = json.optLong("replaySourceExecutedAt"),
            diffSummary = json.optString("diffSummary"),
        )
    }.getOrNull()
}

internal fun undoReviewBridgeExecution(
    context: Context,
): String {
    val bridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE)
    val summary = loadReviewBridgeExecutionSummary(bridgePrefs) ?: return "褰撳墠娌℃湁鍙挙閿€鐨勫涔犺鍒掓墽琛岀粨鏋滐拷?
    if (summary.createdTaskIds.isEmpty()) return "褰撳墠娌℃湁鍙挙閿€鐨勫涔犲緟鍔烇拷?
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
    return "宸叉挙閿€涓婁竴娆″涔犺鍒掓墽琛岋紝绉婚櫎锟?${summary.createdTaskIds.size} 鏉″緟鍔烇紝骞舵竻闄や簡瀵瑰簲鐣寗缁戝畾锟?
}

internal fun loadReviewBridgeExecutionHistory(
    prefs: android.content.SharedPreferences,
): List<ReviewBridgeExecutionSummary> {
    val raw = prefs.getString("execution_history_v1", "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                add(
                    ReviewBridgeExecutionSummary(
                        createdTaskIds = buildList {
                            val ids = json.optJSONArray("createdTaskIds") ?: JSONArray()
                            for (idIndex in 0 until ids.length()) {
                                val id = ids.optString(idIndex)
                                if (id.isNotBlank()) add(id)
                            }
                        },
                        createdTaskTitles = buildList {
                            val titles = json.optJSONArray("createdTaskTitles") ?: JSONArray()
                            for (titleIndex in 0 until titles.length()) {
                                val title = titles.optString(titleIndex)
                                if (title.isNotBlank()) add(title)
                            }
                        },
                        linkedReviewItemIds = buildList {
                            val ids = json.optJSONArray("linkedReviewItemIds") ?: JSONArray()
                            for (idIndex in 0 until ids.length()) {
                                val id = ids.optString(idIndex)
                                if (id.isNotBlank()) add(id)
                            }
                        },
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

private fun buildReviewReplayDiffSummary(
    sourceTitles: List<String>,
    replayTitles: List<String>,
    sourceBoundTitle: String,
    replayBoundTitle: String,
): String {
    val sameTasks = sourceTitles.intersect(replayTitles.toSet()).size
    val missingTasks = sourceTitles.size - sameTasks
    val addedTasks = replayTitles.size - sameTasks
    val bindingText = when {
        sourceBoundTitle.isBlank() && replayBoundTitle.isBlank() -> "鏈仮澶嶇暘鑼勭粦锟?
        sourceBoundTitle == replayBoundTitle -> "宸叉仮澶嶅師鐣寗缁戝畾"
        else -> "鐣寗缁戝畾宸茶皟锟?
    }
    return buildString {
        append("涓庡師鏂规鐩告瘮锟?)
        append("澶嶇敤锟?$sameTasks 锟?)
        if (addedTasks > 0) append("锛屾柊锟?$addedTasks 锟?)
        if (missingTasks > 0) append("锛岀己锟?$missingTasks 锟?)
        append("锟?bindingText锟?)
    }
}

private fun updateReviewExecutionLinks(
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

private fun clearReviewExecutionLinks(
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

private fun resolveReviewExecutionItemIds(
    context: Context,
    execution: ReviewBridgeExecutionSummary,
): List<String> {
    if (execution.linkedReviewItemIds.isNotEmpty()) return execution.linkedReviewItemIds
    val reviewItems = ReviewPlannerStore(context).loadItems()
    val titles = execution.createdTaskTitles.map { it.removePrefix("澶嶄範锟?).trim() }.filter { it.isNotBlank() }.toSet()
    return reviewItems.filter { it.noteTitle in titles }.map { it.id }
}
