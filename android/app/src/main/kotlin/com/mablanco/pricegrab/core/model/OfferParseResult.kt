package com.mablanco.pricegrab.core.model

/**
 * Outcome of trying to turn two raw user input strings (price + quantity)
 * into a validated [Offer]. See
 * [com.mablanco.pricegrab.core.format.OfferParser] for the parsing logic.
 */
sealed interface OfferParseResult {

    data class Success(val offer: Offer) : OfferParseResult

    data object EmptyPrice : OfferParseResult
    data object EmptyQuantity : OfferParseResult

    data class InvalidPrice(val raw: String) : OfferParseResult
    data class InvalidQuantity(val raw: String) : OfferParseResult

    data object NegativePrice : OfferParseResult
    data object NonPositiveQuantity : OfferParseResult
}
