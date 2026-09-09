package com.editnova.app.domain.subscription

import kotlinx.coroutines.flow.StateFlow

/**
 * SubscriptionRepository — the single source of truth for "what plan is the user on".
 *
 * This is an INTERFACE on purpose: every other part of the app (watermark logic,
 * export limits, paywall screens) will depend on this interface, not on how the
 * subscription data is actually fetched. That means when we integrate real Google
 * Play Billing + Firebase later, we only write a new implementation of this interface —
 * nothing that *uses* it has to change.
 *
 * Step 1 ships only [com.editnova.app.data.subscription.FakeSubscriptionRepository],
 * which always returns FREE. No real billing exists yet.
 */
interface SubscriptionRepository {
    /** Observable current subscription state. UI can collect this to react to plan changes. */
    val subscriptionState: StateFlow<SubscriptionState>
}
