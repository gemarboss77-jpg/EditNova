package com.editnova.app.core.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.editnova.app.domain.media.MediaType
import com.editnova.app.domain.media.SelectedMedia
import com.editnova.app.feature.editor.EditorPreviewScreen
import com.editnova.app.feature.home.HomeScreen
import com.editnova.app.feature.onboarding.OnboardingScreen
import com.editnova.app.feature.premium.PremiumScreen
import com.editnova.app.feature.splash.SplashScreen

/**
 * EditNovaNavGraph
 *
 * This is the single source of truth for "which screen can navigate to which".
 * Flow:
 *
 *   Splash  --(auto, after animation)-->  Onboarding  --(tap Get Started)-->  Home
 *   Home  --(tap New Project, pick media)-->  Editor Preview
 *
 * Home itself hosts 5 bottom-nav tabs (Home/Projects/AI Tools/Templates/Settings) as
 * local state — see feature/home/HomeScreen.kt — rather than as separate routes here.
 * The Premium upgrade screen and the Editor Preview screen ARE real routes.
 *
 * Each screen only knows about the callbacks it needs (e.g. Splash only knows
 * "onFinished", not that Onboarding exists) — this keeps screens reusable and
 * testable on their own, and keeps navigation changes contained to this one file.
 */
@Composable
fun EditNovaNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.Onboarding.route) {
                        // Remove Splash from the back stack so the user can't navigate
                        // "back" into it with the system back button.
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onOpenPremium = { navController.navigate(Screen.Premium.route) },
                onOpenEditor = { media -> navController.navigate(Screen.Editor.buildRoute(media)) }
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("mediaType") { type = NavType.StringType },
                navArgument("encodedUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Parsed defensively: a malformed/missing argument (should not normally
            // happen, since we always build this route via Screen.Editor.buildRoute)
            // results in null media rather than a crash — EditorPreviewScreen shows
            // an honest "No media selected" state for that case.
            val media = runCatching {
                val mediaTypeArg = backStackEntry.arguments?.getString("mediaType") ?: return@runCatching null
                val encodedUri = backStackEntry.arguments?.getString("encodedUri") ?: return@runCatching null
                SelectedMedia(
                    uri = Uri.parse(Uri.decode(encodedUri)),
                    type = MediaType.valueOf(mediaTypeArg)
                )
            }.getOrNull()

            EditorPreviewScreen(
                media = media,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
