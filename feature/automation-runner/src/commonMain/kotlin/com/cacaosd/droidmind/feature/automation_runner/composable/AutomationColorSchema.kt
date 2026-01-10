package com.cacaosd.droidmind.feature.automation_runner.composable

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.cacaosd.uikit.theme.config.ColorSchemeProvider

val colorSchemeProvider = object : ColorSchemeProvider {
    override val lightColorScheme: ColorScheme
        get() = LightColorScheme
    override val darkColorScheme: ColorScheme
        get() = DarkColorScheme

}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF136DEC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8E7D9),
    onPrimaryContainer = Color(0xFF001F24),

    secondary = Color(0xFF4A5E5E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE2E2),
    onSecondaryContainer = Color(0xFF051F1F),

    tertiary = Color(0xFF5B5D7E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE8DEF8),
    onTertiaryContainer = Color(0xFF181937),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFF6F7F8),
    onBackground = Color(0xFF191C1C),

    surface = Color(0xFFE0EBE4),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDAE5E1),
    onSurfaceVariant = Color(0xFF3F4947),

    outline = Color(0xFF6F7977),
    outlineVariant = Color(0xFFBEC9C6),

    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2E3131),
    inverseOnSurface = Color(0xFFEFF1EF),
    inversePrimary = Color(0xFF4FDAE6),

    surfaceTint = Color(0xFF136DEC),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FDAE6),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF2A382C),
    onPrimaryContainer = Color(0xFF6FF6FF),

    secondary = Color(0xFFB0C6C6),
    onSecondary = Color(0xFF1B3434),
    secondaryContainer = Color(0xFF31494A),
    onSecondaryContainer = Color(0xFFCCE2E2),

    tertiary = Color(0xFFC7C4DC),
    onTertiary = Color(0xFF30314D),
    tertiaryContainer = Color(0xFF362F3E),
    onTertiaryContainer = Color(0xFFE8DEF8),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF101822),
    onBackground = Color(0xFFE0E3E1),

    surface = Color(0xFF1E2320),
    onSurface = Color(0xFFE0E3E1),
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBEC9C6),

    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4947),

    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE0E3E1),
    inverseOnSurface = Color(0xFF191C1C),
    inversePrimary = Color(0xFF136DEC),

    surfaceTint = Color(0xFF4FDAE6),
)
