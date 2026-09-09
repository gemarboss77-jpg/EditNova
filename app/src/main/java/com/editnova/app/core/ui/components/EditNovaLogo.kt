package com.editnova.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.editnova.app.core.theme.EditNovaBrandGradient

/**
 * EditNovaLogo — PLACEHOLDER.
 *
 * This is NOT the final brand logo. It's a stand-in mark (gradient rounded square +
 * a movie icon) so every screen that needs "the logo" already has a single component
 * to use. When the real logo is designed, we only need to change the inside of THIS
 * function — every screen using <EditNovaLogo /> updates automatically.
 *
 * @param size Overall size of the logo mark, in dp. Defaults to a size good for splash screens.
 */
@Composable
fun EditNovaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(brush = EditNovaBrandGradient, shape = RoundedCornerShape(size * 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Movie,
            contentDescription = "EditNova logo placeholder",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
