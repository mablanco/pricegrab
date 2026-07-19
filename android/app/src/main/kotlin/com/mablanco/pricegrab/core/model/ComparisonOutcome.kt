package com.mablanco.pricegrab.core.model

import java.math.BigDecimal

/**
 * Result of comparing two or more [Offer]s by unit price.
 *
 * - [Tie] — two or more offers share the absolute minimum unit price.
 * - [Winner] — a unique cheapest offer; deltas are versus the second-cheapest.
 *
 * `perUnitDelta` is always `>= 0` and equals
 * `secondCheapest.unitPrice - winner.unitPrice`.
 * `percentDelta` is the savings against the second-cheapest offer, in the
 * range `[0, 100]`. When the winner is free and the second is not,
 * `percentDelta == 100`.
 *
 * [Winner.slotIndex] is the UI index of the winning offer in the full
 * compare list (not the index among only-parsed offers).
 */
sealed interface ComparisonOutcome {

    data object Tie : ComparisonOutcome

    data class Winner(
        val slotIndex: Int,
        /** UI index of the second-cheapest offer (runner-up for savings copy). */
        val secondSlotIndex: Int,
        val perUnitDelta: BigDecimal,
        val percentDelta: BigDecimal,
    ) : ComparisonOutcome
}
