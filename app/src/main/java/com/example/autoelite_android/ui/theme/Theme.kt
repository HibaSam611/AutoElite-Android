package com.example.autoelite_android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AutoEliteDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D57),
    onPrimaryContainer = CyanPrimaryLight,

    secondary = BlueAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003547),
    onSecondaryContainer = Color(0xFFB3E5FC),

    tertiary = GoldAccent,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4A3800),
    onTertiaryContainer = Color(0xFFFFE082),

    background = DarkBackground,
    onBackground = TextPrimaryDark,

    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondaryDark,

    surfaceContainerLowest = Color(0xFF0E1013),
    surfaceContainerLow = Color(0xFF1A1D21),
    surfaceContainer = DarkCard,
    surfaceContainerHigh = DarkCardVariant,
    surfaceContainerHighest = Color(0xFF32373D),

    error = ErrorRedDark,
    onError = Color.Black,
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFB4AB),

    outline = Color(0xFF5F6368),
    outlineVariant = Color(0xFF3C4043),

    inverseSurface = Color(0xFFE8EAED),
    inverseOnSurface = Color(0xFF1A1D21),
    inversePrimary = CyanPrimaryDark,
)

private val AutoEliteLightColorScheme = lightColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF00363D),

    secondary = BlueAccentDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB3E5FC),
    onSecondaryContainer = Color(0xFF001F2A),

    tertiary = Color(0xFFF57F17),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE082),
    onTertiaryContainer = Color(0xFF3E2723),

    background = LightBackground,
    onBackground = TextPrimaryLight,

    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEEF1F6),
    onSurfaceVariant = TextSecondaryLight,

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F9FB),
    surfaceContainer = Color(0xFFF1F3F6),
    surfaceContainerHigh = Color(0xFFEBEDF0),
    surfaceContainerHighest = Color(0xFFE3E5E8),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFF9AA0A6),
    outlineVariant = Color(0xFFDADCE0),

    inverseSurface = Color(0xFF2E3133),
    inverseOnSurface = Color(0xFFF1F3F6),
    inversePrimary = CyanPrimaryLight,
)

@Composable
fun AutoEliteAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AutoEliteDarkColorScheme else AutoEliteLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}