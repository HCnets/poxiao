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

enum class PoxiaoThemePreset(
    val title: String,
    val subtitle: String,
) {
    Forest("\u68ee\u5c7f\u9752\u5c9a", "\u6e05\u6da6\u68ee\u6797\u4e0e\u4e2d\u5f0f\u8349\u6728\u6c14\u606f"),
    Aero("Frutiger Aero", "Y2K \u6c34\u6da6\u73bb\u7483\u4e0e\u6674\u7a7a\u8349\u5730"),
    Ink("\u58a8\u767d\u4e66\u5377", "\u5ba3\u7eb8\u7559\u767d\u3001\u58a8\u9752\u5c42\u6b21\u4e0e\u5b89\u9759\u9605\u8bfb\u611f"),
    Sunset("\u843d\u65e5\u679c\u6c7d", "\u6696\u6a59\u73ca\u745a\u4e0e\u8f7b\u751c\u6d41\u4f53\u5149\u6cfd"),
    Night("\u591c\u822a\u96fe\u5c9b", "\u6df1\u6d77\u58a8\u8272\u4e0e\u51b7\u611f\u9713\u8679\u8fb9\u5149"),
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
    ambientGlow = Color(0x44A3D9AE),
    card = Color.White.copy(alpha = 0.46f),
    cardBorder = BambooStroke.copy(alpha = 0.6f),
    cardGlow = Color(0x1FA4D4B2),
    dock = Color(0xD81C3027),
    dockBorder = Color.White.copy(alpha = 0.14f),
    primary = ForestGreen,
    secondary = Ginkgo,
    ink = PineInk,
    softText = ForestDeep.copy(alpha = 0.76f),
    pillOn = CloudWhite,
    islandStart = Color(0xDA182921),
    islandEnd = Color(0xF31C3329),
)

private val AeroPalette = PoxiaoPalette(
    backgroundTop = Color(0xFFF1FBFF),
    backgroundBottom = Color(0xFFD7F4E8),
    ambientGlow = Color(0x6689D8FF),
    card = Color(0x7AFFFFFF),
    cardBorder = Color(0x99D5F5FF),
    cardGlow = Color(0x3FA5F0FF),
    dock = Color(0xA5D8F6FF),
    dockBorder = Color(0x99FFFFFF),
    primary = Color(0xFF38A9D9),
    secondary = Color(0xFF7FD77E),
    ink = Color(0xFF11455C),
    softText = Color(0xCC2B6176),
    pillOn = Color.White,
    islandStart = Color(0xCC6BC8F4),
    islandEnd = Color(0xCC3A9BE2),
)

private val InkPalette = PoxiaoPalette(
    backgroundTop = Color(0xFFF7F3EB),
    backgroundBottom = Color(0xFFECE4D7),
    ambientGlow = Color(0x335D786F),
    card = Color(0x66FFFDF8),
    cardBorder = Color(0x73D6D0C4),
    cardGlow = Color(0x1F9FB6A4),
    dock = Color(0xD82F342F),
    dockBorder = Color(0x3FFFFFFF),
    primary = Color(0xFF3F6A60),
    secondary = Color(0xFF9F7A4C),
    ink = Color(0xFF1E2825),
    softText = Color(0xCC43514D),
    pillOn = Color.White,
    islandStart = Color(0xCC303734),
    islandEnd = Color(0xCC45514C),
)

private val SunsetPalette = PoxiaoPalette(
    backgroundTop = Color(0xFFFFF4EA),
    backgroundBottom = Color(0xFFFFE0D6),
    ambientGlow = Color(0x55FF9D7A),
    card = Color(0x70FFF9F4),
    cardBorder = Color(0x88FFE2D2),
    cardGlow = Color(0x2FFFB889),
    dock = Color(0xD85B3D35),
    dockBorder = Color(0x40FFFFFF),
    primary = Color(0xFFDD6B4D),
    secondary = Color(0xFFF0B257),
    ink = Color(0xFF512D25),
    softText = Color(0xCC72473B),
    pillOn = Color.White,
    islandStart = Color(0xCCB55742),
    islandEnd = Color(0xCCDD7D5C),
)

private val NightPalette = PoxiaoPalette(
    backgroundTop = Color(0xFF0E1524),
    backgroundBottom = Color(0xFF16263A),
    ambientGlow = Color(0x4448D8C6),
    card = Color(0x4D1E2D47),
    cardBorder = Color(0x667DDCF6),
    cardGlow = Color(0x2F46E0FF),
    dock = Color(0xD3121930),
    dockBorder = Color(0x4D91EDFF),
    primary = Color(0xFF57D6E8),
    secondary = Color(0xFF9D85FF),
    ink = Color(0xFFF4FBFF),
    softText = Color(0xCCCAE9F2),
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
    darkTheme: Boolean = isSystemInDarkTheme() && preset == PoxiaoThemePreset.Night,
    content: @Composable () -> Unit,
) {
    val palette = paletteFor(preset)
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
