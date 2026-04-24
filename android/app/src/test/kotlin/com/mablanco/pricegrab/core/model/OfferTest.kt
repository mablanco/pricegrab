package com.mablanco.pricegrab.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal

class OfferTest {

    @Test
    fun `zero price is allowed and yields zero unit price`() {
        val offer = Offer(price = BigDecimal.ZERO, quantity = BigDecimal("5"))
        assertEquals(0, BigDecimal.ZERO.compareTo(offer.unitPrice))
    }

    @Test
    fun `unit price is price divided by quantity at decimal64 precision`() {
        val offer = Offer(price = BigDecimal("2.50"), quantity = BigDecimal("500"))
        assertEquals(0, BigDecimal("0.005").compareTo(offer.unitPrice))
    }

    @Test
    fun `fractional quantity is supported`() {
        val offer = Offer(price = BigDecimal("1.00"), quantity = BigDecimal("0.5"))
        assertEquals(0, BigDecimal("2.00").compareTo(offer.unitPrice))
    }

    @Test
    fun `negative price is rejected`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            Offer(price = BigDecimal("-0.01"), quantity = BigDecimal("1"))
        }
        assertEquals(true, ex.message!!.contains("price"))
    }

    @Test
    fun `zero quantity is rejected`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            Offer(price = BigDecimal("1"), quantity = BigDecimal.ZERO)
        }
        assertEquals(true, ex.message!!.contains("quantity"))
    }

    @Test
    fun `negative quantity is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Offer(price = BigDecimal("1"), quantity = BigDecimal("-1"))
        }
    }
}
