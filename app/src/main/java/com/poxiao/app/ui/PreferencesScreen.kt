package com.poxiao.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoThemePreset
import com.poxiao.app.ui.theme.PoxiaoThemeState

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
                        if (index != PoxiaoThemePreset.entries.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
            item {
                GlassCard {
                    Text("界面节奏", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("控制列表留白、卡片高度和工具栏密度。", style = MaterialTheme.typography.bodyMedium, color = palette.softText)
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
                    Text("从清透到晶润，控制卡片雾度、边缘发光和液态玻璃存在感。", style = MaterialTheme.typography.bodyMedium, color = palette.softText)
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
private fun LiquidGlassStylePreview(
    stylePreset: LiquidGlassStylePreset,
) {
    val palette = PoxiaoThemeState.palette
    val previewShape = RoundedCornerShape(26.dp)
    val backgroundBrush = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Brush.linearGradient(
            listOf(
                Color(0xFFF8FBFF),
                Color(0xFFF2F7FC),
                Color.White.copy(alpha = 0.94f),
                Color(0xFFEAF3FA),
            ),
        )
        LiquidGlassStylePreset.IOS -> Brush.linearGradient(
            listOf(
                Color(0xFF11161D),
                Color(0xFF22303F),
                Color(0xFF32475E),
            ),
        )
        LiquidGlassStylePreset.Hyper -> Brush.linearGradient(
            listOf(
                Color(0xFF0A1120),
                palette.primary.copy(alpha = 0.34f),
                palette.secondary.copy(alpha = 0.32f),
                Color(0xFF1D2E4A),
            ),
        )
    }
    val capsuleTint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.34f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.04f)
        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.24f)
    }
    val capsuleBorder = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.28f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.36f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.3f)
    }
    val chipTint = when (stylePreset) {
        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.24f)
        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.06f)
        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.24f)
    }

    Surface(
        shape = previewShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, capsuleBorder.copy(alpha = 0.7f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
                .padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            when (stylePreset) {
                LiquidGlassStylePreset.Harmony -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 8.dp, y = 6.dp)
                            .size(82.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.2f),
                                        Color(0xFFEAF3FA).copy(alpha = 0.16f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                }
                LiquidGlassStylePreset.IOS -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                            .width(116.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.48f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                }
                LiquidGlassStylePreset.Hyper -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .width(104.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color.Transparent,
                                        palette.secondary.copy(alpha = 0.26f),
                                        palette.primary.copy(alpha = 0.34f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = chipTint,
                    border = BorderStroke(1.dp, capsuleBorder.copy(alpha = 0.8f)),
                ) {
                    Box(modifier = Modifier.size(26.dp))
                }
                Surface(
                    shape = CircleShape,
                    color = chipTint.copy(alpha = chipTint.alpha * 0.72f),
                    border = BorderStroke(1.dp, capsuleBorder.copy(alpha = 0.55f)),
                ) {
                    Box(modifier = Modifier.size(18.dp))
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                    color = capsuleTint,
                    border = BorderStroke(1.dp, capsuleBorder),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when (stylePreset) {
                                        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.66f)
                                        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.94f)
                                        LiquidGlassStylePreset.Hyper -> palette.secondary.copy(alpha = 0.8f)
                                    },
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (stylePreset) {
                                        LiquidGlassStylePreset.Harmony -> Color.White.copy(alpha = 0.22f)
                                        LiquidGlassStylePreset.IOS -> Color.White.copy(alpha = 0.34f)
                                        LiquidGlassStylePreset.Hyper -> palette.primary.copy(alpha = 0.28f)
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePresetCard(
    preset: PoxiaoThemePreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val previewColors = when (preset) {
        PoxiaoThemePreset.Forest -> listOf(Color(0xFF2F7553), Color(0xFF8FBA75), Color(0xFFC8A95D))
        PoxiaoThemePreset.Aero -> listOf(Color(0xFF38A9D9), Color(0xFF7FD77E), Color(0xFFB8EEFF))
        PoxiaoThemePreset.Ink -> listOf(Color(0xFF3F6A60), Color(0xFF9F7A4C), Color(0xFFF7F3EB))
        PoxiaoThemePreset.Sunset -> listOf(Color(0xFFDD6B4D), Color(0xFFF0B257), Color(0xFFFFD8C2))
        PoxiaoThemePreset.Night -> listOf(Color(0xFF57D6E8), Color(0xFF9D85FF), Color(0xFF182B46))
    }
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) palette.card.copy(alpha = 0.92f) else palette.card.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, if (selected) palette.primary.copy(alpha = 0.34f) else palette.cardBorder.copy(alpha = 0.72f)),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                previewColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(preset.title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
                    if (selected) ActionPill("当前使用", palette.primary, onClick = {})
                }
                Text(preset.subtitle, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
            }
        }
    }
}
