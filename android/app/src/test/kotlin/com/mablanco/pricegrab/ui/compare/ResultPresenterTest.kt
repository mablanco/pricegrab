package com.mablanco.pricegrab.ui.compare

import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.Dimension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class ResultPresenterTest {

    private val enUs: Locale = Locale.forLanguageTag("en-US")
    private val esEs: Locale = Locale.forLanguageTag("es-ES")

    @Test
    fun `null outcome has no savings to display`() {
        assertNull(ResultPresenter.present(null, Dimension.Mass, enUs))
    }

    @Test
    fun `null dimension has no savings to display`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 1,
            perUnitDelta = BigDecimal("0.001"),
            percentDelta = BigDecimal("20"),
        )
        assertNull(ResultPresenter.present(outcome, null, enUs))
    }

    @Test
    fun `tie has no savings to display`() {
        assertNull(ResultPresenter.present(ComparisonOutcome.Tie, Dimension.Mass, enUs))
    }

    @Test
    fun `mass delta one thousandth displays as 1 per kg`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 1,
            perUnitDelta = BigDecimal("0.001"),
            percentDelta = BigDecimal("20"),
        )
        val result = ResultPresenter.present(outcome, Dimension.Mass, enUs)
        assertEquals("1", result?.perUnitDelta)
        assertEquals("20", result?.percentDelta)
    }

    @Test
    fun `volume delta four ten-thousandths displays as 0_4 per L`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 1,
            perUnitDelta = BigDecimal("0.0004"),
            percentDelta = BigDecimal("20"),
        )
        val result = ResultPresenter.present(outcome, Dimension.Volume, enUs)
        assertEquals("0.4", result?.perUnitDelta)
    }

    @Test
    fun `count delta displays without mass scaling`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 1,
            perUnitDelta = BigDecimal("0.083333"),
            percentDelta = BigDecimal("16.7"),
        )
        val result = ResultPresenter.present(outcome, Dimension.Count, enUs)
        assertEquals("0.083333", result?.perUnitDelta)
    }

    @Test
    fun `winner with round percent renders without trailing zero`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 1,
            perUnitDelta = BigDecimal("0.001"),
            percentDelta = BigDecimal("20"),
        )
        val result = ResultPresenter.present(outcome, Dimension.Mass, enUs)
        assertEquals("1", result?.perUnitDelta)
        assertEquals("20", result?.percentDelta)
    }

    @Test
    fun `winner with one-hundred percent (free offer) renders cleanly`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 0,
            perUnitDelta = BigDecimal("0.2"),
            percentDelta = BigDecimal("100"),
        )
        val result = ResultPresenter.present(outcome, Dimension.Mass, enUs)
        assertEquals("200", result?.perUnitDelta)
        assertEquals("100", result?.percentDelta)
    }

    @Test
    fun `non-round percent rounds half up to one decimal`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 1,
            perUnitDelta = BigDecimal("0.5"),
            percentDelta = BigDecimal("16.666666666"),
        )
        val result = ResultPresenter.present(outcome, Dimension.Mass, enUs)
        assertEquals("500", result?.perUnitDelta)
        assertEquals("16.7", result?.percentDelta)
    }

    @Test
    fun `tiny percent rounds down to zero (acceptable for v1)`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 0,
            perUnitDelta = BigDecimal("1"),
            percentDelta = BigDecimal("0.0000001"),
        )
        val result = ResultPresenter.present(outcome, Dimension.Mass, enUs)
        assertEquals("1,000", result?.perUnitDelta)
        assertEquals("0", result?.percentDelta)
    }

    @Test
    fun `es-ES uses comma decimal separator for both numbers`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 1,
            perUnitDelta = BigDecimal("0.001"),
            percentDelta = BigDecimal("16.7"),
        )
        val result = ResultPresenter.present(outcome, Dimension.Mass, esEs)
        assertEquals("1", result?.perUnitDelta)
        assertEquals("16,7", result?.percentDelta)
    }

    @Test
    fun `large per-unit delta is formatted with grouping when relevant`() {
        val outcome = ComparisonOutcome.Winner(
            slotIndex = 0,
            perUnitDelta = BigDecimal("1.2345"),
            percentDelta = BigDecimal("12.5"),
        )
        val result = ResultPresenter.present(outcome, Dimension.Mass, enUs)
        assertEquals("1,234.5", result?.perUnitDelta)
        assertEquals("12.5", result?.percentDelta)
    }
}
