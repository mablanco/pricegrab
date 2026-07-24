package com.mablanco.pricegrab.ui.compare

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme
import java.math.BigDecimal
import org.junit.Assert.assertTrue
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
                        onPriceChange = { _, _ -> },
                        onQuantityChange = { _, _ -> },
                        onUnitChange = { _, _ -> },
                        onAddOffer = {},
                        onRemoveOffer = {},
                        onResetClick = {},
                        onUndoClick = {},
                        onUndoDismissed = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText(BRAND_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_BRANDMARK).assertIsDisplayed()
        assertTagVisible("${TEST_TAG_OFFER_A}_price")
        assertTagVisible("${TEST_TAG_OFFER_A}_quantity")
        assertTagVisible("${TEST_TAG_OFFER_A}_unit")
        assertTagVisible("${TEST_TAG_OFFER_B}_price")
        assertTagVisible("${TEST_TAG_OFFER_B}_quantity")
        assertTagVisible("${TEST_TAG_OFFER_B}_unit")
        assertTagVisible(TEST_TAG_RESULT)

        assertAdaptiveTwoRow(TEST_TAG_OFFER_A)
        assertAdaptiveTwoRow(TEST_TAG_OFFER_B)
    }

    @Test
    fun winnerSavingsBothVisibleAtTwoHundredPercentFontScale() {
        composeRule.setContent {
            val baseDensity = LocalDensity.current
            val largeFontDensity = Density(
                density = baseDensity.density,
                fontScale = LARGE_FONT_SCALE,
            )
            CompositionLocalProvider(LocalDensity provides largeFontDensity) {
                PriceGrabTheme {
                    CompareScreen(
                        state = CompareUiState(
                            offers = listOf(
                                OfferSlotState(priceRaw = "2.50", quantityRaw = "500"),
                                OfferSlotState(priceRaw = "4.00", quantityRaw = "1000"),
                            ),
                            outcome = ComparisonOutcome.Winner(
                                slotIndex = 1,
                                secondSlotIndex = 0,
                                perUnitDelta = BigDecimal("0.001"),
                                percentDelta = BigDecimal("20"),
                            ),
                        ),
                        onPriceChange = { _, _ -> },
                        onQuantityChange = { _, _ -> },
                        onUnitChange = { _, _ -> },
                        onAddOffer = {},
                        onRemoveOffer = {},
                        onResetClick = {},
                        onUndoClick = {},
                        onUndoDismissed = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(TEST_TAG_HERO_RESULT).performScrollTo().assertIsDisplayed()
        assertTagVisible(TEST_TAG_RESULT_SAVINGS)
        assertTagVisible(TEST_TAG_RESULT_SAVINGS_PERCENT)
    }

    private fun assertTagVisible(testTag: String) {
        composeRule.onNodeWithTag(testTag).performScrollTo().assertIsDisplayed()
    }

    private fun assertAdaptiveTwoRow(prefix: String) {
        val price = composeRule.onNodeWithTag("${prefix}_price")
            .fetchSemanticsNode().boundsInRoot
        val quantity = composeRule.onNodeWithTag("${prefix}_quantity")
            .fetchSemanticsNode().boundsInRoot
        val unit = composeRule.onNodeWithTag("${prefix}_unit")
            .fetchSemanticsNode().boundsInRoot

        assertTrue(
            "at 200% font, price bottom should be above quantity top",
            price.bottom <= quantity.top + ADAPTIVE_ROW_SLACK_PX,
        )
        assertTrue("quantity should be left of unit", quantity.left < unit.left)
    }

    private companion object {
        const val LARGE_FONT_SCALE = 2.0f

        /** Allow a few px of rounding / spacing between stacked rows. */
        const val ADAPTIVE_ROW_SLACK_PX = 8f

        // Hardcoded to keep the test independent of locale; the brand name
        // is intentionally identical in every supported language.
        const val BRAND_TITLE = "PriceGrab"
    }
}
