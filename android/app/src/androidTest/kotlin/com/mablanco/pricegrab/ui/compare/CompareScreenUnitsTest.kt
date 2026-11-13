package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompareScreenUnitsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun coldLaunchShowsGramUnitOnBothOffers() {
        val ctx = composeRule.activity
        val gramCode = ctx.getString(R.string.unit_code_g)

        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_unit").assertIsDisplayed()
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_B}_unit").assertIsDisplayed()
    }

    @Test
    fun canonicalMassScenarioShowsPerKgSavings() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1")
        composeRule.selectUnit("${TEST_TAG_OFFER_B}_unit", R.string.unit_name_kilogram)

        val ctx = composeRule.activity
        val winner = ctx.winnerHeadline(R.string.offer_b_title)
        val savings = ctx.getString(
            R.string.result_savings_per_kg,
            "1",
            ctx.getString(R.string.offer_a_title),
        )

        composeRule.onNodeWithText(winner).assertIsDisplayed()
        composeRule.onNodeWithText(savings).assertIsDisplayed()
    }

    @Test
    fun volumeScenarioShowsPerLSavings() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("1.20")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.selectUnit("${TEST_TAG_OFFER_A}_unit", R.string.unit_name_millilitre)
        composeRule.onNodeWithTag("offerB_price").performTextInput("2.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1")
        composeRule.selectUnit("${TEST_TAG_OFFER_B}_unit", R.string.unit_name_litre)

        val ctx = composeRule.activity
        val savings = ctx.getString(
            R.string.result_savings_per_L,
            "0.4",
            ctx.getString(R.string.offer_a_title),
        )
        composeRule.onNodeWithText(savings).assertIsDisplayed()
    }

    @Test
    fun countScenarioShowsPerPieceSavings() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("3.00")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("6")
        composeRule.selectUnit("${TEST_TAG_OFFER_A}_unit", R.string.unit_name_piece)
        composeRule.onNodeWithTag("offerB_price").performTextInput("5.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("12")
        composeRule.selectUnit("${TEST_TAG_OFFER_B}_unit", R.string.unit_name_piece)

        val ctx = composeRule.activity
        val savings = ctx.getString(
            R.string.result_savings_per_piece,
            "0.083333",
            ctx.getString(R.string.offer_a_title),
        )
        composeRule.onNodeWithText(savings).assertIsDisplayed()
    }

    @Test
    fun changingUnitRecomputesResultLive() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1")

        val winner = composeRule.activity.winnerHeadline(R.string.offer_b_title)
        composeRule.onNodeWithText(winner).assertDoesNotExist()

        composeRule.selectUnit("${TEST_TAG_OFFER_B}_unit", R.string.unit_name_kilogram)
        composeRule.onNodeWithText(winner).assertIsDisplayed()
    }

    @Test
    fun lengthScenarioShowsPerMetreSavings() {
        // Issue #56: 5 m @ 49 vs 250 cm @ 22 → B wins (8.80/m vs 9.80/m).
        composeRule.onNodeWithTag("offerA_price").performTextInput("49")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("5")
        composeRule.selectUnit("${TEST_TAG_OFFER_A}_unit", R.string.unit_name_metre)
        composeRule.onNodeWithTag("offerB_price").performTextInput("22")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("250")
        composeRule.selectUnit("${TEST_TAG_OFFER_B}_unit", R.string.unit_name_centimetre)

        val ctx = composeRule.activity
        val savings = ctx.getString(
            R.string.result_savings_per_m,
            "1",
            ctx.getString(R.string.offer_a_title),
        )
        val percent = ctx.getString(R.string.result_savings_percent, "10.2")

        composeRule.onNodeWithText(ctx.winnerHeadline(R.string.offer_b_title)).assertIsDisplayed()
        composeRule.onNodeWithText(savings).assertIsDisplayed()
        composeRule.onNodeWithText(percent).assertIsDisplayed()
    }

    @Test
    fun unitMenuListsLengthUnitsBetweenVolumeAndPieces() {
        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_unit").performClick()
        val ctx = composeRule.activity
        val tops = listOf(
            R.string.unit_name_gram,
            R.string.unit_name_kilogram,
            R.string.unit_name_millilitre,
            R.string.unit_name_litre,
            R.string.unit_name_centimetre,
            R.string.unit_name_metre,
            R.string.unit_name_piece,
        ).map { nameRes ->
            composeRule.onNodeWithContentDescription(ctx.getString(nameRes))
                .fetchSemanticsNode().boundsInRoot.top
        }

        assertEquals(tops.sorted(), tops)
    }
}
