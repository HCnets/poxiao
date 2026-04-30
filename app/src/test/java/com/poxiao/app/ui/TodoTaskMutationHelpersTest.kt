package com.poxiao.app.ui

import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoTaskMutationHelpersTest {
    @Test
    fun `toggleTodoTask advances repeating task instead of marking done`() {
        val now = LocalDateTime.of(2026, 5, 1, 9, 0)
        val tasks = mutableListOf(
            todoTask(
                repeatText = "每天",
                dueText = "05-01 21:00",
                reminderText = "05-01 20:30",
                subtasks = listOf(TodoSubtask("子任务", done = true)),
                focusCount = 2,
            ),
        )

        val message = toggleTodoTask(tasks, 0, now)
        val updated = tasks.first()

        assertEquals("已续期重复任务：复习离散数学", message)
        assertEquals("05-02 21:00", updated.dueText)
        assertEquals("05-02 20:30", updated.reminderText)
        assertEquals(0, updated.focusCount)
        assertFalse(updated.done)
        assertTrue(updated.subtasks.all { !it.done })
    }

    @Test
    fun `applyTodoFocusProgress completes matching subtask and task when goal reached`() {
        val tasks = mutableListOf(
            todoTask(
                title = "番茄专注任务",
                focusGoal = 2,
                focusCount = 1,
                subtasks = listOf(
                    TodoSubtask("一轮整理", done = true),
                    TodoSubtask("二轮复盘", done = false),
                ),
            ),
        )

        val applied = applyTodoFocusProgress(tasks, "番茄专注任务")
        val updated = tasks.first()

        assertTrue(applied)
        assertEquals(2, updated.focusCount)
        assertTrue(updated.done)
        assertTrue(updated.subtasks.all { it.done })
    }

    private fun todoTask(
        title: String = "复习离散数学",
        dueText: String = "05-01 21:00",
        reminderText: String = "提前 30 分钟",
        repeatText: String = "不重复",
        subtasks: List<TodoSubtask> = emptyList(),
        focusCount: Int = 0,
        focusGoal: Int = 0,
    ): TodoTask {
        return TodoTask(
            id = "todo-1",
            title = title,
            note = "测试任务",
            quadrant = TodoQuadrant.ImportantNotUrgent,
            priority = TodoPriority.High,
            dueText = dueText,
            reminderText = reminderText,
            repeatText = repeatText,
            subtasks = subtasks,
            focusCount = focusCount,
            focusGoal = focusGoal,
        )
    }
}
