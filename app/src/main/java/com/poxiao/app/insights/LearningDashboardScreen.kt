package com.poxiao.app.insights

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.poxiao.app.notes.CourseNoteStore
import com.poxiao.app.review.ReviewPlannerStore
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private data class DashboardMetric(
    val title: String,
    val value: String,
    val subtitle: String,
    val accent: Color,
)

private data class DashboardLine(
    val title: String,
    val body: String,
)

private data class DashboardTrend(
    val title: String,
    val value: String,
    val detail: String,
    val accent: Color,
)

private data class DashboardBreakdown(
    val title: String,
    val lines: List<String>,
)

private data class DashboardRisk(
    val level: String,
    val title: String,
    val body: String,
    val accent: Color,
)

private enum class DashboardScope(val title: String) {
    Today("今天"),
    ThisWeek("本周"),
    Recent7Days("最近 7 天"),
    All("全部"),
}

private fun exportRangeNameForScope(scope: DashboardScope): String {
    return when (scope) {
        DashboardScope.Today -> "Today"
        DashboardScope.ThisWeek -> "ThisWeek"
        DashboardScope.Recent7Days -> "Recent7Days"
        DashboardScope.All -> "All"
    }
}

@Composable
fun LearningDashboardScreen(
    modifier: Modifier = Modifier,
    onOpenExportCenter: () -> Unit = {},
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val palette = PoxiaoThemeState.palette
    val noteStore = remember { CourseNoteStore(context) }
    val exportPrefs = remember {
        context.getSharedPreferences("export_center_prefs", Context.MODE_PRIVATE)
    }
    var scope by remember { mutableStateOf(DashboardScope.ThisWeek) }
    val metrics = remember(scope) {
        buildLearningDashboardMetrics(context, noteStore, scope)
    }
    val highlights = remember(scope) {
        buildLearningDashboardHighlights(context, noteStore, scope)
    }
    val trends = remember(scope) {
        buildLearningDashboardTrends(context, noteStore, scope)
    }
    val breakdowns = remember(scope) {
        buildLearningDashboardBreakdowns(context, noteStore, scope)
    }
    val risks = remember(scope) {
        buildLearningDashboardRisks(context, noteStore, scope)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        palette.backgroundTop.copy(alpha = 0.94f),
                        palette.backgroundBottom.copy(alpha = 0.98f),
                    ),
                ),
            ),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DashboardCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth(0.78f)) {
                        Text("学习数据", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
                        Text("汇总课表密度、待办推进、成绩缓存、课程笔记和专注记录。", style = MaterialTheme.typography.bodyLarge, color = palette.softText)
                    }
                    Button(
                        onClick = onOpenExportCenter,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen.copy(alpha = 0.88f), contentColor = Color.White),
                    ) {
                        Text("导出")
                    }
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMist, contentColor = palette.ink),
                    ) {
                        Text("返回")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                DashboardScopeRow(
                    selected = scope,
                    onSelect = {
                        scope = it
                        exportPrefs.edit()
                            .putString("range", exportRangeNameForScope(it))
                            .apply()
                    },
                )
            }
        }
        item {
            DashboardCard {
                Text("核心概览", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    metrics.forEach { metric ->
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, palette.cardBorder),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth(0.72f)) {
                                    Text(metric.title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
                                    Text(metric.subtitle, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                                }
                                Text(metric.value, style = MaterialTheme.typography.headlineSmall, color = metric.accent)
                            }
                        }
                    }
                }
            }
        }
        item {
            DashboardCard {
                Text("重点洞察", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                if (highlights.isEmpty()) {
                    Text("当前本地数据还不足以生成洞察。继续同步和使用后，这里会逐步丰满。", style = MaterialTheme.typography.bodyLarge, color = palette.softText)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        highlights.forEach { line ->
                            Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.2f)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(line.title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
                                    Text(line.body, style = MaterialTheme.typography.bodyLarge, color = palette.softText)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            DashboardCard {
                Text("趋势分析", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    trends.forEach { trend ->
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, palette.cardBorder),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(0.72f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(trend.title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
                                    Text(trend.detail, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                                }
                                Text(trend.value, style = MaterialTheme.typography.headlineSmall, color = trend.accent)
                            }
                        }
                    }
                }
            }
        }
        item {
            DashboardCard {
                Text("结构分布", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    breakdowns.forEach { block ->
                        Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.2f)) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(block.title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
                                block.lines.forEach { line ->
                                    Text(line, style = MaterialTheme.typography.bodyLarge, color = palette.softText)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            DashboardCard {
                Text("风险提醒", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    risks.forEach { risk ->
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, risk.accent.copy(alpha = 0.3f)),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text("${risk.level} · ${risk.title}", style = MaterialTheme.typography.titleMedium, color = risk.accent)
                                Text(risk.body, style = MaterialTheme.typography.bodyLarge, color = palette.softText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(content: @Composable ColumnScope.() -> Unit) {
    val palette = PoxiaoThemeState.palette
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = palette.card.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, palette.cardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.08f))))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = content,
        )
    }
}

@Composable
private fun DashboardScopeRow(
    selected: DashboardScope,
    onSelect: (DashboardScope) -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DashboardScope.entries.forEach { scope ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (scope == selected) palette.primary.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, if (scope == selected) palette.primary.copy(alpha = 0.28f) else palette.cardBorder),
                modifier = Modifier.clickable { onSelect(scope) },
            ) {
                Text(
                    text = scope.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (scope == selected) Color.White else palette.ink,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
    }
}

private fun buildLearningDashboardMetrics(
    context: Context,
    noteStore: CourseNoteStore,
    scope: DashboardScope,
): List<DashboardMetric> {
    val scheduleState = loadDashboardScheduleState(
        context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE),
        context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE),
    )
    val todoSummary = loadScopedTodoSummary(context.getSharedPreferences("todo_board", Context.MODE_PRIVATE), scope)
    val focusSummary = loadScopedFocusSummary(context.getSharedPreferences("focus_records", Context.MODE_PRIVATE), scope)
    val gradeSummary = loadGradeSummary(context.getSharedPreferences("campus_services_prefs", Context.MODE_PRIVATE))
    val reviewSummary = loadReviewSummary(ReviewPlannerStore(context), scope)
    val noteSummary = loadScopedNoteSummary(noteStore, scope)
    return listOf(
        DashboardMetric("课表", if (scope == DashboardScope.Today) "${scheduleState.todayCount}" else "${scheduleState.courseCount}", "${scope.title}口径 · 今日 ${scheduleState.todayCount} 节", ForestGreen),
        DashboardMetric("待办", "${todoSummary.pending}", "${scope.title}命中 ${todoSummary.total} 项 · 已完成 ${todoSummary.done}", Ginkgo),
        DashboardMetric("专注", "${focusSummary.minutes} 分钟", "记录 ${focusSummary.sessions} 轮 · ${scope.title}", MossGreen),
        DashboardMetric("成绩", "${gradeSummary.count} 条", "优秀 ${gradeSummary.excellent} · 预警 ${gradeSummary.warning}", TeaGreen),
        DashboardMetric("复习", "${reviewSummary.todayDue}", "逾期 ${reviewSummary.overdue} · 已形成记忆 ${reviewSummary.mastered}", MossGreen),
        DashboardMetric("笔记", "${noteSummary.total} 篇", "覆盖 ${noteSummary.courseCount} 门课程", ForestGreen),
    )
}

private fun buildLearningDashboardHighlights(
    context: Context,
    noteStore: CourseNoteStore,
    scope: DashboardScope,
): List<DashboardLine> {
    val scheduleState = loadDashboardScheduleState(
        context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE),
        context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE),
    )
    val todoSummary = loadScopedTodoSummary(context.getSharedPreferences("todo_board", Context.MODE_PRIVATE), scope)
    val focusSummary = loadScopedFocusSummary(context.getSharedPreferences("focus_records", Context.MODE_PRIVATE), scope)
    val gradeSummary = loadGradeSummary(context.getSharedPreferences("campus_services_prefs", Context.MODE_PRIVATE))
    val reviewSummary = loadReviewSummary(ReviewPlannerStore(context), scope)
    val noteSummary = loadScopedNoteSummary(noteStore, scope)
    val highlights = mutableListOf<DashboardLine>()
    if (scheduleState.busiestDay.isNotBlank()) {
        highlights += DashboardLine("课表密度", "${scheduleState.busiestDay}最满，共 ${scheduleState.busiestDayCount} 节，可优先避免在这天叠加重任务。")
    }
    if (todoSummary.pending > 0) {
        highlights += DashboardLine("待办推进", "${scope.title}口径下还有 ${todoSummary.pending} 项未完成，其中 ${todoSummary.withGoal} 项带专注目标，建议优先推进高优先事项。")
    }
    if (focusSummary.topTask.isNotBlank()) {
        highlights += DashboardLine("专注重心", "${scope.title}内最投入的是 ${focusSummary.topTask}，累计 ${focusSummary.topTaskMinutes} 分钟。")
    }
    if (gradeSummary.count > 0) {
        highlights += DashboardLine("成绩状态", "最近同步到 ${gradeSummary.count} 条成绩记录，优秀 ${gradeSummary.excellent} 条，预警 ${gradeSummary.warning} 条。")
    }
    if (reviewSummary.total > 0) {
        highlights += DashboardLine(
            "复习节奏",
            "当前有 ${reviewSummary.todayDue} 项待复习、${reviewSummary.overdue} 项逾期，${scope.title}完成 ${reviewSummary.completedRecent} 次回顾。${if (reviewSummary.topCourse.isNotBlank()) "当前压力最大的是 ${reviewSummary.topCourse}。" else ""}",
        )
    }
    if (noteSummary.total > 0 && noteSummary.latestTitle.isNotBlank()) {
        highlights += DashboardLine("笔记沉淀", "${scope.title}最近更新的是《${noteSummary.latestTitle}》。")
    }
    return highlights
}

private fun buildLearningDashboardTrends(
    context: Context,
    noteStore: CourseNoteStore,
    scope: DashboardScope,
): List<DashboardTrend> {
    val scheduleState = loadDashboardScheduleState(
        context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE),
        context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE),
    )
    val todoSummary = loadScopedTodoSummary(context.getSharedPreferences("todo_board", Context.MODE_PRIVATE), scope)
    val focusSummary = loadScopedFocusSummary(context.getSharedPreferences("focus_records", Context.MODE_PRIVATE), scope)
    val gradeSummary = loadGradeSummary(context.getSharedPreferences("campus_services_prefs", Context.MODE_PRIVATE))
    val reviewSummary = loadReviewSummary(ReviewPlannerStore(context), scope)
    val noteSummary = loadScopedNoteSummary(noteStore, scope)
    return listOf(
        DashboardTrend(
            title = "课程密度",
            value = if (scheduleState.courseCount == 0) "空" else "${scheduleState.courseCount} 节",
            detail = if (scheduleState.busiestDay.isBlank()) "本地还没有稳定的课表密度趋势。" else "${scheduleState.busiestDay} 是最高密度日，今天有 ${scheduleState.todayCount} 节。",
            accent = ForestGreen,
        ),
        DashboardTrend(
            title = "执行节奏",
            value = "${todoSummary.done}/${todoSummary.total}",
            detail = if (todoSummary.total == 0) "${scope.title}还没有待办命中项。" else "待完成 ${todoSummary.pending} 项，其中 ${todoSummary.withGoal} 项带专注目标。",
            accent = Ginkgo,
        ),
        DashboardTrend(
            title = "专注投入",
            value = "${focusSummary.minutes} 分钟",
            detail = if (focusSummary.topTask.isBlank()) "${scope.title}还没有足够的专注记录。" else "${scope.title}投入最多的是 ${focusSummary.topTask}，累计 ${focusSummary.topTaskMinutes} 分钟。",
            accent = MossGreen,
        ),
        DashboardTrend(
            title = "成绩状态",
            value = "${gradeSummary.excellent}/${gradeSummary.count}",
            detail = if (gradeSummary.count == 0) "还没有可分析的成绩缓存。" else "优秀 ${gradeSummary.excellent} 条，预警 ${gradeSummary.warning} 条。",
            accent = TeaGreen,
        ),
        DashboardTrend(
            title = "复习执行",
            value = "${reviewSummary.completedRecent} 次",
            detail = if (reviewSummary.total == 0) "还没有形成复习计划。" else "当前待复习 ${reviewSummary.todayDue} 项，逾期 ${reviewSummary.overdue} 项，已形成记忆 ${reviewSummary.mastered} 项。",
            accent = MossGreen,
        ),
        DashboardTrend(
            title = "知识沉淀",
            value = "${noteSummary.total} 篇",
            detail = if (noteSummary.total == 0) "${scope.title}还没有新增笔记。" else "${scope.title}已沉淀 ${noteSummary.total} 篇笔记，覆盖 ${noteSummary.courseCount} 门课程。",
            accent = ForestGreen,
        ),
    )
}

private fun buildLearningDashboardBreakdowns(
    context: Context,
    noteStore: CourseNoteStore,
    scope: DashboardScope,
): List<DashboardBreakdown> {
    val todoSummary = loadScopedTodoSummary(context.getSharedPreferences("todo_board", Context.MODE_PRIVATE), scope)
    val focusSummary = loadScopedFocusSummary(context.getSharedPreferences("focus_records", Context.MODE_PRIVATE), scope)
    val gradeSummary = loadGradeSummary(context.getSharedPreferences("campus_services_prefs", Context.MODE_PRIVATE))
    val reviewSummary = loadReviewSummary(ReviewPlannerStore(context), scope)
    val noteSummary = loadScopedNoteSummary(noteStore, scope)
    return listOf(
        DashboardBreakdown(
            title = "任务结构",
            lines = listOf(
                "总任务 ${todoSummary.total} 项",
                "待完成 ${todoSummary.pending} 项",
                "已完成 ${todoSummary.done} 项",
                "专注目标任务 ${todoSummary.withGoal} 项",
            ),
        ),
        DashboardBreakdown(
            title = "专注结构",
            lines = listOf(
                "${scope.title}专注 ${focusSummary.minutes} 分钟",
                "共完成 ${focusSummary.sessions} 个轮次",
                if (focusSummary.topTask.isBlank()) "暂未识别出主要任务" else "主投入任务 ${focusSummary.topTask}",
            ),
        ),
        DashboardBreakdown(
            title = "成绩结构",
            lines = listOf(
                "已缓存成绩 ${gradeSummary.count} 条",
                "优秀 ${gradeSummary.excellent} 条",
                "预警 ${gradeSummary.warning} 条",
                "稳定项 ${gradeSummary.count - gradeSummary.warning} 条",
            ),
        ),
        DashboardBreakdown(
            title = "复习结构",
            lines = listOf(
                "复习项总数 ${reviewSummary.total} 项",
                "${scope.title}待复习 ${reviewSummary.todayDue} 项",
                "逾期 ${reviewSummary.overdue} 项",
                if (reviewSummary.topCourse.isBlank()) "当前课程压力均衡" else "压力最高 ${reviewSummary.topCourse} · ${reviewSummary.topCourseCount} 项",
            ),
        ),
        DashboardBreakdown(
            title = "笔记结构",
            lines = listOf(
                "${scope.title}笔记 ${noteSummary.total} 篇",
                "覆盖课程 ${noteSummary.courseCount} 门",
                if (noteSummary.total == 0) "当前范围内暂无新增笔记" else "最近更新 ${noteSummary.latestTitle}",
            ),
        ),
    )
}

private fun buildLearningDashboardRisks(
    context: Context,
    noteStore: CourseNoteStore,
    scope: DashboardScope,
): List<DashboardRisk> {
    val scheduleState = loadDashboardScheduleState(
        context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE),
        context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE),
    )
    val todoSummary = loadScopedTodoSummary(context.getSharedPreferences("todo_board", Context.MODE_PRIVATE), scope)
    val gradeSummary = loadGradeSummary(context.getSharedPreferences("campus_services_prefs", Context.MODE_PRIVATE))
    val reviewSummary = loadReviewSummary(ReviewPlannerStore(context), scope)
    val noteSummary = loadScopedNoteSummary(noteStore, scope)
    val risks = mutableListOf<DashboardRisk>()
    if (scheduleState.busiestDayCount >= 5) {
        risks += DashboardRisk(
            level = "中",
            title = "课程堆叠偏高",
            body = "${scheduleState.busiestDay} 课程最满，共 ${scheduleState.busiestDayCount} 节，建议减少额外高强度任务。",
            accent = Ginkgo,
        )
    }
    if (todoSummary.pending >= 8) {
        risks += DashboardRisk(
            level = "高",
            title = "待办堆积",
            body = "当前还有 ${todoSummary.pending} 项未完成，建议优先清理高优先和临期任务。",
            accent = MossGreen,
        )
    }
    if (gradeSummary.warning > 0) {
        risks += DashboardRisk(
            level = "高",
            title = "成绩预警",
            body = "已同步成绩中有 ${gradeSummary.warning} 条预警，建议优先回看对应课程并补充笔记。",
            accent = TeaGreen,
        )
    }
    if (reviewSummary.overdue >= 4) {
        risks += DashboardRisk(
            level = "高",
            title = "复习积压",
            body = "当前有 ${reviewSummary.overdue} 项复习逾期，建议优先清理 ${reviewSummary.topCourse.ifBlank { "当前最重课程" }} 的短知识点。",
            accent = MossGreen,
        )
    }
    if (noteSummary.total == 0) {
        risks += DashboardRisk(
            level = "低",
            title = "课程沉淀不足",
            body = if (scope == DashboardScope.All) {
                "当前还没有课程笔记，后续复习和智能体摘要上下文会偏薄。"
            } else {
                "${scope.title}范围内还没有新增笔记，复盘材料可能不足。"
            },
            accent = ForestGreen,
        )
    }
    if (risks.isEmpty()) {
        risks += DashboardRisk(
            level = "稳",
            title = "当前状态平衡",
            body = "本地数据没有明显风险项，可以继续按当前节奏推进。",
            accent = ForestGreen,
        )
    }
    return risks
}

private data class DashboardScheduleSummary(
    val courseCount: Int,
    val todayCount: Int,
    val busiestDay: String,
    val busiestDayCount: Int,
)

private data class DashboardTodoSummary(
    val total: Int,
    val pending: Int,
    val done: Int,
    val withGoal: Int,
)

private data class DashboardNoteSummary(
    val total: Int,
    val courseCount: Int,
    val latestTitle: String,
)

private data class DashboardFocusSummary(
    val sessions: Int,
    val minutes: Int,
    val topTask: String,
    val topTaskMinutes: Int,
)

private data class DashboardGradeSummary(
    val count: Int,
    val excellent: Int,
    val warning: Int,
)

private data class DashboardReviewSummary(
    val total: Int,
    val todayDue: Int,
    val overdue: Int,
    val completedRecent: Int,
    val mastered: Int,
    val topCourse: String,
    val topCourseCount: Int,
)

private fun loadDashboardScheduleState(
    primaryPrefs: android.content.SharedPreferences,
    fallbackPrefs: android.content.SharedPreferences,
): DashboardScheduleSummary {
    val raw = primaryPrefs.getString("schedule_cache_v1", "").orEmpty()
        .ifBlank { fallbackPrefs.getString("schedule_cache_v1", "").orEmpty() }
    if (raw.isBlank()) return DashboardScheduleSummary(0, 0, "", 0)
    return runCatching {
        val root = JSONObject(raw)
        val schedule = root.optJSONObject("weekSchedule") ?: return@runCatching DashboardScheduleSummary(0, 0, "", 0)
        val courses = schedule.optJSONArray("courses") ?: JSONArray()
        val days = schedule.optJSONArray("days") ?: JSONArray()
        val today = LocalDate.now().toString()
        val todayWeekDay = (0 until days.length())
            .mapNotNull { index -> days.optJSONObject(index) }
            .firstOrNull { day -> day.optString("fullDate").ifBlank { day.optString("date") } == today }
            ?.optInt("weekDay", -1)
            ?: -1
        val counts = mutableMapOf<String, Int>()
        for (index in 0 until days.length()) {
            val day = days.optJSONObject(index) ?: continue
            counts[day.optString("label")] = 0
        }
        for (index in 0 until courses.length()) {
            val course = courses.optJSONObject(index) ?: continue
            val dayOfWeek = course.optInt("dayOfWeek")
            val day = (0 until days.length()).mapNotNull { days.optJSONObject(it) }.firstOrNull { it.optInt("weekDay") == dayOfWeek }
            val label = day?.optString("label").orEmpty()
            if (label.isNotBlank()) counts[label] = (counts[label] ?: 0) + 1
        }
        val busiest = counts.maxByOrNull { it.value }
        DashboardScheduleSummary(
            courseCount = courses.length(),
            todayCount = if (todayWeekDay > 0) {
                (0 until courses.length()).count { index -> courses.optJSONObject(index)?.optInt("dayOfWeek") == todayWeekDay }
            } else {
                root.optJSONArray("selectedDateCourses")?.length() ?: 0
            },
            busiestDay = busiest?.key.orEmpty(),
            busiestDayCount = busiest?.value ?: 0,
        )
    }.getOrDefault(DashboardScheduleSummary(0, 0, "", 0))
}

private fun loadTodoSummary(prefs: android.content.SharedPreferences): DashboardTodoSummary {
    val raw = prefs.getString("todo_tasks", "").orEmpty()
    if (raw.isBlank()) return DashboardTodoSummary(0, 0, 0, 0)
    return runCatching {
        val array = JSONArray(raw)
        var total = 0
        var pending = 0
        var done = 0
        var withGoal = 0
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            total += 1
            val isDone = item.optBoolean("done")
            if (isDone) done += 1 else pending += 1
            if (item.optInt("focusGoal", 0) > 0) withGoal += 1
        }
        DashboardTodoSummary(total, pending, done, withGoal)
    }.getOrDefault(DashboardTodoSummary(0, 0, 0, 0))
}

private fun loadScopedTodoSummary(
    prefs: android.content.SharedPreferences,
    scope: DashboardScope,
): DashboardTodoSummary {
    if (scope == DashboardScope.All) return loadTodoSummary(prefs)
    val raw = prefs.getString("todo_tasks", "").orEmpty()
    if (raw.isBlank()) return DashboardTodoSummary(0, 0, 0, 0)
    return runCatching {
        val array = JSONArray(raw)
        var total = 0
        var pending = 0
        var done = 0
        var withGoal = 0
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            if (!todoMatchesScope(item, scope)) continue
            total += 1
            val isDone = item.optBoolean("done")
            if (isDone) done += 1 else pending += 1
            if (item.optInt("focusGoal", 0) > 0) withGoal += 1
        }
        DashboardTodoSummary(total, pending, done, withGoal)
    }.getOrDefault(DashboardTodoSummary(0, 0, 0, 0))
}

private fun loadFocusSummary(prefs: android.content.SharedPreferences): DashboardFocusSummary {
    val raw = prefs.getString("focus_records", "").orEmpty()
    if (raw.isBlank()) return DashboardFocusSummary(0, 0, "", 0)
    return runCatching {
        val array = JSONArray(raw)
        var seconds = 0
        val perTask = mutableMapOf<String, Int>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val taskTitle = item.optString("taskTitle")
            val taskSeconds = item.optInt("seconds", 0)
            seconds += taskSeconds
            perTask[taskTitle] = (perTask[taskTitle] ?: 0) + taskSeconds
        }
        val topTask = perTask.maxByOrNull { it.value }
        DashboardFocusSummary(
            sessions = array.length(),
            minutes = seconds / 60,
            topTask = topTask?.key.orEmpty(),
            topTaskMinutes = (topTask?.value ?: 0) / 60,
        )
    }.getOrDefault(DashboardFocusSummary(0, 0, "", 0))
}

private fun loadScopedFocusSummary(
    prefs: android.content.SharedPreferences,
    scope: DashboardScope,
): DashboardFocusSummary {
    if (scope == DashboardScope.All) return loadFocusSummary(prefs)
    val raw = prefs.getString("focus_records", "").orEmpty()
    if (raw.isBlank()) return DashboardFocusSummary(0, 0, "", 0)
    return runCatching {
        val array = JSONArray(raw)
        var seconds = 0
        var sessions = 0
        val perTask = mutableMapOf<String, Int>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val finishedAt = parseFocusFinishedDate(item.optString("finishedAt")) ?: continue
            val inScope = matchesDashboardScope(finishedAt, scope)
            if (!inScope) continue
            sessions += 1
            val taskTitle = item.optString("taskTitle")
            val taskSeconds = item.optInt("seconds", 0)
            seconds += taskSeconds
            perTask[taskTitle] = (perTask[taskTitle] ?: 0) + taskSeconds
        }
        val topTask = perTask.maxByOrNull { it.value }
        DashboardFocusSummary(
            sessions = sessions,
            minutes = seconds / 60,
            topTask = topTask?.key.orEmpty(),
            topTaskMinutes = (topTask?.value ?: 0) / 60,
        )
    }.getOrDefault(DashboardFocusSummary(0, 0, "", 0))
}

private fun parseFocusFinishedDate(value: String): LocalDate? {
    if (value.isBlank()) return null
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
    }.recoverCatching {
        val currentYear = LocalDate.now().year
        LocalDateTime.parse(
            "$currentYear-$value",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        ).toLocalDate()
    }.getOrNull()
}

private fun loadGradeSummary(prefs: android.content.SharedPreferences): DashboardGradeSummary {
    val raw = prefs.getString("grade_cache_v1", "").orEmpty()
    if (raw.isBlank()) return DashboardGradeSummary(0, 0, 0)
    return runCatching {
        val array = JSONArray(raw)
        var excellent = 0
        var warning = 0
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val description = item.optString("description")
            val source = item.optString("source")
            if (description.contains("优秀") || source.contains("优秀") || description.contains("90")) excellent += 1
            if (description.contains("预警") || description.contains("不及格") || source.contains("预警") || description.contains("59")) warning += 1
        }
        DashboardGradeSummary(array.length(), excellent, warning)
    }.getOrDefault(DashboardGradeSummary(0, 0, 0))
}

private fun loadScopedNoteSummary(
    noteStore: CourseNoteStore,
    scope: DashboardScope,
): DashboardNoteSummary {
    val notes = noteStore.loadNotes()
    val filtered = if (scope == DashboardScope.All) {
        notes
    } else {
        notes.filter { note -> noteMatchesScope(note.updatedAt, scope) }
    }
    return DashboardNoteSummary(
        total = filtered.size,
        courseCount = filtered.map { it.courseName }.distinct().size,
        latestTitle = filtered.maxByOrNull { it.updatedAt }?.title.orEmpty(),
    )
}

private fun todoMatchesScope(
    item: JSONObject,
    scope: DashboardScope,
): Boolean {
    if (scope == DashboardScope.All) return true
    val dueDate = parseDashboardTodoDueDate(item.optString("dueText")) ?: return false
    return matchesDashboardScope(dueDate, scope)
}

private fun parseDashboardTodoDueDate(value: String): LocalDate? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    val today = LocalDate.now()
    return when {
        trimmed.contains("今天") || trimmed.contains("今晚") -> today
        trimmed.contains("明天") || trimmed.contains("明晚") -> today.plusDays(1)
        else -> {
            val fullMatch = Regex("""(\d{4}-\d{2}-\d{2})""").find(trimmed)?.groupValues?.getOrNull(1)
            val shortMatch = Regex("""(\d{2}-\d{2})""").find(trimmed)?.groupValues?.getOrNull(1)
            when {
                fullMatch != null -> runCatching {
                    LocalDate.parse(fullMatch, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }.getOrNull()
                shortMatch != null -> runCatching {
                    LocalDate.parse("${today.year}-$shortMatch", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }.getOrNull()
                else -> null
            }
        }
    }
}

private fun noteMatchesScope(
    updatedAt: Long,
    scope: DashboardScope,
): Boolean {
    if (scope == DashboardScope.All || updatedAt <= 0L) return scope == DashboardScope.All
    val date = Instant.ofEpochMilli(updatedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    return matchesDashboardScope(date, scope)
}

private fun matchesDashboardScope(
    date: LocalDate,
    scope: DashboardScope,
): Boolean {
    val today = LocalDate.now()
    val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val weekEnd = weekStart.plusDays(6)
    return when (scope) {
        DashboardScope.Today -> date == today
        DashboardScope.ThisWeek -> !date.isBefore(weekStart) && !date.isAfter(weekEnd)
        DashboardScope.Recent7Days -> !date.isBefore(today.minusDays(6)) && !date.isAfter(today)
        DashboardScope.All -> true
    }
}

private fun loadReviewSummary(
    store: ReviewPlannerStore,
    scope: DashboardScope,
): DashboardReviewSummary {
    val items = store.loadItems()
    if (items.isEmpty()) return DashboardReviewSummary(0, 0, 0, 0, 0, "", 0)
    val zone = java.time.ZoneId.systemDefault()
    val now = System.currentTimeMillis()
    val scopedItems = if (scope == DashboardScope.All) {
        items
    } else {
        items.filter { item ->
            val date = Instant.ofEpochMilli(item.nextReviewAt).atZone(zone).toLocalDate()
            matchesDashboardScope(date, scope)
        }
    }
    val scopedCompleted = items.count { item ->
        if (item.lastReviewedAt <= 0L) return@count false
        val date = Instant.ofEpochMilli(item.lastReviewedAt).atZone(zone).toLocalDate()
        matchesDashboardScope(date, scope)
    }
    val pressureMap = scopedItems.groupBy { it.courseName }
    val topCourse = pressureMap.maxByOrNull { (_, courseItems) ->
        courseItems.size
    }
    return DashboardReviewSummary(
        total = scopedItems.size,
        todayDue = scopedItems.size,
        overdue = scopedItems.count { it.nextReviewAt <= now },
        completedRecent = scopedCompleted,
        mastered = scopedItems.count { it.mastery >= 0.9f },
        topCourse = topCourse?.key.orEmpty(),
        topCourseCount = topCourse?.value?.size ?: 0,
    )
}
