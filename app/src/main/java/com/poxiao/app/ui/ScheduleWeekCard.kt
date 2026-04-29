package com.poxiao.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poxiao.app.data.HitaCourseBlock
import com.poxiao.app.data.HitaTimeSlot
import com.poxiao.app.data.HitaWeekDay
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.PineInk

@Composable
internal fun WeekScheduleCard(
    title: String,
    slots: List<HitaTimeSlot>,
    days: List<HitaWeekDay>,
    courses: List<HitaCourseBlock>,
    selectedCourse: HitaCourseBlock?,
    onSelectCourse: (HitaCourseBlock) -> Unit,
) {
    val headerHeight = 60.dp
    val rowHeight = 108.dp
    val freeTimeSummary = remember(slots, days, courses) { weeklyFreeTimeSummary(slots, days, courses) }
    val weeklyAnalysis = remember(days, courses) { weeklyCourseAnalysis(days, courses) }
    GlassCard {
        Text(title, style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Spacer(modifier = Modifier.height(headerHeight))
                slots.forEach { slot ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.24f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.16f)),
                        modifier = Modifier
                            .width(94.dp)
                            .requiredHeight(rowHeight),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
                            Text(slot.label, style = MaterialTheme.typography.labelLarge, color = PineInk)
                            Text(slot.timeRange, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                        }
                    }
                }
            }
            days.forEach { day ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.width(132.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selectedCourse?.dayOfWeek == day.weekDay) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.26f),
                        border = BorderStroke(
                            1.dp,
                            if (selectedCourse?.dayOfWeek == day.weekDay) Color.White.copy(alpha = 0.24f) else BambooStroke.copy(alpha = 0.16f),
                        ),
                        modifier = Modifier.requiredHeight(headerHeight),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(day.label, style = MaterialTheme.typography.titleMedium, color = PineInk)
                            Text(day.date, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                        }
                    }
                    slots.forEach { slot ->
                        val matches = courses.filter { it.dayOfWeek == day.weekDay && it.majorIndex == slot.majorIndex }
                        CourseCell(
                            courses = matches,
                            selectedCourse = selectedCourse,
                            onClick = { course -> onSelectCourse(course) },
                        )
                    }
                }
            }
        }
        if (weeklyAnalysis.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text("本周课表分析", style = MaterialTheme.typography.titleMedium, color = PineInk)
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                weeklyAnalysis.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.14f)),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(item.dayLabel, style = MaterialTheme.typography.titleSmall, color = PineInk)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item.highlights.forEach { label ->
                                    SelectionChip(text = label, chosen = false, onClick = {})
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("本周空闲时段", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            freeTimeSummary.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.14f)),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(item.dayLabel, style = MaterialTheme.typography.titleSmall, color = PineInk)
                            Text("${item.freeCount} 个空闲大节", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.7f))
                        }
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item.labels.forEach { label ->
                                SelectionChip(text = label, chosen = false, onClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CourseCell(
    courses: List<HitaCourseBlock>,
    selectedCourse: HitaCourseBlock?,
    onClick: (HitaCourseBlock) -> Unit,
) {
    val primaryCourse = courses.firstOrNull()
    val hasConflict = courses.size > 1
    val isSelected = primaryCourse != null && selectedCourse?.let {
        courses.any { course ->
            it.courseName == course.courseName && it.dayOfWeek == course.dayOfWeek && it.majorIndex == course.majorIndex
        }
    } == true
    val isRelated = primaryCourse != null && selectedCourse != null && courses.any { selectedCourse.courseName == it.courseName }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = when {
            hasConflict -> Color(0xFFF6E0D2)
            isSelected -> Color.White.copy(alpha = 0.34f)
            isRelated -> Color.White.copy(alpha = 0.28f)
            else -> Color.White.copy(alpha = 0.22f)
        },
        border = BorderStroke(
            1.dp,
            when {
                hasConflict -> Color(0xFFD39B74)
                isSelected -> Color.White.copy(alpha = 0.28f)
                isRelated -> BambooStroke.copy(alpha = 0.2f)
                else -> BambooStroke.copy(alpha = 0.14f)
            },
        ),
        modifier = Modifier
            .requiredHeight(108.dp)
            .clickable(enabled = primaryCourse != null) { primaryCourse?.let(onClick) },
    ) {
        if (primaryCourse == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("留白", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.35f))
            }
        } else {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(primaryCourse.accent)),
                )
                Text(
                    if (hasConflict) "发生冲突" else primaryCourse.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = PineInk,
                )
                if (hasConflict) {
                    Text("${courses.size} 门课程落在同一大节", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9A5B34))
                    Text(courses.joinToString(" / ") { it.courseName }, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.74f), maxLines = 2)
                } else {
                    Text(primaryCourse.classroom, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                    Text(primaryCourse.teacher.ifBlank { "教师待补充" }, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                }
            }
        }
    }
}
