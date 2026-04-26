package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
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
 * Verifies FR-012: inputs and the computed result survive a configuration
 * change (rotation) because the `CompareViewModel` keeps them in its
 * `SavedStateHandle`.
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenConfigChangeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun inputsAndResultSurviveActivityRecreation() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")

        val expectedResult = composeRule.activity.getString(R.string.result_winner_b)
        composeRule.onNodeWithText(expectedResult).assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("offerA_price").assertTextContains("2.50")
        composeRule.onNodeWithTag("offerA_quantity").assertTextContains("500")
        composeRule.onNodeWithTag("offerB_price").assertTextContains("4.00")
        composeRule.onNodeWithTag("offerB_quantity").assertTextContains("1000")
        composeRule.onNodeWithText(expectedResult).assertIsDisplayed()
    }
}
