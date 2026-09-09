package com.editnova.app.domain.subscription

/**
 * PlanType — the two tiers EditNova will offer.
 *
 * NOTE: No billing/payment code exists yet. This is purely the data model so the
 * rest of the app (watermark logic, future paywall UI, feature gating) can be written
 * against a stable shape now, instead of being rewritten when billing is added.
 */
enum class PlanType {
    FREE,
    PREMIUM
}

/** How often a Premium subscription renews. Irrelevant for FREE plans. */
enum class BillingPeriod {
    MONTHLY,
    YEARLY,
    NONE // Used for the Free plan, which has no billing period
}

/**
 * The user's current subscription status.
 * This is intentionally simple in Step 1 — no dates, no payment provider IDs yet.
 * Those will be added once Google Play Billing is integrated.
 */
data class SubscriptionState(
    val plan: PlanType = PlanType.FREE,
    val billingPeriod: BillingPeriod = BillingPeriod.NONE
) {
    val isPremium: Boolean get() = plan == PlanType.PREMIUM
}
