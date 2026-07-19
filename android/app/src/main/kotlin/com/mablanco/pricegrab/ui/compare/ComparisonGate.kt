package com.mablanco.pricegrab.ui.compare

import com.mablanco.pricegrab.core.calc.PriceComparator
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.OfferParseResult

/**
 * Explicit gate before invoking [PriceComparator.compareMany]. Keeps
 * cross-dimension rejection out of the pure calculator and gives tests a
 * named concept.
 */
sealed interface ComparisonGate {
    /** At least two offers parsed; dimensions match — comparator has run. */
    data class Ready(val outcome: ComparisonOutcome) : ComparisonGate

    /** At least two offers parsed; dimensions differ — show error, no winner. */
    data object IncompatibleUnits : ComparisonGate

    /** Fewer than two offers valid — neutral placeholder. */
    data object Incomplete : ComparisonGate
}

/**
 * Evaluates parse results parallel to UI slot indices.
 *
 * Blank / failed slots are ignored when ≥2 others succeed. Any dimension
 * mismatch among successes yields [ComparisonGate.IncompatibleUnits].
 */
fun evaluateComparison(results: List<OfferParseResult>): ComparisonGate {
    val successes = results.mapIndexedNotNull { index, result ->
        (result as? OfferParseResult.Success)?.let { index to it.offer }
    }
    if (successes.size < 2) return ComparisonGate.Incomplete

    val dimension = successes.first().second.quantityUnit.dimension
    if (successes.any { it.second.quantityUnit.dimension != dimension }) {
        return ComparisonGate.IncompatibleUnits
    }
    return ComparisonGate.Ready(PriceComparator.compareMany(successes))
}
