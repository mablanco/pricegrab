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

/**
 * US1 happy-path instrumented tests: the screen surfaces the correct
 * "Offer A / B is cheaper" headline once both offers parse into valid
 * per-unit prices.
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenIdentifyWinnerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bIsCheaperShowsBWinsHeadline() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")

        val expected = composeRule.activity.getString(R.string.result_winner_b)
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun aIsCheaperShowsAWinsHeadline() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("1.00")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("100")
        composeRule.onNodeWithTag("offerB_price").performTextInput("2.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("100")

        val expected = composeRule.activity.getString(R.string.result_winner_a)
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }
}
