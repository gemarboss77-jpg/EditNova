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
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms of Service") },
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
                text = "Terms of Service",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = """
Use of the App

You may use EditNova for lawful personal or commercial editing purposes, subject to applicable laws and these terms.

Your Content

You retain ownership of videos, photos, audio, and other content that you import into EditNova. You are responsible for having the necessary rights and permissions to use that content.

AI Features

AI-generated or AI-assisted results may not always be accurate or suitable for every purpose. You are responsible for reviewing generated results before using or publishing them.

Premium Features

Some features may require a paid subscription. Pricing, billing periods, renewal terms, and cancellation rules are presented through the applicable payment provider or app store.

Prohibited Use

You must not use EditNova for unlawful activities, infringement of third-party rights, or content that violates applicable laws.

Availability

Features may change, be updated, suspended, or discontinued as EditNova develops.

Disclaimer

EditNova is provided on an "as is" and "as available" basis. To the extent permitted by applicable law, EditNova makes no guarantee that every feature will always be available or error-free.

Changes

These Terms of Service may be updated as EditNova develops. Continued use of the app after changes means you accept the updated terms.

Contact

For questions about these Terms of Service, use the official contact information provided by the EditNova developer.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
