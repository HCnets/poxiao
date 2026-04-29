package com.poxiao.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poxiao.app.data.HitaCourseBlock
import com.poxiao.app.ui.theme.BambooGlass
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import java.time.LocalDate
import java.time.YearMonth

@Composable
internal fun MonthScheduleCard(
    month: YearMonth,
    selectedDate: String,
    activeDates: List<String>,
    selectedCourses: List<HitaCourseBlock>,
    selectedEvents: List<ScheduleExtraEvent>,
    selectedCourse: HitaCourseBlock?,
    onSelectDate: (String) -> Unit,
    onSelectCourse: (HitaCourseBlock) -> Unit,
) {
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value % 7
    val cells = remember(month) {
        val values = mutableListOf<LocalDate?>()
        repeat(leading) { values.add(null) }
        for (day in 1..month.lengthOfMonth()) {
            values.add(month.atDay(day))
        }
        values
    }
    val rows = remember(cells) { cells.chunked(7) }

    GlassCard {
        Text("${month.year} 年 ${month.monthValue} 月", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { label ->
                Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = ForestDeep.copy(alpha = 0.68f))
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        rows.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { date ->
                    val iso = date?.toString().orEmpty()
                    val selected = iso == selectedDate
                    val active = iso in activeDates
                    val isToday = iso == LocalDate.now().toString()
                    MonthCell(
                        date = date,
                        selected = selected,
                        active = active,
                        isToday = isToday,
                        onClick = {
                            if (date != null) onSelectDate(date.toString())
                        },
                    )
                }
                repeat(7 - row.size) {
                    Spacer(modifier = Modifier.width(40.dp).height(52.dp))
                }
            }
            if (rowIndex != rows.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("当日详情", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (selectedCourses.isEmpty() && selectedEvents.isEmpty()) {
                DaySummaryCard(title = "暂无课程", body = "选中的日期当前没有返回课程安排。")
            } else {
                selectedCourses.forEach { course ->
                    DaySummaryCard(
                        title = course.courseName,
                        body = "${course.classroom} · ${course.teacher.ifBlank { "教师待补充" }}",
                        selected = selectedCourse?.let {
                            it.courseName == course.courseName && it.dayOfWeek == course.dayOfWeek && it.majorIndex == course.majorIndex
                        } == true,
                        onClick = { onSelectCourse(course) },
                    )
                }
                selectedEvents.forEach { event ->
                    DaySummaryCard(
                        title = event.title,
                        body = "${event.time} · ${event.type}${if (event.note.isBlank()) "" else " · ${event.note}"}",
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCell(
    date: LocalDate?,
    selected: Boolean,
    active: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "month-cell",
    )
    val background = when {
        selected -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.72f), BambooGlass))
        active -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.46f), Color.White.copy(alpha = 0.26f)))
        else -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.12f)))
    }

    Box(
        modifier = Modifier
            .width(40.dp)
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(BorderStroke(1.dp, if (selected) BambooStroke else Color.White.copy(alpha = 0.16f)), RoundedCornerShape(16.dp))
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Ginkgo, Color.Transparent))),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium, color = if (selected) PineInk else ForestDeep)
                if (active) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(MossGreen, TeaGreen))),
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySummaryCard(
    title: String,
    body: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (selected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, if (selected) Color.White.copy(alpha = 0.24f) else BambooStroke.copy(alpha = 0.16f)),
        modifier = Modifier
            .width(220.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = PineInk)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
        }
    }
}
