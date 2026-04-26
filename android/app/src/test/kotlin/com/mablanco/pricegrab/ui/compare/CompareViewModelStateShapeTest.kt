package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Foundational shape tests for the additions feature 002 makes to
 * [CompareUiState] and the derived [isResetEnabled] property.
 *
 * Behaviour proper (the `resetComparison`/`undoReset`/`dismissUndo`
 * methods on the ViewModel) is covered by `CompareViewModelResetTest`
 * once the US1/US2 tasks land. This file pins down only the data shape
 * so the foundational phase has its own green test signal.
 */
class CompareViewModelStateShapeTest {

    @Test
    fun freshStateExposesNoUndoStateAndDisabledResetButton() {
        val viewModel = CompareViewModel(SavedStateHandle())

        val state = viewModel.state.value
        assertNull("undoState is null on a brand-new screen", state.undoState)
        assertFalse(
            "Reset is disabled when all four fields are empty",
            state.isResetEnabled,
        )
    }

    @Test
    fun typingIntoAnyFieldEnablesResetButton() {
        val viewModel = CompareViewModel(SavedStateHandle())

        viewModel.onPriceAChange("2.50")

        assertTrue(
            "Reset becomes enabled after typing into Price A",
            viewModel.state.value.isResetEnabled,
        )
    }

    @Test
    fun pureWhitespaceInputDoesNotEnableResetButton() {
        val viewModel = CompareViewModel(SavedStateHandle())

        // sanitize() in CompareViewModel strips whitespace, so a "   "
        // input round-trips to "". The ResetEnabled derivation looks
        // at the post-sanitize state.
        viewModel.onPriceAChange("   ")

        assertFalse(
            "Reset stays disabled when only whitespace was typed",
            viewModel.state.value.isResetEnabled,
        )
    }

    @Test
    fun typingIntoQuantityBAlsoEnablesResetButton() {
        val viewModel = CompareViewModel(SavedStateHandle())

        viewModel.onQuantityBChange("100")

        assertTrue(
            "Reset becomes enabled when *any* of the four fields is non-empty",
            viewModel.state.value.isResetEnabled,
        )
    }
}
