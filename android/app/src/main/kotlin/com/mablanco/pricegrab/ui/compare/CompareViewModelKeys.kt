package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import com.mablanco.pricegrab.core.model.QuantityUnit

internal object CompareViewModelKeys {
    const val PRICE_A = "priceA"
    const val QUANTITY_A = "quantityA"
    const val PRICE_B = "priceB"
    const val QUANTITY_B = "quantityB"
    const val QUANTITY_UNIT_A = "quantityUnitA"
    const val QUANTITY_UNIT_B = "quantityUnitB"

    const val UNDO_PRICE_A = "undoPriceA"
    const val UNDO_QUANTITY_A = "undoQuantityA"
    const val UNDO_PRICE_B = "undoPriceB"
    const val UNDO_QUANTITY_B = "undoQuantityB"
    const val UNDO_QUANTITY_UNIT_A = "undoQuantityUnitA"
    const val UNDO_QUANTITY_UNIT_B = "undoQuantityUnitB"
    const val UNDO_DEADLINE = "undoDeadline"
}

internal fun readQuantityUnit(savedStateHandle: SavedStateHandle, key: String): QuantityUnit {
    val stored: String? = savedStateHandle[key]
    return stored?.let { runCatching { QuantityUnit.valueOf(it) }.getOrNull() }
        ?: QuantityUnit.Gram
}

internal fun clearUndoFromSavedState(savedStateHandle: SavedStateHandle) {
    savedStateHandle.remove<String>(CompareViewModelKeys.UNDO_PRICE_A)
    savedStateHandle.remove<String>(CompareViewModelKeys.UNDO_QUANTITY_A)
    savedStateHandle.remove<String>(CompareViewModelKeys.UNDO_PRICE_B)
    savedStateHandle.remove<String>(CompareViewModelKeys.UNDO_QUANTITY_B)
    savedStateHandle.remove<String>(CompareViewModelKeys.UNDO_QUANTITY_UNIT_A)
    savedStateHandle.remove<String>(CompareViewModelKeys.UNDO_QUANTITY_UNIT_B)
    savedStateHandle.remove<Long>(CompareViewModelKeys.UNDO_DEADLINE)
}

internal fun restoreUndoStateFromSavedState(savedStateHandle: SavedStateHandle): UndoState? {
    val deadline: Long = savedStateHandle[CompareViewModelKeys.UNDO_DEADLINE] ?: return null
    if (deadline <= System.currentTimeMillis()) return null
    val snapshot = readUndoSnapshotFromSavedState(savedStateHandle) ?: return null
    return UndoState(snapshot, deadline)
}

internal fun readUndoSnapshotFromSavedState(savedStateHandle: SavedStateHandle): PreResetSnapshot? {
    val priceA: String? = savedStateHandle[CompareViewModelKeys.UNDO_PRICE_A]
    val quantityA: String? = savedStateHandle[CompareViewModelKeys.UNDO_QUANTITY_A]
    val priceB: String? = savedStateHandle[CompareViewModelKeys.UNDO_PRICE_B]
    val quantityB: String? = savedStateHandle[CompareViewModelKeys.UNDO_QUANTITY_B]
    val unitA: String? = savedStateHandle[CompareViewModelKeys.UNDO_QUANTITY_UNIT_A]
    val unitB: String? = savedStateHandle[CompareViewModelKeys.UNDO_QUANTITY_UNIT_B]
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
