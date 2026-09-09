package com.editnova.app.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editnova.app.R
import com.editnova.app.core.theme.AccentCyanEnd
import com.editnova.app.core.theme.AccentVioletStart
import com.editnova.app.core.ui.components.EditNovaLogo
import kotlinx.coroutines.delay

/**
 * SplashScreen
 *
 * Shown for a short moment when the app launches. Purely presentational — it does not
 * check login state or subscription state yet (there is no login system yet). It plays
 * a short fade + scale-in animation on the logo/name/tagline, plus a subtle looping glow
 * behind the logo, then calls [onFinished].
 *
 * @param onFinished Called automatically once the intro animation completes. The nav
 *                    graph (see core/navigation/NavGraph.kt) decides what happens next —
 *                    this screen doesn't need to know it leads to Onboarding.
 * @param minimumDisplayMillis How long to hold the splash before navigating away. Kept
 *                    as a parameter so it's easy to tune without hunting through the file.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    minimumDisplayMillis: Long = 1600L
) {
    // Drives the one-shot fade-in + scale-up "premium reveal" animation for the
    // logo/title/tagline.
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing)
        )
        delay((minimumDisplayMillis - 700L).coerceAtLeast(0L))
        onFinished()
    }

    // Animatable's `value` is itself observable by Compose, so reading it directly
    // here (no `by`/State wrapper needed) still triggers recomposition on every
    // animation frame.
    val progress = animationProgress.value

    // A slow, looping, low-key glow behind the logo — subtle and lightweight (a single
    // animated float driving a radial gradient), not a heavy particle/video effect.
    val infiniteTransition = rememberInfiniteTransition(label = "splash_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_glow_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Subtle animated background glow, drawn behind everything else.
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentVioletStart.copy(alpha = glowAlpha),
                        AccentCyanEnd.copy(alpha = 0f)
                    ),
                    center = Offset(size.width / 2f, size.height * 0.4f),
                    radius = size.maxDimension * 0.6f
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EditNovaLogo(
                modifier = Modifier
                    .alpha(progress)
                    .scale(0.85f + (0.15f * progress))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(progress)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(progress)
            )
        }
    }
}
