package com.poxiao.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.poxiao.app.notes.CourseNoteSeed
import com.poxiao.app.ui.theme.BambooGlass
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.CloudWhite
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist

@Composable
internal fun HomeWelcomeCard(
    todayLabel: String,
    homeEditMode: Boolean,
    nextCourseSubtitle: String,
    priorityTodoTitle: String,
    urgentReviewTitle: String,
    pomodoroSubtitle: String,
    onToggleEditMode: () -> Unit,
    onOpenScheduleDay: () -> Unit,
    onOpenTodoPending: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
    onOpenPomodoro: () -> Unit,
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("破晓", style = MaterialTheme.typography.headlineLarge, color = PineInk)
                Text(
                    "$todayLabel · 校园学习工作台",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForestDeep.copy(alpha = 0.68f),
                )
            }
            ActionPill(
                text = if (homeEditMode) "完成编辑" else "编辑工作台",
                background = if (homeEditMode) ForestGreen else WarmMist,
                onClick = onToggleEditMode,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (homeEditMode) {
            Text(
                "当前处于编辑模式，可调整模块显隐、顺序和尺寸。",
                style = MaterialTheme.typography.bodyMedium,
                color = ForestDeep.copy(alpha = 0.68f),
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HomeQuickEntry(
                title = "今日课表",
                subtitle = nextCourseSubtitle,
                accent = ForestGreen,
                icon = PrimarySection.Schedule.icon,
                onClick = onOpenScheduleDay,
            )
            HomeQuickEntry(
                title = "待办清单",
                subtitle = priorityTodoTitle,
                accent = Ginkgo,
                icon = PrimarySection.Todo.icon,
                onClick = onOpenTodoPending,
            )
            HomeQuickEntry(
                title = "复习计划",
                subtitle = urgentReviewTitle,
                accent = TeaGreen,
                icon = Icons.Rounded.AutoAwesome,
                onClick = onOpenReviewPlanner,
            )
            HomeQuickEntry(
                title = "番茄钟",
                subtitle = pomodoroSubtitle,
                accent = MossGreen,
                icon = PrimarySection.Pomodoro.icon,
                onClick = onOpenPomodoro,
            )
        }
    }
}

@Composable
internal fun HomeHeroOverviewCard(
    heroState: HomeHeroState,
    homeEditMode: Boolean,
    todayClassCount: Int,
    pendingTodoCount: Int,
    focusedMinutes: Int,
    pendingReviewCount: Int,
    todayTimeline: List<HomeLineData>,
    onOpenScheduleDay: () -> Unit,
    onOpenScheduleExamWeek: () -> Unit,
    onOpenTodoPending: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onOpenReviewPlanner: () -> Unit,
) {
    GlassCard {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.28f),
            border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.34f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                heroState.accent.copy(alpha = 0.14f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent,
                            ),
                        ),
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionPill(
                        text = heroState.badge,
                        background = heroState.accent,
                        onClick = {
                            when (heroState.badge) {
                                "今日复习" -> onOpenReviewPlanner()
                                "高优先待办" -> onOpenTodoPending()
                                "下一门课" -> onOpenScheduleDay()
                                "专注目标" -> onOpenPomodoro()
                            }
                        },
                    )
                    Text(
                        if (homeEditMode) "首页编排中" else "今日总览",
                        style = MaterialTheme.typography.labelLarge,
                        color = ForestDeep.copy(alpha = 0.64f),
                    )
                }
                Text(
                    heroState.headline,
                    style = MaterialTheme.typography.headlineMedium,
                    color = PineInk,
                )
                Text(
                    heroState.detail,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ForestDeep.copy(alpha = 0.78f),
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        HomeHeroStat("今日课表", "${todayClassCount} 课次", ForestGreen, modifier = Modifier.weight(1f))
                        HomeHeroStat("待办待推", "${pendingTodoCount} 项", Ginkgo, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        HomeHeroStat("专注累计", "${focusedMinutes} 分钟", MossGreen, modifier = Modifier.weight(1f))
                        HomeHeroStat("复习待办", "${pendingReviewCount} 项", TeaGreen, modifier = Modifier.weight(1f))
                    }
                }
                if (todayTimeline.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White.copy(alpha = 0.24f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.24f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("接下来先做什么", style = MaterialTheme.typography.titleMedium, color = PineInk)
                                    Text("把今天最值得先处理的事项压到前三条。", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.64f))
                                }
                                Text(
                                    "${todayTimeline.size} 条",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = heroState.accent,
                                )
                            }
                            todayTimeline.take(3).forEachIndexed { index, item ->
                                HomeLine(
                                    time = item.timeLabel,
                                    title = item.title,
                                    body = item.subtitle,
                                    sizePreset = HomeModuleSize.Standard,
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
                                if (index != minOf(todayTimeline.size, 3) - 1) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HomeSearchPanel(
    searchQuery: String,
    heroAccent: Color,
    searchHistory: List<String>,
    quickKeywords: List<String>,
    localSearchResults: List<HomeSearchResult>,
    gradeSearchResults: List<HomeSearchResult>,
    gradeSearchLoading: Boolean,
    gradeSearchStatus: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onSelectKeyword: (String) -> Unit,
    onResultClick: (HomeSearchResult) -> Unit,
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("快速检索", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Text(
                    "课程、待办、成绩和教学楼统一搜索，不用再来回翻页面。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForestDeep.copy(alpha = 0.68f),
                )
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = heroAccent.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, heroAccent.copy(alpha = 0.24f)),
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "就绪" else "${(localSearchResults.size + gradeSearchResults.size).coerceAtMost(8)} 条",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = heroAccent,
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("搜索课程、待办、成绩、教学楼") },
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (searchQuery.isBlank()) {
            if (searchHistory.isNotEmpty()) {
                Text("最近搜索", style = MaterialTheme.typography.titleMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    searchHistory.take(8).forEach { history ->
                        ActionPill(history, WarmMist) { onSelectHistory(history) }
                    }
                    ActionPill("清空", CloudWhite, onClick = onClearHistory)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            Text("快捷词", style = MaterialTheme.typography.titleMedium, color = PineInk)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                quickKeywords.forEach { keyword ->
                    ActionPill(keyword, BambooGlass) { onSelectKeyword(keyword) }
                }
            }
        }
        if (searchQuery.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            val mergedResults = localSearchResults + gradeSearchResults
            if (mergedResults.isEmpty()) {
                Text(
                    text = if (gradeSearchLoading) "正在整理搜索结果..." else gradeSearchStatus.ifBlank { "当前没有匹配结果。" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForestDeep.copy(alpha = 0.72f),
                )
            } else {
                mergedResults.take(8).forEachIndexed { index, result ->
                    SearchResultRow(
                        result = result,
                        onClick = { onResultClick(result) },
                    )
                    if (index != minOf(mergedResults.size, 8) - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (gradeSearchStatus.isNotBlank() && gradeSearchResults.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(gradeSearchStatus, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.68f))
                }
            }
        }
    }
}

@Composable
internal fun HomeWorkbenchHeader(
    homeEditMode: Boolean,
    visibleModulesCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("工作台模块", style = MaterialTheme.typography.titleLarge, color = PineInk)
            Text(
                if (homeEditMode) {
                    "当前可直接调整模块显隐、顺序和尺寸。"
                } else {
                    "下面开始是首页的可编排模块，按你的学习节奏自由组合。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = ForestDeep.copy(alpha = 0.68f),
            )
        }
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.28f),
            border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.24f)),
        ) {
            Text(
                text = "$visibleModulesCount 个模块",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = PineInk,
            )
        }
    }
}
