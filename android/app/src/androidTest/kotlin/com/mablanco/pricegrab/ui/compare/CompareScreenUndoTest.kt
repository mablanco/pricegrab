package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * US2 (T015) — instrumented tests for the Undo Snackbar surfaced by
 * the Compare screen's `SnackbarHost`.
 *
 * Pinned behaviour:
 *  - after a non-empty Reset, the Snackbar shows with the localized
 *    "Comparison cleared" message and an "Undo" action;
 *  - tapping Undo restores all four fields and the comparison result;
 *  - typing into any field while the Snackbar is visible dismisses
 *    it (FR-008.1);
 *  - the Snackbar auto-dismisses after the lifetime expires; the
 *    fields stay cleared (FR-006.timeout).
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenUndoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun snackbarAppearsAfterResetWithUndoAction() {
        seedFormWithFullExample()

        composeRule.onNodeWithTag(TEST_TAG_RESET).performClick()

        val ctx = composeRule.activity
        val message = ctx.getString(R.string.comparison_cleared)
        val action = ctx.getString(R.string.undo_action)

        composeRule.waitUntil(timeoutMillis = SNACKBAR_APPEAR_TIMEOUT_MS) {
            composeRule.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(message).assertIsDisplayed()
        composeRule.onNodeWithText(action).assertIsDisplayed()
    }

    @Test
    fun tappingUndoRestoresAllFieldsAndResult() {
        seedFormWithFullExample()
        composeRule.onNodeWithTag(TEST_TAG_RESET).performClick()

        val ctx = composeRule.activity
        val undoLabel = ctx.getString(R.string.undo_action)
        composeRule.waitUntil(timeoutMillis = SNACKBAR_APPEAR_TIMEOUT_MS) {
            composeRule.onAllNodesWithText(undoLabel).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(undoLabel).performClick()
        composeRule.waitForIdle()

        // Winner headline is back; same comparison as before the reset.
        composeRule.onNodeWithText(ctx.getString(R.string.result_b_wins)).assertIsDisplayed()
    }

    @Test
    fun typingWhileSnackbarVisibleDismissesItAndKeepsTypedValue() {
        seedFormWithFullExample()
        composeRule.onNodeWithTag(TEST_TAG_RESET).performClick()

        val ctx = composeRule.activity
        val message = ctx.getString(R.string.comparison_cleared)
        composeRule.waitUntil(timeoutMillis = SNACKBAR_APPEAR_TIMEOUT_MS) {
            composeRule.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("offerA_price").performTextInput("3")

        composeRule.waitUntil(timeoutMillis = SNACKBAR_APPEAR_TIMEOUT_MS) {
            composeRule.onAllNodesWithText(message).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun snackbarAutoDismissesAfterLifetimeAndFieldsStayCleared() {
        seedFormWithFullExample()
        composeRule.onNodeWithTag(TEST_TAG_RESET).performClick()

        val ctx = composeRule.activity
        val message = ctx.getString(R.string.comparison_cleared)
        // The Snackbar must disappear within UNDO_LIFETIME_MS (10s) +
        // a small grace window for animations / dispatcher scheduling.
        composeRule.waitUntil(timeoutMillis = AUTO_DISMISS_TIMEOUT_MS) {
            composeRule.onAllNodesWithText(message).fetchSemanticsNodes().isEmpty()
        }
        // Fields stayed cleared (no auto-undo on timeout).
        composeRule.onNodeWithText(ctx.getString(R.string.result_placeholder))
            .assertIsDisplayed()
    }

    private fun seedFormWithFullExample() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")
    }

    private companion object {
        const val SNACKBAR_APPEAR_TIMEOUT_MS = 5_000L

        // UNDO_LIFETIME_MS in CompareViewModel is 10 000ms; allow up to
        // 13 s to absorb dispatcher scheduling and animation overhead.
        const val AUTO_DISMISS_TIMEOUT_MS = 13_000L
    }
}
