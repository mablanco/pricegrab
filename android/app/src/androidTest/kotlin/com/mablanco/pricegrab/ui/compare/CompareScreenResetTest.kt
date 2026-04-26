package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
 * US1 (T008) — instrumented tests for the one-tap Reset action.
 *
 * Pinned behaviour:
 *  (a) tapping Reset after entering values clears all four fields and
 *      replaces the winner headline with the neutral placeholder;
 *  (b) the Reset icon is disabled on cold launch and becomes enabled
 *      after typing into any field;
 *  (c) after Reset, keyboard focus moves to the Price A field
 *      (FR-005.3).
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenResetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun resetClearsAllFieldsAndHidesWinnerHeadline() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")

        val ctx = composeRule.activity
        val winner = ctx.getString(R.string.result_b_wins)
        val placeholder = ctx.getString(R.string.result_placeholder)
        composeRule.onNodeWithText(winner).assertIsDisplayed()

        composeRule.onNodeWithTag(TEST_TAG_RESET).performClick()

        composeRule.onNodeWithTag("offerA_price").assert(hasEmptyEditableText())
        composeRule.onNodeWithTag("offerA_quantity").assert(hasEmptyEditableText())
        composeRule.onNodeWithTag("offerB_price").assert(hasEmptyEditableText())
        composeRule.onNodeWithTag("offerB_quantity").assert(hasEmptyEditableText())
        // The winner headline is gone; the neutral placeholder is back.
        composeRule.onNodeWithText(placeholder).assertIsDisplayed()
    }

    @Test
    fun resetIconIsDisabledOnColdLaunchAndEnabledAfterTyping() {
        composeRule.onNodeWithTag(TEST_TAG_RESET).assertIsNotEnabled()

        composeRule.onNodeWithTag("offerA_price").performTextInput("1.00")

        composeRule.onNodeWithTag(TEST_TAG_RESET).assertIsEnabled()
    }

    @Test
    fun resetMovesFocusToPriceA() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")

        // Focus is currently on the last-typed field (Quantity B).
        composeRule.onNodeWithTag("offerB_quantity").assertIsFocused()

        composeRule.onNodeWithTag(TEST_TAG_RESET).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("offerA_price").assert(isFocusedMatcher())
    }

    private fun isFocusedMatcher(): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.Focused, true)

    private fun hasEmptyEditableText(): SemanticsMatcher =
        SemanticsMatcher("EditableText is empty or absent") { node ->
            if (!node.config.contains(SemanticsProperties.EditableText)) {
                true
            } else {
                node.config[SemanticsProperties.EditableText].text.isEmpty()
            }
        }
}
