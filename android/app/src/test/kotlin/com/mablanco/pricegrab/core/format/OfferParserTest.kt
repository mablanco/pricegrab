package com.mablanco.pricegrab.core.format

import com.mablanco.pricegrab.core.model.OfferParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class OfferParserTest {

    private val enUs: Locale = Locale.forLanguageTag("en-US")
    private val esEs: Locale = Locale.forLanguageTag("es-ES")

    @Test
    fun `happy path en-US`() {
        val result = OfferParser.parse("2.50", "500", enUs)
        assertTrue(result is OfferParseResult.Success)
        val success = result as OfferParseResult.Success
        assertEquals(0, BigDecimal("2.50").compareTo(success.offer.price))
        assertEquals(0, BigDecimal("500").compareTo(success.offer.quantity))
    }

    @Test
    fun `happy path es-ES with comma decimal separator`() {
        val result = OfferParser.parse("2,50", "500", esEs)
        assertTrue(result is OfferParseResult.Success)
    }

    @Test
    fun `empty price yields EmptyPrice`() {
        assertEquals(OfferParseResult.EmptyPrice, OfferParser.parse("", "5", enUs))
        assertEquals(OfferParseResult.EmptyPrice, OfferParser.parse("   ", "5", enUs))
    }

    @Test
    fun `empty quantity yields EmptyQuantity`() {
        assertEquals(OfferParseResult.EmptyQuantity, OfferParser.parse("2.50", "", enUs))
    }

    @Test
    fun `unparseable price yields InvalidPrice with the raw string preserved`() {
        val result = OfferParser.parse("abc", "5", enUs)
        assertTrue(result is OfferParseResult.InvalidPrice)
        assertEquals("abc", (result as OfferParseResult.InvalidPrice).raw)
    }

    @Test
    fun `unparseable quantity yields InvalidQuantity with the raw string preserved`() {
        val result = OfferParser.parse("2.50", "xyz", enUs)
        assertTrue(result is OfferParseResult.InvalidQuantity)
        assertEquals("xyz", (result as OfferParseResult.InvalidQuantity).raw)
    }

    @Test
    fun `negative price yields NegativePrice`() {
        val result = OfferParser.parse("-1", "5", enUs)
        assertEquals(OfferParseResult.NegativePrice, result)
    }

    @Test
    fun `zero quantity yields NonPositiveQuantity`() {
        val result = OfferParser.parse("1", "0", enUs)
        assertEquals(OfferParseResult.NonPositiveQuantity, result)
    }

    @Test
    fun `negative quantity yields NonPositiveQuantity`() {
        val result = OfferParser.parse("1", "-5", enUs)
        assertEquals(OfferParseResult.NonPositiveQuantity, result)
    }

    @Test
    fun `zero price with positive quantity is allowed (free product)`() {
        val result = OfferParser.parse("0", "5", enUs)
        assertTrue(result is OfferParseResult.Success)
    }

    @Test
    fun `dot-decimal input in es-ES is rejected, not silently mis-parsed`() {
        // In es-ES, dot is a grouping separator; we deliberately disable grouping
        // at parse time so "2.50" is rejected rather than parsed as 250.
        val result = OfferParser.parse("2.50", "500", esEs)
        assertTrue(
            "Expected InvalidPrice for dot-decimal in es-ES, got $result",
            result is OfferParseResult.InvalidPrice,
        )
    }
}
