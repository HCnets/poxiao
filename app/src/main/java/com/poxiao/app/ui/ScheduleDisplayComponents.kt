package com.poxiao.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.ui.theme.BambooGlass
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import java.time.LocalDate

@Composable
internal fun DaySummaryCard(
    title: String,
    body: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (selected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.22f),
        border = BorderStroke(
            1.dp,
            if (selected) Color.White.copy(alpha = 0.24f) else BambooStroke.copy(alpha = 0.16f),
        ),
        modifier = Modifier
            .width(220.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = PineInk)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
        }
    }
}

@Composable
internal fun TrendInsightCard(
    title: String,
    headline: String,
    body: String,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        modifier = Modifier.width(228.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SelectionChip(text = title, chosen = true, onClick = {})
            Text(
                headline,
                style = MaterialTheme.typography.titleMedium,
                color = PineInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = ForestDeep.copy(alpha = 0.74f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
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
                    Text(
                        courses.joinToString(" / ") { it.courseName },
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestDeep.copy(alpha = 0.74f),
                        maxLines = 2,
                    )
                } else {
                    Text(primaryCourse.classroom, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                    Text(primaryCourse.teacher.ifBlank { "教师待补充" }, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                }
            }
        }
    }
}

@Composable
internal fun MonthCell(
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
            .border(
                BorderStroke(1.dp, if (selected) BambooStroke else Color.White.copy(alpha = 0.16f)),
                RoundedCornerShape(16.dp),
            )
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
                Text(
                    date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) PineInk else ForestDeep,
                )
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
