package com.mablanco.pricegrab.core.model

import java.math.BigDecimal

/**
 * Result of comparing two [Offer]s by unit price.
 *
 * - [Tie] — both offers have the same unit price (value-based comparison).
 * - [AWins] — offer A is strictly cheaper per unit than offer B.
 * - [BWins] — offer B is strictly cheaper per unit than offer A.
 *
 * `perUnitDelta` is always `>= 0` and equals `|a.unitPrice - b.unitPrice|`.
 * `percentDelta` is the savings expressed against the more expensive offer
 * (the loser), so `B is 20% cheaper` means a 20% reduction relative to A's
 * unit price when A is the loser. `percentDelta` is in the range `[0, 100]`.
 */
sealed interface ComparisonOutcome {

    data object Tie : ComparisonOutcome

    data class AWins(
        val perUnitDelta: BigDecimal,
        val percentDelta: BigDecimal,
    ) : ComparisonOutcome

    data class BWins(
        val perUnitDelta: BigDecimal,
        val percentDelta: BigDecimal,
    ) : ComparisonOutcome
}
