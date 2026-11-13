package com.mablanco.pricegrab.data.currency

import java.util.Currency
import java.util.Locale

/** ISO 4217 code for "no currency". */
private const val NO_CURRENCY_CODE: String = "XXX"

/**
 * The symbol to display for [prefs]: the [locale] region's currency for
 * [CurrencyMode.Automatic], the trimmed custom text for
 * [CurrencyMode.Custom]. Empty means "show no symbol".
 */
fun resolveCurrencySymbol(prefs: CurrencyPreferences, locale: Locale): String =
    when (prefs.mode) {
        CurrencyMode.Automatic -> regionSymbol(locale)
        CurrencyMode.Custom -> prefs.customSymbol.trim()
    }

private fun regionSymbol(locale: Locale): String {
    // Throws for locales without a region (e.g. plain "en").
    val currency = runCatching { Currency.getInstance(locale) }.getOrNull()
    if (currency == null || currency.currencyCode == NO_CURRENCY_CODE) return ""
    return currency.getSymbol(locale)
}
