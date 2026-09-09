package com.editnova.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editnova.app.core.theme.EditNovaBrandGradient

/**
 * GradientButton — the app's primary call-to-action button style.
 *
 * Used anywhere we want the user's eye drawn to "the main action" (e.g. "Get Started",
 * later "Export", "Upgrade to Premium"). Keeping this as one component means the
 * button's look can be tweaked once here instead of in every screen that has a CTA.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = EditNovaBrandGradient, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(PaddingValues(vertical = 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
