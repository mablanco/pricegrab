package com.mablanco.pricegrab.core.calc

import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.Offer
import java.math.BigDecimal
import java.math.MathContext

/**
 * Compares [Offer]s by their per-unit price and describes the outcome.
 *
 * Pure function: no I/O, no clock, no RNG, no mutation. Thread-safe; may be
 * invoked from any thread.
 *
 * Postconditions for [compareMany]:
 * - Returns [ComparisonOutcome.Tie] when two or more offers share the
 *   absolute minimum `unitPrice`.
 * - Otherwise returns [ComparisonOutcome.Winner] for the unique cheapest
 *   offer, with deltas versus the second-cheapest `unitPrice`.
 * - `perUnitDelta = second.unitPrice - winner.unitPrice`, always `>= 0`.
 * - `percentDelta = perUnitDelta / second.unitPrice * 100`.
 *   When the winner is free and the second is not, `percentDelta == 100`.
 */
object PriceComparator {

    private val HUNDRED: BigDecimal = BigDecimal.valueOf(100)

    fun compare(a: Offer, b: Offer): ComparisonOutcome =
        compareMany(listOf(0 to a, 1 to b))

    /**
     * @param offers pairs of `(uiIndex, offer)`; size ≥ 2; all same [Dimension].
     */
    fun compareMany(offers: List<Pair<Int, Offer>>): ComparisonOutcome {
        require(offers.size >= 2) { "Need at least two offers to compare" }
        val dimension = offers.first().second.quantityUnit.dimension
        require(offers.all { it.second.quantityUnit.dimension == dimension }) {
            val mismatch = offers.first {
                it.second.quantityUnit.dimension != dimension
            }
            "Incompatible dimensions: $dimension vs ${mismatch.second.quantityUnit.dimension}"
        }

        val minUnitPrice = offers.minOf { it.second.unitPrice }
        val atMinimum = offers.filter { it.second.unitPrice.compareTo(minUnitPrice) == 0 }
        if (atMinimum.size >= 2) return ComparisonOutcome.Tie

        val (winnerIndex, _) = atMinimum.single()
        val (secondIndex, secondOffer) = offers
            .asSequence()
            .filter { it.first != winnerIndex }
            .minBy { it.second.unitPrice }

        return winner(loser = secondOffer.unitPrice, winner = minUnitPrice) { d, p ->
            ComparisonOutcome.Winner(winnerIndex, secondIndex, d, p)
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
