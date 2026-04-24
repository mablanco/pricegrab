package com.mablanco.pricegrab.ui.compare

import com.mablanco.pricegrab.core.model.ComparisonOutcome

/**
 * Immutable snapshot of the Compare screen.
 *
 * Raw strings are kept exactly as the user typed them so the re-rendered
 * `TextField` never reformats mid-edit and the user never loses their caret
 * position. The per-field [InputError] nullables describe the first error
 * applicable to that field (or `null` when the field is blank or valid).
 *
 * [outcome] is non-null only when both offers parse into valid `Offer`
 * instances; otherwise the result pane shows a neutral placeholder.
 */
data class CompareUiState(
    val priceARaw: String = "",
    val quantityARaw: String = "",
    val priceBRaw: String = "",
    val quantityBRaw: String = "",
    val priceAError: InputError? = null,
    val quantityAError: InputError? = null,
    val priceBError: InputError? = null,
    val quantityBError: InputError? = null,
    val outcome: ComparisonOutcome? = null,
)

/**
 * User-visible validation failure for a single input field. Mapped to a
 * localized string by the screen.
 */
enum class InputError {
    NotANumber,
    NegativePrice,
    NonPositiveQuantity,
}
