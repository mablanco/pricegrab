package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.QuantityUnit
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
        viewModel.onPriceAChange("2.50")
        viewModel.onQuantityAChange("500")
        viewModel.onPriceBChange("1.20")
        viewModel.onQuantityBChange("500")
        viewModel.onQuantityUnitBChange(QuantityUnit.Millilitre)

        val state = viewModel.state.value
        assertTrue(state.incompatibleUnits)
        assertNull(state.outcome)
    }

    @Test
    fun readyWhenKilogramVsGram() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceAChange("2.50")
        viewModel.onQuantityAChange("500")
        viewModel.onPriceBChange("4.00")
        viewModel.onQuantityBChange("1")
        viewModel.onQuantityUnitBChange(QuantityUnit.Kilogram)

        val state = viewModel.state.value
        assertFalse(state.incompatibleUnits)
        assertNotNull(state.outcome)
        assertTrue(state.outcome is ComparisonOutcome.BWins)
    }

    @Test
    fun incompleteWhenFieldsBlank() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onQuantityUnitBChange(QuantityUnit.Kilogram)

        val state = viewModel.state.value
        assertFalse(state.incompatibleUnits)
        assertNull(state.outcome)
    }
}
