package com.editnova.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editnova.app.core.di.AppContainer
import com.editnova.app.core.theme.EditNovaSpacing

/** One row in the Settings list. */
private data class SettingsItem(
    val title: String,
    val subtitle: String?,
    val icon: ImageVector
)

/**
 * SettingsScreen
 *
 * All rows here are placeholders for Step 2 — tapping them does not open any real
 * screen or persist any setting yet (except the "Subscription" row, which opens the
 * real, already-built Premium upgrade screen via [onOpenPremium]).
 *
 * The "Watermark" row is the one exception that reads REAL app state: it asks
 * [com.editnova.app.domain.watermark.WatermarkPolicy] (built in Step 1) whether a
 * watermark currently applies, so this screen honestly reflects the architecture
 * that already exists — while still not touching any actual video export, since
 * no export feature exists yet.
 */
@Composable
fun SettingsScreen(onOpenPremium: () -> Unit) {
    val watermarkOn = AppContainer.watermarkPolicy.shouldApplyWatermark()
    val watermarkSubtitle = if (watermarkOn) {
        "On — Free plan exports include the EditNova watermark"
    } else {
        "Off — Premium plan exports have no watermark"
    }

    val items = listOf(
        SettingsItem("Account", "Not signed in", Icons.Filled.AccountCircle),
        SettingsItem("Subscription", "Free plan", Icons.Filled.WorkspacePremium),
        SettingsItem("Appearance", "Dark (default)", Icons.Filled.Palette),
        SettingsItem("Export Settings", "Default quality", Icons.Filled.FileDownload),
        SettingsItem("Watermark", watermarkSubtitle, Icons.Filled.BrandingWatermark),
        SettingsItem("Notifications", null, Icons.Filled.Notifications),
        SettingsItem("Privacy", null, Icons.Filled.PrivacyTip),
        SettingsItem("Terms", null, Icons.Filled.Description),
        SettingsItem("About EditNova", "Version 0.1.0-step2", Icons.Filled.Info)
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = EditNovaSpacing.md)
    ) {
        items(items) { item ->
            SettingsRow(
                item = item,
                onClick = { if (item.title == "Subscription") onOpenPremium() }
            )
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = EditNovaSpacing.screenHorizontal, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = MaterialTheme.colorScheme.secondary
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = EditNovaSpacing.md)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
