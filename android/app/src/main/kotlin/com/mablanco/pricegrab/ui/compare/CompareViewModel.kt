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
            priceARaw = savedStateHandle[CompareViewModelKeys.PRICE_A] ?: "",
            quantityARaw = savedStateHandle[CompareViewModelKeys.QUANTITY_A] ?: "",
            priceBRaw = savedStateHandle[CompareViewModelKeys.PRICE_B] ?: "",
            quantityBRaw = savedStateHandle[CompareViewModelKeys.QUANTITY_B] ?: "",
            quantityUnitA = readQuantityUnit(savedStateHandle, CompareViewModelKeys.QUANTITY_UNIT_A),
            quantityUnitB = readQuantityUnit(savedStateHandle, CompareViewModelKeys.QUANTITY_UNIT_B),
            undoState = restoreUndoStateFromSavedState(savedStateHandle),
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

        savedStateHandle[CompareViewModelKeys.PRICE_A] = ""
        savedStateHandle[CompareViewModelKeys.QUANTITY_A] = ""
        savedStateHandle[CompareViewModelKeys.PRICE_B] = ""
        savedStateHandle[CompareViewModelKeys.QUANTITY_B] = ""
        savedStateHandle[CompareViewModelKeys.QUANTITY_UNIT_A] = QuantityUnit.Gram.name
        savedStateHandle[CompareViewModelKeys.QUANTITY_UNIT_B] = QuantityUnit.Gram.name
        savedStateHandle[CompareViewModelKeys.UNDO_PRICE_A] = snapshot.priceARaw
        savedStateHandle[CompareViewModelKeys.UNDO_QUANTITY_A] = snapshot.quantityARaw
        savedStateHandle[CompareViewModelKeys.UNDO_PRICE_B] = snapshot.priceBRaw
        savedStateHandle[CompareViewModelKeys.UNDO_QUANTITY_B] = snapshot.quantityBRaw
        savedStateHandle[CompareViewModelKeys.UNDO_QUANTITY_UNIT_A] = snapshot.quantityUnitA.name
        savedStateHandle[CompareViewModelKeys.UNDO_QUANTITY_UNIT_B] = snapshot.quantityUnitB.name
        savedStateHandle[CompareViewModelKeys.UNDO_DEADLINE] = deadline

        val cleared = CompareUiState(
            undoState = UndoState(snapshot, deadline),
        )
        _state.value = recomputeOutcome(cleared)
    }

    fun undoReset() {
        val undo = _state.value.undoState ?: return
        val snap = undo.snapshot

        savedStateHandle[CompareViewModelKeys.PRICE_A] = snap.priceARaw
        savedStateHandle[CompareViewModelKeys.QUANTITY_A] = snap.quantityARaw
        savedStateHandle[CompareViewModelKeys.PRICE_B] = snap.priceBRaw
        savedStateHandle[CompareViewModelKeys.QUANTITY_B] = snap.quantityBRaw
        savedStateHandle[CompareViewModelKeys.QUANTITY_UNIT_A] = snap.quantityUnitA.name
        savedStateHandle[CompareViewModelKeys.QUANTITY_UNIT_B] = snap.quantityUnitB.name
        clearUndoFromSavedState(savedStateHandle)

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
        clearUndoFromSavedState(savedStateHandle)
        _state.value = _state.value.copy(undoState = null)
    }

    private fun update(transform: (CompareUiState) -> CompareUiState) {
        var next = transform(_state.value)
        if (next.undoState != null) {
            clearUndoFromSavedState(savedStateHandle)
            next = next.copy(undoState = null)
        }
        savedStateHandle[CompareViewModelKeys.PRICE_A] = next.priceARaw
        savedStateHandle[CompareViewModelKeys.QUANTITY_A] = next.quantityARaw
        savedStateHandle[CompareViewModelKeys.PRICE_B] = next.priceBRaw
        savedStateHandle[CompareViewModelKeys.QUANTITY_B] = next.quantityBRaw
        savedStateHandle[CompareViewModelKeys.QUANTITY_UNIT_A] = next.quantityUnitA.name
        savedStateHandle[CompareViewModelKeys.QUANTITY_UNIT_B] = next.quantityUnitB.name
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

    private companion object {
        const val UNDO_LIFETIME_MS = 10_000L

        val BLOCKED_CHARS: Set<Char> = setOf('-', '+', 'e', 'E')
    }
}
