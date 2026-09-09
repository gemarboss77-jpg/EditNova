package com.editnova.app.domain.watermark

import com.editnova.app.domain.subscription.SubscriptionRepository

/**
 * WatermarkPolicy — decides WHETHER an exported video should carry the EditNova
 * watermark. This is ARCHITECTURE ONLY for Step 1.
 *
 * Rule (to be enforced once export actually exists):
 *   - FREE users    -> watermark IS applied
 *   - PREMIUM users -> watermark is NOT applied
 *
 * WHAT THIS CLASS DOES NOT DO YET:
 *   - It does not touch any video file.
 *   - It does not draw/render a watermark image onto anything.
 *   - There is no export pipeline calling this class yet.
 * That work belongs to a future "export" feature module. This class exists now so the
 * *decision logic* (free vs premium) is centralized and ready to be wired into the real
 * exporter later, instead of that decision being duplicated/hardcoded in UI code.
 */
class WatermarkPolicy(
    private val subscriptionRepository: SubscriptionRepository
) {
    /**
     * @return true if a watermark should be added to an exported video right now,
     * based on the user's current subscription plan.
     */
    fun shouldApplyWatermark(): Boolean {
        return !subscriptionRepository.subscriptionState.value.isPremium
    }
}
