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
        assertEquals("", state.priceARaw)
        assertEquals("", state.quantityARaw)
        assertEquals("", state.priceBRaw)
        assertEquals("", state.quantityBRaw)
        assertEquals(QuantityUnit.Gram, state.quantityUnitA)
        assertEquals(QuantityUnit.Gram, state.quantityUnitB)
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
        viewModel.onQuantityUnitAChange(QuantityUnit.Kilogram)
        val before = System.currentTimeMillis()

        viewModel.resetComparison()

        val undo = viewModel.state.value.undoState
        assertNotNull("UndoState is published after a non-empty reset", undo)
        requireNotNull(undo)
        assertEquals("2.50", undo.snapshot.priceARaw)
        assertEquals("500", undo.snapshot.quantityARaw)
        assertEquals("4.00", undo.snapshot.priceBRaw)
        assertEquals("1000", undo.snapshot.quantityBRaw)
        assertEquals(QuantityUnit.Kilogram, undo.snapshot.quantityUnitA)
        assertEquals(QuantityUnit.Gram, undo.snapshot.quantityUnitB)
        assertTrue(
            "Undo deadline is strictly in the future",
            undo.expiresAtEpochMillis > before,
        )
    }

    @Test
    fun undoResetRestoresQuantityUnits() {
        val viewModel = CompareViewModel(SavedStateHandle()).withFullExample()
        viewModel.onQuantityUnitAChange(QuantityUnit.Kilogram)
        viewModel.onQuantityUnitBChange(QuantityUnit.Kilogram)

        viewModel.resetComparison()
        viewModel.undoReset()

        val state = viewModel.state.value
        assertEquals(QuantityUnit.Kilogram, state.quantityUnitA)
        assertEquals(QuantityUnit.Kilogram, state.quantityUnitB)
    }

    @Test
    fun typingIntoAnyFieldAfterResetDismissesUndoState() {
        val viewModel = CompareViewModel(SavedStateHandle()).withFullExample()
        viewModel.resetComparison()
        assertNotNull(viewModel.state.value.undoState)

        viewModel.onPriceAChange("3.00")

        assertNull(
            "Typing into Price A dismisses the active UndoState",
            viewModel.state.value.undoState,
        )
        assertEquals("3.00", viewModel.state.value.priceARaw)
    }

    private fun CompareViewModel.withFullExample(): CompareViewModel = apply {
        onPriceAChange("2.50")
        onQuantityAChange("500")
        onPriceBChange("4.00")
        onQuantityBChange("1000")
    }
}
