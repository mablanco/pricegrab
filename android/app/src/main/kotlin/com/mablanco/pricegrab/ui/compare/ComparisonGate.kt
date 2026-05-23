package com.mablanco.pricegrab.ui.compare

import com.mablanco.pricegrab.core.calc.PriceComparator
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.OfferParseResult

/**
 * Explicit gate before invoking [PriceComparator.compare]. Keeps cross-dimension
 * rejection out of the pure calculator and gives tests a named concept.
 */
sealed interface ComparisonGate {
    /** Both offers parsed; dimensions match — comparator has run. */
    data class Ready(val outcome: ComparisonOutcome) : ComparisonGate

    /** Both offers parsed; dimensions differ — show error, no winner. */
    data object IncompatibleUnits : ComparisonGate

    /** One or both offers invalid or incomplete — neutral placeholder. */
    data object Incomplete : ComparisonGate
}

fun evaluateComparison(
    aResult: OfferParseResult,
    bResult: OfferParseResult,
): ComparisonGate {
    if (aResult !is OfferParseResult.Success || bResult !is OfferParseResult.Success) {
        return ComparisonGate.Incomplete
    }
    val a = aResult.offer
    val b = bResult.offer
    if (a.quantityUnit.dimension != b.quantityUnit.dimension) {
        return ComparisonGate.IncompatibleUnits
    }
    return ComparisonGate.Ready(PriceComparator.compare(a, b))
}
