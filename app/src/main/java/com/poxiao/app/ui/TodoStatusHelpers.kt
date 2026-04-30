package com.poxiao.app.ui

import androidx.compose.ui.graphics.Color
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import java.time.LocalDateTime

internal fun todoDueStatus(task: TodoTask): TodoDueStatus {
    if (task.done) return TodoDueStatus("已完成", MossGreen)
    val now = LocalDateTime.now()
    val dueAt = parseTodoDateTime(task.dueText, now)
    val isGoalTask = task.focusGoal > 0 && task.focusCount < task.focusGoal
    if (dueAt != null) {
        val today = now.toLocalDate()
        val dueDate = dueAt.toLocalDate()
        val thisWeekEnd = today.plusDays((7 - today.dayOfWeek.value).coerceAtLeast(0).toLong())
        return when {
            dueAt.isBefore(now.minusMinutes(1)) -> if (isGoalTask) TodoDueStatus("目标超期", Ginkgo) else TodoDueStatus("已逾期", Ginkgo)
            dueDate == today -> if (isGoalTask) TodoDueStatus("目标临期", Ginkgo) else TodoDueStatus("临近截止", Ginkgo)
            dueDate == today.plusDays(1) -> if (isGoalTask) TodoDueStatus("目标将至", TeaGreen) else TodoDueStatus("即将到期", TeaGreen)
            !dueDate.isAfter(thisWeekEnd) -> if (isGoalTask) TodoDueStatus("目标推进中", ForestGreen) else TodoDueStatus("本周处理", ForestGreen)
            else -> if (isGoalTask) TodoDueStatus("目标推进中", ForestGreen) else TodoDueStatus("已安排", WarmMist)
        }
    }
    if (isGoalTask) {
        return when {
            task.dueText.contains("今天") || task.dueText.contains("今晚") -> TodoDueStatus("目标临期", Ginkgo)
            task.dueText.contains("明天") || task.dueText.contains("明晚") -> TodoDueStatus("目标将至", TeaGreen)
            else -> TodoDueStatus("目标推进中", ForestGreen)
        }
    }
    return when {
        task.dueText.contains("今天") || task.dueText.contains("今晚") -> TodoDueStatus("临近截止", Ginkgo)
        task.dueText.contains("明天") || task.dueText.contains("明晚") -> TodoDueStatus("即将到期", TeaGreen)
        task.dueText.contains("周") || task.dueText.contains("本周") -> TodoDueStatus("本周处理", ForestGreen)
        else -> TodoDueStatus("已安排", WarmMist)
    }
}
