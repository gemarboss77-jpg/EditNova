package com.editnova.app.core.navigation

import android.net.Uri
import com.editnova.app.domain.media.SelectedMedia

/**
 * Every destination in the app gets one entry here instead of raw string literals
 * scattered across the codebase. This is a small thing now, but it prevents typo bugs
 * ("hom" vs "home") once we have 15+ screens (editor, export, paywall, AI tools, etc).
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Premium : Screen("premium")

    /**
     * Editor Preview screen (Step 3). The selected media is passed as two route
     * arguments — a media type and a URL-encoded content URI — rather than as an
     * object, since Navigation Compose routes are plain strings. Use [buildRoute]
     * to construct a concrete, navigable route for a given [SelectedMedia].
     */
    data object Editor : Screen("editor/{mediaType}/{encodedUri}") {
        fun buildRoute(media: SelectedMedia): String =
            "editor/${media.type.name}/${Uri.encode(media.uri.toString())}"
    }
}

