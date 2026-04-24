package com.mablanco.pricegrab.core.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

/**
 * Locale-aware number parsing and formatting. Wraps `java.text.NumberFormat`
 * so the rest of the codebase stays decoupled from the JVM API.
 *
 * Parsing intentionally disables grouping separators to keep user input
 * unambiguous across locales: for example, in `es-ES`, "2.50" would be
 * interpreted as 250 (dot = thousands separator). By disabling grouping at
 * parse time we reject such inputs instead of silently mis-parsing them.
 */
object LocaleNumberFormatter {

    /**
     * Parse a user-typed number in the given [locale].
     *
     * Returns `null` when:
     * - [raw] is blank;
     * - [raw] cannot be decoded as a number at all; or
     * - [raw] contains trailing garbage after a valid number prefix.
     *
     * The parse always uses `BigDecimal` as the target type to avoid
     * floating-point representation error.
     */
    @Suppress("ReturnCount") // Early returns are the clearest form of validation here.
    fun parse(raw: String, locale: Locale): BigDecimal? {
        if (raw.isBlank()) return null
        val fmt = (NumberFormat.getInstance(locale) as? DecimalFormat) ?: return null
        fmt.isParseBigDecimal = true
        fmt.isGroupingUsed = false
        val pos = ParsePosition(0)
        val parsed = fmt.parse(raw, pos) as? BigDecimal ?: return null
        if (pos.index != raw.length) return null
        return parsed
    }

    /**
     * Format [value] as a human-readable string in the given [locale].
     *
     * @param minFractionDigits minimum decimal places to show (pad with zeros if needed).
     * @param maxFractionDigits maximum decimal places to keep; the rest is rounded away.
     * @param roundingMode how to round when [maxFractionDigits] forces truncation.
     * @param useGrouping whether to insert the locale's thousands separator.
     */
    fun format(
        value: BigDecimal,
        locale: Locale,
        minFractionDigits: Int = 0,
        maxFractionDigits: Int = MAX_FRACTION_DIGITS_DEFAULT,
        roundingMode: RoundingMode = RoundingMode.HALF_UP,
        useGrouping: Boolean = true,
    ): String {
        val fmt = NumberFormat.getInstance(locale)
        fmt.minimumFractionDigits = minFractionDigits
        fmt.maximumFractionDigits = maxFractionDigits
        fmt.roundingMode = roundingMode
        fmt.isGroupingUsed = useGrouping
        return fmt.format(value)
    }

    /**
     * The locale's decimal separator — useful when sanitizing user input
     * (e.g. dropping minus signs that some IMEs leak through) without
     * dropping the legitimate separator.
     */
    fun decimalSeparator(locale: Locale): Char =
        java.text.DecimalFormatSymbols.getInstance(locale).decimalSeparator

    private const val MAX_FRACTION_DIGITS_DEFAULT = 6
}
