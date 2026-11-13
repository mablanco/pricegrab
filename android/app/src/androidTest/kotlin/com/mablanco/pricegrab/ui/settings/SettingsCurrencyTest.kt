package com.mablanco.pricegrab.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.data.appearance.AppearancePreferencesRepository
import com.mablanco.pricegrab.data.currency.CurrencyPreferences
import com.mablanco.pricegrab.data.currency.resolveCurrencySymbol
import com.mablanco.pricegrab.ui.compare.TEST_TAG_OFFER_A
import com.mablanco.pricegrab.ui.compare.TEST_TAG_SETTINGS_OPEN
import com.mablanco.pricegrab.ui.compare.priceLabel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Feature 010 / US2: currency symbol choice in Settings. */
@RunWith(AndroidJUnit4::class)
class SettingsCurrencyTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    @After
    fun clearPreferences() {
        runBlocking { AppearancePreferencesRepository(context).clear() }
        composeRule.waitForIdle()
    }

    @Test
    fun automaticIsSelectedByDefaultAndShowsTheResolvedSymbol() {
        openSettings()

        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_AUTOMATIC).performScrollTo().assertIsSelected()
        composeRule.onNodeWithText(automaticLabel()).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_SYMBOL_FIELD).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun customSymbolReachesCompare() {
        openSettings()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_CUSTOM).performScrollTo().performClick()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_SYMBOL_FIELD).assertIsEnabled().performTextInput("zł")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_BACK).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price_label", useUnmergedTree = true)
            .assertTextEquals(context.priceLabel("zł"))
    }

    @Test
    fun customSymbolIsCappedAtFourCharacters() {
        openSettings()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_CUSTOM).performScrollTo().performClick()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_SYMBOL_FIELD).performTextInput("ABCDE")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("ABCD").assertIsDisplayed()
        composeRule.onNodeWithText("ABCDE").assertDoesNotExist()
    }

    @Test
    fun emptyCustomSymbolHidesTheSymbol() {
        openSettings()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_CUSTOM).performScrollTo().performClick()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_SYMBOL_FIELD).performTextInput("zł")
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_SYMBOL_FIELD).performTextClearance()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_BACK).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price_label", useUnmergedTree = true)
            .assertTextEquals(context.priceLabel(""))
    }

    @Test
    fun switchingBackToAutomaticRestoresTheRegionSymbol() {
        openSettings()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_CUSTOM).performScrollTo().performClick()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_SYMBOL_FIELD).performTextInput("zł")
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_AUTOMATIC).performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TEST_TAG_CURRENCY_AUTOMATIC).assertIsSelected()
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_BACK).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("${TEST_TAG_OFFER_A}_price_label", useUnmergedTree = true)
            .assertTextEquals(context.priceLabel(automaticSymbol()))
    }

    private fun openSettings() {
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_OPEN).performClick()
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_SCREEN).assertIsDisplayed()
    }

    private fun automaticSymbol(): String =
        resolveCurrencySymbol(CurrencyPreferences(), context.resources.configuration.locales[0])

    private fun automaticLabel(): String {
        val symbol = automaticSymbol()
        return if (symbol.isEmpty()) {
            context.getString(R.string.settings_currency_automatic_none)
        } else {
            context.getString(R.string.settings_currency_automatic, symbol)
        }
    }
}
