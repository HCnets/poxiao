package com.poxiao.app.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.poxiao.app.ui.theme.PoxiaoThemeState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

/**
 * 陀螺仪偏移量数据类
 */
data class GyroOffset(
    val x: Float = 0f,
    val y: Float = 0f
)

/**
 * 全局陀螺仪 CompositionLocal
 */
val LocalGyroOffset = staticCompositionLocalOf { GyroOffset() }

@Composable
fun GyroScopeProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val gyroOffset = remember { Animatable(0f) } // 用于平滑处理
    val xOffset = remember { Animatable(0f) }
    val yOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    // 用于低通滤波的平滑状态
    var smoothedRoll by remember { mutableStateOf(0f) }
    var smoothedPitch by remember { mutableStateOf(0f) }
    
    // 初始校准参考值
    var baseRoll by remember { mutableStateOf<Float?>(null) }
    var basePitch by remember { mutableStateOf<Float?>(null) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                
                // GRAVITY 传感器返回的是各轴的重力加速度 (m/s^2)
                val pitch = event.values[1] // Y轴，上下倾斜
                val roll = event.values[0]  // X轴，左右倾斜 (纠正: 之前误用 values[2] 且未考虑设备平放姿态)

                // 1. 初始化基准面 (首次拿到数据时设定)
                if (baseRoll == null || basePitch == null) {
                    baseRoll = roll
                    basePitch = pitch
                }

                // 2. 计算相对偏移量
                val relativeRoll = roll - (baseRoll ?: 0f)
                val relativePitch = pitch - (basePitch ?: 0f)

                // 3. 低通滤波平滑处理 (去除微小抖动)
                val alpha = 0.15f // 滤波系数，越小越平滑但延迟越大
                smoothedRoll = smoothedRoll + alpha * (relativeRoll - smoothedRoll)
                smoothedPitch = smoothedPitch + alpha * (relativePitch - smoothedPitch)
                
                scope.launch {
                    // 4. 将加速度转换为合理的范围映射 (-1 到 1)，增加阻尼感
                    val targetX = (smoothedRoll / 6f).coerceIn(-1f, 1f)
                    val targetY = (smoothedPitch / 6f).coerceIn(-1f, 1f)
                    
                    xOffset.animateTo(
                        targetValue = targetX,
                        animationSpec = tween(durationMillis = 60, easing = LinearEasing)
                    )
                    yOffset.animateTo(
                        targetValue = targetY,
                        animationSpec = tween(durationMillis = 60, easing = LinearEasing)
                    )
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorManager.registerListener(listener, gravitySensor, SensorManager.SENSOR_DELAY_UI)
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    CompositionLocalProvider(
        LocalGyroOffset provides GyroOffset(xOffset.value, yOffset.value)
    ) {
        content()
    }
}

enum class LiquidGlassStylePreset(
    val title: String,
    val subtitle: String,
    val blurScale: Float,
    val refractionScale: Float,
    val tintScale: Float,
    val glowScale: Float,
    val edgeScale: Float,
    val whiteSheenScale: Float,
    val chromaticAberration: Boolean,
) {
    Harmony(
        title = "星辉雾晶 (Aura)",
        subtitle = "柔雾、温润、散射宽，如星光平铺的通透柔雾面。",
        blurScale = 1.56f,
        refractionScale = 0.54f,
        tintScale = 1.34f,
        glowScale = 0.46f,
        edgeScale = 0.62f,
        whiteSheenScale = 0.76f,
        chromaticAberration = false,
    ),
    IOS(
        title = "冰川透镜 (Glacier)",
        subtitle = "冷白纯净，顶部高光和边缘切光最强，质感如冰晶般硬朗。",
        blurScale = 0.82f,
        refractionScale = 2.36f,
        tintScale = 0f,
        glowScale = 0.06f,
        edgeScale = 1.66f,
        whiteSheenScale = 1.08f,
        chromaticAberration = true,
    ),
    Hyper(
        title = "流光霓虹 (Prism)",
        subtitle = "彩色边光、斜向流体光带，折射出深邃的霓虹光效，科技感最强。",
        blurScale = 0.72f,
        refractionScale = 2.12f,
        tintScale = 1.16f,
        glowScale = 2.46f,
        edgeScale = 1.26f,
        whiteSheenScale = 1.08f,
        chromaticAberration = true,
    ),
}

val LocalLiquidGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }
val LocalLiquidGlassStylePreset = staticCompositionLocalOf { LiquidGlassStylePreset.IOS }
val LocalCustomBlur = staticCompositionLocalOf { 1f }
val LocalCustomGlow = staticCompositionLocalOf { 1f }
val LocalCustomAlpha = staticCompositionLocalOf { 1f }

class SmoothSuperellipseShape(
    private val exponent: Float = 4.6f,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val halfWidth = size.width / 2f
        val halfHeight = size.height / 2f
        val power = (2f / exponent).toDouble()
        val path = Path()
        val steps = 96
        repeat(steps + 1) { index ->
            val theta = (index.toDouble() / steps.toDouble()) * (PI * 2.0)
            val cosTheta = cos(theta)
            val sinTheta = sin(theta)
            val x = halfWidth + (halfWidth * sign(cosTheta) * abs(cosTheta).pow(power)).toFloat()
            val y = halfHeight + (halfHeight * sign(sinTheta) * abs(sinTheta).pow(power)).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun LiquidGlassScene(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val backdrop = rememberLayerBackdrop()
    Box(modifier = modifier) {
        LiquidGlassBackdropLayer(
            backdrop = backdrop,
            modifier = Modifier.fillMaxSize(),
        )
        CompositionLocalProvider(LocalLiquidGlassBackdrop provides backdrop) {
            content()
        }
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    tint: Color = PoxiaoThemeState.palette.card.copy(alpha = 0.4f),
    borderColor: Color = PoxiaoThemeState.palette.cardBorder.copy(alpha = 0.82f),
    glowColor: Color = PoxiaoThemeState.palette.cardGlow.copy(alpha = 0.3f),
    blurRadius: Dp = 18.dp,
    refractionHeight: Dp = 16.dp,
    refractionAmount: Dp = 26.dp,
    highlightAlpha: Float = 1f,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassSurface(
        modifier = modifier,
        cornerRadius = cornerRadius,
        contentPadding = contentPadding,
        tint = tint,
        borderColor = borderColor,
        glowColor = glowColor,
        blurRadius = blurRadius,
        refractionHeight = refractionHeight,
        refractionAmount = refractionAmount,
        highlightAlpha = highlightAlpha,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30.dp,
    shapeOverride: Shape? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    tint: Color = PoxiaoThemeState.palette.card.copy(alpha = 0.4f),
    borderColor: Color = PoxiaoThemeState.palette.cardBorder.copy(alpha = 0.82f),
    glowColor: Color = PoxiaoThemeState.palette.cardGlow.copy(alpha = 0.3f),
    shadowColor: Color = Color.Black.copy(alpha = 0.18f),
    blurRadius: Dp = 18.dp,
    refractionHeight: Dp = 16.dp,
    refractionAmount: Dp = 26.dp,
    highlightAlpha: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val defaultShape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val shape = shapeOverride ?: defaultShape
    val backdrop = LocalLiquidGlassBackdrop.current
    val stylePreset = LocalLiquidGlassStylePreset.current
    val glassStrength = LocalGlassStrengthPreset.current
    val customBlurVal = LocalCustomBlur.current
    val customGlowVal = LocalCustomGlow.current
    val customAlphaVal = LocalCustomAlpha.current
    
    val styledTint = tint.copy(alpha = (tint.alpha * stylePreset.tintScale * glassStrength.cardAlpha * customAlphaVal * 1.15f).coerceIn(0f, 1f)) // 提升基础色浓度
    val styledGlow = glowColor.copy(alpha = (glowColor.alpha * stylePreset.glowScale * glassStrength.glowScale * customGlowVal).coerceIn(0f, 1f))
    val styledBorder = borderColor.copy(alpha = (borderColor.alpha * stylePreset.edgeScale * 1.4f).coerceIn(0f, 1f)) // 大幅增强边缘对比度

    val gyroOffset = LocalGyroOffset.current
    val parallaxModifier = modifier.graphicsLayer {
        translationX = gyroOffset.x * 12.dp.toPx() // 卡片位移
        translationY = gyroOffset.y * 12.dp.toPx()
    }

    val modifierWithEffect = if (backdrop != null) {
        parallaxModifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                // 动态雾度：透明度越低，雾度越高以维持文字可视度
                val baseBlur = blurRadius.toPx() * stylePreset.blurScale * customBlurVal
                val strengthFactor = (1.2f - glassStrength.cardAlpha * customAlphaVal).coerceIn(0.5f, 2.0f)
                blur(baseBlur * strengthFactor) 
                
                lens(
                    refractionHeight = refractionHeight.toPx(),
                    refractionAmount = refractionAmount.toPx() * stylePreset.refractionScale,
                    depthEffect = true,
                    chromaticAberration = stylePreset.chromaticAberration,
                )
            },
            highlight = {
                Highlight.Ambient.copy(
                    width = (2.2f * stylePreset.edgeScale).dp,
                    blurRadius = (26f * stylePreset.edgeScale).dp,
                    alpha = (highlightAlpha * 1.35f).coerceAtMost(1f),
                )
            },
            onDrawSurface = {
                when (stylePreset) {
                    LiquidGlassStylePreset.Harmony -> {
                        this.drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.22f),
                                    styledTint.copy(alpha = (styledTint.alpha * 0.58f).coerceAtMost(1f)),
                                    Color(0xFFF5FAFF).copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.05f),
                                ),
                            ),
                        )
                        this.drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.18f),
                                    Color(0xFFF4FAFF).copy(alpha = 0.1f),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.3f),
                                radius = size.maxDimension * 0.88f,
                            ),
                            blendMode = BlendMode.Screen,
                        )
                    }

                    LiquidGlassStylePreset.IOS -> {
                        this.drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.012f),
                                    Color.Transparent,
                                    Color.Transparent,
                                ),
                            ),
                            blendMode = BlendMode.Screen,
                        )
                        if (styledTint.alpha > 0f) {
                            this.drawRect(
                                styledTint.copy(alpha = (styledTint.alpha * 0.12f).coerceAtMost(0.008f)),
                                blendMode = BlendMode.Hue,
                            )
                            this.drawRect(
                                styledTint.copy(alpha = (styledTint.alpha * 0.18f).coerceAtMost(0.005f)),
                            )
                        }
                    }

                    LiquidGlassStylePreset.Hyper -> {
                        this.drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    styledTint.copy(alpha = (styledTint.alpha * 0.88f).coerceAtMost(1f)),
                                    palette.primary.copy(alpha = 0.2f),
                                    palette.secondary.copy(alpha = 0.28f),
                                    styledGlow.copy(alpha = (styledGlow.alpha * 0.92f).coerceAtMost(1f)),
                                ),
                                start = Offset(size.width * 0.08f, size.height * 0.08f),
                                end = Offset(size.width * 0.92f, size.height),
                            ),
                        )
                        this.drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    palette.secondary.copy(alpha = 0.18f),
                                    palette.primary.copy(alpha = 0.14f),
                                    Color.Transparent,
                                ),
                                start = Offset(size.width * 0.14f, size.height * 0.12f),
                                end = Offset(size.width * 0.88f, size.height * 0.46f),
                            ),
                            blendMode = BlendMode.Screen,
                        )
                    }
                }
            },
            onDrawFront = {
                this.drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(
                                alpha = when (stylePreset) {
                                    LiquidGlassStylePreset.IOS -> 0.015f
                                    else -> (0.22f * stylePreset.whiteSheenScale).coerceAtMost(0.4f)
                                },
                            ),
                            Color.White.copy(
                                alpha = when (stylePreset) {
                                    LiquidGlassStylePreset.IOS -> 0.004f
                                    else -> (0.1f * stylePreset.whiteSheenScale).coerceAtMost(0.2f)
                                },
                            ),
                            Color.Transparent,
                            styledGlow.copy(
                                alpha = when (stylePreset) {
                                    LiquidGlassStylePreset.IOS -> (styledGlow.alpha * 0.02f).coerceAtMost(1f)
                                    else -> (styledGlow.alpha * 0.4f).coerceAtMost(1f)
                                },
                            ),
                        ),
                    ),
                    blendMode = BlendMode.Screen,
                )
                this.drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(
                                alpha = when (stylePreset) {
                                    LiquidGlassStylePreset.IOS -> 0.07f
                                    else -> (0.28f * stylePreset.whiteSheenScale).coerceAtMost(0.44f)
                                },
                            ),
                            Color.White.copy(
                                alpha = when (stylePreset) {
                                    LiquidGlassStylePreset.IOS -> 0.014f
                                    else -> (0.08f * stylePreset.whiteSheenScale).coerceAtMost(0.16f)
                                },
                            ),
                            Color.Transparent,
                        ),
                        start = Offset(size.width * 0.06f, size.height * 0.02f),
                        end = Offset(size.width * 0.62f, size.height * 0.42f),
                    ),
                    blendMode = BlendMode.Screen,
                )
                when (stylePreset) {
                    LiquidGlassStylePreset.Harmony -> {
                        this.drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color(0xFFF7FBFF).copy(alpha = 0.12f),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.36f),
                                radius = size.maxDimension * 1.02f,
                            ),
                            blendMode = BlendMode.Screen,
                        )
                        this.drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.14f),
                                    Color.Transparent,
                                    Color(0xFFF1F8FF).copy(alpha = 0.06f),
                                ),
                            ),
                            blendMode = BlendMode.Screen,
                        )
                        this.drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent,
                                ),
                                startX = size.width * 0.12f,
                                endX = size.width * 0.86f,
                            ),
                            topLeft = Offset(size.width * 0.12f, size.height * 0.18f),
                            size = Size(size.width * 0.72f, size.height * 0.08f),
                            blendMode = BlendMode.Screen,
                        )
                    }

                    LiquidGlassStylePreset.IOS -> {
                        this.drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.92f),
                                    Color.White.copy(alpha = 0.24f),
                                    Color.Transparent,
                                ),
                                startX = size.width * 0.04f,
                                endX = size.width * 0.52f,
                            ),
                            topLeft = Offset(size.width * 0.06f, size.height * 0.06f),
                            size = Size(size.width * 0.48f, size.height * 0.012f),
                            blendMode = BlendMode.Screen,
                        )
                        this.drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.24f, size.height * 0.14f),
                                radius = size.minDimension * 0.34f,
                            ),
                            blendMode = BlendMode.Screen,
                        )
                        this.drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.Transparent,
                                ),
                                start = Offset(size.width * 0.16f, size.height * 0.12f),
                                end = Offset(size.width * 0.46f, size.height * 0.28f),
                            ),
                            blendMode = BlendMode.Screen,
                        )
                        this.drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent,
                                ),
                                start = Offset(size.width * 0.78f, size.height * 0.04f),
                                end = Offset(size.width * 0.92f, size.height * 0.34f),
                            ),
                            blendMode = BlendMode.Screen,
                        )
                        this.drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.06f),
                                    Color.White.copy(alpha = 0.18f),
                                ),
                                startX = size.width * 0.72f,
                                endX = size.width * 0.98f,
                            ),
                            topLeft = Offset(size.width * 0.84f, size.height * 0.12f),
                            size = Size(size.width * 0.02f, size.height * 0.54f),
                            blendMode = BlendMode.Screen,
                        )
                        this.drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent,
                                ),
                                startX = size.width * 0.2f,
                                endX = size.width * 0.84f,
                            ),
                            topLeft = Offset(size.width * 0.24f, size.height * 0.9f),
                            size = Size(size.width * 0.48f, size.height * 0.012f),
                            blendMode = BlendMode.Screen,
                        )
                    }

                    LiquidGlassStylePreset.Hyper -> {
                        this.drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    palette.primary.copy(alpha = 0.24f),
                                    palette.secondary.copy(alpha = 0.34f),
                                    styledGlow.copy(alpha = 0.2f),
                                    Color.Transparent,
                                ),
                                start = Offset(size.width * 0.08f, size.height * 0.12f),
                                end = Offset(size.width * 0.92f, size.height * 0.88f),
                            ),
                            blendMode = BlendMode.Screen,
                        )
                        this.drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.16f),
                                    palette.secondary.copy(alpha = 0.24f),
                                    palette.primary.copy(alpha = 0.14f),
                                    Color.Transparent,
                                ),
                                start = Offset(size.width * 0.18f, size.height * 0.08f),
                                end = Offset(size.width * 0.86f, size.height * 0.38f),
                            ),
                            blendMode = BlendMode.Screen,
                        )
                        this.drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    palette.primary.copy(alpha = 0.2f),
                                    palette.secondary.copy(alpha = 0.28f),
                                    Color.Transparent,
                                ),
                                start = Offset(size.width * 0.1f, size.height * 0.74f),
                                end = Offset(size.width * 0.92f, size.height * 0.52f),
                            ),
                            blendMode = BlendMode.Screen,
                        )
                    }
                }
            },
        )
    } else {
        modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        styledTint,
                        styledGlow.copy(alpha = (styledGlow.alpha * 0.72f).coerceAtMost(1f)),
                    ),
                ),
            )
    }

    Box(
        modifier = modifierWithEffect
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(
                            alpha = when (stylePreset) {
                                LiquidGlassStylePreset.IOS -> 0f
                                else -> (0.1f * stylePreset.whiteSheenScale).coerceAtMost(0.16f)
                            },
                        ),
                        Color.Transparent,
                        styledGlow.copy(
                            alpha = when (stylePreset) {
                                LiquidGlassStylePreset.IOS -> 0f
                                else -> (styledGlow.alpha * 0.36f).coerceAtMost(1f)
                            },
                        ),
                    ),
                ),
            )
            .border(width = 1.dp, color = styledBorder, shape = shape),
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
private fun LiquidGlassBackdropLayer(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
) {
    val palette = PoxiaoThemeState.palette
    val stylePreset = LocalLiquidGlassStylePreset.current
    val transition = rememberInfiniteTransition(label = "liquid-glass-backdrop")
    val driftA = transition.animateFloat(
        initialValue = -0.08f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blob-a",
    )
    val driftB = transition.animateFloat(
        initialValue = 0.1f,
        targetValue = -0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blob-b",
    )
    val driftC = transition.animateFloat(
        initialValue = -0.04f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blob-c",
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .layerBackdrop(backdrop),
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    palette.backgroundTop.copy(alpha = 0.98f),
                    palette.backgroundBottom.copy(alpha = 0.98f),
                    palette.backgroundBottom.copy(alpha = 0.95f),
                ),
            ),
        )

        val minSize = size.minDimension
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.ambientGlow.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.66f
                            LiquidGlassStylePreset.IOS -> 0.06f
                            LiquidGlassStylePreset.Hyper -> 0.82f
                        },
                    ),
                    palette.cardGlow.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.1f
                            LiquidGlassStylePreset.IOS -> 0.008f
                            LiquidGlassStylePreset.Hyper -> (0.34f * stylePreset.glowScale).coerceAtMost(0.68f)
                        },
                    ),
                    Color.Transparent,
                ),
                center = Offset(size.width * (0.18f + driftA.value), size.height * 0.2f),
                radius = when (stylePreset) {
                    LiquidGlassStylePreset.Harmony -> minSize * 0.62f
                    LiquidGlassStylePreset.IOS -> minSize * 0.38f
                    LiquidGlassStylePreset.Hyper -> minSize * 0.48f
                },
            ),
            radius = when (stylePreset) {
                LiquidGlassStylePreset.Harmony -> minSize * 0.62f
                LiquidGlassStylePreset.IOS -> minSize * 0.38f
                LiquidGlassStylePreset.Hyper -> minSize * 0.48f
            },
            center = Offset(size.width * (0.18f + driftA.value), size.height * 0.2f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.primary.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.08f
                            LiquidGlassStylePreset.IOS -> 0.004f
                            LiquidGlassStylePreset.Hyper -> 0.28f
                        },
                    ),
                    palette.secondary.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.04f
                            LiquidGlassStylePreset.IOS -> 0.003f
                            LiquidGlassStylePreset.Hyper -> 0.2f
                        },
                    ),
                    Color.Transparent,
                ),
                center = Offset(size.width * (0.82f + driftB.value), size.height * 0.24f),
                radius = when (stylePreset) {
                    LiquidGlassStylePreset.Harmony -> minSize * 0.34f
                    LiquidGlassStylePreset.IOS -> minSize * 0.36f
                    LiquidGlassStylePreset.Hyper -> minSize * 0.44f
                },
            ),
            radius = when (stylePreset) {
                LiquidGlassStylePreset.Harmony -> minSize * 0.34f
                LiquidGlassStylePreset.IOS -> minSize * 0.36f
                LiquidGlassStylePreset.Hyper -> minSize * 0.44f
            },
            center = Offset(size.width * (0.82f + driftB.value), size.height * 0.24f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.12f
                            LiquidGlassStylePreset.IOS -> 0.035f
                            LiquidGlassStylePreset.Hyper -> 0.18f
                        },
                    ),
                    palette.cardGlow.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.08f
                            LiquidGlassStylePreset.IOS -> 0.008f
                            LiquidGlassStylePreset.Hyper -> 0.22f
                        },
                    ),
                    Color.Transparent,
                ),
                center = Offset(size.width * (0.48f + driftC.value), size.height * 0.74f),
                radius = when (stylePreset) {
                    LiquidGlassStylePreset.Harmony -> minSize * 0.56f
                    LiquidGlassStylePreset.IOS -> minSize * 0.44f
                    LiquidGlassStylePreset.Hyper -> minSize * 0.5f
                },
            ),
            radius = when (stylePreset) {
                LiquidGlassStylePreset.Harmony -> minSize * 0.56f
                LiquidGlassStylePreset.IOS -> minSize * 0.44f
                LiquidGlassStylePreset.Hyper -> minSize * 0.5f
            },
            center = Offset(size.width * (0.48f + driftC.value), size.height * 0.74f),
        )

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = when (stylePreset) {
                    LiquidGlassStylePreset.Harmony -> listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.16f),
                        Color(0xFFF4FAFF).copy(alpha = 0.08f),
                        Color.Transparent,
                    )
                    LiquidGlassStylePreset.IOS -> listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.004f),
                        Color.Transparent,
                    )
                    LiquidGlassStylePreset.Hyper -> listOf(
                        Color.Transparent,
                        palette.secondary.copy(alpha = 0.22f),
                        palette.primary.copy(alpha = 0.18f),
                        Color.Transparent,
                    )
                },
                start = Offset(size.width * 0.04f, size.height * 0.58f),
                end = Offset(size.width * 0.96f, size.height * 0.78f),
            ),
            topLeft = Offset(size.width * (-0.06f + driftA.value * 0.4f), size.height * 0.58f),
            size = Size(size.width * 1.12f, size.height * 0.18f),
            cornerRadius = CornerRadius(size.height * 0.16f, size.height * 0.16f),
            blendMode = BlendMode.Screen,
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.16f
                            LiquidGlassStylePreset.IOS -> 0.14f
                            LiquidGlassStylePreset.Hyper -> 0.16f
                        },
                    ),
                    Color.Transparent,
                    Color.White.copy(
                        alpha = when (stylePreset) {
                            LiquidGlassStylePreset.Harmony -> 0.04f
                            LiquidGlassStylePreset.IOS -> 0.004f
                            LiquidGlassStylePreset.Hyper -> 0.08f
                        },
                    ),
                ),
                start = Offset(size.width * 0.08f, size.height * 0.08f),
                end = Offset(size.width * 0.84f, size.height * 0.18f),
            ),
            topLeft = Offset(size.width * 0.08f, size.height * 0.08f),
            size = Size(size.width * 0.72f, size.height * 0.09f),
            cornerRadius = CornerRadius(size.height * 0.08f, size.height * 0.08f),
            blendMode = BlendMode.Screen,
        )

        when (stylePreset) {
            LiquidGlassStylePreset.Harmony -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color(0xFFF5FAFF).copy(alpha = 0.1f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.52f, size.height * 0.48f),
                        radius = minSize * 0.72f,
                    ),
                    radius = minSize * 0.72f,
                    center = Offset(size.width * 0.52f, size.height * 0.48f),
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                    topLeft = Offset(size.width * 0.18f, size.height * 0.12f),
                    size = Size(size.width * 0.46f, size.height * 0.16f),
                    cornerRadius = CornerRadius(size.height * 0.12f, size.height * 0.12f),
                    blendMode = BlendMode.Screen,
                )
            }

            LiquidGlassStylePreset.IOS -> {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.Transparent,
                        ),
                        start = Offset(size.width * 0.16f, size.height * 0.14f),
                        end = Offset(size.width * 0.74f, size.height * 0.34f),
                    ),
                    topLeft = Offset(size.width * 0.16f, size.height * 0.14f),
                    size = Size(size.width * 0.5f, size.height * 0.08f),
                    cornerRadius = CornerRadius(size.height * 0.08f, size.height * 0.08f),
                    blendMode = BlendMode.Screen,
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        start = Offset(size.width * 0.68f, size.height * 0.1f),
                        end = Offset(size.width * 0.92f, size.height * 0.34f),
                    ),
                    topLeft = Offset(size.width * 0.66f, size.height * 0.1f),
                    size = Size(size.width * 0.16f, size.height * 0.12f),
                    cornerRadius = CornerRadius(size.height * 0.12f, size.height * 0.12f),
                    blendMode = BlendMode.Screen,
                )
            }

            LiquidGlassStylePreset.Hyper -> {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            palette.secondary.copy(alpha = 0.24f),
                            palette.primary.copy(alpha = 0.3f),
                            Color.Transparent,
                        ),
                        start = Offset(size.width * 0.1f, size.height * 0.66f),
                        end = Offset(size.width * 0.92f, size.height * 0.88f),
                    ),
                    topLeft = Offset(size.width * 0.06f, size.height * 0.64f),
                    size = Size(size.width * 0.92f, size.height * 0.16f),
                    cornerRadius = CornerRadius(size.height * 0.14f, size.height * 0.14f),
                    blendMode = BlendMode.Screen,
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            palette.primary.copy(alpha = 0.22f),
                            palette.secondary.copy(alpha = 0.34f),
                            Color.Transparent,
                        ),
                        start = Offset(size.width * 0.2f, size.height * 0.16f),
                        end = Offset(size.width * 0.84f, size.height * 0.42f),
                    ),
                    topLeft = Offset(size.width * 0.22f, size.height * 0.14f),
                    size = Size(size.width * 0.48f, size.height * 0.1f),
                    cornerRadius = CornerRadius(size.height * 0.1f, size.height * 0.1f),
                    blendMode = BlendMode.Screen,
                )
            }
        }
    }
}
