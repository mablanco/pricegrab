package com.mablanco.pricegrab.core.calc

import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.Offer
import java.math.BigDecimal
import java.math.MathContext

/**
 * Compares two [Offer]s by their per-unit price and describes the outcome.
 *
 * Pure function: no I/O, no clock, no RNG, no mutation. Thread-safe; may be
 * invoked from any thread.
 *
 * Preconditions (also enforced by [Offer]'s constructor):
 * - `a.price >= 0 && a.quantity > 0`
 * - `b.price >= 0 && b.quantity > 0`
 *
 * Postconditions:
 * - Returns [ComparisonOutcome.Tie] iff `a.unitPrice == b.unitPrice`
 *   (value-based, via [BigDecimal.compareTo]).
 * - Returns [ComparisonOutcome.AWins] iff `a.unitPrice < b.unitPrice`.
 * - Returns [ComparisonOutcome.BWins] iff `a.unitPrice > b.unitPrice`.
 * - `perUnitDelta = |a.unitPrice - b.unitPrice|`, always `>= 0`.
 * - `percentDelta = perUnitDelta / max(a.unitPrice, b.unitPrice) * 100`.
 *   When exactly one `unitPrice` is zero, the zero side wins with
 *   `percentDelta == 100`.
 */
object PriceComparator {

    private val HUNDRED: BigDecimal = BigDecimal.valueOf(100)

    fun compare(a: Offer, b: Offer): ComparisonOutcome {
        val unitA = a.unitPrice
        val unitB = b.unitPrice

        return when {
            unitA.compareTo(unitB) == 0 -> ComparisonOutcome.Tie
            unitA < unitB -> winner(loser = unitB, winner = unitA) { d, p ->
                ComparisonOutcome.AWins(d, p)
            }
            else -> winner(loser = unitA, winner = unitB) { d, p ->
                ComparisonOutcome.BWins(d, p)
            }
        }
    }

    private inline fun <T : ComparisonOutcome> winner(
        loser: BigDecimal,
        winner: BigDecimal,
        factory: (perUnitDelta: BigDecimal, percentDelta: BigDecimal) -> T,
    ): T {
        val perUnitDelta = loser.subtract(winner).abs()
        // loser > winner >= 0, so loser is strictly positive; division is safe.
        val percentDelta = perUnitDelta
            .divide(loser, MathContext.DECIMAL64)
            .multiply(HUNDRED)
        return factory(perUnitDelta, percentDelta)
    }
}
