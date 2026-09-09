package com.editnova.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

/**
 * EditNova is a "dark-first" app: dark mode is the primary, designed-for experience
 * (standard for video editing tools since it's easier on the eyes and makes video
 * previews stand out). We still provide a light scheme as a fallback for accessibility,
 * but every screen in this project is designed and tested against the dark scheme.
 */
private val EditNovaDarkColorScheme = darkColorScheme(
    primary = AccentVioletStart,
    secondary = AccentCyanEnd,
    background = BackgroundPrimary,
    surface = SurfaceElevated,
    surfaceVariant = SurfaceElevatedHigh,
    onPrimary = TextPrimary,
    onSecondary = BackgroundPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

private val EditNovaLightColorScheme = lightColorScheme(
    primary = AccentVioletStart,
    secondary = AccentCyanEnd,
    background = androidx.compose.ui.graphics.Color(0xFFF7F7FA),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    error = ErrorRed
)

/** The signature violet -> cyan gradient used for primary buttons, logo, and highlights. */
val EditNovaBrandGradient: Brush
    @Composable
    get() = Brush.linearGradient(listOf(AccentVioletStart, AccentCyanEnd))

/**
 * Wrap any screen (or the whole app) in this to apply EditNova's colors, typography,
 * and shapes consistently. Usage: EditNovaTheme { /* your screen content */ }
 *
 * NOTE: EditNova is dark-first by design (see file header), so [darkTheme] defaults
 * to true regardless of the system setting for Step 1. A future step can wire this
 * up to a real user preference (e.g. a Settings toggle) instead of a hardcoded value.
 */
@Composable
fun EditNovaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EditNovaDarkColorScheme else EditNovaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EditNovaTypography,
        content = content
    )
}
