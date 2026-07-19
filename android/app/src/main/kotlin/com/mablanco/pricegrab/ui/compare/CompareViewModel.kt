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
 * Owns the state for the Compare screen. Keeps 2..3 offer slots in the
 * [SavedStateHandle] so process death and configuration changes do not wipe
 * them out.
 */
class CompareViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state: MutableStateFlow<CompareUiState> = MutableStateFlow(
        CompareUiState(
            offers = readOffersFromSavedState(savedStateHandle),
            undoState = restoreUndoStateFromSavedState(savedStateHandle),
        ),
    )

    val state: StateFlow<CompareUiState> = _state.asStateFlow()

    init {
        _state.value = recomputeOutcome(_state.value)
    }

    fun onPriceChange(index: Int, value: String) = updateSlot(index) {
        it.copy(priceRaw = sanitize(value))
    }

    fun onQuantityChange(index: Int, value: String) = updateSlot(index) {
        it.copy(quantityRaw = sanitize(value))
    }

    fun onUnitChange(index: Int, unit: QuantityUnit) = updateSlot(index) {
        it.copy(quantityUnit = unit)
    }

    fun addOffer() {
        val current = _state.value
        if (current.offers.size >= MAX_OFFERS) return
        update { it.copy(offers = it.offers + OfferSlotState()) }
    }

    fun removeOffer() {
        val current = _state.value
        if (current.offers.size <= MIN_OFFERS) return
        update { it.copy(offers = it.offers.dropLast(1)) }
    }

    fun resetComparison() {
        val current = _state.value
        if (!current.isResetEnabled) return

        val snapshot = PreResetSnapshot(
            slots = current.offers.map { slot ->
                OfferSlotSnapshot(
                    priceRaw = slot.priceRaw,
                    quantityRaw = slot.quantityRaw,
                    quantityUnit = slot.quantityUnit,
                )
            },
        )
        val deadline = System.currentTimeMillis() + UNDO_LIFETIME_MS

        persistOffersToSavedState(
            savedStateHandle,
            listOf(OfferSlotState(), OfferSlotState()),
        )
        persistUndoToSavedState(savedStateHandle, snapshot, deadline)

        val cleared = CompareUiState(
            offers = listOf(OfferSlotState(), OfferSlotState()),
            undoState = UndoState(snapshot, deadline),
        )
        _state.value = recomputeOutcome(cleared)
    }

    fun undoReset() {
        val undo = _state.value.undoState ?: return
        val snap = undo.snapshot
        val restoredOffers = snap.slots.map { slot ->
            OfferSlotState(
                priceRaw = slot.priceRaw,
                quantityRaw = slot.quantityRaw,
                quantityUnit = slot.quantityUnit,
            )
        }

        persistOffersToSavedState(savedStateHandle, restoredOffers)
        clearUndoFromSavedState(savedStateHandle)

        val restored = CompareUiState(offers = restoredOffers)
        _state.value = recomputeOutcome(restored)
    }

    fun dismissUndo() {
        if (_state.value.undoState == null) return
        clearUndoFromSavedState(savedStateHandle)
        _state.value = _state.value.copy(undoState = null)
    }

    private fun updateSlot(index: Int, transform: (OfferSlotState) -> OfferSlotState) {
        update { state ->
            require(index in state.offers.indices) { "Offer index out of range: $index" }
            state.copy(
                offers = state.offers.mapIndexed { i, slot ->
                    if (i == index) transform(slot) else slot
                },
            )
        }
    }

    private fun update(transform: (CompareUiState) -> CompareUiState) {
        var next = transform(_state.value)
        if (next.undoState != null) {
            clearUndoFromSavedState(savedStateHandle)
            next = next.copy(undoState = null)
        }
        persistOffersToSavedState(savedStateHandle, next.offers)
        _state.value = recomputeOutcome(next)
    }

    private fun recomputeOutcome(state: CompareUiState): CompareUiState {
        val locale = Locale.getDefault()
        val parseResults = state.offers.map { slot ->
            OfferParser.parse(slot.priceRaw, slot.quantityRaw, slot.quantityUnit, locale)
        }

        val gate = evaluateComparison(parseResults)
        val outcome = when (gate) {
            is ComparisonGate.Ready -> gate.outcome
            ComparisonGate.Incomplete, ComparisonGate.IncompatibleUnits -> null
        }

        val offersWithErrors = state.offers.mapIndexed { index, slot ->
            val result = parseResults[index]
            slot.copy(
                priceError = priceErrorFor(result, slot.priceRaw),
                quantityError = quantityErrorFor(result, slot.quantityRaw),
            )
        }

        return state.copy(
            offers = offersWithErrors,
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
