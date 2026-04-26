package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
        val headline = ctx.getString(R.string.result_winner_b)
        val savings = ctx.getString(R.string.result_savings, "0.001")

        composeRule.onNodeWithTag("result").assert(hasContentDescriptionContaining(headline))
        composeRule.onNodeWithTag("result").assert(hasContentDescriptionContaining(savings))
    }

    /**
     * US002 (T009): the Reset action surfaced in the top app bar must
     * carry a non-empty content description, the `Role.Button` semantic
     * role (so TalkBack reads "button" after the description), and the
     * disabled state exposed via `SemanticsProperties.Disabled` when the
     * form is empty.
     */
    @Test
    fun resetActionExposesContentDescriptionAndButtonRole() {
        val ctx = composeRule.activity
        val description = ctx.getString(R.string.reset_action_description)

        composeRule.onNodeWithTag(TEST_TAG_RESET).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_RESET)
            .assert(hasContentDescriptionContaining(description))
        composeRule.onNodeWithTag(TEST_TAG_RESET)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun resetActionIsDisabledOnColdLaunch() {
        composeRule.onNodeWithTag(TEST_TAG_RESET).assertIsNotEnabled()
        composeRule.onNodeWithTag(TEST_TAG_RESET)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Disabled))
    }

    /**
     * US003 / FR-002: the top app bar exposes the brand identity via a
     * brandmark glyph immediately followed by the plain-text "PriceGrab"
     * title. The glyph is purely decorative, so:
     *   - the merged semantic tree announces "PriceGrab" exactly once
     *     (TalkBack must not double-read the icon and the text);
     *   - the brandmark node carries no contentDescription of its own.
     */
    @Test
    fun topAppBarBrandingExposesTitleOnceAndBrandmarkIsDecorative() {
        val ctx = composeRule.activity
        val brand = ctx.getString(R.string.app_name)

        composeRule.onAllNodesWithText(brand).assertCountEquals(1)

        composeRule.onNodeWithTag(TEST_TAG_BRANDMARK).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_BRANDMARK).assert(
            SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription),
        )
    }

    /**
     * Feature 003 / US2 — the hero result card's headline carries the
     * `heading` semantics flag so TalkBack announces it as a heading
     * (not just as plain text). The savings body line stays a regular
     * `Text` (no heading flag) so the heading hierarchy on screen is:
     *   - top-app-bar title "PriceGrab" (Material 3 default heading)
     *   - in-content "Compare two offers" (plain titleLarge)
     *   - hero result headline (`heading()` semantic flag)
     */
    @Test
    fun heroResultHeadlineCarriesHeadingSemantics() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")

        composeRule.onNodeWithTag(TEST_TAG_HERO_RESULT).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_RESULT_TEXT).assert(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading),
        )
        // The savings body line stays announceable via the live region's
        // contentDescription, but it should NOT itself be a heading — that
        // would force TalkBack into a two-level heading hierarchy inside
        // the same card and over-announce the result.
        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS).assert(
            SemanticsMatcher.keyNotDefined(SemanticsProperties.Heading),
        )
    }
}
