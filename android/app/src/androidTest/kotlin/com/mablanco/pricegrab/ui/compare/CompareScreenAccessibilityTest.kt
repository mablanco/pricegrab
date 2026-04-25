package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies FR-010 / Principle III (accessibility):
 * - every input has a localized content description so TalkBack can announce it;
 * - the result pane is marked as a polite live region so screen readers pick up
 *   updates when the user finishes typing.
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenAccessibilityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun inputFieldsExposeContentDescriptions() {
        val ctx = composeRule.activity
        val offerAName = ctx.getString(R.string.offer_a_title)
        val offerBName = ctx.getString(R.string.offer_b_title)

        composeRule.onNodeWithTag("offerA_price")
            .assert(hasContentDescriptionContaining(ctx.getString(R.string.cd_price_field, offerAName)))
        composeRule.onNodeWithTag("offerA_quantity")
            .assert(hasContentDescriptionContaining(ctx.getString(R.string.cd_quantity_field, offerAName)))
        composeRule.onNodeWithTag("offerB_price")
            .assert(hasContentDescriptionContaining(ctx.getString(R.string.cd_price_field, offerBName)))
        composeRule.onNodeWithTag("offerB_quantity")
            .assert(hasContentDescriptionContaining(ctx.getString(R.string.cd_quantity_field, offerBName)))
    }

    @Test
    fun resultPaneIsAPoliteLiveRegion() {
        composeRule.onNodeWithTag("result").assertIsDisplayed()
        composeRule.onNodeWithTag("result").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite),
        )
    }

    @Test
    fun resultPaneAnnouncesHeadlineAndSavings() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")

        val ctx = composeRule.activity
        val headline = ctx.getString(R.string.result_b_wins)
        val savings = ctx.getString(R.string.savings_template, "0.001", "20")

        composeRule.onNodeWithTag("result").assert(hasContentDescriptionContaining(headline))
        composeRule.onNodeWithTag("result").assert(hasContentDescriptionContaining(savings))
    }
}
