package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompareScreenValidationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun incompleteInputLeavesPlaceholderResult() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        // quantity A left empty; result should stay at placeholder
        val placeholder = composeRule.activity.getString(R.string.result_placeholder)
        composeRule.onNodeWithText(placeholder).assertIsDisplayed()
    }

    @Test
    fun nonNumericQuantityShowsInlineError() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("abc")

        val expected = composeRule.activity.getString(R.string.error_not_a_number)
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun zeroQuantityShowsInlineError() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("0")

        val expected = composeRule.activity.getString(R.string.error_non_positive_quantity)
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }
}
