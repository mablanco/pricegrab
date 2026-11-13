package com.mablanco.pricegrab.core.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MoneyFormatterTest {

    private val esEs: Locale = Locale.forLanguageTag("es-ES")
    private val enUs: Locale = Locale.forLanguageTag("en-US")

    @Test
    fun `spanish places the symbol after the amount with a non-breaking space`() {
        val affixes = MoneyFormatter.affixes("€", esEs)
        assertEquals(MoneyAffixes(prefix = "", suffix = " €"), affixes)
        assertEquals("1 €", MoneyFormatter.wrap("1", affixes))
    }

    @Test
    fun `us english places the symbol before the amount without a space`() {
        val affixes = MoneyFormatter.affixes("$", enUs)
        assertEquals(MoneyAffixes(prefix = "$", suffix = ""), affixes)
        assertEquals("$1", MoneyFormatter.wrap("1", affixes))
    }

    @Test
    fun `custom symbol follows the locale placement`() {
        assertEquals("1 zł", MoneyFormatter.wrap("1", MoneyFormatter.affixes("zł", esEs)))
        assertEquals("zł1", MoneyFormatter.wrap("1", MoneyFormatter.affixes("zł", enUs)))
    }

    @Test
    fun `empty symbol leaves the amount unchanged`() {
        val affixes = MoneyFormatter.affixes("", esEs)
        assertEquals(MoneyAffixes.NONE, affixes)
        assertEquals("0,4", MoneyFormatter.wrap("0,4", affixes))
    }

    @Test
    fun `wrapping keeps the amount text as given`() {
        assertEquals("$0.083333", MoneyFormatter.wrap("0.083333", MoneyFormatter.affixes("$", enUs)))
    }
}
