package com.editnova.app.data.subscription

import com.editnova.app.domain.subscription.SubscriptionRepository
import com.editnova.app.domain.subscription.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * FakeSubscriptionRepository — Step 1 placeholder implementation.
 *
 * Every user is treated as being on the FREE plan. There is no persistence, no server
 * calls, and no real billing here — this class exists ONLY so the rest of the app
 * (like [com.editnova.app.domain.watermark.WatermarkPolicy]) has something real to
 * depend on while we build out the UI, instead of leaving those parts unbuildable.
 *
 * This will be REPLACED (not extended) by a real implementation backed by
 * Google Play Billing + Firebase in a future step.
 */
class FakeSubscriptionRepository : SubscriptionRepository {
    override val subscriptionState: StateFlow<SubscriptionState> =
        MutableStateFlow(SubscriptionState()) // Defaults to FREE / NONE
}
