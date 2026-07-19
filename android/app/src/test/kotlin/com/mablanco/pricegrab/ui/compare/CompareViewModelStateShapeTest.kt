package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Foundational shape tests for [CompareUiState] and [isResetEnabled].
 */
class CompareViewModelStateShapeTest {

    @get:Rule
    val localeRule = EnUsLocaleRule()

    @Test
    fun freshStateExposesNoUndoStateAndDisabledResetButton() {
        val viewModel = CompareViewModel(SavedStateHandle())

        val state = viewModel.state.value
        assertEquals(2, state.offers.size)
        assertNull("undoState is null on a brand-new screen", state.undoState)
        assertFalse(
            "Reset is disabled when all fields are empty",
            state.isResetEnabled,
        )
    }

    @Test
    fun typingIntoAnyFieldEnablesResetButton() {
        val viewModel = CompareViewModel(SavedStateHandle())

        viewModel.onPriceChange(0, "2.50")

        assertTrue(
            "Reset becomes enabled after typing into Price A",
            viewModel.state.value.isResetEnabled,
        )
    }

    @Test
    fun pureWhitespaceInputDoesNotEnableResetButton() {
        val viewModel = CompareViewModel(SavedStateHandle())

        viewModel.onPriceChange(0, "   ")

        assertFalse(
            "Reset stays disabled when only whitespace was typed",
            viewModel.state.value.isResetEnabled,
        )
    }

    @Test
    fun typingIntoQuantityBAlsoEnablesResetButton() {
        val viewModel = CompareViewModel(SavedStateHandle())

        viewModel.onQuantityChange(1, "100")

        assertTrue(
            "Reset becomes enabled when *any* of the fields is non-empty",
            viewModel.state.value.isResetEnabled,
        )
    }
}
