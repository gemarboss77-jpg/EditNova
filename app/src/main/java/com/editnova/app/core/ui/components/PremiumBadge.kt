package com.editnova.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editnova.app.core.theme.PremiumGold

/**
 * PremiumBadge — a small pill showing the user's current plan ("Free" or "Premium").
 *
 * This is a pure UI component: it takes [isPremium] as a parameter rather than reading
 * subscription state itself, so it stays reusable and easy to preview/test. The screen
 * that places it is responsible for reading the real value from
 * [com.editnova.app.domain.subscription.SubscriptionRepository] (see AppContainer).
 *
 * Tapping it is meant to open the upgrade screen — pass [onClick] for that; a Premium
 * user tapping it could later open plan management instead, but that's a Step 3 concern.
 */
@Composable
fun PremiumBadge(
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isPremium) PremiumGold else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isPremium) androidx.compose.ui.graphics.Color(0xFF241A00) else MaterialTheme.colorScheme.onSurfaceVariant

    Text(
        text = if (isPremium) "PREMIUM" else "FREE",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = modifier
            .clickable(onClick = onClick)
            .background(color = backgroundColor, shape = RoundedCornerShape(50))
            .padding(PaddingValues(horizontal = 10.dp, vertical = 5.dp))
    )
}
