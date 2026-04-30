package com.poxiao.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import kotlin.math.roundToInt

@Composable
internal fun HomeWorkbenchEditorCard(
    visibleModules: List<HomeModule>,
    moduleSizes: Map<HomeModule, HomeModuleSize>,
    draggingModule: HomeModule?,
    dragOffsetY: Float,
    dragSwapThreshold: Float,
    onToggleModuleVisibility: (HomeModule) -> Unit,
    onDragStart: (HomeModule) -> Unit,
    onDragCancel: () -> Unit,
    onDragEnd: () -> Unit,
    onDragMove: (HomeModule, Float) -> Unit,
    onSelectModuleSize: (HomeModule, HomeModuleSize) -> Unit,
) {
    GlassCard {
        Text("工作台编排", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Text("选择首页要保留哪些模块，按自己的使用节奏裁剪工作台。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            HomeModule.entries.forEach { module ->
                SelectionChip(
                    text = module.title,
                    chosen = module in visibleModules,
                    onClick = { onToggleModuleVisibility(module) },
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("长按下面的模块卡可直接拖动排序。", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.62f))
        Spacer(modifier = Modifier.height(12.dp))
        visibleModules.forEachIndexed { index, module ->
            key(module) {
                val isDragging = draggingModule == module
                val moduleSize = moduleSizes[module] ?: defaultHomeModuleSize(module)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDragging) TeaGreen.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.24f),
                    border = BorderStroke(1.dp, if (isDragging) ForestGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.18f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(0, if (isDragging) dragOffsetY.roundToInt() else 0) }
                        .pointerInput(module, visibleModules.size, dragSwapThreshold) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart(module) },
                                onDragCancel = { onDragCancel() },
                                onDragEnd = { onDragEnd() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDragMove(module, dragAmount.y)
                                },
                            )
                        },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(module.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                            Text(if (isDragging) "拖动中" else "长按后上下拖动", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.62f))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                HomeModuleSize.entries.forEach { sizePreset ->
                                    SelectionChip(
                                        text = sizePreset.title,
                                        chosen = moduleSize == sizePreset,
                                        onClick = { onSelectModuleSize(module, sizePreset) },
                                    )
                                }
                            }
                        }
                        Text(
                            "≡",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ForestDeep.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp, start = 12.dp),
                        )
                    }
                }
                if (index != visibleModules.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun HomeModuleRowsSection(
    moduleRows: List<List<HomeModule>>,
    renderHomeModule: @Composable (HomeModule, Modifier, Boolean) -> Unit,
) {
    moduleRows.forEach { rowModules ->
        if (rowModules.size == 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                rowModules.forEach { module ->
                    renderHomeModule(module, Modifier.weight(1f), true)
                }
            }
        } else {
            renderHomeModule(rowModules.first(), Modifier.fillMaxWidth(), false)
        }
    }
}
