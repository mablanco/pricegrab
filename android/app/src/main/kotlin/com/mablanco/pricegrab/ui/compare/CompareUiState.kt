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
 *
 * [undoState] is non-null only while a transient Material 3 Snackbar
 * with an Undo affordance is on screen, immediately after a non-empty
 * Reset. Cleared on undo, on Snackbar timeout, on the user typing into
 * any of the four fields, or on the host activity reaching `ON_STOP`.
 * See feature 002 spec FR-006/FR-008.
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
    val undoState: UndoState? = null,
)

/**
 * True iff at least one of the four raw input fields is non-empty.
 *
 * Drives feature 002 FR-004: the Reset control is `enabled` when this
 * is `true` and visibly disabled otherwise. Implemented as an extension
 * property (rather than a separate `StateFlow<Boolean>` on the
 * ViewModel as plan.md initially suggested) because the screen already
 * collects the full state, so an extra flow would only add ceremony.
 */
val CompareUiState.isResetEnabled: Boolean
    get() = priceARaw.isNotBlank() ||
        quantityARaw.isNotBlank() ||
        priceBRaw.isNotBlank() ||
        quantityBRaw.isNotBlank()

/**
 * The four raw strings as they were *immediately before* a Reset, kept
 * for as long as the Undo affordance is offered. Holds raw strings only;
 * the cached comparison outcome is intentionally absent because the
 * ViewModel rebuilds it deterministically from the four raw strings on
 * `undoReset()` (single source of truth, no risk of drift).
 */
data class PreResetSnapshot(
    val priceARaw: String,
    val quantityARaw: String,
    val priceBRaw: String,
    val quantityBRaw: String,
)

/**
 * Live undo affordance: the snapshot to restore plus the wall-clock
 * deadline at which the Snackbar should auto-dismiss.
 *
 * Storing a deadline (rather than a remaining duration) is what makes
 * AS-2.4 ("survives rotation with remaining lifetime intact") cheap:
 * the Composable computes `remaining = max(0, expiresAtEpochMillis -
 * System.currentTimeMillis())` whenever the state arrives and shows
 * the Snackbar for that long.
 */
data class UndoState(
    val snapshot: PreResetSnapshot,
    val expiresAtEpochMillis: Long,
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
