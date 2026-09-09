package com.editnova.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.editnova.app.core.navigation.EditNovaNavGraph
import com.editnova.app.core.theme.EditNovaTheme

/**
 * MainActivity
 *
 * EditNova uses the "single Activity" pattern: there is only ONE real Android Activity.
 * Every screen (Splash, Onboarding, Home, and everything we add later) is a Composable
 * function, and navigating between them is handled by Jetpack Navigation for Compose
 * inside [EditNovaNavGraph] — not by launching new Activities.
 *
 * This is the standard, scalable approach for modern Android apps because it keeps
 * navigation, animations, and shared state simple as the app grows.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lets our dark background draw behind the status/navigation bars for a premium,
        // edge-to-edge look — common in modern video/photo editing apps.
        enableEdgeToEdge()

        setContent {
            // EditNovaTheme applies our dark-first color palette, typography, and shapes
            // to every screen below it in the tree.
            EditNovaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    EditNovaNavGraph()
                }
            }
        }
    }
}
