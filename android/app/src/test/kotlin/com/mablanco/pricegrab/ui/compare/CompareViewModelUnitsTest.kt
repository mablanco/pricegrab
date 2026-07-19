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
}
