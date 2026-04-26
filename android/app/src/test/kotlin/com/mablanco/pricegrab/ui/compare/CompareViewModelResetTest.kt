package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * US1 (T007) — JVM unit tests for [CompareViewModel.resetComparison].
 *
 * Pinned behaviour:
 *  (a) reset on a non-empty form clears all four `*Raw` fields and
 *      the cached outcome;
 *  (b) reset on an already-empty form is a no-op (no `UndoState`,
 *      no state change);
 *  (c) reset on a non-empty form populates `undoState` with a
 *      [PreResetSnapshot] containing the *pre-reset* values, and the
 *      deadline is in the future.
 *  (d) reset followed by typing into any field clears `undoState`
 *      (FR-008.1) — the typing branch of "dismiss undo on input".
 */
class CompareViewModelResetTest {

    @Test
    fun resetOnNonEmptyFormClearsAllFieldsAndOutcome() {
        val viewModel = CompareViewModel(SavedStateHandle()).withFullExample()

        // Sanity: the example produces a real outcome and reset is enabled.
        assertNotNull(viewModel.state.value.outcome)
        assertTrue(viewModel.state.value.isResetEnabled)

        viewModel.resetComparison()

        val state = viewModel.state.value
        assertEquals("", state.priceARaw)
        assertEquals("", state.quantityARaw)
        assertEquals("", state.priceBRaw)
        assertEquals("", state.quantityBRaw)
        assertNull("Result is hidden after reset", state.outcome)
        assertFalse(
            "Reset becomes disabled again on an empty form",
            state.isResetEnabled,
        )
    }

    @Test
    fun resetOnAlreadyEmptyFormIsANoOpAndPublishesNoUndoState() {
        val viewModel = CompareViewModel(SavedStateHandle())
        // Sanity: the form is empty and the button is disabled.
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
        val before = System.currentTimeMillis()

        viewModel.resetComparison()

        val undo = viewModel.state.value.undoState
        assertNotNull("UndoState is published after a non-empty reset", undo)
        requireNotNull(undo)
        assertEquals("2.50", undo.snapshot.priceARaw)
        assertEquals("500", undo.snapshot.quantityARaw)
        assertEquals("4.00", undo.snapshot.priceBRaw)
        assertEquals("1000", undo.snapshot.quantityBRaw)
        assertTrue(
            "Undo deadline is strictly in the future",
            undo.expiresAtEpochMillis > before,
        )
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
