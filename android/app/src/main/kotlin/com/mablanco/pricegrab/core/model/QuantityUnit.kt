package com.mablanco.pricegrab.core.model

import java.math.BigDecimal

/**
 * One selectable unit per offer quantity field. Maps to a [Dimension] and
 * a base-unit multiplier for normalizing shopper-entered quantities.
 */
enum class QuantityUnit {
    Gram,
    Kilogram,
    Millilitre,
    Litre,
    Centimetre,
    Metre,
    Piece,
    ;

    val dimension: Dimension
        get() = when (this) {
            Gram, Kilogram -> Dimension.Mass
            Millilitre, Litre -> Dimension.Volume
            Centimetre, Metre -> Dimension.Length
            Piece -> Dimension.Count
        }

    /** Multiply the shopper-entered quantity to express it in base units. */
    val toBaseMultiplier: BigDecimal
        get() = when (this) {
            Gram, Millilitre, Centimetre, Piece -> BigDecimal.ONE
            Metre -> BigDecimal("100")
            Kilogram, Litre -> BigDecimal("1000")
        }

    companion object {
        val DEFAULT: QuantityUnit = Gram
    }
}
