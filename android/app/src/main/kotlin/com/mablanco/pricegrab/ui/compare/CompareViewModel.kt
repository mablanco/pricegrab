package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mablanco.pricegrab.core.format.OfferParser
import com.mablanco.pricegrab.core.model.OfferParseResult
import com.mablanco.pricegrab.core.model.QuantityUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Owns the state for the Compare screen. Keeps the four raw strings and both
 * quantity units in the [SavedStateHandle] so process death and configuration
 * changes do not wipe them out.
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
            quantityUnitA = readQuantityUnit(KEY_QUANTITY_UNIT_A),
            quantityUnitB = readQuantityUnit(KEY_QUANTITY_UNIT_B),
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

    fun onQuantityUnitAChange(unit: QuantityUnit) = update { it.copy(quantityUnitA = unit) }

    fun onQuantityUnitBChange(unit: QuantityUnit) = update { it.copy(quantityUnitB = unit) }

    fun resetComparison() {
        val current = _state.value
        if (!current.isResetEnabled) return

        val snapshot = PreResetSnapshot(
            priceARaw = current.priceARaw,
            quantityARaw = current.quantityARaw,
            priceBRaw = current.priceBRaw,
            quantityBRaw = current.quantityBRaw,
            quantityUnitA = current.quantityUnitA,
            quantityUnitB = current.quantityUnitB,
        )
        val deadline = System.currentTimeMillis() + UNDO_LIFETIME_MS

        savedStateHandle[KEY_PRICE_A] = ""
        savedStateHandle[KEY_QUANTITY_A] = ""
        savedStateHandle[KEY_PRICE_B] = ""
        savedStateHandle[KEY_QUANTITY_B] = ""
        savedStateHandle[KEY_QUANTITY_UNIT_A] = QuantityUnit.Gram.name
        savedStateHandle[KEY_QUANTITY_UNIT_B] = QuantityUnit.Gram.name
        savedStateHandle[KEY_UNDO_PRICE_A] = snapshot.priceARaw
        savedStateHandle[KEY_UNDO_QUANTITY_A] = snapshot.quantityARaw
        savedStateHandle[KEY_UNDO_PRICE_B] = snapshot.priceBRaw
        savedStateHandle[KEY_UNDO_QUANTITY_B] = snapshot.quantityBRaw
        savedStateHandle[KEY_UNDO_QUANTITY_UNIT_A] = snapshot.quantityUnitA.name
        savedStateHandle[KEY_UNDO_QUANTITY_UNIT_B] = snapshot.quantityUnitB.name
        savedStateHandle[KEY_UNDO_DEADLINE] = deadline

        val cleared = CompareUiState(
            undoState = UndoState(snapshot, deadline),
        )
        _state.value = recomputeOutcome(cleared)
    }

    fun undoReset() {
        val undo = _state.value.undoState ?: return
        val snap = undo.snapshot

        savedStateHandle[KEY_PRICE_A] = snap.priceARaw
        savedStateHandle[KEY_QUANTITY_A] = snap.quantityARaw
        savedStateHandle[KEY_PRICE_B] = snap.priceBRaw
        savedStateHandle[KEY_QUANTITY_B] = snap.quantityBRaw
        savedStateHandle[KEY_QUANTITY_UNIT_A] = snap.quantityUnitA.name
        savedStateHandle[KEY_QUANTITY_UNIT_B] = snap.quantityUnitB.name
        clearUndoFromSavedState()

        val restored = CompareUiState(
            priceARaw = snap.priceARaw,
            quantityARaw = snap.quantityARaw,
            priceBRaw = snap.priceBRaw,
            quantityBRaw = snap.quantityBRaw,
            quantityUnitA = snap.quantityUnitA,
            quantityUnitB = snap.quantityUnitB,
        )
        _state.value = recomputeOutcome(restored)
    }

    fun dismissUndo() {
        if (_state.value.undoState == null) return
        clearUndoFromSavedState()
        _state.value = _state.value.copy(undoState = null)
    }

    private fun update(transform: (CompareUiState) -> CompareUiState) {
        var next = transform(_state.value)
        if (next.undoState != null) {
            clearUndoFromSavedState()
            next = next.copy(undoState = null)
        }
        savedStateHandle[KEY_PRICE_A] = next.priceARaw
        savedStateHandle[KEY_QUANTITY_A] = next.quantityARaw
        savedStateHandle[KEY_PRICE_B] = next.priceBRaw
        savedStateHandle[KEY_QUANTITY_B] = next.quantityBRaw
        savedStateHandle[KEY_QUANTITY_UNIT_A] = next.quantityUnitA.name
        savedStateHandle[KEY_QUANTITY_UNIT_B] = next.quantityUnitB.name
        _state.value = recomputeOutcome(next)
    }

    private fun recomputeOutcome(state: CompareUiState): CompareUiState {
        val locale = Locale.getDefault()
        val aResult = OfferParser.parse(
            state.priceARaw,
            state.quantityARaw,
            state.quantityUnitA,
            locale,
        )
        val bResult = OfferParser.parse(
            state.priceBRaw,
            state.quantityBRaw,
            state.quantityUnitB,
            locale,
        )

        val gate = evaluateComparison(aResult, bResult)
        val outcome = when (gate) {
            is ComparisonGate.Ready -> gate.outcome
            ComparisonGate.Incomplete, ComparisonGate.IncompatibleUnits -> null
        }

        return state.copy(
            priceAError = priceErrorFor(aResult, state.priceARaw),
            quantityAError = quantityErrorFor(aResult, state.quantityARaw),
            priceBError = priceErrorFor(bResult, state.priceBRaw),
            quantityBError = quantityErrorFor(bResult, state.quantityBRaw),
            outcome = outcome,
            incompatibleUnits = gate is ComparisonGate.IncompatibleUnits,
        )
    }

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

    private fun readQuantityUnit(key: String): QuantityUnit {
        val stored: String? = savedStateHandle[key]
        return stored?.let { runCatching { QuantityUnit.valueOf(it) }.getOrNull() }
            ?: QuantityUnit.Gram
    }

    private fun clearUndoFromSavedState() {
        savedStateHandle.remove<String>(KEY_UNDO_PRICE_A)
        savedStateHandle.remove<String>(KEY_UNDO_QUANTITY_A)
        savedStateHandle.remove<String>(KEY_UNDO_PRICE_B)
        savedStateHandle.remove<String>(KEY_UNDO_QUANTITY_B)
        savedStateHandle.remove<String>(KEY_UNDO_QUANTITY_UNIT_A)
        savedStateHandle.remove<String>(KEY_UNDO_QUANTITY_UNIT_B)
        savedStateHandle.remove<Long>(KEY_UNDO_DEADLINE)
    }

    private fun restoreUndoStateFromSavedState(): UndoState? {
        val deadline: Long = savedStateHandle[KEY_UNDO_DEADLINE] ?: return null
        if (deadline <= System.currentTimeMillis()) return null
        val snapshot = readUndoSnapshotFromSavedState() ?: return null
        return UndoState(snapshot, deadline)
    }

    private fun readUndoSnapshotFromSavedState(): PreResetSnapshot? {
        val priceA: String? = savedStateHandle[KEY_UNDO_PRICE_A]
        val quantityA: String? = savedStateHandle[KEY_UNDO_QUANTITY_A]
        val priceB: String? = savedStateHandle[KEY_UNDO_PRICE_B]
        val quantityB: String? = savedStateHandle[KEY_UNDO_QUANTITY_B]
        val unitA: String? = savedStateHandle[KEY_UNDO_QUANTITY_UNIT_A]
        val unitB: String? = savedStateHandle[KEY_UNDO_QUANTITY_UNIT_B]
        if (priceA == null || quantityA == null) return null
        if (priceB == null || quantityB == null) return null
        return PreResetSnapshot(
            priceARaw = priceA,
            quantityARaw = quantityA,
            priceBRaw = priceB,
            quantityBRaw = quantityB,
            quantityUnitA = unitA?.let { runCatching { QuantityUnit.valueOf(it) }.getOrNull() }
                ?: QuantityUnit.Gram,
            quantityUnitB = unitB?.let { runCatching { QuantityUnit.valueOf(it) }.getOrNull() }
                ?: QuantityUnit.Gram,
        )
    }

    private companion object {
        const val KEY_PRICE_A = "priceA"
        const val KEY_QUANTITY_A = "quantityA"
        const val KEY_PRICE_B = "priceB"
        const val KEY_QUANTITY_B = "quantityB"
        const val KEY_QUANTITY_UNIT_A = "quantityUnitA"
        const val KEY_QUANTITY_UNIT_B = "quantityUnitB"

        const val KEY_UNDO_PRICE_A = "undoPriceA"
        const val KEY_UNDO_QUANTITY_A = "undoQuantityA"
        const val KEY_UNDO_PRICE_B = "undoPriceB"
        const val KEY_UNDO_QUANTITY_B = "undoQuantityB"
        const val KEY_UNDO_QUANTITY_UNIT_A = "undoQuantityUnitA"
        const val KEY_UNDO_QUANTITY_UNIT_B = "undoQuantityUnitB"
        const val KEY_UNDO_DEADLINE = "undoDeadline"

        const val UNDO_LIFETIME_MS = 10_000L

        val BLOCKED_CHARS: Set<Char> = setOf('-', '+', 'e', 'E')
    }
}
