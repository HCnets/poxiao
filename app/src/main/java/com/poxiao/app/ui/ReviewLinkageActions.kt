package com.poxiao.app.ui

import android.content.Context
import com.poxiao.app.review.ReviewPlannerStore
import com.poxiao.app.schedule.HitaTimeSlot
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.roundToInt

internal val ReviewIntervals = intArrayOf(0, 1, 3, 7, 14, 30)

internal fun handleReviewTaskCompletion(
    context: Context,
    taskId: String,
): String? {
    if (!taskId.startsWith("assistant-review-")) return null
    val reviewItemId = taskId.removePrefix("assistant-review-")
    
    val store = ReviewPlannerStore(context)
    val items = store.loadItems()
    val itemIndex = items.indexOfFirst { it.id == reviewItemId }
    if (itemIndex < 0) return null

    val item = items[itemIndex]
    // 自动按 "Normal" 信心度推进
    val nextStage = minOf(item.stageIndex + 1, ReviewIntervals.lastIndex)
    val days = ReviewIntervals[nextStage]
    val nextTime = LocalDateTime.now()
        .plusDays(days.toLong())
        .withHour(20)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val oldMastery = item.mastery
    val newMastery = minOf(1f, item.mastery + 0.14f)
    
    val updatedItem = item.copy(
        stageIndex = nextStage,
        reviewCount = item.reviewCount + 1,
        lastReviewedAt = System.currentTimeMillis(),
        nextReviewAt = nextTime,
        mastery = newMastery,
        assistantExecutionAt = 0L // 完成后清除桥接标记
    )

    val updatedItems = items.toMutableList()
    updatedItems[itemIndex] = updatedItem
    store.saveItems(updatedItems)
    
    return "掌握度 ${(oldMastery * 100).roundToInt()}% → ${(newMastery * 100).roundToInt()}%，下次复习已安排在 ${ReviewIntervals[nextStage]} 天后。"
}

internal fun applyScheduleReviewOptimization(
    context: Context,
    block: ScheduleReviewBlock,
): String {
    val suggestedIndex = block.suggestedMajorIndex ?: return "该项无需调优。"
    val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    val tasks = loadTodoTasks(todoPrefs).toMutableList()
    val taskIndex = tasks.indexOfFirst { it.id == block.taskId }
    if (taskIndex < 0) return "未找到对应待办任务。"

    val task = tasks[taskIndex]
    val schedulePrefs = context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE)
    val slots = loadCachedScheduleUiState(schedulePrefs)?.weekSchedule?.timeSlots ?: emptyList()
    val targetSlot = slots.firstOrNull { slot -> slot.majorIndex == suggestedIndex } ?: return "未找到目标时段。"
    
    // 更新待办的时间文本和提醒
    val oldDue = task.dueText
    val timePart = targetSlot.timeRange.substringBefore(" - ")
    val newDue = oldDue.substringBeforeLast(" ") + " " + timePart
    
    tasks[taskIndex] = task.copy(
        dueText = newDue,
        reminderText = "提前 10 分钟" // 智能调优后默认给个合理的提醒
    )
    saveTodoTasks(todoPrefs, tasks)
    return "已将《${block.title}》调优至第 ${suggestedIndex} 大节（${timePart}）。"
}
