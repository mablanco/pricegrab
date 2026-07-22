package com.mablanco.pricegrab.ui.compare

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.core.model.QuantityUnit
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * Feature 006 / US1: at default font scale, price | quantity | unit share one
 * horizontal row (see `contracts/offer-row-layout.md`).
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenCompactRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun offerAAndBFieldsShareOneRowAtDefaultFontScale() {
        setCompareScreen(CompareUiState())

        assertCompactSingleRow(TEST_TAG_OFFER_A)
        assertCompactSingleRow(TEST_TAG_OFFER_B)
    }

    @Test
    fun offerCUsesSameSingleRowGeometryAfterAdd() {
        composeRule.setContent {
            val baseDensity = LocalDensity.current
            var state by remember { mutableStateOf(CompareUiState()) }
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = baseDensity.density,
                    fontScale = DEFAULT_FONT_SCALE,
                ),
            ) {
                PriceGrabTheme {
                    CompareScreen(
                        state = state,
                        onPriceChange = { _, _ -> },
                        onQuantityChange = { _, _ -> },
                        onUnitChange = { _, _ -> },
                        onAddOffer = {
                            if (state.offers.size < 3) {
                                state = state.copy(
                                    offers = state.offers + OfferSlotState(
                                        quantityUnit = QuantityUnit.Gram,
                                    ),
                                )
                            }
                        },
                        onRemoveOffer = {},
                        onResetClick = {},
                        onUndoClick = {},
                        onUndoDismissed = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(TEST_TAG_ADD_OFFER).performClick()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_C}_price").assertIsDisplayed()

        assertCompactSingleRow(TEST_TAG_OFFER_C)
    }

    @Test
    fun quantityErrorSupportingTextVisibleInCompactRow() {
        val errorMessage = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getString(R.string.error_non_positive_quantity)

        setCompareScreen(
            CompareUiState(
                offers = listOf(
                    OfferSlotState(
                        priceRaw = "2.50",
                        quantityRaw = "0",
                        quantityError = InputError.NonPositiveQuantity,
                    ),
                    OfferSlotState(),
                ),
            ),
        )

        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price").assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_quantity").assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_unit").assertIsDisplayed()
        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    private fun setCompareScreen(state: CompareUiState) {
        composeRule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = baseDensity.density,
                    fontScale = DEFAULT_FONT_SCALE,
                ),
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

    private fun assertCompactSingleRow(prefix: String) {
        val price = bounds("${prefix}_price")
        val quantity = bounds("${prefix}_quantity")
        val unit = bounds("${prefix}_unit")

        // Tops align in CompactSingleRow (Alignment.Top). Prefer top delta over
        // centerY because the unit selector has no floating label and is shorter.
        assertTrue(
            "price and quantity tops should align (within ${ROW_Y_TOLERANCE_PX}px)",
            abs(price.top - quantity.top) < ROW_Y_TOLERANCE_PX,
        )
        assertTrue(
            "quantity and unit tops should align (within ${ROW_Y_TOLERANCE_PX}px)",
            abs(quantity.top - unit.top) < ROW_Y_TOLERANCE_PX,
        )
        assertTrue("price should be left of quantity", price.left < quantity.left)
        assertTrue("quantity should be left of unit", quantity.left < unit.left)
    }

    private fun bounds(testTag: String): Rect =
        composeRule.onNodeWithTag(testTag).fetchSemanticsNode().boundsInRoot

    private companion object {
        const val DEFAULT_FONT_SCALE = 1.0f

        /** ~24dp at xxhdpi; fails the previous full-width stacked price layout. */
        const val ROW_Y_TOLERANCE_PX = 72f
    }
}
