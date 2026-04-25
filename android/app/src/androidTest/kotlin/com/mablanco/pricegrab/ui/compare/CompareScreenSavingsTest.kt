package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.test.assertDoesNotExist
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

/**
 * US2 instrumented tests: once both offers parse, the result card shows the
 * absolute per-unit savings and the percent-less figure, hidden on tie.
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenSavingsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bWinsShowsAbsoluteAndPercentSavings() {
        // 2.50/500 = 0.005, 4.00/1000 = 0.004 → B wins by 0.001 per unit, 20% less.
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")

        val ctx = composeRule.activity
        val expected = ctx.getString(R.string.savings_template, "0.001", "20")

        composeRule.onNodeWithTag("result_savings").assertIsDisplayed()
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun freeOfferShowsHundredPercentSavings() {
        // Offer A is free; B costs 5/100 = 0.05 → A wins by 0.05 per unit, 100% less.
        composeRule.onNodeWithTag("offerA_price").performTextInput("0")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("100")
        composeRule.onNodeWithTag("offerB_price").performTextInput("5.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("100")

        val ctx = composeRule.activity
        val expected = ctx.getString(R.string.savings_template, "0.05", "100")

        composeRule.onNodeWithTag("result_savings").assertIsDisplayed()
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun tieHidesSavingsRow() {
        // 1.00/100 = 0.01 == 2.00/200 = 0.01 → tie → no savings row.
        composeRule.onNodeWithTag("offerA_price").performTextInput("1.00")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("100")
        composeRule.onNodeWithTag("offerB_price").performTextInput("2.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("200")

        composeRule.onNodeWithTag("result_savings").assertDoesNotExist()
    }
}
