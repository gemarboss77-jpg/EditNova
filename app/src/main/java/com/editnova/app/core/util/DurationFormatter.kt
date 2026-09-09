package com.editnova.app.core.util

import java.util.Locale

/**
 * Formats a duration given in milliseconds as "m:ss" (or "h:mm:ss" once it's over an
 * hour). Centralized here so every screen that shows a duration — project cards, the
 * Editor's playback controls, the timeline placeholder — formats it identically
 * instead of each screen writing its own formatting logic.
 */
fun formatDurationMs(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
