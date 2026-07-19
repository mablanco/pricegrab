package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import com.mablanco.pricegrab.core.model.QuantityUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CompareViewModelResetTest {

    @get:Rule
    val localeRule = EnUsLocaleRule()

    @Test
    fun resetOnNonEmptyFormClearsAllFieldsAndOutcome() {
        val viewModel = CompareViewModel(SavedStateHandle()).withFullExample()

        assertNotNull(viewModel.state.value.outcome)
        assertTrue(viewModel.state.value.isResetEnabled)

        viewModel.resetComparison()

        val state = viewModel.state.value
        assertEquals(2, state.offers.size)
        assertEquals("", state.offers[0].priceRaw)
        assertEquals("", state.offers[0].quantityRaw)
        assertEquals("", state.offers[1].priceRaw)
        assertEquals("", state.offers[1].quantityRaw)
        assertEquals(QuantityUnit.Gram, state.offers[0].quantityUnit)
        assertEquals(QuantityUnit.Gram, state.offers[1].quantityUnit)
        assertNull("Result is hidden after reset", state.outcome)
        assertFalse(
            "Reset becomes disabled again on an empty form",
            state.isResetEnabled,
        )
    }

    @Test
    fun resetOnAlreadyEmptyFormIsANoOpAndPublishesNoUndoState() {
        val viewModel = CompareViewModel(SavedStateHandle())
        assertFalse(viewModel.state.value.isResetEnabled)
        assertNull(viewModel.state.value.undoState)

        viewModel.resetComparison()

        val state = viewModel.state.value
        assertNull("No UndoState is created from an empty reset", state.undoState)
        assertFalse(state.isResetEnabled)
    }

    @Test
    fun resetOnNonEmptyFormCapturesPreResetSnapshotWithFutureDeadline() {
        val viewModel = CompareViewModel(SavedStateHandle()).withFullExample()
        viewModel.onUnitChange(0, QuantityUnit.Kilogram)
        val before = System.currentTimeMillis()

        viewModel.resetComparison()

        val undo = viewModel.state.value.undoState
        assertNotNull("UndoState is published after a non-empty reset", undo)
        requireNotNull(undo)
        assertEquals(2, undo.snapshot.slots.size)
        assertEquals("2.50", undo.snapshot.slots[0].priceRaw)
        assertEquals("500", undo.snapshot.slots[0].quantityRaw)
        assertEquals("4.00", undo.snapshot.slots[1].priceRaw)
        assertEquals("1000", undo.snapshot.slots[1].quantityRaw)
        assertEquals(QuantityUnit.Kilogram, undo.snapshot.slots[0].quantityUnit)
        assertEquals(QuantityUnit.Gram, undo.snapshot.slots[1].quantityUnit)
        assertTrue(
            "Undo deadline is strictly in the future",
            undo.expiresAtEpochMillis > before,
        )
    }

    @Test
    fun undoResetRestoresQuantityUnits() {
        val viewModel = CompareViewModel(SavedStateHandle()).withFullExample()
        viewModel.onUnitChange(0, QuantityUnit.Kilogram)
        viewModel.onUnitChange(1, QuantityUnit.Kilogram)

        viewModel.resetComparison()
        viewModel.undoReset()

        val state = viewModel.state.value
        assertEquals(QuantityUnit.Kilogram, state.offers[0].quantityUnit)
        assertEquals(QuantityUnit.Kilogram, state.offers[1].quantityUnit)
    }

    @Test
    fun typingIntoAnyFieldAfterResetDismissesUndoState() {
        val viewModel = CompareViewModel(SavedStateHandle()).withFullExample()
        viewModel.resetComparison()
        assertNotNull(viewModel.state.value.undoState)

        viewModel.onPriceChange(0, "3.00")

        assertNull(
            "Typing into Price A dismisses the active UndoState",
            viewModel.state.value.undoState,
        )
        assertEquals("3.00", viewModel.state.value.offers[0].priceRaw)
    }

    @Test
    fun resetWithThreeOffersReturnsToTwoEmptySlots() {
        val viewModel = CompareViewModel(SavedStateHandle()).withThreeOffers()
        assertEquals(3, viewModel.state.value.offers.size)
        assertTrue(viewModel.state.value.isResetEnabled)

        viewModel.resetComparison()

        val state = viewModel.state.value
        assertEquals(2, state.offers.size)
        assertTrue(state.offers.all { it.priceRaw.isEmpty() && it.quantityRaw.isEmpty() })
        assertEquals(3, state.undoState?.snapshot?.slots?.size)
    }

    @Test
    fun threeOfferSlotAloneEnablesReset() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.addOffer()
        assertEquals(3, viewModel.state.value.offers.size)
        assertTrue(viewModel.state.value.isResetEnabled)
    }

    private fun CompareViewModel.withFullExample(): CompareViewModel = apply {
        onPriceChange(0, "2.50")
        onQuantityChange(0, "500")
        onPriceChange(1, "4.00")
        onQuantityChange(1, "1000")
    }

    private fun CompareViewModel.withThreeOffers(): CompareViewModel = apply {
        withFullExample()
        addOffer()
        onPriceChange(2, "3.00")
        onQuantityChange(2, "750")
    }
}
