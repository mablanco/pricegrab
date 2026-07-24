package com.mablanco.pricegrab.data.appearance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppearancePreferencesTest {

    @Test
    fun defaultsAreSystemThemeAndMaterialYouOff() {
        val prefs = AppearancePreferences()
        assertEquals(AppearanceMode.System, prefs.mode)
        assertFalse(prefs.materialYouEnabled)
    }

    @Test
    fun fromStorageMapsKnownValues() {
        assertEquals(AppearanceMode.System, AppearanceMode.fromStorage("system"))
        assertEquals(AppearanceMode.Light, AppearanceMode.fromStorage("light"))
        assertEquals(AppearanceMode.Dark, AppearanceMode.fromStorage("dark"))
    }

    @Test
    fun fromStorageMapsMissingOrInvalidToSystem() {
        assertEquals(AppearanceMode.System, AppearanceMode.fromStorage(null))
        assertEquals(AppearanceMode.System, AppearanceMode.fromStorage(""))
        assertEquals(AppearanceMode.System, AppearanceMode.fromStorage("auto"))
        assertEquals(AppearanceMode.System, AppearanceMode.fromStorage("SYSTEM"))
    }

    @Test
    fun toStorageRoundTrips() {
        AppearanceMode.entries.forEach { mode ->
            assertEquals(mode, AppearanceMode.fromStorage(mode.toStorage()))
        }
    }
}
