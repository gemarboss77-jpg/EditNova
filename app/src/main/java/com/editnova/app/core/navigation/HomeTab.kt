package com.editnova.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * HomeTab — the 5 destinations reachable from EditNova's bottom navigation bar
 * (Home, Projects, AI Tools, Templates, Settings).
 *
 * These are switched with simple in-memory state inside HomeScreen (see
 * feature/home/HomeScreen.kt) rather than being separate Navigation-Compose routes.
 * This is a deliberate, common pattern for bottom-nav tabs: they represent "which
 * view am I looking at right now" rather than "where in a back-stack am I", so a
 * back-stack entry per tab isn't needed. Screens reached by tapping something
 * *inside* a tab (e.g. the Premium upgrade screen) still use real navigation — see
 * Screen.kt / NavGraph.kt.
 */
enum class HomeTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    PROJECTS("Projects", Icons.Filled.VideoLibrary),
    AI_TOOLS("AI Tools", Icons.Filled.AutoAwesome),
    TEMPLATES("Templates", Icons.Filled.ViewModule),
    SETTINGS("Settings", Icons.Filled.Settings)
}
