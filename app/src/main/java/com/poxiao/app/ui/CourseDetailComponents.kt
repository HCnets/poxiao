package com.poxiao.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.PineInk

@Composable
internal fun CourseMetaRow(
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
