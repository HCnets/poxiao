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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poxiao.app.data.AssistantContextSummary
import com.poxiao.app.data.AssistantConversation
import com.poxiao.app.data.AssistantToolDefinition
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.CloudWhite
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import java.time.Instant
import java.time.LocalDateTime

@Composable
internal fun HomeAssistantModuleCard(
    modifier: Modifier,
    paired: Boolean,
    moduleSize: HomeModuleSize,
    collapsed: Boolean,
    conversations: List<AssistantConversation>,
    activeConversationId: String,
    activeConversation: AssistantConversation,
    assistantTools: List<AssistantToolDefinition>,
    assistantSummaries: List<AssistantContextSummary>,
    reviewExecutionSummary: ReviewBridgeExecutionSummary?,
    reviewExecutionHistory: List<ReviewBridgeExecutionSummary>,
    expandedReviewExecutionAt: Long?,
    prompt: String,
    assistantBusy: Boolean,
    onToggleCollapsed: () -> Unit,
    onSelectConversation: (String) -> Unit,
    onCreateConversation: () -> Unit,
    onOpenAssistantPermissions: () -> Unit,
    onPromptTool: (AssistantToolDefinition) -> Unit,
    onInjectSummary: (AssistantContextSummary) -> Unit,
    onToggleExecutionExpanded: (Long) -> Unit,
    onOpenReviewPlannerSeeded: (ReviewBridgeExecutionSummary) -> Unit,
    onOpenTodoPending: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onUndoExecution: (ReviewBridgeExecutionSummary) -> Unit,
    onReplayExecution: (ReviewBridgeExecutionSummary) -> Unit,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    GlassCard(modifier = modifier) {
        val activeSize = if (paired) HomeModuleSize.Compact else moduleSize
        val visibleMessages = activeConversation.messages.takeLast(
            when {
                paired -> 2
                moduleSize == HomeModuleSize.Compact -> 2
                moduleSize == HomeModuleSize.Standard -> 3
                else -> 4
            },
        )
        HomeModuleHeader(
            title = "智能体",
            collapsed = collapsed,
            collapsible = true,
            sizePreset = activeSize,
            onToggleCollapsed = onToggleCollapsed,
        )
        Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
        if (collapsed) {
            Text(
                "已收起智能体面板。当前保留最近 ${visibleMessages.size} 条对话摘要入口。",
                style = homeSectionBodyStyle(activeSize),
                color = ForestDeep.copy(alpha = 0.72f),
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                conversations.sortedByDescending { it.updatedAt }.take(6).forEach { conversation ->
                    SelectionChip(
                        text = conversation.title,
                        chosen = conversation.id == activeConversationId,
                        onClick = { onSelectConversation(conversation.id) },
                    )
                }
                ActionPill("新建会话", WarmMist, onClick = onCreateConversation)
                ActionPill("权限", Ginkgo, onClick = onOpenAssistantPermissions)
            }
            Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
            if (assistantTools.isNotEmpty()) {
                Text(
                    "可用工具",
                    style = if (activeSize == HomeModuleSize.Hero) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    color = PineInk,
                )
                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    assistantTools.take(6).forEach { tool ->
                        SelectionChip(
                            text = tool.title,
                            chosen = false,
                            onClick = { onPromptTool(tool) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
            }
            if (assistantSummaries.isNotEmpty()) {
                Text(
                    "可用上下文",
                    style = if (activeSize == HomeModuleSize.Hero) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    color = PineInk,
                )
                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                assistantSummaries.take(if (activeSize == HomeModuleSize.Hero) 4 else 3).forEachIndexed { index, summary ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.42f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.22f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInjectSummary(summary) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("${summary.source} · ${summary.title}", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                            Text(summary.body, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                        }
                    }
                    if (index != minOf(assistantSummaries.size, if (activeSize == HomeModuleSize.Hero) 4 else 3) - 1) {
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                    }
                }
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
            }
            reviewExecutionSummary?.let { execution ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.38f),
                    border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.18f)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("复习计划执行结果", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                        Text(execution.summary, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                        Text(
                            "执行于 ${formatSyncTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(execution.executedAt), java.time.ZoneId.systemDefault()))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestDeep.copy(alpha = 0.68f),
                        )
                        if (execution.createdTaskTitles.isNotEmpty() || execution.boundTaskTitle.isNotBlank()) {
                            ActionPill(
                                if (expandedReviewExecutionAt == execution.executedAt) "收起明细" else "展开明细",
                                TeaGreen.copy(alpha = 0.28f),
                            ) {
                                onToggleExecutionExpanded(execution.executedAt)
                            }
                        }
                        if (expandedReviewExecutionAt == execution.executedAt) {
                            ReviewExecutionDetailBlock(execution = execution)
                        }
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ActionPill("回到复习页", Ginkgo.copy(alpha = 0.82f)) {
                                onOpenReviewPlannerSeeded(execution)
                            }
                            ActionPill("查看待办", Ginkgo, onClick = onOpenTodoPending)
                            ActionPill("查看番茄钟", WarmMist, onClick = onOpenPomodoro)
                            ActionPill("撤销执行", CloudWhite) {
                                onUndoExecution(execution)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
            }
            if (reviewExecutionHistory.isNotEmpty()) {
                Text("最近接管历史", style = MaterialTheme.typography.titleMedium, color = PineInk)
                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                reviewExecutionHistory.take(3).forEachIndexed { index, execution ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.16f)),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                "执行于 ${formatSyncTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(execution.executedAt), java.time.ZoneId.systemDefault()))}",
                                style = MaterialTheme.typography.labelLarge,
                                color = ForestGreen,
                            )
                            Text(execution.summary, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ActionPill(
                                    if (expandedReviewExecutionAt == execution.executedAt) "收起明细" else "展开明细",
                                    TeaGreen.copy(alpha = 0.24f),
                                ) {
                                    onToggleExecutionExpanded(execution.executedAt)
                                }
                                ActionPill("回放方案", WarmMist) {
                                    onReplayExecution(execution)
                                }
                                ActionPill("回到复习页", Ginkgo.copy(alpha = 0.82f)) {
                                    onOpenReviewPlannerSeeded(execution)
                                }
                            }
                            if (expandedReviewExecutionAt == execution.executedAt) {
                                ReviewExecutionDetailBlock(execution = execution)
                            }
                        }
                    }
                    if (index != minOf(reviewExecutionHistory.size, 3) - 1) {
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                    }
                }
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
            }
            visibleMessages.forEachIndexed { index, message ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (message.role) {
                        "assistant" -> Color.White.copy(alpha = 0.58f)
                        "system" -> TeaGreen.copy(alpha = 0.24f)
                        else -> Color(0x2A2F7553)
                    },
                ) {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(14.dp),
                        style = if (activeSize == HomeModuleSize.Hero) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                        color = PineInk,
                    )
                }
                if (index != visibleMessages.lastIndex) Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
            }
            if (activeConversation.toolCalls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
                Text("最近工具调用", style = MaterialTheme.typography.titleMedium, color = PineInk)
                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                activeConversation.toolCalls.takeLast(3).reversed().forEachIndexed { index, toolCall ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.36f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.18f)),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("${toolCall.title} · ${toolCall.status}", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                            Text(toolCall.summary, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.72f))
                        }
                    }
                    if (index != minOf(activeConversation.toolCalls.size, 3) - 1) {
                        Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                    }
                }
            }
            Spacer(modifier = Modifier.height(homeSectionSpacing(activeSize) - 2.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                label = { Text("给智能体一句话") },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            if (assistantBusy) {
                Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize)))
                Text("智能体正在整理回复...", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.68f))
            }
            Spacer(modifier = Modifier.height(homeSecondarySpacing(activeSize) + 2.dp))
            Button(
                onClick = onSend,
                enabled = prompt.isNotBlank() && !assistantBusy,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("发送")
            }
        }
    }
}
