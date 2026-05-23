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
 * `specs/001-unit-price-comparison/contracts/price-comparator.md`.
 *
 * Each numbered case below maps 1:1 with the numbered row in that contract.
 */
class PriceComparatorTest {

    // ---- Canonical case 1: B is cheaper, round percent ----------------------

    @Test
    fun `case 1 - supermarket sizes, B wins with 20 percent savings`() {
        val outcome = PriceComparator.compare(
            a = offer("2.50", "500", QuantityUnit.Gram),
            b = offer("4.00", "1000", QuantityUnit.Gram),
        )
        assertBWins(outcome, expectedDelta = "0.001", expectedPercent = "20")
    }

    // ---- Canonical case 2: non-round percent, DECIMAL64 precision -----------

    @Test
    fun `case 2 - B wins with non-terminating percent`() {
        val outcome = PriceComparator.compare(
            a = offer("3.00", "1", QuantityUnit.Gram),
            b = offer("5.00", "2", QuantityUnit.Gram),
        )
        assertTrue(outcome is ComparisonOutcome.BWins)
        val b = outcome as ComparisonOutcome.BWins
        assertEquals(0, BigDecimal("0.5").compareTo(b.perUnitDelta))
        assertApprox("16.666666666", b.percentDelta, tolerance = "0.00001")
    }

    // ---- Canonical case 3: tie at different absolute scales -----------------

    @Test
    fun `case 3 - tie with different scales`() {
        val outcome = PriceComparator.compare(
            a = offer("2.00", "100", QuantityUnit.Gram),
            b = offer("4.00", "200", QuantityUnit.Gram),
        )
        assertEquals(ComparisonOutcome.Tie, outcome)
    }

    // ---- Canonical case 4: A is free -----------------------------------------

    @Test
    fun `case 4 - A is free, A wins with 100 percent savings`() {
        val outcome = PriceComparator.compare(
            a = offer("0", "5", QuantityUnit.Gram),
            b = offer("1", "5", QuantityUnit.Gram),
        )
        assertAWins(outcome, expectedDelta = "0.2", expectedPercent = "100")
    }

    // ---- Canonical case 5: both offers free ----------------------------------

    @Test
    fun `case 5 - both offers free is a tie`() {
        val outcome = PriceComparator.compare(
            a = offer("0", "5", QuantityUnit.Gram),
            b = offer("0", "10", QuantityUnit.Gram),
        )
        assertEquals(ComparisonOutcome.Tie, outcome)
    }

    // ---- Canonical case 6: fractional quantities -----------------------------

    @Test
    fun `case 6 - fractional quantities, A wins`() {
        val outcome = PriceComparator.compare(
            a = offer("1.00", "0.5", QuantityUnit.Gram),
            b = offer("1.00", "0.25", QuantityUnit.Gram),
        )
        assertAWins(outcome, expectedDelta = "2.0", expectedPercent = "50")
    }

    // ---- Canonical case 7: identical inputs ----------------------------------

    @Test
    fun `case 7 - identical inputs are a tie`() {
        val outcome = PriceComparator.compare(
            a = offer("1.00", "3", QuantityUnit.Gram),
            b = offer("1.00", "3", QuantityUnit.Gram),
        )
        assertEquals(ComparisonOutcome.Tie, outcome)
    }

    // ---- Canonical case 8: underflow-resistant very small values -------------

    @Test
    fun `case 8 - very small values still produce a deterministic winner`() {
        val outcome = PriceComparator.compare(
            a = offer("0.01", "1000000", QuantityUnit.Gram),
            b = offer("0.01", "999999", QuantityUnit.Gram),
        )
        assertTrue("Expected AWins, got $outcome", outcome is ComparisonOutcome.AWins)
        val a = outcome as ComparisonOutcome.AWins
        assertTrue("perUnitDelta must be positive", a.perUnitDelta.signum() > 0)
        assertTrue("percentDelta must be positive", a.percentDelta.signum() > 0)
    }

    // ---- Canonical case 9: overflow-resistant very large values --------------

    @Test
    fun `case 9 - very large values, A wins with tiny percent`() {
        val outcome = PriceComparator.compare(
            a = offer("999999999", "1", QuantityUnit.Gram),
            b = offer("1000000000", "1", QuantityUnit.Gram),
        )
        assertTrue(outcome is ComparisonOutcome.AWins)
        val a = outcome as ComparisonOutcome.AWins
        assertEquals(0, BigDecimal("1").compareTo(a.perUnitDelta))
        assertApprox("0.0000001", a.percentDelta, tolerance = "0.0000000001")
    }

    // ---- Canonical cases 10-11: invariant violations -------------------------

    @Test(expected = IllegalArgumentException::class)
    fun `case 10 - negative price is rejected upstream by Offer`() {
        Offer(price = BigDecimal("-1"), quantity = BigDecimal("5"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `case 11 - zero quantity is rejected upstream by Offer`() {
        Offer(price = BigDecimal("1"), quantity = BigDecimal.ZERO)
    }

    // ---- Feature 004 unit conversion cases -----------------------------------

    @Test
    fun `U1 - 500 g vs 1 kg, B wins with base delta one thousandth`() {
        val outcome = PriceComparator.compare(
            a = offer("2.50", "500", QuantityUnit.Gram),
            b = offer("4.00", "1", QuantityUnit.Kilogram),
        )
        assertBWins(outcome, expectedDelta = "0.001", expectedPercent = "20")
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
        assertAWins(outcome, expectedDelta = "0.2", expectedPercent = "100")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `U7 - comparing mass and volume throws`() {
        PriceComparator.compare(
            a = offer("2.50", "500", QuantityUnit.Gram),
            b = offer("4.00", "500", QuantityUnit.Millilitre),
        )
    }

    // ---- Extra: symmetry -----------------------------------------------------

    @Test
    fun `swapping A and B flips the winner and preserves magnitudes`() {
        val a = offer("2.50", "500", QuantityUnit.Gram)
        val b = offer("4.00", "1000", QuantityUnit.Gram)

        val forward = PriceComparator.compare(a, b) as ComparisonOutcome.BWins
        val backward = PriceComparator.compare(b, a) as ComparisonOutcome.AWins

        assertEquals(0, forward.perUnitDelta.compareTo(backward.perUnitDelta))
        assertApprox(forward.percentDelta.toPlainString(), backward.percentDelta, tolerance = "0.00000001")
    }

    // ---- Helpers -------------------------------------------------------------

    private fun offer(price: String, quantity: String, unit: QuantityUnit): Offer =
        Offer(price = BigDecimal(price), quantity = BigDecimal(quantity), quantityUnit = unit)

    private fun assertAWins(outcome: ComparisonOutcome, expectedDelta: String, expectedPercent: String) {
        assertTrue("Expected AWins, got $outcome", outcome is ComparisonOutcome.AWins)
        val a = outcome as ComparisonOutcome.AWins
        assertEquals(0, BigDecimal(expectedDelta).compareTo(a.perUnitDelta))
        assertEquals(0, BigDecimal(expectedPercent).compareTo(a.percentDelta))
    }

    private fun assertBWins(outcome: ComparisonOutcome, expectedDelta: String, expectedPercent: String) {
        assertTrue("Expected BWins, got $outcome", outcome is ComparisonOutcome.BWins)
        val b = outcome as ComparisonOutcome.BWins
        assertEquals(0, BigDecimal(expectedDelta).compareTo(b.perUnitDelta))
        assertEquals(0, BigDecimal(expectedPercent).compareTo(b.percentDelta))
    }

    private fun assertApprox(expected: String, actual: BigDecimal, tolerance: String) {
        val diff = actual.subtract(BigDecimal(expected)).abs()
        assertTrue(
            "actual=$actual, expected≈$expected, diff=$diff > tolerance=$tolerance",
            diff.compareTo(BigDecimal(tolerance)) <= 0,
        )
    }
}
