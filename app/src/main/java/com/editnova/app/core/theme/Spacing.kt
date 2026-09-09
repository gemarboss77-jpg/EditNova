package com.editnova.app.core.theme

import androidx.compose.ui.unit.dp

/**
 * EditNovaSpacing — the single source of truth for spacing values across the app.
 *
 * Using a shared scale (instead of scattering "16.dp", "20.dp" etc. across screens)
 * keeps the UI visually consistent and makes it easy to rebalance spacing app-wide
 * later by changing values in one place.
 */
object EditNovaSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp

    /** Standard horizontal screen padding used by every top-level screen. */
    val screenHorizontal = 20.dp
}
