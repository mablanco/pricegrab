package com.mablanco.pricegrab.ui.compare

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.core.format.MoneyFormatter
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.QuantityUnit
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme
import java.math.BigDecimal
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * Feature 010: money amounts carry the [LocalCurrencySymbol]; quantities
 * and percentages do not. See `contracts/currency-display.md`.
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenCurrencyTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val locale = context.resources.configuration.locales[0]

    @Test
    fun priceFieldsShowTheSymbolAndQuantityFieldsDoNot() {
        setCompareScreen(CompareUiState(), symbol = "€")

        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price_label", useUnmergedTree = true)
            .assertTextEquals(context.priceLabel("€"))
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_B}_price_label", useUnmergedTree = true)
            .assertTextEquals(context.priceLabel("€"))
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_quantity_label", useUnmergedTree = true)
            .assertTextEquals(context.getString(R.string.quantity_label))
    }

    @Test
    fun priceContentDescriptionIncludesTheSymbol() {
        setCompareScreen(CompareUiState(), symbol = "€")

        val priceCd = context.getString(
            R.string.cd_price_field,
            context.getString(R.string.offer_a_title),
        )
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price")
            .assert(hasContentDescriptionContaining("$priceCd, €"))
    }

    @Test
    fun savingsAmountIsWrappedAndPercentIsNot() {
        setCompareScreen(winnerState(), symbol = "€")

        val savings = context.getString(
            R.string.result_savings_per_kg,
            MoneyFormatter.wrap("1", MoneyFormatter.affixes("€", locale)),
            context.getString(R.string.offer_a_title),
        )
        val percent = context.getString(R.string.result_savings_percent, "20")
        composeRule.onNodeWithText(savings).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(percent).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun emptySymbolRendersAmountsAsBefore() {
        setCompareScreen(winnerState(), symbol = "")

        val savings = context.getString(
            R.string.result_savings_per_kg,
            "1",
            context.getString(R.string.offer_a_title),
        )
        composeRule.onNodeWithText(savings).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price_label", useUnmergedTree = true)
            .assertTextEquals(context.priceLabel(""))
    }

    @Test
    fun fourCharacterSymbolKeepsTheCompactRow() {
        setCompareScreen(CompareUiState(), symbol = "CHF")

        val price = bounds("${TEST_TAG_OFFER_A}_price")
        val quantity = bounds("${TEST_TAG_OFFER_A}_quantity")
        val unit = bounds("${TEST_TAG_OFFER_A}_unit")
        assertTrue(abs(price.bottom - quantity.bottom) < ROW_Y_TOLERANCE_PX)
        assertTrue(abs(quantity.bottom - unit.bottom) < ROW_Y_TOLERANCE_PX)
        assertTrue(price.left < quantity.left)
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price_label", useUnmergedTree = true)
            .assertTextEquals(context.priceLabel("CHF"))
            .assertIsDisplayed()
    }

    @Test
    fun symbolAndAmountStayVisibleAtTwoHundredPercentFontScale() {
        setCompareScreen(
            CompareUiState(
                offers = listOf(
                    OfferSlotState(priceRaw = "1234.56", quantityRaw = "500"),
                    OfferSlotState(priceRaw = "4.00", quantityRaw = "1000"),
                ),
            ),
            symbol = "CHF",
            fontScale = LARGE_FONT_SCALE,
        )

        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price_label", useUnmergedTree = true)
            .assertTextEquals(context.priceLabel("CHF"))
            .assertIsDisplayed()
        composeRule.onNodeWithText("1234.56", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun winnerState() = CompareUiState(
        offers = listOf(
            OfferSlotState(priceRaw = "2.50", quantityRaw = "500"),
            OfferSlotState(priceRaw = "4.00", quantityRaw = "1", quantityUnit = QuantityUnit.Kilogram),
        ),
        outcome = ComparisonOutcome.Winner(
            slotIndex = 1,
            secondSlotIndex = 0,
            perUnitDelta = BigDecimal("0.001"),
            percentDelta = BigDecimal("20"),
        ),
    )

    private fun setCompareScreen(
        state: CompareUiState,
        symbol: String,
        fontScale: Float = 1f,
    ) {
        composeRule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = baseDensity.density, fontScale = fontScale),
                LocalCurrencySymbol provides symbol,
            ) {
                PriceGrabTheme {
                    CompareScreen(
                        state = state,
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
    }

    private fun bounds(testTag: String) =
        composeRule.onNodeWithTag(testTag).fetchSemanticsNode().boundsInRoot

    private companion object {
        const val LARGE_FONT_SCALE = 2.0f
        const val ROW_Y_TOLERANCE_PX = 72f
    }
}
