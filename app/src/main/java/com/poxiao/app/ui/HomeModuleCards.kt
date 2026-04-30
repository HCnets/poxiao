package com.poxiao.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poxiao.app.review.ReviewItem
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist

@Composable
internal fun HomeMetricsModuleCard(
    modifier: Modifier,
    paired: Boolean,
    moduleSize: HomeModuleSize,
    todayClassCount: Int,
    pendingExamCount: Int,
    pendingTodoCount: Int,
    focusedMinutes: Int,
    onOpenScheduleDay: () -> Unit,
    onOpenScheduleExamWeek: () -> Unit,
    onOpenTodoPending: () -> Unit,
    onOpenPomodoro: () -> Unit,
) {
    if (paired) {
        GlassCard(modifier = modifier.clickable(onClick = onOpenScheduleDay)) {
            Text("核心指标", style = homeSectionTitleStyle(moduleSize), color = PineInk)
            Spacer(modifier = Modifier.height(homeSectionSpacing(moduleSize)))
            HomeLine("课表", "今日课次 $todayClassCount", "待办 $pendingTodoCount · 专注 $focusedMinutes 分钟", sizePreset = HomeModuleSize.Compact)
            Spacer(modifier = Modifier.height(homeLineGap(HomeModuleSize.Compact)))
            HomeLine("考试周", "待处理 $pendingExamCount", "点此查看当前冲刺项", sizePreset = HomeModuleSize.Compact)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(homeMetricSpacing(moduleSize)), modifier = modifier.horizontalScroll(rememberScrollState())) {
            MetricCard("今日课次", todayClassCount.toString(), ForestGreen, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenScheduleDay))
            MetricCard("考试周", pendingExamCount.toString(), TeaGreen, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenScheduleExamWeek))
            MetricCard("待完成", pendingTodoCount.toString(), Ginkgo, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenTodoPending))
            MetricCard("专注时长", "$focusedMinutes 分钟", MossGreen, sizePreset = moduleSize, modifier = Modifier.clickable(onClick = onOpenPomodoro))
        }
    }
}

@Composable
internal fun HomeRhythmModuleCard(
    modifier: Modifier,
    paired: Boolean,
    moduleSize: HomeModuleSize,
    collapsed: Boolean,
    todayTimeline: List<HomeLineData>,
    onToggleCollapsed: () -> Unit,
    onOpenScheduleDay: () -> Unit,
    onOpenScheduleExamWeek: () -> Unit,
    onOpenTodoPending: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
) {
    GlassCard(modifier = modifier) {
        HomeModuleHeader(
            title = "今天的节奏",
            collapsed = collapsed,
            collapsible = true,
            sizePreset = moduleSize,
            onToggleCollapsed = onToggleCollapsed,
        )
        Spacer(modifier = Modifier.height(homeSectionSpacing(moduleSize)))
        if (collapsed) {
            Text("已收起今日节奏，保留摘要入口。", style = homeSectionBodyStyle(moduleSize), color = ForestDeep.copy(alpha = 0.72f))
        } else if (todayTimeline.isEmpty()) {
            Text("课表、待办和专注记录会在这里自动汇总。", style = homeSectionBodyStyle(moduleSize), color = ForestDeep.copy(alpha = 0.72f))
        } else {
            val visibleTimeline = if (paired) todayTimeline.take(2) else todayTimeline
            visibleTimeline.forEachIndexed { index, item ->
                HomeLine(
                    time = item.timeLabel,
                    title = item.title,
                    body = item.subtitle,
                    sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                    modifier = Modifier.clickable {
                        when (item.timeLabel) {
                            "今日课程" -> onOpenScheduleDay()
                            "考试周" -> onOpenScheduleExamWeek()
                            "今日复习" -> onOpenReviewPlanner()
                            "待办优先" -> onOpenTodoPending()
                            "专注目标" -> onOpenTodoPending()
                            "专注绑定" -> onOpenPomodoro()
                            "专注趋势" -> onOpenPomodoro()
                        }
                    },
                )
                if (index != visibleTimeline.lastIndex) {
                    Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                }
            }
        }
    }
}

@Composable
internal fun HomeLearningModuleCard(
    modifier: Modifier,
    paired: Boolean,
    moduleSize: HomeModuleSize,
    collapsed: Boolean,
    pendingExamItems: List<ExamWeekItem>,
    topFocusTask: FocusTaskStat?,
    pendingReviewItems: List<ReviewItem>,
    urgentReviewItem: ReviewItem?,
    pendingGoalTodo: TodoTask?,
    onToggleCollapsed: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
    onOpenScheduleExamWeek: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onOpenTodoPending: () -> Unit,
    onBindReviewFocus: (ReviewItem) -> Unit,
    onBindGoalTodoFocus: (TodoTask) -> Unit,
) {
    if (pendingExamItems.isEmpty() && topFocusTask == null && pendingReviewItems.isEmpty()) return
    GlassCard(modifier = modifier) {
        val visibleExamItems = when {
            paired -> pendingExamItems.take(1)
            moduleSize == HomeModuleSize.Compact -> pendingExamItems.take(1)
            moduleSize == HomeModuleSize.Standard -> pendingExamItems.take(2)
            else -> pendingExamItems.take(3)
        }
        HomeModuleHeader(
            title = "学习推进",
            collapsed = collapsed,
            collapsible = true,
            sizePreset = moduleSize,
            onToggleCollapsed = onToggleCollapsed,
        )
        Spacer(modifier = Modifier.height(homeSectionSpacing(moduleSize)))
        if (collapsed) {
            val summary = buildString {
                if (pendingExamItems.isNotEmpty()) append("待处理 ${pendingExamItems.size} 项")
                urgentReviewItem?.let {
                    if (isNotBlank()) append(" · ")
                    append("复习 ${it.noteTitle}")
                }
                topFocusTask?.let {
                    if (isNotBlank()) append(" · ")
                    append("专注排行 ${it.title}")
                }
                pendingGoalTodo?.let {
                    if (isNotBlank()) append(" · ")
                    append("目标 ${it.title}")
                }
            }.ifBlank { "当前没有学习推进项。" }
            Text(summary, style = homeSectionBodyStyle(moduleSize), color = ForestDeep.copy(alpha = 0.72f))
        } else {
            if (pendingReviewItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenReviewPlanner),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.34f),
                    border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.3f)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("复习计划", style = MaterialTheme.typography.titleMedium, color = PineInk)
                            ActionPill("打开", ForestGreen, onClick = onOpenReviewPlanner)
                        }
                        Text(
                            "今天应复习 ${pendingReviewItems.size} 项，最紧急的是 ${urgentReviewItem?.noteTitle ?: "当前知识点"}。",
                            style = homeSectionBodyStyle(if (paired) HomeModuleSize.Compact else moduleSize),
                            color = ForestDeep.copy(alpha = 0.74f),
                        )
                        urgentReviewItem?.let { review ->
                            Text(
                                "${review.courseName} · 建议 ${review.recommendedMinutes} 分钟 · ${if (review.nextReviewAt <= System.currentTimeMillis()) "已进入遗忘风险" else "按计划推进"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ForestDeep.copy(alpha = 0.68f),
                            )
                        }
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            urgentReviewItem?.let { review ->
                                ActionPill(
                                    text = "绑定专注",
                                    background = MossGreen,
                                    onClick = { onBindReviewFocus(review) },
                                )
                            }
                            ActionPill(
                                text = "查看今日复习",
                                background = Ginkgo,
                                onClick = onOpenReviewPlanner,
                            )
                        }
                    }
                }
                if (visibleExamItems.isNotEmpty() || topFocusTask != null || pendingGoalTodo != null) {
                    Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                }
            }
            visibleExamItems.forEachIndexed { index, item ->
                HomeLine(
                    time = "考试周",
                    title = item.title,
                    body = "${item.countdownLabel} · ${item.detail}",
                    sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                    modifier = Modifier.clickable(onClick = onOpenScheduleExamWeek),
                )
                if (index != visibleExamItems.lastIndex || topFocusTask != null) {
                    Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                }
            }
            urgentReviewItem?.let {
                if (visibleExamItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                }
                HomeLine(
                    time = "今日复习",
                    title = it.noteTitle,
                    body = "${it.courseName} · 建议 ${it.recommendedMinutes} 分钟",
                    sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                    modifier = Modifier.clickable(onClick = onOpenReviewPlanner),
                )
            }
            topFocusTask?.let {
                if (visibleExamItems.isNotEmpty() || urgentReviewItem != null) {
                    Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                }
                HomeLine(
                    time = "专注排行",
                    title = it.title,
                    body = "${it.minutes} 分钟 · ${it.count} 轮",
                    sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                    modifier = Modifier.clickable(onClick = onOpenPomodoro),
                )
            }
            pendingGoalTodo?.let { task ->
                if (visibleExamItems.isNotEmpty() || topFocusTask != null) {
                    Spacer(modifier = Modifier.height(homeLineGap(if (paired) HomeModuleSize.Compact else moduleSize)))
                }
                HomeLine(
                    time = "专注目标",
                    title = task.title,
                    body = "还差 ${task.focusGoal - task.focusCount} 轮 · ${task.dueText}",
                    sizePreset = if (paired) HomeModuleSize.Compact else moduleSize,
                    modifier = Modifier.clickable(onClick = onOpenTodoPending),
                )
                Spacer(modifier = Modifier.height(homeSecondarySpacing(if (paired) HomeModuleSize.Compact else moduleSize)))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionPill("查看待办", WarmMist, onClick = onOpenTodoPending)
                    ActionPill("绑定专注", ForestGreen) { onBindGoalTodoFocus(task) }
                }
            }
        }
    }
}

@Composable
internal fun HomeQuickPointsModuleCard(
    modifier: Modifier,
    paired: Boolean,
    moduleSize: HomeModuleSize,
    favoritePoints: List<String>,
    onOpenMap: () -> Unit,
) {
    if (favoritePoints.isEmpty()) return
    val activeSize = if (paired) HomeModuleSize.Compact else moduleSize
    GlassCard(modifier = modifier.clickable(onClick = onOpenMap)) {
        Text("首页快捷点位", style = homeSectionTitleStyle(activeSize), color = PineInk)
        Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
        Text(
            favoritePoints.take(if (activeSize == HomeModuleSize.Hero) 5 else if (activeSize == HomeModuleSize.Standard) 4 else 3).joinToString(" · "),
            style = homeSectionBodyStyle(activeSize),
            color = ForestDeep.copy(alpha = 0.76f),
        )
        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
        Text(
            "已从校园地图同步点位，可继续查看导航。",
            style = if (activeSize == HomeModuleSize.Compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = ForestDeep.copy(alpha = 0.68f),
        )
    }
}

@Composable
internal fun HomeRecentPointsModuleCard(
    modifier: Modifier,
    paired: Boolean,
    moduleSize: HomeModuleSize,
    recentPoints: List<String>,
    onOpenMap: () -> Unit,
) {
    if (recentPoints.isEmpty()) return
    val activeSize = if (paired) HomeModuleSize.Compact else moduleSize
    GlassCard(modifier = modifier.clickable(onClick = onOpenMap)) {
        Text("最近访问", style = homeSectionTitleStyle(activeSize), color = PineInk)
        Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
        Text(
            recentPoints.take(if (activeSize == HomeModuleSize.Hero) 4 else 3).joinToString(" · "),
            style = homeSectionBodyStyle(activeSize),
            color = ForestDeep.copy(alpha = 0.76f),
        )
        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
        Text(
            "点此回到校园地图继续查看。",
            style = if (activeSize == HomeModuleSize.Compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = ForestDeep.copy(alpha = 0.68f),
        )
    }
}
