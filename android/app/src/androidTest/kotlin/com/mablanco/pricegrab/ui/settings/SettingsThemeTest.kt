package com.mablanco.pricegrab.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
class SettingsThemeTest {

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
    fun settingsReachableAndThemeSelectionUpdates() {
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_OPEN).performClick()
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_THEME_SYSTEM).assertIsSelected()

        composeRule.onNodeWithTag(TEST_TAG_THEME_LIGHT).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TEST_TAG_THEME_LIGHT).assertIsSelected()

        composeRule.onNodeWithTag(TEST_TAG_THEME_DARK).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TEST_TAG_THEME_DARK).assertIsSelected()

        composeRule.onNodeWithTag(TEST_TAG_THEME_SYSTEM).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TEST_TAG_THEME_SYSTEM).assertIsSelected()
    }
}
