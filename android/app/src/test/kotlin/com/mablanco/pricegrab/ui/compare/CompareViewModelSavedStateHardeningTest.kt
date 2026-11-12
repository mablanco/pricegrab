package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * MainActivity is exported and AndroidX seeds [SavedStateHandle] defaults
 * from the launch intent's extras, so another app can plant arbitrary values
 * under our keys. These tests pin that hostile or malformed seeds never crash
 * the ViewModel and never get more authority than typing would.
 */
class CompareViewModelSavedStateHardeningTest {

    @get:Rule
    val localeRule = EnUsLocaleRule()

    @Test
    fun wrongTypedExtrasFallBackToDefaultsInsteadOfCrashing() {
        val handle = SavedStateHandle(
            mapOf(
                CompareViewModelKeys.OFFER_COUNT to "x",
                CompareViewModelKeys.price(0) to 42,
                CompareViewModelKeys.quantity(0) to listOf("500"),
                CompareViewModelKeys.quantityUnit(0) to 7,
                CompareViewModelKeys.UNDO_OFFER_COUNT to "2",
                CompareViewModelKeys.UNDO_DEADLINE to "tomorrow",
            ),
        )

        val state = CompareViewModel(handle).state.value

        assertEquals(MIN_OFFERS, state.offers.size)
        assertEquals("", state.offers[0].priceRaw)
        assertEquals("", state.offers[0].quantityRaw)
        assertNull(state.undoState)
        assertNull(state.outcome)
    }

    @Test
    fun hugeExponentInSeededPriceIsSanitizedBeforeParsing() {
        val handle = SavedStateHandle(
            mapOf(
                CompareViewModelKeys.price(0) to "1E2147483647",
                CompareViewModelKeys.quantity(0) to "1e-2147483647",
                CompareViewModelKeys.price(1) to "1",
                CompareViewModelKeys.quantity(1) to "1",
            ),
        )

        val state = CompareViewModel(handle).state.value

        assertEquals("12147483647", state.offers[0].priceRaw)
        assertEquals("12147483647", state.offers[0].quantityRaw)
    }

    @Test
    fun seededUndoSnapshotStringsAreSanitized() {
        val handle = SavedStateHandle(
            mapOf(
                CompareViewModelKeys.UNDO_OFFER_COUNT to 2,
                CompareViewModelKeys.UNDO_DEADLINE to System.currentTimeMillis() + 5_000L,
                CompareViewModelKeys.undoPrice(0) to "-1E99999",
                CompareViewModelKeys.undoQuantity(0) to "1",
                CompareViewModelKeys.undoPrice(1) to "2",
                CompareViewModelKeys.undoQuantity(1) to "2",
            ),
        )

        val undo = requireNotNull(CompareViewModel(handle).state.value.undoState)

        assertEquals("199999", undo.snapshot.slots[0].priceRaw)
    }

    @Test
    fun undoDeadlineBeyondOneLifetimeIsRejected() {
        val handle = SavedStateHandle(
            mapOf(
                CompareViewModelKeys.UNDO_OFFER_COUNT to 2,
                CompareViewModelKeys.UNDO_DEADLINE to Long.MAX_VALUE,
                CompareViewModelKeys.undoPrice(0) to "1",
                CompareViewModelKeys.undoQuantity(0) to "1",
                CompareViewModelKeys.undoPrice(1) to "2",
                CompareViewModelKeys.undoQuantity(1) to "2",
            ),
        )

        assertNull(CompareViewModel(handle).state.value.undoState)
    }
}
