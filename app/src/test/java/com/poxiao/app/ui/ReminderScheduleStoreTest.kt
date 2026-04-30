package com.poxiao.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderScheduleStoreTest {
    @Test
    fun `buildReminderScheduleSnapshot captures ids and content signature`() {
        val plans = listOf(
            ScheduledReminderPlan(
                id = "todo_1",
                title = "待办提醒",
                body = "复习高数 · 今天 21:00",
                triggerAtMillis = 1_000L,
            ),
            ScheduledReminderPlan(
                id = "exam_2",
                title = "考试周提醒",
                body = "线代期中 · 还剩 3 天",
                triggerAtMillis = 2_000L,
            ),
        )

        val snapshot = buildReminderScheduleSnapshot(plans)

        assertEquals(listOf("todo_1", "exam_2"), snapshot.ids)
        assertEquals(
            "todo_1@1000@${"待办提醒".hashCode()}@${"复习高数 · 今天 21:00".hashCode()}|" +
                "exam_2@2000@${"考试周提醒".hashCode()}@${"线代期中 · 还剩 3 天".hashCode()}",
            snapshot.signature,
        )
    }
}
