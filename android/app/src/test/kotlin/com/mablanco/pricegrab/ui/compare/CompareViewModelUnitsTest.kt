package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.QuantityUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CompareViewModelUnitsTest {

    @get:Rule
    val localeRule = EnUsLocaleRule()

    @Test
    fun incompatibleUnitsWhenMassVsVolume() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceChange(0, "2.50")
        viewModel.onQuantityChange(0, "500")
        viewModel.onPriceChange(1, "1.20")
        viewModel.onQuantityChange(1, "500")
        viewModel.onUnitChange(1, QuantityUnit.Millilitre)

        val state = viewModel.state.value
        assertTrue(state.incompatibleUnits)
        assertNull(state.outcome)
    }

    @Test
    fun readyWhenKilogramVsGram() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceChange(0, "2.50")
        viewModel.onQuantityChange(0, "500")
        viewModel.onPriceChange(1, "4.00")
        viewModel.onQuantityChange(1, "1")
        viewModel.onUnitChange(1, QuantityUnit.Kilogram)

        val state = viewModel.state.value
        assertFalse(state.incompatibleUnits)
        assertNotNull(state.outcome)
        assertTrue(state.outcome is ComparisonOutcome.Winner)
        assertEquals(1, (state.outcome as ComparisonOutcome.Winner).slotIndex)
    }

    @Test
    fun incompleteWhenFieldsBlank() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onUnitChange(1, QuantityUnit.Kilogram)

        val state = viewModel.state.value
        assertFalse(state.incompatibleUnits)
        assertNull(state.outcome)
    }

    @Test
    fun blankThirdOfferIsIgnoredWhenAAndBParse() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceChange(0, "2.50")
        viewModel.onQuantityChange(0, "500")
        viewModel.onPriceChange(1, "4.00")
        viewModel.onQuantityChange(1, "1000")
        viewModel.addOffer()

        val state = viewModel.state.value
        assertEquals(3, state.offers.size)
        assertEquals("", state.offers[2].priceRaw)
        assertFalse(state.incompatibleUnits)
        assertTrue(state.outcome is ComparisonOutcome.Winner)
        assertEquals(1, (state.outcome as ComparisonOutcome.Winner).slotIndex)
    }

    @Test
    fun thirdOfferWithConflictingDimensionIsIncompatible() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceChange(0, "2.50")
        viewModel.onQuantityChange(0, "500")
        viewModel.onPriceChange(1, "4.00")
        viewModel.onQuantityChange(1, "1000")
        viewModel.addOffer()
        viewModel.onPriceChange(2, "1.00")
        viewModel.onQuantityChange(2, "500")
        viewModel.onUnitChange(2, QuantityUnit.Millilitre)

        val state = viewModel.state.value
        assertTrue(state.incompatibleUnits)
        assertNull(state.outcome)
    }

    @Test
    fun threeOffersRanksCheapestVsSecond() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceChange(0, "2.50")
        viewModel.onQuantityChange(0, "500") // 0.005
        viewModel.onPriceChange(1, "4.00")
        viewModel.onQuantityChange(1, "1000") // 0.004
        viewModel.addOffer()
        viewModel.onPriceChange(2, "3.00")
        viewModel.onQuantityChange(2, "500") // 0.006

        val state = viewModel.state.value
        val winner = state.outcome as ComparisonOutcome.Winner
        assertEquals(1, winner.slotIndex)
        assertEquals(0, java.math.BigDecimal("0.001").compareTo(winner.perUnitDelta))
        assertEquals(0, java.math.BigDecimal("20").compareTo(winner.percentDelta))
    }

    // ---- Feature 009: length units -------------------------------------------

    @Test
    fun readyWhenMetreVsCentimetre() {
        val viewModel = CompareViewModel(SavedStateHandle()).withLengthExample()

        val state = viewModel.state.value
        assertFalse(state.incompatibleUnits)
        val winner = state.outcome as ComparisonOutcome.Winner
        assertEquals(1, winner.slotIndex)
        assertEquals(0, java.math.BigDecimal("0.01").compareTo(winner.perUnitDelta))
    }

    @Test
    fun incompatibleUnitsWhenLengthVsAnyOtherDimension() {
        listOf(QuantityUnit.Gram, QuantityUnit.Kilogram, QuantityUnit.Litre, QuantityUnit.Piece)
            .forEach { other ->
                val viewModel = CompareViewModel(SavedStateHandle()).withLengthExample()
                viewModel.onUnitChange(1, other)

                val state = viewModel.state.value
                assertTrue("Metre vs $other", state.incompatibleUnits)
                assertNull("Metre vs $other", state.outcome)
            }
    }

    @Test
    fun resetReturnsLengthUnitsToGramAndUndoRestoresThem() {
        val viewModel = CompareViewModel(SavedStateHandle()).withLengthExample()

        viewModel.resetComparison()
        assertEquals(QuantityUnit.Gram, viewModel.state.value.offers[0].quantityUnit)
        assertEquals(QuantityUnit.Gram, viewModel.state.value.offers[1].quantityUnit)

        viewModel.undoReset()
        assertEquals(QuantityUnit.Metre, viewModel.state.value.offers[0].quantityUnit)
        assertEquals(QuantityUnit.Centimetre, viewModel.state.value.offers[1].quantityUnit)
    }

    @Test
    fun savedLengthUnitIsRestoredAfterProcessDeath() {
        val handle = SavedStateHandle()
        CompareViewModel(handle).withLengthExample()

        val restored = CompareViewModel(handle).state.value
        assertEquals(QuantityUnit.Metre, restored.offers[0].quantityUnit)
        assertEquals(QuantityUnit.Centimetre, restored.offers[1].quantityUnit)
        assertTrue(restored.outcome is ComparisonOutcome.Winner)
    }

    /** Issue #56: 5 m @ 49 vs 250 cm @ 22 → B wins (8.80/m vs 9.80/m). */
    private fun CompareViewModel.withLengthExample(): CompareViewModel = apply {
        onPriceChange(0, "49")
        onQuantityChange(0, "5")
        onUnitChange(0, QuantityUnit.Metre)
        onPriceChange(1, "22")
        onQuantityChange(1, "250")
        onUnitChange(1, QuantityUnit.Centimetre)
    }
}
