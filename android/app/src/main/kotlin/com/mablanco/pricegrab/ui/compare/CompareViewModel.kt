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
 *
 * Feature 002 additions:
 *  - [resetComparison] captures a [PreResetSnapshot], clears the four raw
 *    fields, and exposes an [UndoState] for the screen to surface as a
 *    Snackbar.
 *  - [undoReset] restores the captured snapshot atomically.
 *  - [dismissUndo] clears the [UndoState] without restoring (used by the
 *    typing-while-snackbar-visible path and by the host activity's
 *    `ON_STOP` listener).
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
            undoState = restoreUndoStateFromSavedState(),
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

    /**
     * Atomically clear the four raw inputs and the displayed result, and
     * (when the form was non-empty) start an [UndoState] that the screen
     * can surface as a Material 3 Snackbar.
     *
     * No-op when the form is already empty (FR-004 / AS-1.3): no state
     * change, no `UndoState`. The button is also `enabled = false` in
     * that case, so reaching this branch through normal interaction is
     * not possible — it is defended here for completeness.
     */
    fun resetComparison() {
        val current = _state.value
        if (!current.isResetEnabled) return

        val snapshot = PreResetSnapshot(
            priceARaw = current.priceARaw,
            quantityARaw = current.quantityARaw,
            priceBRaw = current.priceBRaw,
            quantityBRaw = current.quantityBRaw,
        )
        val deadline = System.currentTimeMillis() + UNDO_LIFETIME_MS

        savedStateHandle[KEY_PRICE_A] = ""
        savedStateHandle[KEY_QUANTITY_A] = ""
        savedStateHandle[KEY_PRICE_B] = ""
        savedStateHandle[KEY_QUANTITY_B] = ""
        savedStateHandle[KEY_UNDO_PRICE_A] = snapshot.priceARaw
        savedStateHandle[KEY_UNDO_QUANTITY_A] = snapshot.quantityARaw
        savedStateHandle[KEY_UNDO_PRICE_B] = snapshot.priceBRaw
        savedStateHandle[KEY_UNDO_QUANTITY_B] = snapshot.quantityBRaw
        savedStateHandle[KEY_UNDO_DEADLINE] = deadline

        val cleared = CompareUiState(
            undoState = UndoState(snapshot, deadline),
        )
        _state.value = recomputeOutcome(cleared)
    }

    /**
     * Restore the four raw strings to whatever they were immediately
     * before the most recent [resetComparison]. No-op when no
     * [UndoState] is active.
     */
    fun undoReset() {
        val undo = _state.value.undoState ?: return
        val snap = undo.snapshot

        savedStateHandle[KEY_PRICE_A] = snap.priceARaw
        savedStateHandle[KEY_QUANTITY_A] = snap.quantityARaw
        savedStateHandle[KEY_PRICE_B] = snap.priceBRaw
        savedStateHandle[KEY_QUANTITY_B] = snap.quantityBRaw
        clearUndoFromSavedState()

        val restored = CompareUiState(
            priceARaw = snap.priceARaw,
            quantityARaw = snap.quantityARaw,
            priceBRaw = snap.priceBRaw,
            quantityBRaw = snap.quantityBRaw,
        )
        _state.value = recomputeOutcome(restored)
    }

    /**
     * Clear the active [UndoState] without restoring. Triggered by:
     *  - the user starting to type into any of the four fields
     *    (handled internally by [update]);
     *  - the host activity reaching `Lifecycle.Event.ON_STOP`;
     *  - the Snackbar reaching its dismissal threshold (timeout or swipe).
     */
    fun dismissUndo() {
        if (_state.value.undoState == null) return
        clearUndoFromSavedState()
        _state.value = _state.value.copy(undoState = null)
    }

    private fun update(transform: (CompareUiState) -> CompareUiState) {
        var next = transform(_state.value)
        // FR-008.1: typing into any field dismisses an active Undo
        // affordance. We mutate `next` *before* writing to the
        // SavedStateHandle so the cleared keys are the ones that
        // survive a configuration change.
        if (next.undoState != null) {
            clearUndoFromSavedState()
            next = next.copy(undoState = null)
        }
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

    private fun clearUndoFromSavedState() {
        savedStateHandle.remove<String>(KEY_UNDO_PRICE_A)
        savedStateHandle.remove<String>(KEY_UNDO_QUANTITY_A)
        savedStateHandle.remove<String>(KEY_UNDO_PRICE_B)
        savedStateHandle.remove<String>(KEY_UNDO_QUANTITY_B)
        savedStateHandle.remove<Long>(KEY_UNDO_DEADLINE)
    }

    /**
     * Reconstruct an [UndoState] from the SavedStateHandle, or `null`
     * when no Undo is active or when the deadline has already passed
     * (e.g. process death survived the lifetime).
     */
    private fun restoreUndoStateFromSavedState(): UndoState? {
        val deadline: Long = savedStateHandle[KEY_UNDO_DEADLINE] ?: return null
        if (deadline <= System.currentTimeMillis()) return null
        val priceA: String = savedStateHandle[KEY_UNDO_PRICE_A] ?: return null
        val quantityA: String = savedStateHandle[KEY_UNDO_QUANTITY_A] ?: return null
        val priceB: String = savedStateHandle[KEY_UNDO_PRICE_B] ?: return null
        val quantityB: String = savedStateHandle[KEY_UNDO_QUANTITY_B] ?: return null
        return UndoState(
            snapshot = PreResetSnapshot(priceA, quantityA, priceB, quantityB),
            expiresAtEpochMillis = deadline,
        )
    }

    private companion object {
        const val KEY_PRICE_A = "priceA"
        const val KEY_QUANTITY_A = "quantityA"
        const val KEY_PRICE_B = "priceB"
        const val KEY_QUANTITY_B = "quantityB"

        const val KEY_UNDO_PRICE_A = "undoPriceA"
        const val KEY_UNDO_QUANTITY_A = "undoQuantityA"
        const val KEY_UNDO_PRICE_B = "undoPriceB"
        const val KEY_UNDO_QUANTITY_B = "undoQuantityB"
        const val KEY_UNDO_DEADLINE = "undoDeadline"

        /**
         * Material 3 `SnackbarDuration.Long` is documented as ~10 seconds.
         * We track the deadline in wall-clock time (research.md §3) so the
         * affordance can survive rotation with its remaining lifetime
         * intact; the Snackbar UI uses `withTimeoutOrNull(remaining)` to
         * truncate the show duration when the state is restored
         * mid-window after a configuration change.
         */
        const val UNDO_LIFETIME_MS = 10_000L

        val BLOCKED_CHARS: Set<Char> = setOf('-', '+', 'e', 'E')
    }
}
