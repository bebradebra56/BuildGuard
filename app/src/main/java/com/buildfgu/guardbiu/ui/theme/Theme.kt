package com.buildfgu.guardbiu.ui.theme

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
    primary = Blue700,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    secondary = Teal500,
    onSecondary = Color.White,
    secondaryContainer = Teal200,
    onSecondaryContainer = Teal700,
    tertiary = Amber600,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = Gray600,
    error = Red600,
    onError = Color.White,
    outline = Gray300
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue300,
    onPrimary = Blue900,
    primaryContainer = Blue800,
    onPrimaryContainer = Blue100,
    secondary = Teal200,
    onSecondary = Teal700,
    secondaryContainer = Color(0xFF1A4A42),
    onSecondaryContainer = Teal200,
    tertiary = Amber400,
    onTertiary = Gray900,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceBright = Color(0xFF454B60),
    surfaceDim = DarkBackground,
    surfaceTint = Blue300,
    error = Red400,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = Color(0xFFE4E6F0),
    inverseOnSurface = Color(0xFF2A2C38),
    inversePrimary = Blue700,
    scrim = Color.Black
)

@Composable
fun BuildGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Off by default so dark mode keeps strong contrast (dynamic dark often looks flat). */
    dynamicColor: Boolean = false,
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
