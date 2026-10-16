package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = HospitalNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E6F8),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = HospitalTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0F0F3),
    onSecondaryContainer = Color(0xFF002023),
    tertiary = MedicalRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410002),
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = CardSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = HospitalNavyDarkTheme,
    onPrimary = Color(0xFF003258),
    primaryContainer = HospitalNavyContainerDark,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF7CD4DF),
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF9EE0EB),
    tertiary = Color(0xFFFFB4AB),
    onTertiary = Color(0xFF690005),
    tertiaryContainer = Color(0xFF93000A),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = SurfaceDark,
    onBackground = Color(0xFFE2E8F0),
    surface = CardSurfaceDark,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep hospital branding consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
