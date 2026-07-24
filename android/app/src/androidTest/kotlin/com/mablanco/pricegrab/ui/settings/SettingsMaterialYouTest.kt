package com.mablanco.pricegrab.ui.settings

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.data.appearance.AppearancePreferencesRepository
import com.mablanco.pricegrab.data.appearance.DYNAMIC_COLOR_MIN_SDK
import com.mablanco.pricegrab.ui.compare.TEST_TAG_SETTINGS_OPEN
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsMaterialYouTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearAppearancePreferences() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking {
            AppearancePreferencesRepository(context).clear()
        }
    }

    @Test
    fun materialYouSwitchDefaultsOffAndRespectsDeviceCapability() {
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_OPEN).performClick()
        composeRule.onNodeWithTag(TEST_TAG_SETTINGS_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_MATERIAL_YOU_SWITCH).assertIsOff()

        if (Build.VERSION.SDK_INT >= DYNAMIC_COLOR_MIN_SDK) {
            composeRule.onNodeWithTag(TEST_TAG_MATERIAL_YOU_SWITCH).assertIsEnabled()
            composeRule.onNodeWithTag(TEST_TAG_MATERIAL_YOU_SWITCH).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(TEST_TAG_MATERIAL_YOU_SWITCH).assertIsOn()
            composeRule.onNodeWithTag(TEST_TAG_MATERIAL_YOU_SWITCH).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(TEST_TAG_MATERIAL_YOU_SWITCH).assertIsOff()
        } else {
            composeRule.onNodeWithTag(TEST_TAG_MATERIAL_YOU_SWITCH).assertIsNotEnabled()
        }
    }
}
