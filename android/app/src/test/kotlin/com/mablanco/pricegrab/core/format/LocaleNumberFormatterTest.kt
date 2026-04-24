package com.mablanco.pricegrab.core.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class LocaleNumberFormatterTest {

    private val enUs: Locale = Locale.forLanguageTag("en-US")
    private val esEs: Locale = Locale.forLanguageTag("es-ES")

    // ---- parse ---------------------------------------------------------------

    @Test
    fun `parse en-US accepts dot as decimal separator`() {
        val result = LocaleNumberFormatter.parse("2.50", enUs)
        assertNotNull(result)
        assertEquals(0, BigDecimal("2.50").compareTo(result))
    }

    @Test
    fun `parse es-ES accepts comma as decimal separator`() {
        val result = LocaleNumberFormatter.parse("2,50", esEs)
        assertNotNull(result)
        assertEquals(0, BigDecimal("2.50").compareTo(result))
    }

    @Test
    fun `parse en-US rejects trailing garbage`() {
        val result = LocaleNumberFormatter.parse("2.50abc", enUs)
        assertNull(result)
    }

    @Test
    fun `parse returns null for blank input`() {
        assertNull(LocaleNumberFormatter.parse("", enUs))
        assertNull(LocaleNumberFormatter.parse("   ", enUs))
    }

    @Test
    fun `parse es-ES with grouping disabled rejects dot as thousands separator`() {
        // "2.50" in es-ES with grouping disabled is not a valid number — we prefer
        // rejecting it over silently parsing it as 250.
        val result = LocaleNumberFormatter.parse("2.50", esEs)
        assertNull(result)
    }

    @Test
    fun `parse preserves precision via BigDecimal (no float round-trip)`() {
        val result = LocaleNumberFormatter.parse("0.1", enUs)
        assertNotNull(result)
        assertEquals(0, BigDecimal("0.1").compareTo(result))
    }

    @Test
    fun `parse supports integer input`() {
        val result = LocaleNumberFormatter.parse("42", enUs)
        assertNotNull(result)
        assertEquals(0, BigDecimal("42").compareTo(result))
    }

    @Test
    fun `parse returns null for non-numeric input`() {
        assertNull(LocaleNumberFormatter.parse("abc", enUs))
    }

    // ---- format --------------------------------------------------------------

    @Test
    fun `format en-US uses dot as decimal separator`() {
        val result = LocaleNumberFormatter.format(
            BigDecimal("2.5"),
            enUs,
            minFractionDigits = 2,
            maxFractionDigits = 2,
        )
        assertEquals("2.50", result)
    }

    @Test
    fun `format es-ES uses comma as decimal separator`() {
        val result = LocaleNumberFormatter.format(
            BigDecimal("2.5"),
            esEs,
            minFractionDigits = 2,
            maxFractionDigits = 2,
        )
        assertEquals("2,50", result)
    }

    @Test
    fun `format strips trailing zeros when min fraction digits is zero`() {
        val result = LocaleNumberFormatter.format(
            BigDecimal("2.5000"),
            enUs,
            minFractionDigits = 0,
            maxFractionDigits = 6,
        )
        assertEquals("2.5", result)
    }

    @Test
    fun `format can disable grouping`() {
        val result = LocaleNumberFormatter.format(
            BigDecimal("1234567"),
            enUs,
            useGrouping = false,
        )
        assertEquals("1234567", result)
    }

    // ---- decimalSeparator ----------------------------------------------------

    @Test
    fun `decimalSeparator returns locale-correct character`() {
        assertEquals('.', LocaleNumberFormatter.decimalSeparator(enUs))
        assertEquals(',', LocaleNumberFormatter.decimalSeparator(esEs))
    }

    // ---- Sanity across many locales -----------------------------------------

    @Test
    fun `parse and format are round-trip consistent for a variety of locales`() {
        val locales = listOf(
            Locale.forLanguageTag("en-US"),
            Locale.forLanguageTag("es-ES"),
            Locale.forLanguageTag("fr-FR"),
            Locale.forLanguageTag("de-DE"),
        )
        for (locale in locales) {
            val formatted = LocaleNumberFormatter.format(
                BigDecimal("1234.5"),
                locale,
                minFractionDigits = 1,
                maxFractionDigits = 1,
                useGrouping = false,
            )
            val parsed = LocaleNumberFormatter.parse(formatted, locale)
            assertNotNull("round-trip failed for $locale (formatted=$formatted)", parsed)
            assertTrue(
                "round-trip mismatch for $locale: $formatted -> $parsed",
                BigDecimal("1234.5").compareTo(parsed) == 0,
            )
        }
    }
}
