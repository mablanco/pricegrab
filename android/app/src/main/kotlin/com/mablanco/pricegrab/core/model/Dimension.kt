package com.mablanco.pricegrab.core.model

import java.math.BigDecimal

/**
 * The physical kind of measure for a [QuantityUnit]. Two offers are
 * comparable only when both units map to the same dimension.
 */
enum class Dimension(
    /** Multiply base-unit [ComparisonOutcome] `perUnitDelta` for shelf-style display. */
    val displayUnitScale: BigDecimal,
) {
    Mass(BigDecimal("1000")),
    Volume(BigDecimal("1000")),
    Count(BigDecimal.ONE),
}
