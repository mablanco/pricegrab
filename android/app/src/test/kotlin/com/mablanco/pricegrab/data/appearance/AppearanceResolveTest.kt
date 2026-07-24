package com.mablanco.pricegrab.data.appearance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppearanceResolveTest {

    @Test
    fun systemModeFollowsSystemDarkFlag() {
        val prefs = AppearancePreferences(mode = AppearanceMode.System)
        assertFalse(resolveAppearance(prefs, systemDark = false, sdkInt = 34).darkTheme)
        assertTrue(resolveAppearance(prefs, systemDark = true, sdkInt = 34).darkTheme)
    }

    @Test
    fun lightModeForcesLightRegardlessOfSystem() {
        val prefs = AppearancePreferences(mode = AppearanceMode.Light)
        assertFalse(resolveAppearance(prefs, systemDark = true, sdkInt = 34).darkTheme)
        assertFalse(resolveAppearance(prefs, systemDark = false, sdkInt = 34).darkTheme)
    }

    @Test
    fun darkModeForcesDarkRegardlessOfSystem() {
        val prefs = AppearancePreferences(mode = AppearanceMode.Dark)
        assertTrue(resolveAppearance(prefs, systemDark = false, sdkInt = 34).darkTheme)
        assertTrue(resolveAppearance(prefs, systemDark = true, sdkInt = 34).darkTheme)
    }

    @Test
    fun dynamicColorOnlyWhenEnabledAndSdkAtLeast31() {
        val enabled = AppearancePreferences(materialYouEnabled = true)
        assertTrue(resolveAppearance(enabled, systemDark = false, sdkInt = 31).useDynamicColor)
        assertTrue(resolveAppearance(enabled, systemDark = false, sdkInt = 35).useDynamicColor)
        assertFalse(resolveAppearance(enabled, systemDark = false, sdkInt = 30).useDynamicColor)
    }

    @Test
    fun dynamicColorFalseWhenUserOptedOutEvenOnApi31Plus() {
        val prefs = AppearancePreferences(materialYouEnabled = false)
        assertFalse(resolveAppearance(prefs, systemDark = false, sdkInt = 34).useDynamicColor)
    }

    @Test
    fun storedMaterialYouTrueStillYieldsBrandOnSdkBelow31() {
        val prefs = AppearancePreferences(
            mode = AppearanceMode.Dark,
            materialYouEnabled = true,
        )
        val resolved = resolveAppearance(prefs, systemDark = false, sdkInt = 29)
        assertTrue(resolved.darkTheme)
        assertFalse(resolved.useDynamicColor)
    }

    @Test
    fun allModeAndDynamicCombinationsMatchContract() {
        data class Case(
            val mode: AppearanceMode,
            val systemDark: Boolean,
            val materialYou: Boolean,
            val sdk: Int,
        )
        val cases = buildList {
            for (mode in AppearanceMode.entries) {
                for (systemDark in listOf(false, true)) {
                    for (materialYou in listOf(false, true)) {
                        for (sdk in listOf(24, 30, 31, 35)) {
                            add(Case(mode, systemDark, materialYou, sdk))
                        }
                    }
                }
            }
        }
        for (case in cases) {
            val prefs = AppearancePreferences(case.mode, case.materialYou)
            val resolved = resolveAppearance(prefs, case.systemDark, case.sdk)
            val expectedDark = when (case.mode) {
                AppearanceMode.System -> case.systemDark
                AppearanceMode.Light -> false
                AppearanceMode.Dark -> true
            }
            assertEquals(
                "mode=${case.mode} systemDark=${case.systemDark}",
                expectedDark,
                resolved.darkTheme,
            )
            assertEquals(
                "my=${case.materialYou} sdk=${case.sdk}",
                case.materialYou && case.sdk >= DYNAMIC_COLOR_MIN_SDK,
                resolved.useDynamicColor,
            )
        }
    }
}
