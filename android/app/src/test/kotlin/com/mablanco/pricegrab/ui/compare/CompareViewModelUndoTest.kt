package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * US2 (T013, T014) — JVM unit tests for [CompareViewModel.undoReset]
 * and [CompareViewModel.dismissUndo], plus the SavedStateHandle round
 * trip that survives process death.
 */
class CompareViewModelUndoTest {

    @Test
    fun undoResetRestoresAllFourFieldsAndClearsUndoState() {
        val viewModel = CompareViewModel(SavedStateHandle()).withFullExample()
        viewModel.resetComparison()
        // Sanity: reset cleared the fields and started an Undo.
        assertEquals("", viewModel.state.value.priceARaw)
        assertNotNull(viewModel.state.value.undoState)

        viewModel.undoReset()

        val state = viewModel.state.value
        assertEquals("2.50", state.priceARaw)
        assertEquals("500", state.quantityARaw)
        assertEquals("4.00", state.priceBRaw)
        assertEquals("1000", state.quantityBRaw)
        assertNotNull("Outcome rebuilt deterministically from the snapshot", state.outcome)
        assertNull("UndoState is cleared after a successful undo", state.undoState)
    }

    @Test
    fun dismissUndoClearsUndoStateWithoutTouchingFields() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceAChange("9.99")
        // Reset on a non-empty form populates UndoState.
        viewModel.resetComparison()
        assertNotNull(viewModel.state.value.undoState)

        viewModel.dismissUndo()

        val state = viewModel.state.value
        assertNull("UndoState is cleared", state.undoState)
        assertEquals("Fields stay cleared (dismiss != restore)", "", state.priceARaw)
    }

    @Test
    fun undoResetIsNoOpWhenNoUndoStateActive() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceAChange("1.00")
        // No reset has happened, so UndoState is null.
        assertNull(viewModel.state.value.undoState)

        viewModel.undoReset()

        // State is unchanged.
        assertEquals("1.00", viewModel.state.value.priceARaw)
    }

    @Test
    fun savedStateHandleRoundTripRestoresActiveUndoStateAcrossViewModelRecreation() {
        val handle = SavedStateHandle()
        val first = CompareViewModel(handle).withFullExample()
        first.resetComparison()
        val originalDeadline = first.state.value.undoState?.expiresAtEpochMillis
        assertNotNull(originalDeadline)

        // Simulate process death: drop `first` on the floor and build a
        // fresh ViewModel from the same SavedStateHandle.
        val restored = CompareViewModel(handle)

        val undo = restored.state.value.undoState
        assertNotNull("UndoState is restored after process death", undo)
        requireNotNull(undo)
        assertEquals(originalDeadline, undo.expiresAtEpochMillis)
        assertEquals("2.50", undo.snapshot.priceARaw)
        assertEquals("500", undo.snapshot.quantityARaw)
        assertEquals("4.00", undo.snapshot.priceBRaw)
        assertEquals("1000", undo.snapshot.quantityBRaw)
        // The restored ViewModel sees the post-reset (cleared) fields.
        assertEquals("", restored.state.value.priceARaw)
        // Undo on the new instance still works.
        restored.undoReset()
        assertEquals("2.50", restored.state.value.priceARaw)
    }

    @Test
    fun savedStateHandleDropsExpiredUndoStateOnRestore() {
        val handle = SavedStateHandle().apply {
            set("undoPriceA", "2.50")
            set("undoQuantityA", "500")
            set("undoPriceB", "4.00")
            set("undoQuantityB", "1000")
            // Deadline strictly in the past → restore should drop it.
            set("undoDeadline", System.currentTimeMillis() - 1L)
        }

        val viewModel = CompareViewModel(handle)

        assertNull(
            "Stale UndoState (deadline in the past) is dropped on restore",
            viewModel.state.value.undoState,
        )
        assertFalse(
            "isResetEnabled stays false on a restored empty form",
            viewModel.state.value.isResetEnabled,
        )
    }

    private fun CompareViewModel.withFullExample(): CompareViewModel = apply {
        onPriceAChange("2.50")
        onQuantityAChange("500")
        onPriceBChange("4.00")
        onQuantityBChange("1000")
    }
}
