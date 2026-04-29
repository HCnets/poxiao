package com.poxiao.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.poxiao.app.data.FeedCard
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist

internal data class GradeTrendPoint(
    val termName: String,
    val averageScore: Double,
    val averageGradePoint: Double,
    val credits: Double,
    val excellentCount: Int,
    val warningCount: Int,
    val courseCount: Int,
    val rawCards: List<FeedCard>,
)

@Composable
internal fun GradeTrendCard(
    loading: Boolean,
    status: String,
    points: List<GradeTrendPoint>,
    selectedTerm: String?,
    onSelectTerm: (String) -> Unit,
) {
    val selectedPoint = points.firstOrNull { it.termName == selectedTerm } ?: points.firstOrNull()
    var courseKeyword by remember(selectedPoint?.termName) { mutableStateOf("") }
    val bestPoint = points.maxByOrNull { it.averageScore }
    val riskyPoint = points.maxByOrNull {
        val total = it.courseCount.takeIf { count -> count > 0 } ?: 1
        (it.warningCount.toDouble() / total) + if (it.warningCount > 0) 1.0 else 0.0
    }
    var detailExpanded by remember(selectedPoint?.termName) { mutableStateOf(false) }
    val visibleCards = remember(selectedPoint?.termName, selectedPoint?.rawCards, courseKeyword) {
        val cards = selectedPoint?.rawCards.orEmpty()
        if (courseKeyword.isBlank()) {
            cards
        } else {
            cards.filter { listOf(it.title, it.source, it.description).any { text -> text.contains(courseKeyword, ignoreCase = true) } }
        }
    }
    GlassCard {
        Text("成绩趋势", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Text(status, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
        if (loading) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("正在整理平均分、绩点和预警课程。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
            return@GlassCard
        }
        if (points.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("当前还没有可用于分析的成绩记录。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.7f))
            return@GlassCard
        }
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(options = points, selected = selectedPoint ?: points.first(), label = { it.termName }, onSelect = { onSelectTerm(it.termName) })
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            MetricCard("平均分", formatTrendValue(selectedPoint?.averageScore), ForestGreen)
            MetricCard("平均绩点", formatTrendValue(selectedPoint?.averageGradePoint), Ginkgo)
            MetricCard("学分", formatTrendValue(selectedPoint?.credits), MossGreen)
            MetricCard("优秀率", formatPercent(selectedPoint?.excellentCount, selectedPoint?.courseCount), TeaGreen)
            MetricCard("预警率", formatPercent(selectedPoint?.warningCount, selectedPoint?.courseCount), WarmMist)
        }
        if (bestPoint != null || riskyPoint != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                bestPoint?.let { point ->
                    TrendInsightCard(
                        title = "最佳学期",
                        headline = point.termName,
                        body = "平均分 ${formatTrendValue(point.averageScore)} · 绩点 ${formatTrendValue(point.averageGradePoint)}",
                        accent = ForestGreen,
                    )
                }
                riskyPoint?.let { point ->
                    TrendInsightCard(
                        title = "风险提醒",
                        headline = point.termName,
                        body = if (point.warningCount > 0) "共有 ${point.warningCount} 门预警课程，建议优先复盘。" else "当前没有明显预警课程，可继续保持。",
                        accent = WarmMist,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("学期对比", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        val maxScore = points.maxOfOrNull { it.averageScore }?.takeIf { it > 0.0 } ?: 100.0
        points.forEachIndexed { index, point ->
            val scoreRatio = (point.averageScore / maxScore).coerceIn(0.08, 1.0).toFloat()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(point.termName, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                    Text(formatTrendValue(point.averageScore), style = MaterialTheme.typography.labelLarge, color = PineInk)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(scoreRatio)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(if (point.termName == selectedPoint?.termName) ForestGreen else MossGreen),
                    )
                }
            }
            if (index != points.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "课程 ${selectedPoint?.courseCount ?: 0} 门 · 优秀 ${selectedPoint?.excellentCount ?: 0} 门 · 预警 ${selectedPoint?.warningCount ?: 0} 门",
            style = MaterialTheme.typography.bodyLarge,
            color = ForestDeep.copy(alpha = 0.76f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ActionPill(if (detailExpanded) "收起详情" else "展开单科", if (detailExpanded) MossGreen else ForestGreen) {
                detailExpanded = !detailExpanded
            }
        }
        if (detailExpanded && selectedPoint != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = courseKeyword,
                onValueChange = { courseKeyword = it },
                label = { Text("搜索当前学期课程") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (visibleCards.isEmpty()) {
                Text("当前搜索条件下没有匹配课程。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.7f))
            }
            visibleCards.forEachIndexed { index, card ->
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.26f)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(card.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Text(card.source, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                        Text(card.description, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
                    }
                }
                if (index != visibleCards.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        points.forEachIndexed { index, point ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (point.termName == selectedPoint?.termName) Color.White.copy(alpha = 0.38f) else Color.White.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, if (point.termName == selectedPoint?.termName) ForestGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(point.termName, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Text("平均分 ${formatTrendValue(point.averageScore)} · 绩点 ${formatTrendValue(point.averageGradePoint)}", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                    }
                    Text("${point.warningCount} 项预警", style = MaterialTheme.typography.labelLarge, color = if (point.warningCount > 0) Color(0xFF9A5B34) else ForestDeep.copy(alpha = 0.68f))
                }
            }
            if (index != points.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

internal fun formatTrendValue(value: Double?): String {
    return if (value == null || value == 0.0) "--" else String.format("%.2f", value)
}

internal fun formatPercent(count: Int?, total: Int?): String {
    if (count == null || total == null || total <= 0) return "--"
    return "${(count * 100 / total)}%"
}

internal fun buildGradeTrendPoints(cardsByTerm: List<Pair<String, List<FeedCard>>>): List<GradeTrendPoint> {
    return cardsByTerm.map { (termName, cards) ->
        val parsed = cards.map { parseGradeCard(it) }
        val scores = parsed.mapNotNull { it.score }
        val gradePoints = parsed.mapNotNull { it.gradePoint }
        val credits = parsed.mapNotNull { it.credits }
        GradeTrendPoint(
            termName = termName,
            averageScore = if (scores.isEmpty()) 0.0 else scores.average(),
            averageGradePoint = if (gradePoints.isEmpty()) 0.0 else gradePoints.average(),
            credits = credits.sum(),
            excellentCount = parsed.count { (it.score ?: 0.0) >= 90.0 },
            warningCount = parsed.count {
                val score = it.score
                score == null || score < 60.0
            },
            courseCount = cards.size,
            rawCards = cards,
        )
    }
}

private data class ParsedGradeCard(
    val score: Double?,
    val gradePoint: Double?,
    val credits: Double?,
)

private fun parseGradeCard(card: FeedCard): ParsedGradeCard {
    return ParsedGradeCard(
        score = extractNumericValue(card.source, "总评"),
        gradePoint = extractNumericValue(card.source, "绩点"),
        credits = extractNumericValue(card.description, "学分"),
    )
}

private fun extractNumericValue(text: String, key: String): Double? {
    val afterKey = text.substringAfter(key, "").trim()
    if (afterKey.isBlank()) return null
    val token = afterKey.takeWhile { it.isDigit() || it == '.' }
    return token.toDoubleOrNull()
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
            Text(headline, style = MaterialTheme.typography.titleMedium, color = PineInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(body, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.74f), maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}
