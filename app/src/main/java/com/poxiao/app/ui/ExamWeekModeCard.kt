package com.poxiao.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poxiao.app.data.HitaWeekSchedule
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private enum class ExamWeekFilter(val title: String) {
    All("全部"),
    Pending("待处理"),
    Urgent("临近"),
    Finished("已完成"),
}

private enum class ExamWeekTypeFilter(val title: String) {
    All("全部类型"),
    Exam("只看考试"),
    Assignment("只看作业"),
    Review("只看复习"),
}

internal data class ExamWeekItem(
    val id: String,
    val date: String,
    val title: String,
    val subtitle: String,
    val detail: String,
    val accent: Color,
    val priority: Int,
    val countdownLabel: String,
    val finished: Boolean = false,
)

internal fun buildExamWeekItems(
    schedule: HitaWeekSchedule,
    events: List<ScheduleExtraEvent>,
    completedIds: List<String>,
): List<ExamWeekItem> {
    val scheduleItems = schedule.days.flatMap { day ->
        schedule.courses
            .filter { it.dayOfWeek == day.weekDay }
            .sortedBy { it.majorIndex }
            .map { course ->
                ExamWeekItem(
                    id = "course-${day.fullDate}-${course.courseName}-${course.majorIndex}",
                    date = day.fullDate,
                    title = course.courseName,
                    subtitle = "${day.label} · 第 ${course.majorIndex} 大节",
                    detail = buildString {
                        append(course.classroom.ifBlank { "教室待补充" })
                        append(" · ")
                        append(course.teacher.ifBlank { "教师待补充" })
                    },
                    accent = Color(course.accent),
                    priority = countdownPriority(day.fullDate),
                    countdownLabel = countdownLabel(day.fullDate),
                    finished = "course-${day.fullDate}-${course.courseName}-${course.majorIndex}" in completedIds,
                )
            }
    }
    val eventItems = events
        .filter { it.type == "考试" || it.type == "作业" || it.type == "复习" }
        .sortedWith(compareBy<ScheduleExtraEvent> { it.date }.thenBy { eventSortKey(it.time) })
        .map { event ->
            ExamWeekItem(
                id = event.id,
                date = event.date,
                title = event.title,
                subtitle = "${event.date.substringAfterLast("-")} · ${event.time} · ${event.type}",
                detail = event.note.ifBlank { "已加入考试周冲刺列表" },
                accent = when (event.type) {
                    "考试" -> Ginkgo
                    "作业" -> MossGreen
                    else -> ForestGreen
                },
                priority = countdownPriority(event.date, event.type),
                countdownLabel = countdownLabel(event.date),
                finished = event.id in completedIds,
            )
        }
    return (eventItems + scheduleItems)
        .sortedWith(compareBy<ExamWeekItem> { it.priority }.thenBy { it.date }.thenBy { it.subtitle })
}

private fun countdownLabel(date: String): String {
    val target = runCatching { LocalDate.parse(date) }.getOrNull() ?: return "待定"
    val days = ChronoUnit.DAYS.between(LocalDate.now(), target).toInt()
    return when {
        days < 0 -> "已结束"
        days == 0 -> "今天"
        days == 1 -> "明天"
        else -> "还有 ${days} 天"
    }
}

private fun countdownPriority(date: String, type: String = ""): Int {
    val target = runCatching { LocalDate.parse(date) }.getOrNull() ?: return 2
    val days = ChronoUnit.DAYS.between(LocalDate.now(), target).toInt()
    return when {
        type == "考试" && days <= 1 -> 0
        days <= 2 -> 1
        else -> 2
    }
}

@Composable
internal fun ExamWeekModeCard(
    weekTitle: String,
    selectedDate: String,
    items: List<ExamWeekItem>,
    onCreateTodo: (ExamWeekItem) -> Unit,
    onBindFocus: (ExamWeekItem) -> Unit,
    onBindFocusGroup: (String, List<ExamWeekItem>) -> Unit,
    onCreateTodoGroup: (String, List<ExamWeekItem>) -> Unit,
    onToggleFinishedGroup: (List<ExamWeekItem>, Boolean) -> Unit,
    onToggleFinished: (ExamWeekItem) -> Unit,
    onClearFinished: () -> Unit,
) {
    var filter by remember { mutableStateOf(ExamWeekFilter.All) }
    var typeFilter by remember { mutableStateOf(ExamWeekTypeFilter.All) }
    val collapsedDates = remember { mutableStateListOf<String>() }
    val filteredItems = remember(items, filter, typeFilter) {
        val base = when (filter) {
            ExamWeekFilter.All -> items
            ExamWeekFilter.Pending -> items.filterNot { it.finished }
            ExamWeekFilter.Urgent -> items.filter { !it.finished && (it.priority <= 1 || it.countdownLabel == "今天" || it.countdownLabel == "明天") }
            ExamWeekFilter.Finished -> items.filter { it.finished }
        }
        base.filter { item ->
            when (typeFilter) {
                ExamWeekTypeFilter.All -> true
                ExamWeekTypeFilter.Exam -> item.subtitle.contains("考试") || item.title.contains("考试")
                ExamWeekTypeFilter.Assignment -> item.subtitle.contains("作业") || item.title.contains("作业")
                ExamWeekTypeFilter.Review -> item.subtitle.contains("复习") || item.title.contains("复习")
            }
        }
    }
    val grouped = filteredItems
        .sortedWith(compareBy<ExamWeekItem> { it.priority }.thenBy { it.date }.thenBy { it.subtitle })
        .groupBy { it.date }
    val urgentPending = items.firstOrNull { !it.finished && (it.priority == 0 || it.countdownLabel == "今天" || it.countdownLabel == "明天") }
    GlassCard {
        Text("考试周模式", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Text("把考试、作业和本周课程压成一条冲刺视图，便于临近考试时快速排程。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            MetricCard("当前周", weekTitle, ForestGreen)
            MetricCard("冲刺项", items.size.toString(), Ginkgo)
            MetricCard("今日", selectedDate.substringAfterLast("-"), MossGreen)
            MetricCard("已完成", items.count { it.finished }.toString(), TeaGreen)
        }
        urgentPending?.let { item ->
            Spacer(modifier = Modifier.height(14.dp))
            TrendInsightCard(
                title = "临近事项",
                headline = item.title,
                body = "${item.countdownLabel} · ${item.subtitle}",
                accent = item.accent,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(
            options = ExamWeekFilter.entries.toList(),
            selected = filter,
            label = { it.title },
            onSelect = { filter = it },
        )
        Spacer(modifier = Modifier.height(10.dp))
        SelectionRow(
            options = ExamWeekTypeFilter.entries.toList(),
            selected = typeFilter,
            label = { it.title },
            onSelect = { typeFilter = it },
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (items.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                ActionPill("清空已完成", WarmMist, onClick = onClearFinished)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (filteredItems.isEmpty()) {
            Text("当前还没有考试、作业或复习事件。可以先在日视图里加入“考试”或“作业”事件。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.72f))
        } else {
            grouped.entries.forEachIndexed { index, entry ->
                val collapsed = entry.key in collapsedDates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        entry.key,
                        modifier = Modifier.clickable {
                            if (collapsed) collapsedDates.remove(entry.key) else collapsedDates.add(entry.key)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = PineInk,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (collapsed) "展开 ${entry.value.size} 项" else "收起 ${entry.value.size} 项",
                            modifier = Modifier.clickable {
                                if (collapsed) collapsedDates.remove(entry.key) else collapsedDates.add(entry.key)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestDeep.copy(alpha = 0.72f),
                        )
                        Text(
                            "整组转待办",
                            modifier = Modifier.clickable { onCreateTodoGroup(entry.key, entry.value) },
                            style = MaterialTheme.typography.labelMedium,
                            color = PineInk,
                        )
                        Text(
                            "整组绑定专注",
                            modifier = Modifier.clickable { onBindFocusGroup(entry.key, entry.value) },
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestDeep.copy(alpha = 0.82f),
                        )
                        Text(
                            if (entry.value.all { it.finished }) "整组恢复" else "整组完成",
                            modifier = Modifier.clickable {
                                onToggleFinishedGroup(entry.value, !entry.value.all { it.finished })
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (entry.value.all { it.finished }) ForestDeep.copy(alpha = 0.74f) else TeaGreen.copy(alpha = 0.92f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (!collapsed) {
                    entry.value.forEachIndexed { itemIndex, item ->
                        Surface(shape = RoundedCornerShape(22.dp), color = if (item.finished) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.34f)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.width(24.dp).height(4.dp).clip(CircleShape).background(item.accent))
                                        Text(item.title, style = MaterialTheme.typography.titleMedium, color = if (item.finished) ForestDeep.copy(alpha = 0.58f) else PineInk)
                                    }
                                    SelectionChip(text = if (item.finished) "已完成" else item.countdownLabel, chosen = item.priority == 0 || item.finished, onClick = {})
                                }
                                Text(
                                    when (item.priority) {
                                        0 -> "最高优先"
                                        1 -> "临近处理"
                                        else -> "常规安排"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ForestDeep.copy(alpha = 0.68f),
                                )
                                Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                                Text(item.detail, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    ActionPill(if (item.finished) "恢复" else "完成", if (item.finished) WarmMist else TeaGreen) { onToggleFinished(item) }
                                    ActionPill("转待办", MossGreen) { onCreateTodo(item) }
                                    ActionPill("绑定专注", ForestGreen) { onBindFocus(item) }
                                }
                            }
                        }
                        if (itemIndex != entry.value.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.18f)) {
                        Text(
                            "当前分组已折叠，保留 ${entry.value.size} 项摘要。",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestDeep.copy(alpha = 0.7f),
                        )
                    }
                }
                if (index != grouped.entries.size - 1) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
