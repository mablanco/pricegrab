package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Feature 008: unique winners show absolute + percent savings on the hero;
 * tie / empty / incompatible stay quiet about both figures.
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
        val expectedAbsolute = ctx.getString(
            R.string.result_savings_per_kg,
            "1",
            ctx.getString(R.string.offer_a_title),
        )
        val expectedPercent = ctx.getString(R.string.result_savings_percent, "20")

        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS).assertIsDisplayed()
        composeRule.onNodeWithText(expectedAbsolute).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS_PERCENT).assertIsDisplayed()
        composeRule.onNodeWithText(expectedPercent).assertIsDisplayed()
    }

    @Test
    fun freeOfferShowsAbsoluteAndHundredPercentSavings() {
        // Offer A is free; B costs 5/100 = 0.05 → A wins by 0.05 per unit, 100% less.
        composeRule.onNodeWithTag("offerA_price").performTextInput("0")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("100")
        composeRule.onNodeWithTag("offerB_price").performTextInput("5.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("100")

        val ctx = composeRule.activity
        val expectedAbsolute = ctx.getString(
            R.string.result_savings_per_kg,
            "50",
            ctx.getString(R.string.offer_b_title),
        )
        val expectedPercent = ctx.getString(R.string.result_savings_percent, "100")

        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS).assertIsDisplayed()
        composeRule.onNodeWithText(expectedAbsolute).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS_PERCENT).assertIsDisplayed()
        composeRule.onNodeWithText(expectedPercent).assertIsDisplayed()
    }

    @Test
    fun threeOffersShowsAbsoluteAndPercentVsSecondCheapest() {
        // A=0.005, B=0.004, C=0.006 → B wins vs A by 0.001 (€1/kg), 20%
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")
        composeRule.onNodeWithTag(TEST_TAG_ADD_OFFER).performClick()
        composeRule.onNodeWithTag("offerC_price").performScrollTo().performTextInput("3.00")
        composeRule.onNodeWithTag("offerC_quantity").performTextInput("500")

        val ctx = composeRule.activity
        val expectedAbsolute = ctx.getString(
            R.string.result_savings_per_kg,
            "1",
            ctx.getString(R.string.offer_a_title),
        )
        val expectedPercent = ctx.getString(R.string.result_savings_percent, "20")

        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS).assertIsDisplayed()
        composeRule.onNodeWithText(expectedAbsolute).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS_PERCENT).assertIsDisplayed()
        composeRule.onNodeWithText(expectedPercent).assertIsDisplayed()
    }

    @Test
    fun tieHidesSavingsRow() {
        // 1.00/100 = 0.01 == 2.00/200 = 0.01 → tie → no savings rows.
        composeRule.onNodeWithTag("offerA_price").performTextInput("1.00")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("100")
        composeRule.onNodeWithTag("offerB_price").performTextInput("2.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("200")

        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS).assertDoesNotExist()
        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS_PERCENT).assertDoesNotExist()
    }

    @Test
    fun incompleteInputHidesBothSavingsTags() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        // Quantity A and all of B still empty → no outcome.

        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS).assertDoesNotExist()
        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS_PERCENT).assertDoesNotExist()
        composeRule.onNodeWithTag(TEST_TAG_HERO_RESULT).assertDoesNotExist()
    }
}
