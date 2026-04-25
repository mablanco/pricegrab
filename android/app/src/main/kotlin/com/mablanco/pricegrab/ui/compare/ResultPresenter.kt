package com.mablanco.pricegrab.ui.compare

import com.mablanco.pricegrab.core.format.LocaleNumberFormatter
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Pure presentation layer that turns a [ComparisonOutcome] into the
 * locale-formatted savings strings the result row needs:
 *
 * - `perUnitDelta` — the absolute per-unit savings, e.g. `"0.001"` (en-US)
 *   or `"0,001"` (es-ES). Up to six decimal places, trailing zeros stripped.
 * - `percentDelta` — the relative savings rounded to at most one decimal
 *   place, e.g. `"20"`, `"16.7"`, or `"100"`. The literal `%` sign is added
 *   by the screen from a `<string>` template, so this presenter does no
 *   percent-symbol formatting and can be unit-tested without an Android
 *   resource bundle.
 *
 * Returns `null` when there are no savings to show — either the result is
 * a tie or no outcome has been computed yet.
 */
object ResultPresenter {

    fun present(outcome: ComparisonOutcome?, locale: Locale): SavingsPresentation? = when (outcome) {
        null, ComparisonOutcome.Tie -> null
        is ComparisonOutcome.AWins -> format(outcome.perUnitDelta, outcome.percentDelta, locale)
        is ComparisonOutcome.BWins -> format(outcome.perUnitDelta, outcome.percentDelta, locale)
    }

    private fun format(
        perUnitDelta: BigDecimal,
        percentDelta: BigDecimal,
        locale: Locale,
    ): SavingsPresentation = SavingsPresentation(
        perUnitDelta = LocaleNumberFormatter.format(
            value = perUnitDelta,
            locale = locale,
            minFractionDigits = 0,
            maxFractionDigits = MAX_DELTA_FRACTION_DIGITS,
            roundingMode = RoundingMode.HALF_UP,
        ),
        percentDelta = LocaleNumberFormatter.format(
            value = percentDelta,
            locale = locale,
            minFractionDigits = 0,
            maxFractionDigits = MAX_PERCENT_FRACTION_DIGITS,
            roundingMode = RoundingMode.HALF_UP,
        ),
    )

    private const val MAX_DELTA_FRACTION_DIGITS = 6
    private const val MAX_PERCENT_FRACTION_DIGITS = 1
}

/**
 * Locale-formatted savings ready to drop into a `<string>` template.
 */
data class SavingsPresentation(
    val perUnitDelta: String,
    val percentDelta: String,
)
