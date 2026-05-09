package com.poxiao.app.ui

import android.content.Context
import com.poxiao.app.review.ReviewPlannerStore

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

internal fun clearReviewExecutionLink(
    context: Context,
    reviewItemId: String,
) {
    if (reviewItemId.isBlank()) return
    val store = ReviewPlannerStore(context)
    val updated = store.loadItems().map { item ->
        if (item.id == reviewItemId) item.copy(assistantExecutionAt = 0L) else item
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
