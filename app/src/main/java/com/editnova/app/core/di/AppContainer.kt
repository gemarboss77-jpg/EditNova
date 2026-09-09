package com.editnova.app.core.di

import com.editnova.app.data.subscription.FakeSubscriptionRepository
import com.editnova.app.domain.subscription.SubscriptionRepository
import com.editnova.app.domain.watermark.WatermarkPolicy

/**
 * AppContainer — a simple, manual dependency injection container.
 *
 * WHY MANUAL DI (instead of Hilt/Koin) FOR STEP 1?
 * Frameworks like Hilt are great for large apps, but they add annotation-processing
 * build complexity that's easy to misconfigure for a first setup. A plain object graph
 * like this one is explicit, beginner-friendly, and easy to understand — and it can be
 * swapped for Hilt later without changing how screens *use* their dependencies, because
 * screens depend on interfaces (like [SubscriptionRepository]), not concrete classes.
 *
 * HOW IT'S USED:
 * ViewModels/screens read what they need from here. In Step 1 nothing consumes this yet
 * (Home/Splash/Onboarding have no real data needs), but it's wired up now so Step 2+
 * features (subscriptions, projects, AI tools) have a consistent place to get their
 * dependencies from on day one.
 */
object AppContainer {

    /** Single shared instance of the subscription repository (see domain/subscription/). */
    val subscriptionRepository: SubscriptionRepository by lazy {
        FakeSubscriptionRepository()
    }

    /** Decides watermark behavior based on the user's current plan. See domain/watermark/. */
    val watermarkPolicy: WatermarkPolicy by lazy {
        WatermarkPolicy(subscriptionRepository)
    }
}
