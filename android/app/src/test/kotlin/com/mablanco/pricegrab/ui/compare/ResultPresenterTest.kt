package com.mablanco.pricegrab.ui.compare

import com.mablanco.pricegrab.core.model.ComparisonOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class ResultPresenterTest {

    private val enUs: Locale = Locale.forLanguageTag("en-US")
    private val esEs: Locale = Locale.forLanguageTag("es-ES")

    // ---- Tie / null ---------------------------------------------------------

    @Test
    fun `null outcome has no savings to display`() {
        assertNull(ResultPresenter.present(null, enUs))
    }

    @Test
    fun `tie has no savings to display`() {
        assertNull(ResultPresenter.present(ComparisonOutcome.Tie, enUs))
    }

    // ---- Round savings ------------------------------------------------------

    @Test
    fun `B wins with round percent renders without trailing zero`() {
        val outcome = ComparisonOutcome.BWins(
            perUnitDelta = BigDecimal("0.001"),
            percentDelta = BigDecimal("20"),
        )
        val result = ResultPresenter.present(outcome, enUs)
        assertEquals("0.001", result?.perUnitDelta)
        assertEquals("20", result?.percentDelta)
    }

    @Test
    fun `A wins with one-hundred percent (free offer) renders cleanly`() {
        val outcome = ComparisonOutcome.AWins(
            perUnitDelta = BigDecimal("0.2"),
            percentDelta = BigDecimal("100"),
        )
        val result = ResultPresenter.present(outcome, enUs)
        assertEquals("0.2", result?.perUnitDelta)
        assertEquals("100", result?.percentDelta)
    }

    // ---- Non-round percent rounds to one decimal place ---------------------

    @Test
    fun `non-round percent rounds half up to one decimal`() {
        val outcome = ComparisonOutcome.BWins(
            perUnitDelta = BigDecimal("0.5"),
            percentDelta = BigDecimal("16.666666666"),
        )
        val result = ResultPresenter.present(outcome, enUs)
        assertEquals("0.5", result?.perUnitDelta)
        assertEquals("16.7", result?.percentDelta)
    }

    @Test
    fun `tiny percent rounds down to zero (acceptable for v1)`() {
        // 1e-7 percent — case 9 of the canonical contract.
        val outcome = ComparisonOutcome.AWins(
            perUnitDelta = BigDecimal("1"),
            percentDelta = BigDecimal("0.0000001"),
        )
        val result = ResultPresenter.present(outcome, enUs)
        assertEquals("1", result?.perUnitDelta)
        assertEquals("0", result?.percentDelta)
    }

    // ---- Locale-aware decimal separator ------------------------------------

    @Test
    fun `es-ES uses comma decimal separator for both numbers`() {
        val outcome = ComparisonOutcome.BWins(
            perUnitDelta = BigDecimal("0.001"),
            percentDelta = BigDecimal("16.7"),
        )
        val result = ResultPresenter.present(outcome, esEs)
        assertEquals("0,001", result?.perUnitDelta)
        assertEquals("16,7", result?.percentDelta)
    }

    @Test
    fun `large per-unit delta is formatted with grouping when relevant`() {
        // 1234.5 per unit, en-US default grouping → "1,234.5".
        val outcome = ComparisonOutcome.AWins(
            perUnitDelta = BigDecimal("1234.5"),
            percentDelta = BigDecimal("12.5"),
        )
        val result = ResultPresenter.present(outcome, enUs)
        assertEquals("1,234.5", result?.perUnitDelta)
        assertEquals("12.5", result?.percentDelta)
    }
}
