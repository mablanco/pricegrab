package com.mablanco.pricegrab.data.currency

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyPreferencesTest {

    @Test
    fun defaultsAreAutomaticWithEmptyCustomSymbol() {
        val prefs = CurrencyPreferences()
        assertEquals(CurrencyMode.Automatic, prefs.mode)
        assertEquals("", prefs.customSymbol)
    }

    @Test
    fun storageRoundTrips() {
        CurrencyMode.entries.forEach { mode ->
            assertEquals(mode, CurrencyMode.fromStorage(mode.toStorage()))
        }
    }

    @Test
    fun missingOrUnknownStorageMapsToAutomatic() {
        assertEquals(CurrencyMode.Automatic, CurrencyMode.fromStorage(null))
        assertEquals(CurrencyMode.Automatic, CurrencyMode.fromStorage(""))
        assertEquals(CurrencyMode.Automatic, CurrencyMode.fromStorage("CUSTOM"))
    }

    @Test
    fun customSymbolIsCappedAtFourCharacters() {
        assertEquals("ABCD", capCustomSymbol("ABCDE"))
        assertEquals("zł", capCustomSymbol("zł"))
        assertEquals("", capCustomSymbol(""))
    }
}
