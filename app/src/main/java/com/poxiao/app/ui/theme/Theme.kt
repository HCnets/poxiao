package com.poxiao.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.time.LocalTime

import android.graphics.Color as AndroidColor

fun Color.shiftHSV(hueOffset: Float, saturationMult: Float): Color {
    if (hueOffset == 0f && saturationMult == 1f) return this
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(this.toArgb(), hsv)
    hsv[0] = (hsv[0] + hueOffset) % 360f
    if (hsv[0] < 0) hsv[0] += 360f
    hsv[1] = (hsv[1] * saturationMult).coerceIn(0f, 1f)
    return Color(AndroidColor.HSVToColor(this.alpha.times(255).toInt(), hsv))
}

fun PoxiaoPalette.shifted(hueOffset: Float, saturationMult: Float): PoxiaoPalette {
    if (hueOffset == 0f && saturationMult == 1f) return this
    return PoxiaoPalette(
        backgroundTop = backgroundTop.shiftHSV(hueOffset, saturationMult),
        backgroundBottom = backgroundBottom.shiftHSV(hueOffset, saturationMult),
        ambientGlow = ambientGlow.shiftHSV(hueOffset, saturationMult),
        card = card.shiftHSV(hueOffset, saturationMult),
        cardBorder = cardBorder.shiftHSV(hueOffset, saturationMult),
        cardGlow = cardGlow.shiftHSV(hueOffset, saturationMult),
        dock = dock.shiftHSV(hueOffset, saturationMult),
        dockBorder = dockBorder.shiftHSV(hueOffset, saturationMult),
        primary = primary.shiftHSV(hueOffset, saturationMult),
        secondary = secondary.shiftHSV(hueOffset, saturationMult),
        ink = ink, 
        softText = softText,
        pillOn = pillOn,
        islandStart = islandStart.shiftHSV(hueOffset, saturationMult),
        islandEnd = islandEnd.shiftHSV(hueOffset, saturationMult),
    )
}

enum class PoxiaoThemePreset(
    val title: String,
    val subtitle: String,
) {
    Forest("\u68ee\u5c7f\u9752\u5c9a", "\u6e05\u6da6\u68ee\u6797\u4e0e\u4e2d\u5f0f\u8349\u6728\u6c14\u606f"),
    Aero("Frutiger Aero", "Y2K \u6c34\u6da6\u73bb\u7483\u4e0e\u6674\u7a7a\u8349\u5730"),
    Ink("\u58a8\u767d\u4e66\u5377", "\u5ba3\u7eb8\u7559\u767d\u3001\u58a8\u9752\u5c42\u6b21\u4e0e\u5b89\u9759\u9605\u8bfb\u611f"),
    Sunset("\u843d\u65e5\u679c\u6c7d", "\u6696\u6a59\u73ca\u745a\u4e0e\u8f7b\u751c\u6d41\u4f53\u5149\u6cfd"),
    Night("\u591c\u822a\u96fe\u5c9b", "\u6df1\u6d77\u58a8\u8272\u4e0e\u51b7\u611f\u9713\u8679\u8fb9\u5149"),
    Dynamic("时光流转", "跟随现实时间，从晨曦到夜航自动变换氛围色调"),
}

@Immutable
data class PoxiaoPalette(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val ambientGlow: Color,
    val card: Color,
    val cardBorder: Color,
    val cardGlow: Color,
    val dock: Color,
    val dockBorder: Color,
    val primary: Color,
    val secondary: Color,
    val ink: Color,
    val softText: Color,
    val pillOn: Color,
    val islandStart: Color,
    val islandEnd: Color,
)

private val ForestPalette = PoxiaoPalette(
    backgroundTop = WarmMist,
    backgroundBottom = Color(0xFFE7F1E7),
    ambientGlow = Color(0x22A3D9AE),
    card = Color.White,
    cardBorder = Color(0x1A000000),
    cardGlow = Color(0x0A000000),
    dock = Color(0xD81C3027),
    dockBorder = Color.White.copy(alpha = 0.08f),
    primary = ForestGreen,
    secondary = Ginkgo,
    ink = PineInk,
    softText = Color(0x99000000),
    pillOn = CloudWhite,
    islandStart = Color(0xDA182921),
    islandEnd = Color(0xF31C3329),
)

private val AeroPalette = PoxiaoPalette(
    backgroundTop = Color(0xFFF1FBFF),
    backgroundBottom = Color(0xFFD7F4E8),
    ambientGlow = Color(0x3389D8FF),
    card = Color.White,
    cardBorder = Color(0x1A000000),
    cardGlow = Color(0x0A000000),
    dock = Color(0xA5D8F6FF),
    dockBorder = Color.White.copy(alpha = 0.1f),
    primary = Color(0xFF38A9D9),
    secondary = Color(0xFF7FD77E),
    ink = Color(0xFF11455C),
    softText = Color(0x99000000),
    pillOn = Color.White,
    islandStart = Color(0xCC6BC8F4),
    islandEnd = Color(0xCC3A9BE2),
)

private val InkPalette = PoxiaoPalette(
    backgroundTop = Color(0xFFF7F3EB),
    backgroundBottom = Color(0xFFECE4D7),
    ambientGlow = Color(0x185D786F),
    card = Color.White,
    cardBorder = Color(0x1A000000),
    cardGlow = Color(0x0A000000),
    dock = Color(0xD82F342F),
    dockBorder = Color(0x20FFFFFF),
    primary = Color(0xFF3F6A60),
    secondary = Color(0xFF9F7A4C),
    ink = Color(0xFF1E2825),
    softText = Color(0x99000000),
    pillOn = Color.White,
    islandStart = Color(0xCC303734),
    islandEnd = Color(0xCC45514C),
)

private val SunsetPalette = PoxiaoPalette(
    backgroundTop = Color(0xFFFFF4EA),
    backgroundBottom = Color(0xFFFFE0D6),
    ambientGlow = Color(0x28FF9D7A),
    card = Color.White,
    cardBorder = Color(0x1A000000),
    cardGlow = Color(0x0A000000),
    dock = Color(0xD85B3D35),
    dockBorder = Color(0x20FFFFFF),
    primary = Color(0xFFDD6B4D),
    secondary = Color(0xFFF0B257),
    ink = Color(0xFF512D25),
    softText = Color(0x99000000),
    pillOn = Color.White,
    islandStart = Color(0xCCB55742),
    islandEnd = Color(0xCCDD7D5C),
)

private val NightPalette = PoxiaoPalette(
    backgroundTop = Color(0xFF0E1524),
    backgroundBottom = Color(0xFF16263A),
    ambientGlow = Color(0x2248D8C6),
    card = Color(0xFF1E2D47),
    cardBorder = Color(0x1AFFFFFF),
    cardGlow = Color(0x0AFFFFFF),
    dock = Color(0xD3121930),
    dockBorder = Color(0x2091EDFF),
    primary = Color(0xFF57D6E8),
    secondary = Color(0xFF9D85FF),
    ink = Color(0xFFF4FBFF),
    softText = Color(0x99FFFFFF),
    pillOn = Color(0xFF0F1D2A),
    islandStart = Color(0xCC112038),
    islandEnd = Color(0xCC1D355A),
)

private fun paletteFor(preset: PoxiaoThemePreset): PoxiaoPalette {
    return when (preset) {
        PoxiaoThemePreset.Forest -> ForestPalette
        PoxiaoThemePreset.Aero -> AeroPalette
        PoxiaoThemePreset.Ink -> InkPalette
        PoxiaoThemePreset.Sunset -> SunsetPalette
        PoxiaoThemePreset.Night -> NightPalette
        PoxiaoThemePreset.Dynamic -> {
            val hour = LocalTime.now().hour
            when {
                hour in 6..11 -> ForestPalette // 晨间：森林
                hour in 12..16 -> AeroPalette // 午间：水润
                hour in 17..19 -> SunsetPalette // 傍晚：落日
                else -> NightPalette // 夜间：夜航
            }
        }
    }
}

val LocalPoxiaoPalette = staticCompositionLocalOf { ForestPalette }

object PoxiaoThemeState {
    val palette: PoxiaoPalette
        @Composable get() = LocalPoxiaoPalette.current
}

@Composable
fun PoxiaoTheme(
    preset: PoxiaoThemePreset = PoxiaoThemePreset.Forest,
    customHueOffset: Float = 0f,
    customSaturation: Float = 1f,
    darkTheme: Boolean = isSystemInDarkTheme() && preset == PoxiaoThemePreset.Night,
    content: @Composable () -> Unit,
) {
    val basePalette = paletteFor(preset)
    val palette = basePalette.shifted(customHueOffset, customSaturation)
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = palette.pillOn,
            secondary = palette.secondary,
            background = palette.backgroundTop,
            onBackground = palette.ink,
            surface = palette.card,
            onSurface = palette.ink,
            surfaceVariant = palette.cardGlow,
            outline = palette.cardBorder,
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = palette.pillOn,
            primaryContainer = palette.cardGlow,
            onPrimaryContainer = palette.ink,
            secondary = palette.secondary,
            onSecondary = palette.ink,
            background = palette.backgroundTop,
            onBackground = palette.ink,
            surface = palette.card,
            onSurface = palette.ink,
            surfaceVariant = palette.cardGlow,
            outline = palette.cardBorder,
        )
    }

    CompositionLocalProvider(LocalPoxiaoPalette provides palette) {
        MaterialTheme(
            colorScheme = colors,
            typography = PoxiaoTypography,
            content = content,
        )
    }
}
