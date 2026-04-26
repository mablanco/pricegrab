package com.mablanco.pricegrab.ui.compare

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Feature 003 / FR-006: every Compare screen surface must remain usable at
 * the largest accessibility font scale (200%) without truncation or layout
 * collapse. A real Settings → Accessibility → Font Size override is part of
 * the manual verification (tasks.md T022); this test gives an automated
 * signal by injecting a [Density] with `fontScale = 2f` via `LocalDensity`,
 * which is what the Material 3 type system reads to size every Text node.
 *
 * The assertions are deliberately structural (the brand title is rendered,
 * each input field still emits its semantic node) rather than pixel-exact:
 * pixel positions vary across emulators while the structural signals are
 * stable and catch the failure mode this test exists to prevent — a label
 * or field disappearing because Compose lays it out off-screen at 2x.
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenLargeFontTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compareScreenRendersAtTwoHundredPercentFontScale() {
        composeRule.setContent {
            val baseDensity = LocalDensity.current
            val largeFontDensity = Density(
                density = baseDensity.density,
                fontScale = LARGE_FONT_SCALE,
            )
            CompositionLocalProvider(LocalDensity provides largeFontDensity) {
                PriceGrabTheme {
                    CompareScreen(
                        state = CompareUiState(),
                        onPriceAChange = {},
                        onQuantityAChange = {},
                        onPriceBChange = {},
                        onQuantityBChange = {},
                        onResetClick = {},
                        onUndoClick = {},
                        onUndoDismissed = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText(BRAND_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_BRANDMARK).assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price").assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_quantity").assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_B}_price").assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_B}_quantity").assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_RESULT).assertIsDisplayed()
    }

    private companion object {
        const val LARGE_FONT_SCALE = 2.0f

        // Hardcoded to keep the test independent of locale; the brand name
        // is intentionally identical in every supported language.
        const val BRAND_TITLE = "PriceGrab"
    }
}
