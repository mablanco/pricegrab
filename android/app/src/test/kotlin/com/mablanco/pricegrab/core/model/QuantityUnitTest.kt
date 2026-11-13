package com.mablanco.pricegrab.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class QuantityUnitTest {

    @Test
    fun gramMapsToMassWithBaseMultiplierOne() {
        assertEquals(Dimension.Mass, QuantityUnit.Gram.dimension)
        assertEquals(0, BigDecimal.ONE.compareTo(QuantityUnit.Gram.toBaseMultiplier))
    }

    @Test
    fun kilogramMapsToMassWithBaseMultiplierOneThousand() {
        assertEquals(Dimension.Mass, QuantityUnit.Kilogram.dimension)
        assertEquals(0, BigDecimal("1000").compareTo(QuantityUnit.Kilogram.toBaseMultiplier))
    }

    @Test
    fun millilitreMapsToVolumeWithBaseMultiplierOne() {
        assertEquals(Dimension.Volume, QuantityUnit.Millilitre.dimension)
        assertEquals(0, BigDecimal.ONE.compareTo(QuantityUnit.Millilitre.toBaseMultiplier))
    }

    @Test
    fun litreMapsToVolumeWithBaseMultiplierOneThousand() {
        assertEquals(Dimension.Volume, QuantityUnit.Litre.dimension)
        assertEquals(0, BigDecimal("1000").compareTo(QuantityUnit.Litre.toBaseMultiplier))
    }

    @Test
    fun centimetreMapsToLengthWithBaseMultiplierOne() {
        assertEquals(Dimension.Length, QuantityUnit.Centimetre.dimension)
        assertEquals(0, BigDecimal.ONE.compareTo(QuantityUnit.Centimetre.toBaseMultiplier))
    }

    @Test
    fun metreMapsToLengthWithBaseMultiplierOneHundred() {
        assertEquals(Dimension.Length, QuantityUnit.Metre.dimension)
        assertEquals(0, BigDecimal("100").compareTo(QuantityUnit.Metre.toBaseMultiplier))
    }

    @Test
    fun lengthIsDisplayedPerMetre() {
        assertEquals(0, BigDecimal("100").compareTo(Dimension.Length.displayUnitScale))
    }

    @Test
    fun pieceMapsToCountWithBaseMultiplierOne() {
        assertEquals(Dimension.Count, QuantityUnit.Piece.dimension)
        assertEquals(0, BigDecimal.ONE.compareTo(QuantityUnit.Piece.toBaseMultiplier))
    }

    @Test
    fun selectorOrderGroupsPhysicalMeasuresBeforePieces() {
        assertEquals(
            listOf("Gram", "Kilogram", "Millilitre", "Litre", "Centimetre", "Metre", "Piece"),
            QuantityUnit.entries.map { it.name },
        )
    }

    @Test
    fun defaultUnitIsGram() {
        assertEquals(QuantityUnit.Gram, QuantityUnit.DEFAULT)
    }
}
