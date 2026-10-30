package com.lugonja.savora.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Savora design tokens, the same palette as the Flutter and iOS apps. */
@Immutable
data class SavoraColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val stroke: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primary: Color,
    val star: Color,
    val danger: Color,
    val shadow: Color,
    val glowWarm: Color,
    val glowCool: Color,
    val glass: Color,
) {
    val brandGradient: Brush get() = Brush.horizontalGradient(listOf(Color(0xFFFF8A3D), Color(0xFFFF4D6D)))

    companion object {
        val Light = SavoraColors(
            background = Color(0xFFFFF7F2),
            surface = Color(0xFFFFFFFF),
            surfaceRaised = Color(0xFFFFEEE5),
            stroke = Color(0xFFF1DED4),
            textPrimary = Color(0xFF22141F),
            textSecondary = Color(0xFF6E5A66),
            textTertiary = Color(0xFFA8949E),
            primary = Color(0xFFFF5A47),
            star = Color(0xFFFFB13D),
            danger = Color(0xFFE5484D),
            shadow = Color(0x337A2E1F),
            glowWarm = Color(0x33FFB13D),
            glowCool = Color(0x26FF4D6D),
            glass = Color(0xD9FFFFFF),
        )

        val Dark = SavoraColors(
            background = Color(0xFF130E17),
            surface = Color(0xFF1E1724),
            surfaceRaised = Color(0xFF2A2131),
            stroke = Color(0xFF332A3A),
            textPrimary = Color(0xFFFBF3EE),
            textSecondary = Color(0xFFBBAAB6),
            textTertiary = Color(0xFF7D6D79),
            primary = Color(0xFFFF6B57),
            star = Color(0xFFFFC15C),
            danger = Color(0xFFFF6369),
            shadow = Color(0x66000000),
            glowWarm = Color(0x29FF8A3D),
            glowCool = Color(0x2E9B5CFF),
            glass = Color(0xCC1E1724),
        )
    }
}

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp

    /** Bottom inset that keeps scrollable content clear of the floating tab bar. */
    val tabBarClearance = 120.dp
}

object Radius {
    val sm = 12.dp
    val md = 20.dp
    val lg = 28.dp
}

private val LocalSavoraColors = staticCompositionLocalOf { SavoraColors.Light }

object SavoraTheme {
    val colors: SavoraColors
        @Composable @ReadOnlyComposable get() = LocalSavoraColors.current

    /** Serif display face for headlines, the role Fraunces plays in the Flutter app. */
    fun display(size: Int): TextStyle = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = size.sp, lineHeight = (size * 1.1).sp)
}

@Composable
fun SavoraTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) SavoraColors.Dark else SavoraColors.Light
    val scheme = if (darkTheme) {
        darkColorScheme(primary = colors.primary, background = colors.background, surface = colors.surface, onSurface = colors.textPrimary, onBackground = colors.textPrimary)
    } else {
        lightColorScheme(primary = colors.primary, background = colors.background, surface = colors.surface, onSurface = colors.textPrimary, onBackground = colors.textPrimary)
    }
    val base = Typography()
    val typography = base.copy(
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Bold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Bold),
    )
    CompositionLocalProvider(LocalSavoraColors provides colors) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
