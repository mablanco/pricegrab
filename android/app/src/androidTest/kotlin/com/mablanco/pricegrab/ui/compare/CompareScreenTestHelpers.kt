package com.mablanco.pricegrab.ui.compare

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.core.format.MoneyFormatter
import com.mablanco.pricegrab.data.currency.CurrencyPreferencesRepository
import com.mablanco.pricegrab.data.currency.resolveCurrencySymbol
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Opens [testTag] and picks a unit from the dropdown by its accessibility
 * label ([unitNameRes]). Menu items expose the full unit name as content
 * description while the closed selector shows the short code, so text-based
 * matching would fail when the same code is already visible on another offer.
 */
internal fun AndroidComposeTestRule<*, MainActivity>.selectUnit(
    testTag: String,
    @StringRes unitNameRes: Int,
) {
    onNodeWithTag(testTag).performClick()
    onNodeWithContentDescription(activity.getString(unitNameRes)).performClick()
    waitForIdle()
}

/** Localized winner headline for the given offer title resource. */
internal fun Context.winnerHeadline(@StringRes offerTitleRes: Int): String =
    getString(R.string.result_winner, getString(offerTitleRes))

/**
 * [amount] as the real app shows it: wrapped with the currency symbol the
 * stored preference resolves to for this device's locale (feature 010).
 */
internal fun Context.expectedMoney(amount: String): String {
    val locale = resources.configuration.locales[0]
    val prefs = runBlocking { CurrencyPreferencesRepository(this@expectedMoney).preferences.first() }
    val symbol = resolveCurrencySymbol(prefs, locale)
    return MoneyFormatter.wrap(amount, MoneyFormatter.affixes(symbol, locale))
}

/** Price field label as shown with [symbol] ("Price (€)"), or plain when empty. */
internal fun Context.priceLabel(symbol: String): String {
    val label = getString(R.string.price_label)
    return if (symbol.isEmpty()) label else getString(R.string.field_label_with_currency, label, symbol)
}
