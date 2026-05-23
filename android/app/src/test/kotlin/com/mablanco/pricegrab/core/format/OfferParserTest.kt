package com.mablanco.pricegrab.core.format

import com.mablanco.pricegrab.core.model.OfferParseResult
import com.mablanco.pricegrab.core.model.QuantityUnit
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
        val result = OfferParser.parse("2.50", "500", QuantityUnit.Gram, enUs)
        assertTrue(result is OfferParseResult.Success)
        val success = result as OfferParseResult.Success
        assertEquals(0, BigDecimal("2.50").compareTo(success.offer.price))
        assertEquals(0, BigDecimal("500").compareTo(success.offer.quantity))
        assertEquals(QuantityUnit.Gram, success.offer.quantityUnit)
    }

    @Test
    fun `happy path es-ES with comma decimal separator`() {
        val result = OfferParser.parse("2,50", "500", QuantityUnit.Gram, esEs)
        assertTrue(result is OfferParseResult.Success)
    }

    @Test
    fun `kilogram unit round-trips on success`() {
        val result = OfferParser.parse("4.00", "1", QuantityUnit.Kilogram, enUs)
        assertTrue(result is OfferParseResult.Success)
        assertEquals(QuantityUnit.Kilogram, (result as OfferParseResult.Success).offer.quantityUnit)
    }

    @Test
    fun `millilitre unit round-trips on success`() {
        val result = OfferParser.parse("1.20", "500", QuantityUnit.Millilitre, enUs)
        assertTrue(result is OfferParseResult.Success)
        assertEquals(QuantityUnit.Millilitre, (result as OfferParseResult.Success).offer.quantityUnit)
    }

    @Test
    fun `litre unit round-trips on success`() {
        val result = OfferParser.parse("2.00", "1", QuantityUnit.Litre, enUs)
        assertTrue(result is OfferParseResult.Success)
        assertEquals(QuantityUnit.Litre, (result as OfferParseResult.Success).offer.quantityUnit)
    }

    @Test
    fun `piece unit round-trips on success`() {
        val result = OfferParser.parse("3.00", "6", QuantityUnit.Piece, enUs)
        assertTrue(result is OfferParseResult.Success)
        assertEquals(QuantityUnit.Piece, (result as OfferParseResult.Success).offer.quantityUnit)
    }

    @Test
    fun `empty price yields EmptyPrice`() {
        assertEquals(OfferParseResult.EmptyPrice, OfferParser.parse("", "5", QuantityUnit.Gram, enUs))
        assertEquals(OfferParseResult.EmptyPrice, OfferParser.parse("   ", "5", QuantityUnit.Gram, enUs))
    }

    @Test
    fun `empty quantity yields EmptyQuantity`() {
        assertEquals(OfferParseResult.EmptyQuantity, OfferParser.parse("2.50", "", QuantityUnit.Gram, enUs))
    }

    @Test
    fun `unparseable price yields InvalidPrice with the raw string preserved`() {
        val result = OfferParser.parse("abc", "5", QuantityUnit.Gram, enUs)
        assertTrue(result is OfferParseResult.InvalidPrice)
        assertEquals("abc", (result as OfferParseResult.InvalidPrice).raw)
    }

    @Test
    fun `unparseable quantity yields InvalidQuantity with the raw string preserved`() {
        val result = OfferParser.parse("2.50", "xyz", QuantityUnit.Gram, enUs)
        assertTrue(result is OfferParseResult.InvalidQuantity)
        assertEquals("xyz", (result as OfferParseResult.InvalidQuantity).raw)
    }

    @Test
    fun `negative price yields NegativePrice`() {
        val result = OfferParser.parse("-1", "5", QuantityUnit.Gram, enUs)
        assertEquals(OfferParseResult.NegativePrice, result)
    }

    @Test
    fun `zero quantity yields NonPositiveQuantity`() {
        val result = OfferParser.parse("1", "0", QuantityUnit.Gram, enUs)
        assertEquals(OfferParseResult.NonPositiveQuantity, result)
    }

    @Test
    fun `negative quantity yields NonPositiveQuantity`() {
        val result = OfferParser.parse("1", "-5", QuantityUnit.Gram, enUs)
        assertEquals(OfferParseResult.NonPositiveQuantity, result)
    }

    @Test
    fun `zero price with positive quantity is allowed (free product)`() {
        val result = OfferParser.parse("0", "5", QuantityUnit.Gram, enUs)
        assertTrue(result is OfferParseResult.Success)
    }

    @Test
    fun `dot-decimal input in es-ES is rejected, not silently mis-parsed`() {
        val result = OfferParser.parse("2.50", "500", QuantityUnit.Gram, esEs)
        assertTrue(
            "Expected InvalidPrice for dot-decimal in es-ES, got $result",
            result is OfferParseResult.InvalidPrice,
        )
    }
}
