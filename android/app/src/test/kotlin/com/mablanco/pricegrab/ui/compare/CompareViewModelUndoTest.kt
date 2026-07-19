package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * US2 — JVM unit tests for [CompareViewModel.undoReset]
 * and [CompareViewModel.dismissUndo], plus the SavedStateHandle round
 * trip that survives process death.
 */
class CompareViewModelUndoTest {

    @get:Rule
    val localeRule = EnUsLocaleRule()

    @Test
    fun undoResetRestoresAllFourFieldsAndClearsUndoState() {
        val viewModel = CompareViewModel(SavedStateHandle()).withFullExample()
        viewModel.resetComparison()
        assertEquals("", viewModel.state.value.offers[0].priceRaw)
        assertNotNull(viewModel.state.value.undoState)

        viewModel.undoReset()

        val state = viewModel.state.value
        assertEquals("2.50", state.offers[0].priceRaw)
        assertEquals("500", state.offers[0].quantityRaw)
        assertEquals("4.00", state.offers[1].priceRaw)
        assertEquals("1000", state.offers[1].quantityRaw)
        assertNotNull("Outcome rebuilt deterministically from the snapshot", state.outcome)
        assertNull("UndoState is cleared after a successful undo", state.undoState)
    }

    @Test
    fun dismissUndoClearsUndoStateWithoutTouchingFields() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceChange(0, "9.99")
        viewModel.resetComparison()
        assertNotNull(viewModel.state.value.undoState)

        viewModel.dismissUndo()

        val state = viewModel.state.value
        assertNull("UndoState is cleared", state.undoState)
        assertEquals("Fields stay cleared (dismiss != restore)", "", state.offers[0].priceRaw)
    }

    @Test
    fun undoResetIsNoOpWhenNoUndoStateActive() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceChange(0, "1.00")
        assertNull(viewModel.state.value.undoState)

        viewModel.undoReset()

        assertEquals("1.00", viewModel.state.value.offers[0].priceRaw)
    }

    @Test
    fun savedStateHandleRoundTripRestoresActiveUndoStateAcrossViewModelRecreation() {
        val handle = SavedStateHandle()
        val first = CompareViewModel(handle).withFullExample()
        first.resetComparison()
        val originalDeadline = first.state.value.undoState?.expiresAtEpochMillis
        assertNotNull(originalDeadline)

        val restored = CompareViewModel(handle)

        val undo = restored.state.value.undoState
        assertNotNull("UndoState is restored after process death", undo)
        requireNotNull(undo)
        assertEquals(originalDeadline, undo.expiresAtEpochMillis)
        assertEquals("2.50", undo.snapshot.slots[0].priceRaw)
        assertEquals("500", undo.snapshot.slots[0].quantityRaw)
        assertEquals("4.00", undo.snapshot.slots[1].priceRaw)
        assertEquals("1000", undo.snapshot.slots[1].quantityRaw)
        assertEquals("", restored.state.value.offers[0].priceRaw)
        restored.undoReset()
        assertEquals("2.50", restored.state.value.offers[0].priceRaw)
    }

    @Test
    fun savedStateHandleDropsExpiredUndoStateOnRestore() {
        val handle = SavedStateHandle().apply {
            set("undoOfferCount", 2)
            set("undoPrice0", "2.50")
            set("undoQuantity0", "500")
            set("undoPrice1", "4.00")
            set("undoQuantity1", "1000")
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

    @Test
    fun undoRestoresThreeOfferSnapshot() {
        val viewModel = CompareViewModel(SavedStateHandle())
        viewModel.onPriceChange(0, "2.50")
        viewModel.onQuantityChange(0, "500")
        viewModel.onPriceChange(1, "4.00")
        viewModel.onQuantityChange(1, "1000")
        viewModel.addOffer()
        viewModel.onPriceChange(2, "3.00")
        viewModel.onQuantityChange(2, "750")

        viewModel.resetComparison()
        assertEquals(2, viewModel.state.value.offers.size)

        viewModel.undoReset()

        val state = viewModel.state.value
        assertEquals(3, state.offers.size)
        assertEquals("3.00", state.offers[2].priceRaw)
        assertEquals("750", state.offers[2].quantityRaw)
        assertNotNull(state.outcome)
    }

    @Test
    fun addAndRemoveOfferRespectBounds() {
        val viewModel = CompareViewModel(SavedStateHandle())
        assertEquals(2, viewModel.state.value.offers.size)

        viewModel.removeOffer()
        assertEquals(2, viewModel.state.value.offers.size)

        viewModel.addOffer()
        assertEquals(3, viewModel.state.value.offers.size)

        viewModel.addOffer()
        assertEquals(3, viewModel.state.value.offers.size)

        viewModel.removeOffer()
        assertEquals(2, viewModel.state.value.offers.size)
        assertTrue(viewModel.state.value.offers.none { it.priceRaw.isNotEmpty() })
    }

    private fun CompareViewModel.withFullExample(): CompareViewModel = apply {
        onPriceChange(0, "2.50")
        onQuantityChange(0, "500")
        onPriceChange(1, "4.00")
        onQuantityChange(1, "1000")
    }
}
