package com.editnova.app.feature.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editnova.app.core.theme.EditNovaSpacing
import com.editnova.app.core.ui.components.GradientButton
import com.editnova.app.domain.subscription.BillingPeriod
import kotlinx.coroutines.launch

/** The two billing choices shown on this screen. */
private data class PlanOption(
    val period: BillingPeriod,
    val title: String,
    val priceLabel: String,
    val subLabel: String
)

private val planOptions = listOf(
    PlanOption(BillingPeriod.MONTHLY, "Monthly", "$9.99/mo", "Billed monthly"),
    PlanOption(BillingPeriod.YEARLY, "Yearly", "$59.99/yr", "Save over 45% — billed yearly")
)

private data class Benefit(val icon: ImageVector, val text: String)

private val benefits = listOf(
    Benefit(Icons.Filled.WaterDrop, "Export videos with no EditNova watermark"),
    Benefit(Icons.Filled.HighQuality, "Higher quality video export"),
    Benefit(Icons.Filled.AutoAwesome, "Access to premium AI tools")
)

/**
 * PremiumScreen — the "Upgrade to EditNova Premium" screen.
 *
 * THIS IS UI ONLY. There is no real payment processing, no Google Play Billing call,
 * and tapping "Upgrade Now" does not change the user's plan anywhere in the app —
 * [com.editnova.app.data.subscription.FakeSubscriptionRepository] always reports Free
 * regardless of what happens on this screen. The button shows an honest "coming soon"
 * message instead of silently doing nothing, so it doesn't look broken.
 */
@Composable
fun PremiumScreen(onBack: () -> Unit) {
    var selectedPeriod by remember { mutableStateOf(BillingPeriod.YEARLY) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(EditNovaSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = EditNovaSpacing.screenHorizontal)
        ) {
            Text(
                text = "Upgrade to EditNova Premium",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(EditNovaSpacing.sm))

            Text(
                text = "Unlock the full EditNova experience.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(EditNovaSpacing.lg))

            // --- Benefits list ---
            benefits.forEach { benefit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = benefit.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = EditNovaSpacing.sm)
                    )
                }
            }

            Spacer(modifier = Modifier.height(EditNovaSpacing.lg))

            // --- Plan selection ---
            planOptions.forEach { option ->
                PlanCard(
                    option = option,
                    selected = selectedPeriod == option.period,
                    onSelect = { selectedPeriod = option.period }
                )
                Spacer(modifier = Modifier.height(EditNovaSpacing.sm))
            }

            Spacer(modifier = Modifier.height(EditNovaSpacing.md))

            GradientButton(
                text = "Upgrade Now",
                onClick = {
                    // Intentionally non-functional: no billing integration exists yet.
                    // We surface this honestly instead of pretending the tap worked.
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Payments aren't available yet — coming in a future update.")
                    }
                }
            )

            Spacer(modifier = Modifier.height(EditNovaSpacing.lg))
        }
    }
}

@Composable
private fun PlanCard(option: PlanOption, selected: Boolean, onSelect: () -> Unit) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .padding(EditNovaSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = option.subLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = option.priceLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
    }
}
