package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Feature 003 / US2 — pin the hero result card's lifecycle on the Compare
 * screen.
 *
 * Three transitions are pinned:
 *  - cold launch → no `heroResult` node (only the placeholder Text appears
 *    inside the always-emitted `result` live region);
 *  - valid input on both offers → the `heroResult` ElevatedCard surfaces
 *    with a non-empty headline and a non-empty body;
 *  - tap Reset → the `heroResult` node disappears within one frame and
 *    the placeholder Text returns.
 *
 * The card is rendered conditionally (T016 from
 * `specs/003-visual-polish-branding/tasks.md`); these assertions catch a
 * regression where it would either leak into cold launch or stick around
 * across a Reset.
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenLayoutTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun heroResultCardIsAbsentOnColdLaunch() {
        composeRule.onNodeWithTag(TEST_TAG_HERO_RESULT).assertDoesNotExist()

        val placeholder = composeRule.activity.getString(R.string.result_placeholder)
        composeRule.onNodeWithText(placeholder).assertIsDisplayed()
    }

    @Test
    fun heroResultCardSurfacesAfterValidInput() {
        seedFormWithBetterB()

        composeRule.onNodeWithTag(TEST_TAG_HERO_RESULT).assertIsDisplayed()

        // Headline and savings line both populate from the new feature 003
        // strings; their textual contents are pinned by
        // CompareScreenIdentifyWinnerTest / CompareScreenSavingsTest. Here
        // we just confirm both are present inside the hero card subtree
        // (not stale from a previous comparison).
        composeRule.onNodeWithTag(TEST_TAG_RESULT_TEXT).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_RESULT_SAVINGS).assertIsDisplayed()
    }

    @Test
    fun heroResultCardDisappearsAfterReset() {
        seedFormWithBetterB()
        composeRule.onNodeWithTag(TEST_TAG_HERO_RESULT).assertIsDisplayed()

        composeRule.onNodeWithTag(TEST_TAG_RESET).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TEST_TAG_HERO_RESULT).assertDoesNotExist()

        // The placeholder Text is back inside the always-emitted result
        // region; the screen does not leave a stale empty rectangle.
        val placeholder = composeRule.activity.getString(R.string.result_placeholder)
        composeRule.onNodeWithText(placeholder).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_RESULT).assertIsDisplayed()
    }

    private fun seedFormWithBetterB() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")
    }
}
