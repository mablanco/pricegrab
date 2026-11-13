package com.mablanco.pricegrab.data.currency

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CurrencyResolveTest {

    private val automatic = CurrencyPreferences(mode = CurrencyMode.Automatic)

    @Test
    fun automaticUsesTheRegionCurrency() {
        assertEquals("€", resolveCurrencySymbol(automatic, Locale.forLanguageTag("es-ES")))
        assertEquals("$", resolveCurrencySymbol(automatic, Locale.forLanguageTag("en-US")))
        assertEquals("£", resolveCurrencySymbol(automatic, Locale.forLanguageTag("en-GB")))
    }

    @Test
    fun automaticWithoutRegionResolvesNoSymbol() {
        assertEquals("", resolveCurrencySymbol(automatic, Locale.forLanguageTag("en")))
        assertEquals("", resolveCurrencySymbol(automatic, Locale.forLanguageTag("es")))
    }

    @Test
    fun automaticIgnoresTheStoredCustomSymbol() {
        val prefs = CurrencyPreferences(mode = CurrencyMode.Automatic, customSymbol = "zł")
        assertEquals("€", resolveCurrencySymbol(prefs, Locale.forLanguageTag("es-ES")))
    }

    @Test
    fun customIsTrimmed() {
        val prefs = CurrencyPreferences(mode = CurrencyMode.Custom, customSymbol = " zł ")
        assertEquals("zł", resolveCurrencySymbol(prefs, Locale.forLanguageTag("en-US")))
    }

    @Test
    fun blankCustomMeansNoSymbol() {
        listOf("", "   ").forEach { blank ->
            val prefs = CurrencyPreferences(mode = CurrencyMode.Custom, customSymbol = blank)
            assertEquals("", resolveCurrencySymbol(prefs, Locale.forLanguageTag("es-ES")))
        }
    }
}
