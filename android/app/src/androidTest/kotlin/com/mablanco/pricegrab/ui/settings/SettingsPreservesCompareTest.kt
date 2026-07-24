package com.mablanco.pricegrab.ui.settings

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.data.appearance.AppearancePreferencesRepository
import com.mablanco.pricegrab.ui.compare.TEST_TAG_SETTINGS_OPEN
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsPreservesCompareTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearAppearancePreferences() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking {
            AppearancePreferencesRepository(context).clear()
        }
        composeRule.waitForIdle()
    }

    @Test
    fun compareInputsSurviveSettingsRoundTripAndThemeChange() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2.50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4.00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")

        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_OPEN).performClick()
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_THEME_LIGHT).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_BACK).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("offerA_price").assert(hasEditableText("2.50"))
        composeRule.onNodeWithTag("offerA_quantity").assert(hasEditableText("500"))
        composeRule.onNodeWithTag("offerB_price").assert(hasEditableText("4.00"))
        composeRule.onNodeWithTag("offerB_quantity").assert(hasEditableText("1000"))
    }

    private fun hasEditableText(expected: String): SemanticsMatcher =
        SemanticsMatcher("EditableText equals \"$expected\"") { node ->
            node.config.contains(SemanticsProperties.EditableText) &&
                node.config[SemanticsProperties.EditableText].text == expected
        }
}
