package com.mablanco.pricegrab.core.model

import java.math.BigDecimal
import java.math.MathContext

/**
 * A single side of a price comparison (A or B). Immutable.
 *
 * Invariants, enforced at construction time:
 * - `price` must be `>= 0` (a zero-price offer is legal: a free product).
 * - `quantity` must be `> 0` (zero or negative quantity is meaningless).
 *
 * Callers that receive raw user input should go through
 * [com.mablanco.pricegrab.core.format.OfferParser] instead of constructing an
 * `Offer` directly, so that parse / validation errors can be rendered to the
 * user without throwing.
 */
data class Offer(
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quantityUnit: QuantityUnit = QuantityUnit.Gram,
) {
    init {
        require(price.signum() >= 0) {
            "price must be >= 0, was $price"
        }
        require(quantity.signum() > 0) {
            "quantity must be > 0, was $quantity"
        }
    }

    /** Shopper quantity normalized to base units (g, ml, or pcs). */
    val quantityInBaseUnits: BigDecimal = quantity.multiply(quantityUnit.toBaseMultiplier)

    /**
     * Price per base unit, at `MathContext.DECIMAL64` precision (16 significant
     * decimal digits). Deterministic and pure.
     */
    val unitPrice: BigDecimal = price.divide(quantityInBaseUnits, MathContext.DECIMAL64)
}
