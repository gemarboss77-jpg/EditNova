package com.editnova.app

import android.app.Application

/**
 * EditNovaApplication
 *
 * This runs once when the app process starts, before any screen is shown.
 * It's the right place for app-wide setup that every screen depends on.
 *
 * WHAT LIVES HERE IN STEP 1:
 * - Nothing yet — this is intentionally minimal.
 *
 * WHAT WILL LIVE HERE LATER:
 * - Firebase.initializeApp(this)              (when we add accounts)
 * - Crash reporting / analytics setup
 * - AppContainer initialization (see core/di/AppContainer.kt) if it grows
 *   beyond simple object creation.
 */
class EditNovaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Intentionally empty in Step 1.
        // Example of what a future line here looks like:
        // com.google.firebase.FirebaseApp.initializeApp(this)
    }
}
