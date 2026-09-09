package com.editnova.app.core.theme

import androidx.compose.ui.graphics.Color

/**
 * EditNova's color palette.
 *
 * Design direction: dark-first, "professional editing suite" look (similar in spirit to
 * tools like CapCut Pro / Premiere Pro) — near-black backgrounds, soft elevated surfaces,
 * and a violet -> cyan gradient accent so the brand feels modern and "AI-powered".
 *
 * Keep ALL raw color values here. Screens and components should reference
 * MaterialTheme.colorScheme (see Theme.kt) or the EditNovaGradient below —
 * never hardcode a hex value inside a screen file.
 */

// Backgrounds
val BackgroundPrimary = Color(0xFF0B0B12)   // App background, near-black with a hint of blue
val SurfaceElevated = Color(0xFF16161F)     // Cards, sheets, nav bars
val SurfaceElevatedHigh = Color(0xFF1F1F2C) // Hovered / pressed surfaces, dialogs

// Brand accent gradient (used for primary actions, highlights, logo mark)
val AccentVioletStart = Color(0xFF7C4DFF)
val AccentCyanEnd = Color(0xFF00E5FF)

// Text
val TextPrimary = Color(0xFFF5F5FA)
val TextSecondary = Color(0xFFA3A3B2)
val TextDisabled = Color(0xFF5C5C6B)

// Semantic
val SuccessGreen = Color(0xFF2ECC71)
val WarningAmber = Color(0xFFFFB020)
val ErrorRed = Color(0xFFFF5470)

// Premium plan accent (used later for "Premium" badges / paywall UI)
val PremiumGold = Color(0xFFFFC670)
