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
class CompareScreenTieTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun equalUnitPricesShowTieHeadline() {
        // Same unit price (0.02) at different absolute scales.
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.00")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("100")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("200")

        val expected = composeRule.activity.getString(R.string.result_tie)
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }
}
