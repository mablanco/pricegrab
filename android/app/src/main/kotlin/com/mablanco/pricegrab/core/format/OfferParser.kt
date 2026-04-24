package com.mablanco.pricegrab.core.format

import com.mablanco.pricegrab.core.model.Offer
import com.mablanco.pricegrab.core.model.OfferParseResult
import java.util.Locale

/**
 * Turns two raw user strings (price + quantity) into a validated [Offer] or
 * an explanatory [OfferParseResult] variant so the UI can render a specific
 * inline error next to the offending field.
 */
object OfferParser {

    @Suppress("ReturnCount") // Each validation step has its own typed error variant.
    fun parse(
        rawPrice: String,
        rawQuantity: String,
        locale: Locale,
    ): OfferParseResult {
        if (rawPrice.isBlank()) return OfferParseResult.EmptyPrice
        if (rawQuantity.isBlank()) return OfferParseResult.EmptyQuantity

        val price = LocaleNumberFormatter.parse(rawPrice, locale)
            ?: return OfferParseResult.InvalidPrice(rawPrice)
        val quantity = LocaleNumberFormatter.parse(rawQuantity, locale)
            ?: return OfferParseResult.InvalidQuantity(rawQuantity)

        if (price.signum() < 0) return OfferParseResult.NegativePrice
        if (quantity.signum() <= 0) return OfferParseResult.NonPositiveQuantity

        return OfferParseResult.Success(Offer(price = price, quantity = quantity))
    }
}
