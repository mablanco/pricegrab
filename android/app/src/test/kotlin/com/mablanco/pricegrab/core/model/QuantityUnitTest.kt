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
    fun pieceMapsToCountWithBaseMultiplierOne() {
        assertEquals(Dimension.Count, QuantityUnit.Piece.dimension)
        assertEquals(0, BigDecimal.ONE.compareTo(QuantityUnit.Piece.toBaseMultiplier))
    }

    @Test
    fun defaultUnitIsGram() {
        assertEquals(QuantityUnit.Gram, QuantityUnit.DEFAULT)
    }
}
