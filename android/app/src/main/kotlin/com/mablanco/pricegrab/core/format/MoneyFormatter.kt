package com.mablanco.pricegrab.core.format

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/** Text placed around a formatted amount to mark it as money. */
data class MoneyAffixes(val prefix: String, val suffix: String) {
    companion object {
        val NONE: MoneyAffixes = MoneyAffixes(prefix = "", suffix = "")
    }
}

/**
 * Places a currency symbol around an already-formatted amount, following
 * the [Locale]'s currency pattern (e.g. es-ES `1 €`, en-US `$1`).
 *
 * Only the position and spacing come from the locale; the amount keeps
 * whatever precision the caller formatted it with, so `1` stays `1 €`
 * rather than the currency's default `1,00 €`.
 */
object MoneyFormatter {

    fun affixes(symbol: String, locale: Locale): MoneyAffixes {
        if (symbol.isEmpty()) return MoneyAffixes.NONE
        val format = NumberFormat.getCurrencyInstance(locale) as? DecimalFormat
            ?: return MoneyAffixes(prefix = "", suffix = " $symbol")
        val symbols = format.decimalFormatSymbols
        symbols.currencySymbol = symbol
        format.decimalFormatSymbols = symbols
        return MoneyAffixes(prefix = format.positivePrefix, suffix = format.positiveSuffix)
    }

    fun wrap(amount: String, affixes: MoneyAffixes): String =
        affixes.prefix + amount + affixes.suffix
}
