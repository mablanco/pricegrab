package com.mablanco.pricegrab.ui.compare

import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.QuantityUnit

const val MIN_OFFERS: Int = 2
const val MAX_OFFERS: Int = 3

/**
 * One offer slot on the Compare screen (raw strings + unit + field errors).
 */
data class OfferSlotState(
    val priceRaw: String = "",
    val quantityRaw: String = "",
    val quantityUnit: QuantityUnit = QuantityUnit.Gram,
    val priceError: InputError? = null,
    val quantityError: InputError? = null,
)

/**
 * Immutable snapshot of the Compare screen.
 *
 * Raw strings are kept exactly as the user typed them so the re-rendered
 * `TextField` never reformats mid-edit and the user never loses their caret
 * position. The per-field [InputError] nullables describe the first error
 * applicable to that field (or `null` when the field is blank or valid).
 *
 * [outcome] is non-null only when at least two offers parse into valid
 * `Offer` instances with matching dimensions; otherwise the result pane
 * shows a neutral placeholder or an incompatible-units error.
 *
 * [undoState] is non-null only while a transient Material 3 Snackbar
 * with an Undo affordance is on screen, immediately after a non-empty
 * Reset. Cleared on undo, on Snackbar timeout, on the user typing into
 * any field, or on the host activity reaching `ON_STOP`.
 */
data class CompareUiState(
    val offers: List<OfferSlotState> = listOf(OfferSlotState(), OfferSlotState()),
    val outcome: ComparisonOutcome? = null,
    val incompatibleUnits: Boolean = false,
    val undoState: UndoState? = null,
)

/**
 * True iff there are more than [MIN_OFFERS] slots, or at least one slot has
 * a non-empty raw field or a unit other than [QuantityUnit.Gram].
 */
val CompareUiState.isResetEnabled: Boolean
    get() = offers.size > MIN_OFFERS ||
        offers.any { slot ->
            slot.priceRaw.isNotBlank() ||
                slot.quantityRaw.isNotBlank() ||
                slot.quantityUnit != QuantityUnit.Gram
        }

/**
 * Slot values as they were *immediately before* a Reset, kept for as long
 * as the Undo affordance is offered. Holds raw strings only; the cached
 * comparison outcome is intentionally absent because the ViewModel rebuilds
 * it deterministically from the snapshot on `undoReset()`.
 */
data class OfferSlotSnapshot(
    val priceRaw: String,
    val quantityRaw: String,
    val quantityUnit: QuantityUnit,
)

data class PreResetSnapshot(
    val slots: List<OfferSlotSnapshot>,
)

/**
 * Undo affordance: the snapshot to restore plus the wall-clock
 * deadline at which the Snackbar should auto-dismiss.
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
