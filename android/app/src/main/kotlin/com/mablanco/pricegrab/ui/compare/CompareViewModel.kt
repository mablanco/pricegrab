package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mablanco.pricegrab.core.calc.PriceComparator
import com.mablanco.pricegrab.core.format.OfferParser
import com.mablanco.pricegrab.core.model.OfferParseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Owns the state for the Compare screen. Keeps the four raw strings in the
 * [SavedStateHandle] so process death and configuration changes (rotation,
 * dark-mode flip, font scale change, language change) do not wipe them out.
 *
 * Reactive model: every keystroke re-parses both offers and re-computes the
 * comparison outcome. This is cheap because the full state only holds 4 small
 * strings; there is no async work or I/O.
 */
class CompareViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state: MutableStateFlow<CompareUiState> = MutableStateFlow(
        CompareUiState(
            priceARaw = savedStateHandle[KEY_PRICE_A] ?: "",
            quantityARaw = savedStateHandle[KEY_QUANTITY_A] ?: "",
            priceBRaw = savedStateHandle[KEY_PRICE_B] ?: "",
            quantityBRaw = savedStateHandle[KEY_QUANTITY_B] ?: "",
        ),
    )

    val state: StateFlow<CompareUiState> = _state.asStateFlow()

    init {
        _state.value = recomputeOutcome(_state.value)
    }

    fun onPriceAChange(value: String) = update { it.copy(priceARaw = sanitize(value)) }
    fun onQuantityAChange(value: String) = update { it.copy(quantityARaw = sanitize(value)) }
    fun onPriceBChange(value: String) = update { it.copy(priceBRaw = sanitize(value)) }
    fun onQuantityBChange(value: String) = update { it.copy(quantityBRaw = sanitize(value)) }

    private fun update(transform: (CompareUiState) -> CompareUiState) {
        val next = transform(_state.value)
        savedStateHandle[KEY_PRICE_A] = next.priceARaw
        savedStateHandle[KEY_QUANTITY_A] = next.quantityARaw
        savedStateHandle[KEY_PRICE_B] = next.priceBRaw
        savedStateHandle[KEY_QUANTITY_B] = next.quantityBRaw
        _state.value = recomputeOutcome(next)
    }

    private fun recomputeOutcome(state: CompareUiState): CompareUiState {
        val locale = Locale.getDefault()
        val aResult = OfferParser.parse(state.priceARaw, state.quantityARaw, locale)
        val bResult = OfferParser.parse(state.priceBRaw, state.quantityBRaw, locale)

        val outcome = if (aResult is OfferParseResult.Success && bResult is OfferParseResult.Success) {
            PriceComparator.compare(aResult.offer, bResult.offer)
        } else {
            null
        }

        return state.copy(
            priceAError = priceErrorFor(aResult, state.priceARaw),
            quantityAError = quantityErrorFor(aResult, state.quantityARaw),
            priceBError = priceErrorFor(bResult, state.priceBRaw),
            quantityBError = quantityErrorFor(bResult, state.quantityBRaw),
            outcome = outcome,
        )
    }

    /**
     * Strip characters some OEM IMEs leak through `KeyboardType.Decimal`
     * that would either bypass FR-005 (the leading "-" for negatives) or
     * confuse the parser (exponent notation, stray whitespace).
     *
     * Everything else — including typo letters and extra separators — is
     * preserved so the parser can surface a specific inline "not a number"
     * error instead of silently swallowing the input.
     */
    private fun sanitize(value: String): String =
        value.filter { c -> c !in BLOCKED_CHARS && !c.isWhitespace() }

    private fun priceErrorFor(result: OfferParseResult, raw: String): InputError? = when {
        raw.isBlank() -> null
        result is OfferParseResult.InvalidPrice -> InputError.NotANumber
        result is OfferParseResult.NegativePrice -> InputError.NegativePrice
        else -> null
    }

    private fun quantityErrorFor(result: OfferParseResult, raw: String): InputError? = when {
        raw.isBlank() -> null
        result is OfferParseResult.InvalidQuantity -> InputError.NotANumber
        result is OfferParseResult.NonPositiveQuantity -> InputError.NonPositiveQuantity
        else -> null
    }

    private companion object {
        const val KEY_PRICE_A = "priceA"
        const val KEY_QUANTITY_A = "quantityA"
        const val KEY_PRICE_B = "priceB"
        const val KEY_QUANTITY_B = "quantityB"

        val BLOCKED_CHARS: Set<Char> = setOf('-', '+', 'e', 'E')
    }
}
