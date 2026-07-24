package com.mablanco.pricegrab.ui.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.data.appearance.AppearancePreferences
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsLargeFontTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsControlsRemainDisplayedAtLargeFontScale() {
        val themeSection = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getString(R.string.settings_theme_section)

        composeRule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = baseDensity.density,
                    fontScale = 2.0f,
                ),
            ) {
                PriceGrabTheme {
                    SettingsScreen(
                        preferences = AppearancePreferences(),
                        materialYouSupported = true,
                        onModeSelected = {},
                        onMaterialYouChange = {},
                        onBack = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_THEME_SYSTEM).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_THEME_LIGHT).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_THEME_DARK).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_MATERIAL_YOU_SWITCH)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(themeSection).assertIsDisplayed()
    }
}
