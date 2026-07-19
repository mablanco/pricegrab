package com.mablanco.pricegrab.core.calc

import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.Offer
import com.mablanco.pricegrab.core.model.QuantityUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * Canonical test suite for the contract documented at
 * `specs/001-unit-price-comparison/contracts/price-comparator.md`,
 * extended by feature 005 multi-offer ranking.
 */
class PriceComparatorTest {

    @Test
    fun `case 1 - supermarket sizes, B wins with 20 percent savings`() {
        val outcome = PriceComparator.compare(
            a = offer("2.50", "500", QuantityUnit.Gram),
            b = offer("4.00", "1000", QuantityUnit.Gram),
        )
        assertWinner(outcome, index = 1, expectedDelta = "0.001", expectedPercent = "20")
    }

    @Test
    fun `case 2 - B wins with non-terminating percent`() {
        val outcome = PriceComparator.compare(
            a = offer("3.00", "1", QuantityUnit.Gram),
            b = offer("5.00", "2", QuantityUnit.Gram),
        )
        assertTrue(outcome is ComparisonOutcome.Winner)
        val w = outcome as ComparisonOutcome.Winner
        assertEquals(1, w.slotIndex)
        assertEquals(0, BigDecimal("0.5").compareTo(w.perUnitDelta))
        assertApprox("16.666666666", w.percentDelta, tolerance = "0.00001")
    }

    @Test
    fun `case 3 - tie with different scales`() {
        val outcome = PriceComparator.compare(
            a = offer("2.00", "100", QuantityUnit.Gram),
            b = offer("4.00", "200", QuantityUnit.Gram),
        )
        assertEquals(ComparisonOutcome.Tie, outcome)
    }

    @Test
    fun `case 4 - A is free, A wins with 100 percent savings`() {
        val outcome = PriceComparator.compare(
            a = offer("0", "5", QuantityUnit.Gram),
            b = offer("1", "5", QuantityUnit.Gram),
        )
        assertWinner(outcome, index = 0, expectedDelta = "0.2", expectedPercent = "100")
    }

    @Test
    fun `case 5 - both offers free is a tie`() {
        val outcome = PriceComparator.compare(
            a = offer("0", "5", QuantityUnit.Gram),
            b = offer("0", "10", QuantityUnit.Gram),
        )
        assertEquals(ComparisonOutcome.Tie, outcome)
    }

    @Test
    fun `case 6 - fractional quantities, A wins`() {
        val outcome = PriceComparator.compare(
            a = offer("1.00", "0.5", QuantityUnit.Gram),
            b = offer("1.00", "0.25", QuantityUnit.Gram),
        )
        assertWinner(outcome, index = 0, expectedDelta = "2.0", expectedPercent = "50")
    }

    @Test
    fun `case 7 - identical inputs are a tie`() {
        val outcome = PriceComparator.compare(
            a = offer("1.00", "3", QuantityUnit.Gram),
            b = offer("1.00", "3", QuantityUnit.Gram),
        )
        assertEquals(ComparisonOutcome.Tie, outcome)
    }

    @Test
    fun `case 8 - very small values still produce a deterministic winner`() {
        val outcome = PriceComparator.compare(
            a = offer("0.01", "1000000", QuantityUnit.Gram),
            b = offer("0.01", "999999", QuantityUnit.Gram),
        )
        assertTrue("Expected Winner(0), got $outcome", outcome is ComparisonOutcome.Winner)
        val w = outcome as ComparisonOutcome.Winner
        assertEquals(0, w.slotIndex)
        assertTrue("perUnitDelta must be positive", w.perUnitDelta.signum() > 0)
        assertTrue("percentDelta must be positive", w.percentDelta.signum() > 0)
    }

    @Test
    fun `case 9 - very large values, A wins with tiny percent`() {
        val outcome = PriceComparator.compare(
            a = offer("999999999", "1", QuantityUnit.Gram),
            b = offer("1000000000", "1", QuantityUnit.Gram),
        )
        assertTrue(outcome is ComparisonOutcome.Winner)
        val w = outcome as ComparisonOutcome.Winner
        assertEquals(0, w.slotIndex)
        assertEquals(0, BigDecimal("1").compareTo(w.perUnitDelta))
        assertApprox("0.0000001", w.percentDelta, tolerance = "0.0000000001")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `case 10 - negative price is rejected upstream by Offer`() {
        Offer(price = BigDecimal("-1"), quantity = BigDecimal("5"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `case 11 - zero quantity is rejected upstream by Offer`() {
        Offer(price = BigDecimal("1"), quantity = BigDecimal.ZERO)
    }

    @Test
    fun `U1 - 500 g vs 1 kg, B wins with base delta one thousandth`() {
        val outcome = PriceComparator.compare(
            a = offer("2.50", "500", QuantityUnit.Gram),
            b = offer("4.00", "1", QuantityUnit.Kilogram),
        )
        assertWinner(outcome, index = 1, expectedDelta = "0.001", expectedPercent = "20")
    }

    @Test
    fun `U4 - equal base unit price across different gram quantities is a tie`() {
        val outcome = PriceComparator.compare(
            a = offer("2.00", "100", QuantityUnit.Gram),
            b = offer("4.00", "200", QuantityUnit.Gram),
        )
        assertEquals(ComparisonOutcome.Tie, outcome)
    }

    @Test
    fun `U6 - free offer with units unchanged`() {
        val outcome = PriceComparator.compare(
            a = offer("0", "5", QuantityUnit.Gram),
            b = offer("1", "5", QuantityUnit.Gram),
        )
        assertWinner(outcome, index = 0, expectedDelta = "0.2", expectedPercent = "100")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `U7 - comparing mass and volume throws`() {
        PriceComparator.compare(
            a = offer("2.50", "500", QuantityUnit.Gram),
            b = offer("4.00", "500", QuantityUnit.Millilitre),
        )
    }

    @Test
    fun `swapping A and B flips the winner and preserves magnitudes`() {
        val a = offer("2.50", "500", QuantityUnit.Gram)
        val b = offer("4.00", "1000", QuantityUnit.Gram)

        val forward = PriceComparator.compare(a, b) as ComparisonOutcome.Winner
        val backward = PriceComparator.compare(b, a) as ComparisonOutcome.Winner

        assertEquals(1, forward.slotIndex)
        assertEquals(0, backward.slotIndex)
        assertEquals(0, forward.perUnitDelta.compareTo(backward.perUnitDelta))
        assertApprox(forward.percentDelta.toPlainString(), backward.percentDelta, tolerance = "0.00000001")
    }

    // ---- Feature 005: three-offer ranking ------------------------------------

    @Test
    fun `three offers - unique cheapest wins vs second cheapest`() {
        // unit prices: A=0.005, B=0.004, C=0.006 → B wins vs A (0.001 / 20%)
        val outcome = PriceComparator.compareMany(
            listOf(
                0 to offer("2.50", "500", QuantityUnit.Gram),
                1 to offer("4.00", "1000", QuantityUnit.Gram),
                2 to offer("3.00", "500", QuantityUnit.Gram),
            ),
        )
        assertWinner(outcome, index = 1, expectedDelta = "0.001", expectedPercent = "20")
    }

    @Test
    fun `three offers - top two tie yields Tie`() {
        val outcome = PriceComparator.compareMany(
            listOf(
                0 to offer("2.00", "100", QuantityUnit.Gram),
                1 to offer("4.00", "200", QuantityUnit.Gram),
                2 to offer("5.00", "100", QuantityUnit.Gram),
            ),
        )
        assertEquals(ComparisonOutcome.Tie, outcome)
    }

    @Test
    fun `three offers - free offer wins with 100 percent vs second`() {
        val outcome = PriceComparator.compareMany(
            listOf(
                0 to offer("1", "5", QuantityUnit.Gram),
                1 to offer("0", "5", QuantityUnit.Gram),
                2 to offer("2", "5", QuantityUnit.Gram),
            ),
        )
        assertWinner(outcome, index = 1, expectedDelta = "0.2", expectedPercent = "100")
    }

    @Test
    fun `three offers - unique min wins even when others tie for second`() {
        // A=0.003, B=0.005, C=0.005 → A wins vs second (0.002 / 40%)
        val outcome = PriceComparator.compareMany(
            listOf(
                0 to offer("3.00", "1000", QuantityUnit.Gram),
                1 to offer("5.00", "1000", QuantityUnit.Gram),
                2 to offer("2.50", "500", QuantityUnit.Gram),
            ),
        )
        assertWinner(outcome, index = 0, expectedDelta = "0.002", expectedPercent = "40")
    }

    @Test
    fun `compareMany preserves UI indices when slots are sparse`() {
        // UI indices 0 and 2 (slot 1 blank / omitted)
        val outcome = PriceComparator.compareMany(
            listOf(
                0 to offer("2.50", "500", QuantityUnit.Gram),
                2 to offer("4.00", "1000", QuantityUnit.Gram),
            ),
        )
        assertWinner(outcome, index = 2, expectedDelta = "0.001", expectedPercent = "20")
    }

    // ---- Helpers -------------------------------------------------------------

    private fun offer(price: String, quantity: String, unit: QuantityUnit): Offer =
        Offer(price = BigDecimal(price), quantity = BigDecimal(quantity), quantityUnit = unit)

    private fun assertWinner(
        outcome: ComparisonOutcome,
        index: Int,
        expectedDelta: String,
        expectedPercent: String,
    ) {
        assertTrue("Expected Winner($index), got $outcome", outcome is ComparisonOutcome.Winner)
        val w = outcome as ComparisonOutcome.Winner
        assertEquals(index, w.slotIndex)
        assertEquals(0, BigDecimal(expectedDelta).compareTo(w.perUnitDelta))
        assertEquals(0, BigDecimal(expectedPercent).compareTo(w.percentDelta))
    }

    private fun assertApprox(expected: String, actual: BigDecimal, tolerance: String) {
        val diff = actual.subtract(BigDecimal(expected)).abs()
        assertTrue(
            "actual=$actual, expected≈$expected, diff=$diff > tolerance=$tolerance",
            diff.compareTo(BigDecimal(tolerance)) <= 0,
        )
    }
}
