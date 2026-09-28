package com.editnova.app.feature.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = """
Information We May Process

EditNova may process information needed to provide app features, such as media files selected by you, app settings, and information required for features you choose to use.

Media Files

Media selected for editing is used to provide editing features. EditNova does not claim ownership of your media.

AI Features

If an AI feature requires processing outside your device, the app will disclose that requirement before the feature is used. Do not submit sensitive or confidential information unless you understand how that feature processes your content.

Subscriptions

If you purchase a premium subscription, payment processing is handled by the applicable app store or payment provider. EditNova does not directly receive your full payment credentials.

Data Security

Reasonable measures are used to protect information handled by the app. No method of electronic storage or transmission can be guaranteed to be completely secure.

Children

EditNova is not intended to knowingly collect personal information from children without appropriate authorization.

Changes

This Privacy Policy may be updated as EditNova develops. The latest version should be made available within the app.

Contact

For privacy questions or requests, use the official contact information provided by the EditNova developer.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
