package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.poxiao.app.data.HitaCourseBlock
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import org.json.JSONObject

internal data class DayTimelineEntry(
    val sortKey: Int,
    val title: String,
    val subtitle: String,
    val detail: String,
    val accent: Color,
    val tags: List<String>,
    val selectableCourse: HitaCourseBlock? = null,
    val extraEvent: ScheduleExtraEvent? = null,
)

private data class ScheduleEventDraft(
    val eventId: String?,
    val date: String,
    val title: String,
    val time: String,
    val type: String,
    val note: String,
)

@Composable
internal fun DayScheduleCard(
    dates: List<String>,
    selectedDate: String,
    courses: List<HitaCourseBlock>,
    events: List<ScheduleExtraEvent>,
    selectedCourse: HitaCourseBlock?,
    selectedEventId: String?,
    onSelectDate: (String) -> Unit,
    onSelectCourse: (HitaCourseBlock) -> Unit,
    onSelectEvent: (ScheduleExtraEvent) -> Unit,
    onAddEvent: (ScheduleExtraEvent) -> Unit,
    onUpdateEvent: (ScheduleExtraEvent) -> Unit,
    onDeleteEvent: (String) -> Unit,
) {
    val context = LocalContext.current
    val draftPrefs = remember { context.getSharedPreferences("schedule_event_draft", Context.MODE_PRIVATE) }
    var draftTitle by remember(selectedDate) { mutableStateOf("") }
    var draftTime by remember(selectedDate) { mutableStateOf("19:30") }
    var draftType by remember(selectedDate) { mutableStateOf("作业") }
    var draftNote by remember(selectedDate) { mutableStateOf("") }
    var eventHint by remember(selectedDate) { mutableStateOf("") }
    var draftReady by remember(selectedDate) { mutableStateOf(false) }
    val timelineEntries = buildDayTimelineEntries(courses, events)
    val editingEvent = events.firstOrNull { it.id == selectedEventId }

    LaunchedEffect(selectedDate, editingEvent?.id) {
        val restoredDraft = loadScheduleEventDraft(draftPrefs)
        if (editingEvent != null) {
            if (restoredDraft != null && restoredDraft.eventId == editingEvent.id) {
                draftTitle = restoredDraft.title
                draftTime = restoredDraft.time.ifBlank { editingEvent.time }
                draftType = restoredDraft.type.ifBlank { editingEvent.type }
                draftNote = restoredDraft.note
                eventHint = "已恢复 ${editingEvent.title} 的未完成编辑。"
            } else {
                draftTitle = editingEvent.title
                draftTime = editingEvent.time
                draftType = editingEvent.type
                draftNote = editingEvent.note
                eventHint = "正在编辑 ${editingEvent.title}"
            }
        } else {
            if (restoredDraft != null && restoredDraft.date == selectedDate && restoredDraft.eventId == null) {
                draftTitle = restoredDraft.title
                draftTime = restoredDraft.time.ifBlank { "19:30" }
                draftType = restoredDraft.type.ifBlank { "作业" }
                draftNote = restoredDraft.note
                eventHint = if (restoredDraft.title.isNotBlank() || restoredDraft.note.isNotBlank()) {
                    "已恢复 $selectedDate 的未完成事件草稿。"
                } else {
                    ""
                }
            } else {
                draftTitle = ""
                draftTime = "19:30"
                draftType = "作业"
                draftNote = ""
            }
        }
        draftReady = true
    }

    LaunchedEffect(selectedDate, draftTitle, draftTime, draftType, draftNote, editingEvent?.id, draftReady) {
        if (!draftReady) return@LaunchedEffect
        if (draftTitle.isBlank() && draftNote.isBlank() && draftTime == "19:30" && draftType == "作业") {
            clearScheduleEventDraft(draftPrefs)
        } else {
            saveScheduleEventDraft(
                draftPrefs,
                ScheduleEventDraft(
                    eventId = editingEvent?.id,
                    date = selectedDate,
                    title = draftTitle,
                    time = draftTime,
                    type = draftType,
                    note = draftNote,
                ),
            )
        }
    }

    GlassCard {
        Text("当天安排", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        SelectionRow(options = dates, selected = selectedDate, label = { it.substringAfterLast("-") }, onSelect = onSelectDate)
        Spacer(modifier = Modifier.height(12.dp))
        DayScheduleTimelineList(
            timelineEntries = timelineEntries,
            courses = courses,
            selectedCourse = selectedCourse,
            selectedEventId = selectedEventId,
            onSelectCourse = onSelectCourse,
            onSelectEvent = onSelectEvent,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(if (editingEvent == null) "添加学习事件" else "编辑学习事件", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = draftTitle,
            onValueChange = { draftTitle = it },
            label = { Text("事件标题") },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = draftTime,
                onValueChange = { draftTime = it },
                label = { Text("时间") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = draftType,
                onValueChange = { draftType = it },
                label = { Text("类型") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = draftNote,
            onValueChange = { draftNote = it },
            label = { Text("说明") },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (draftTitle.isNotBlank()) {
                        val updatedEvent = ScheduleExtraEvent(
                            id = editingEvent?.id ?: "event-${System.currentTimeMillis()}",
                            date = selectedDate,
                            title = draftTitle,
                            time = draftTime,
                            type = draftType.ifBlank { "事件" },
                            note = draftNote,
                        )
                        if (editingEvent == null) {
                            onAddEvent(updatedEvent)
                            eventHint = "已加入 $selectedDate $draftTime 的${draftType.ifBlank { "学习" }}事件"
                            clearScheduleEventDraft(draftPrefs)
                        } else {
                            onUpdateEvent(updatedEvent)
                            eventHint = "已更新 ${updatedEvent.title}"
                            clearScheduleEventDraft(draftPrefs)
                        }
                        draftTitle = ""
                        draftTime = "19:30"
                        draftType = "作业"
                        draftNote = ""
                    } else {
                        eventHint = "请先填写事件标题"
                    }
                },
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.weight(1f),
            ) {
                Text(if (editingEvent == null) "加入当天时间线" else "保存修改")
            }
            if (editingEvent != null) {
                OutlinedButton(
                    onClick = {
                        onDeleteEvent(editingEvent.id)
                        clearScheduleEventDraft(draftPrefs)
                        draftTitle = ""
                        draftTime = "19:30"
                        draftType = "作业"
                        draftNote = ""
                        eventHint = "已删除 ${editingEvent.title}"
                    },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("删除事件")
                }
            }
        }
        if (eventHint.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = eventHint,
                style = MaterialTheme.typography.bodyMedium,
                color = ForestDeep.copy(alpha = 0.76f),
            )
        }
    }
}

@Composable
private fun DayScheduleTimelineList(
    timelineEntries: List<DayTimelineEntry>,
    courses: List<HitaCourseBlock>,
    selectedCourse: HitaCourseBlock?,
    selectedEventId: String?,
    onSelectCourse: (HitaCourseBlock) -> Unit,
    onSelectEvent: (ScheduleExtraEvent) -> Unit,
) {
    if (timelineEntries.isEmpty()) {
        Text("当天暂时没有课程。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.7f))
        return
    }

    timelineEntries.forEachIndexed { index, entry ->
        val course = entry.selectableCourse
        val extraEvent = entry.extraEvent
        val isSelected = course != null && selectedCourse?.let {
            it.courseName == course.courseName && it.dayOfWeek == course.dayOfWeek && it.majorIndex == course.majorIndex
        } == true
        val isEventSelected = extraEvent?.id == selectedEventId
        val relationTags = if (course != null) remember(courses, course) { dayCourseTags(courses, course) } else entry.tags
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = if (isSelected || isEventSelected) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.24f),
            border = BorderStroke(
                1.dp,
                if (isSelected || isEventSelected) Color.White.copy(alpha = 0.26f) else BambooStroke.copy(alpha = 0.16f),
            ),
            modifier = Modifier.clickable(enabled = course != null || extraEvent != null) {
                when {
                    course != null -> onSelectCourse(course)
                    extraEvent != null -> onSelectEvent(extraEvent)
                }
            },
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(64.dp)
                        .clip(CircleShape)
                        .background(entry.accent),
                )
                Column {
                    Text(entry.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Text(entry.subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                    Text(entry.detail, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                    if (relationTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            relationTags.forEach { tag ->
                                SelectionChip(text = tag, chosen = false, onClick = {})
                            }
                        }
                    }
                }
            }
        }
        if (index != timelineEntries.lastIndex) Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
internal fun CourseDetailCard(
    course: HitaCourseBlock,
    weekTitle: String,
    dayLabel: String,
    onOpenNotes: () -> Unit,
    onClose: () -> Unit,
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("课程详情", style = MaterialTheme.typography.titleMedium, color = PineInk)
                Text(
                    if (dayLabel.isBlank()) weekTitle else "$weekTitle · $dayLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.7f),
                )
            }
            SelectionChip(text = "收起", chosen = false, onClick = onClose)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(course.courseName, style = MaterialTheme.typography.headlineSmall, color = PineInk)
        Spacer(modifier = Modifier.height(12.dp))
        CourseMetaRow("教师", course.teacher.ifBlank { "教师待补充" })
        CourseMetaRow("教室", course.classroom.ifBlank { "教室待补充" })
        CourseMetaRow("时段", "第 ${course.majorIndex} 大节")
        CourseMetaRow("周内", if (dayLabel.isBlank()) "待补充" else dayLabel)
        CourseMetaRow("课程状态", "已接入教务周课表")
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionChip(text = "课程笔记", chosen = true, onClick = onOpenNotes)
            SelectionChip(text = "收起详情", chosen = false, onClick = onClose)
        }
    }
}

@Composable
private fun CourseMetaRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = PineInk,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(0.7f),
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

private fun dayCourseTags(
    courses: List<HitaCourseBlock>,
    course: HitaCourseBlock,
): List<String> {
    val sameSlotCount = courses.count { it.majorIndex == course.majorIndex }
    val hasPrev = courses.any { it.majorIndex == course.majorIndex - 1 }
    val hasNext = courses.any { it.majorIndex == course.majorIndex + 1 }
    val tags = mutableListOf<String>()
    if (sameSlotCount > 1) tags += "冲突 ${sameSlotCount} 门"
    if (hasPrev || hasNext) tags += "连堂"
    return tags
}

private fun buildDayTimelineEntries(
    courses: List<HitaCourseBlock>,
    events: List<ScheduleExtraEvent>,
): List<DayTimelineEntry> {
    val courseEntries = courses.map { course ->
        DayTimelineEntry(
            sortKey = course.majorIndex * 100,
            title = course.courseName,
            subtitle = "第 ${course.majorIndex} 时段",
            detail = "${course.classroom.ifBlank { "教室待补充" }} · ${course.teacher.ifBlank { "教师待补充" }}",
            accent = Color(course.accent),
            tags = emptyList(),
            selectableCourse = course,
        )
    }
    val eventEntries = events.map { event ->
        DayTimelineEntry(
            sortKey = eventSortKey(event.time),
            title = event.title,
            subtitle = "${event.time} · ${event.type}",
            detail = event.note.ifBlank { "已加入当天学习时间线" },
            accent = when (event.type) {
                "考试" -> Ginkgo
                "作业" -> MossGreen
                else -> ForestGreen
            },
            tags = listOf(event.type),
            extraEvent = event,
        )
    }
    return (courseEntries + eventEntries).sortedBy { it.sortKey }
}

private fun loadScheduleEventDraft(
    prefs: SharedPreferences,
): ScheduleEventDraft? {
    val raw = prefs.getString("schedule_event_draft_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val json = JSONObject(raw)
        ScheduleEventDraft(
            eventId = json.optString("eventId").ifBlank { null },
            date = json.optString("date"),
            title = json.optString("title"),
            time = json.optString("time", "19:30"),
            type = json.optString("type", "作业"),
            note = json.optString("note"),
        )
    }.getOrNull()
}

private fun saveScheduleEventDraft(
    prefs: SharedPreferences,
    draft: ScheduleEventDraft,
) {
    prefs.edit()
        .putString(
            "schedule_event_draft_v1",
            JSONObject().apply {
                put("eventId", draft.eventId)
                put("date", draft.date)
                put("title", draft.title)
                put("time", draft.time)
                put("type", draft.type)
                put("note", draft.note)
            }.toString(),
        )
        .apply()
}

private fun clearScheduleEventDraft(prefs: SharedPreferences) {
    prefs.edit().remove("schedule_event_draft_v1").apply()
}
