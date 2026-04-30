package com.poxiao.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.poxiao.app.data.AssistantPermissionStore
import com.poxiao.app.data.AssistantToolKit
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.PoxiaoThemePreset
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.theme.WarmMist

@Composable
internal fun PreferencesScreen(
    modifier: Modifier = Modifier,
    currentPreset: PoxiaoThemePreset,
    currentDensity: UiDensityPreset,
    currentGlassStrength: GlassStrengthPreset,
    currentGlassStyle: LiquidGlassStylePreset,
    onSelectPreset: (PoxiaoThemePreset) -> Unit,
    onSelectDensity: (UiDensityPreset) -> Unit,
    onSelectGlassStrength: (GlassStrengthPreset) -> Unit,
    onSelectGlassStyle: (LiquidGlassStylePreset) -> Unit,
    onBack: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Box(modifier = modifier) {
        ScreenColumn {
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("界面偏好", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
                        ActionPill("返回", palette.secondary, onClick = onBack)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "从自然书卷、Y2K 水润玻璃到夜航霓光，选择更适合自己的使用氛围。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.softText,
                    )
                }
            }
            item {
                GlassCard {
                    Text("主题风格", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                    Spacer(modifier = Modifier.height(12.dp))
                    PoxiaoThemePreset.entries.forEachIndexed { index, preset ->
                        ThemePresetCard(
                            preset = preset,
                            selected = preset == currentPreset,
                            onClick = { onSelectPreset(preset) },
                        )
                        if (index != PoxiaoThemePreset.entries.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            item {
                GlassCard {
                    Text("界面节奏", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "控制列表留白、卡片高度和工具栏密度。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.softText,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SelectionRow(
                        options = UiDensityPreset.entries.toList(),
                        selected = currentDensity,
                        label = { it.title },
                        onSelect = onSelectDensity,
                    )
                }
            }
            item {
                GlassCard {
                    Text("液态玻璃版本", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "可分别切换为 HarmonyOS 6、iOS 26、HyperOS 风格，主要影响折射、高光和边缘质感。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.softText,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SelectionRow(
                        options = LiquidGlassStylePreset.entries.toList(),
                        selected = currentGlassStyle,
                        label = { it.title },
                        onSelect = onSelectGlassStyle,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(currentGlassStyle.subtitle, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                    Spacer(modifier = Modifier.height(12.dp))
                    LiquidGlassStylePreview(stylePreset = currentGlassStyle)
                }
            }
            item {
                GlassCard {
                    Text("玻璃强度", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "从清透到晶润，控制卡片雾度、边缘发光和液态玻璃存在感。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.softText,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SelectionRow(
                        options = GlassStrengthPreset.entries.toList(),
                        selected = currentGlassStrength,
                        label = { it.title },
                        onSelect = onSelectGlassStrength,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AssistantPermissionScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val store = remember { AssistantPermissionStore(context) }
    val toolkit = remember { AssistantToolKit() }
    var permissionState by remember { mutableStateOf(store.load()) }
    val tools = remember(permissionState) { toolkit.availableTools(permissionState) }
    val palette = PoxiaoThemeState.palette
    Box(modifier = modifier) {
        ScreenColumn {
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 72.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("智能体权限", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
                            Text(
                                "控制智能体可读取哪些数据，以及可触发哪些本地 mock 工具。",
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.softText,
                            )
                        }
                        ActionPill("返回", WarmMist, onClick = onBack)
                    }
                }
            }
            item {
                GlassCard {
                    Text("数据读取", style = MaterialTheme.typography.titleLarge, color = PineInk)
                    Spacer(modifier = Modifier.height(12.dp))
                    ToggleLine("允许读取课表与考试周", permissionState.readSchedule) {
                        permissionState = permissionState.copy(readSchedule = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许读取待办与目标", permissionState.readTodo) {
                        permissionState = permissionState.copy(readTodo = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许读取专注记录", permissionState.readFocus) {
                        permissionState = permissionState.copy(readFocus = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许读取成绩缓存", permissionState.readGrades) {
                        permissionState = permissionState.copy(readGrades = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许读取地图点位", permissionState.readMap) {
                        permissionState = permissionState.copy(readMap = it)
                        store.save(permissionState)
                    }
                }
            }
            item {
                GlassCard {
                    Text("动作能力", style = MaterialTheme.typography.titleLarge, color = PineInk)
                    Spacer(modifier = Modifier.height(12.dp))
                    ToggleLine("允许生成待办建议", permissionState.createTodo) {
                        permissionState = permissionState.copy(createTodo = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许生成专注绑定建议", permissionState.bindPomodoro) {
                        permissionState = permissionState.copy(bindPomodoro = it)
                        store.save(permissionState)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleLine("允许生成地图跳转建议", permissionState.openCampusMap) {
                        permissionState = permissionState.copy(openCampusMap = it)
                        store.save(permissionState)
                    }
                }
            }
            item {
                GlassCard {
                    Text("当前工具清单", style = MaterialTheme.typography.titleLarge, color = PineInk)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (tools.isEmpty()) {
                        Text(
                            "当前所有工具都已关闭，智能体只会保留纯文本对话。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ForestDeep.copy(alpha = 0.72f),
                        )
                    } else {
                        tools.forEachIndexed { index, tool ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.34f),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(tool.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                                    Text(
                                        tool.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ForestDeep.copy(alpha = 0.72f),
                                    )
                                }
                            }
                            if (index != tools.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
