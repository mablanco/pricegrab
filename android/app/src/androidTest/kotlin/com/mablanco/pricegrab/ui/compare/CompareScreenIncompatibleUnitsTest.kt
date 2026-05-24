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
class CompareScreenIncompatibleUnitsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun gramVsMillilitreShowsErrorAndNoWinner() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("500")
        composeRule.selectUnit("${TEST_TAG_OFFER_B}_unit", R.string.unit_name_millilitre)

        val ctx = composeRule.activity
        val error = ctx.getString(R.string.error_incompatible_units)
        val winner = ctx.getString(R.string.result_winner_b)

        composeRule.onNodeWithText(error).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_INCOMPATIBLE_UNITS).assertIsDisplayed()
        composeRule.onNodeWithText(winner).assertDoesNotExist()
    }

    @Test
    fun fixingUnitToMassClearsErrorAndShowsWinner() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("500")
        composeRule.selectUnit("${TEST_TAG_OFFER_B}_unit", R.string.unit_name_millilitre)

        val ctx = composeRule.activity
        val error = ctx.getString(R.string.error_incompatible_units)
        composeRule.onNodeWithText(error).assertIsDisplayed()

        composeRule.selectUnit("${TEST_TAG_OFFER_B}_unit", R.string.unit_name_kilogram)

        val winner = ctx.getString(R.string.result_winner_b)
        composeRule.onNodeWithText(error).assertDoesNotExist()
        composeRule.onNodeWithText(winner).assertIsDisplayed()
    }
}
