package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
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
 * Feature 005: add/remove a third offer slot and rank three filled offers.
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenMultiOfferTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun coldLaunchShowsTwoOffersPlusAddWithoutRemove() {
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price").assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_B}_price").assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_C}_price").assertDoesNotExist()
        composeRule.onNodeWithTag(TEST_TAG_ADD_OFFER).assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithTag(TEST_TAG_REMOVE_OFFER).assertDoesNotExist()
    }

    @Test
    fun addShowsOfferCAndDisablesFurtherAdd() {
        composeRule.onNodeWithTag(TEST_TAG_ADD_OFFER).performClick()

        composeRule.onNodeWithTag("${TEST_TAG_OFFER_C}_price").assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.offer_c_title))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_ADD_OFFER).assertDoesNotExist()
        composeRule.onNodeWithTag(TEST_TAG_REMOVE_OFFER).assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun removeReturnsToTwoOffers() {
        composeRule.onNodeWithTag(TEST_TAG_ADD_OFFER).performClick()
        composeRule.onNodeWithTag(TEST_TAG_REMOVE_OFFER).performClick()

        composeRule.onNodeWithTag("${TEST_TAG_OFFER_C}_price").assertDoesNotExist()
        composeRule.onNodeWithTag(TEST_TAG_ADD_OFFER).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_REMOVE_OFFER).assertDoesNotExist()
    }

    @Test
    fun threeOffersShowsCheapestWinnerAndPerKgSavingsVsSecond() {
        // A=0.005, B=0.004, C=0.006 → B wins vs A by 0.001 (€1/kg), 20%
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")
        composeRule.onNodeWithTag(TEST_TAG_ADD_OFFER).performClick()
        composeRule.onNodeWithTag("offerC_price").performScrollTo().performTextInput("3.00")
        composeRule.onNodeWithTag("offerC_quantity").performTextInput("500")

        val ctx = composeRule.activity
        val headline = ctx.winnerHeadline(R.string.offer_b_title)
        val savings = ctx.getString(
            R.string.result_savings_per_kg,
            "1",
            ctx.getString(R.string.offer_a_title),
        )
        val percent = ctx.getString(R.string.result_savings_percent, "20")

        composeRule.onNodeWithText(headline).assertIsDisplayed()
        composeRule.onNodeWithText(savings).assertIsDisplayed()
        composeRule.onNodeWithText(percent).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS_PERCENT).assertIsDisplayed()
    }

    @Test
    fun blankOfferCStillComparesAAndB() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")
        composeRule.onNodeWithTag(TEST_TAG_ADD_OFFER).performClick()

        val headline = composeRule.activity.winnerHeadline(R.string.offer_b_title)
        composeRule.onNodeWithText(headline).assertIsDisplayed()
    }
}
