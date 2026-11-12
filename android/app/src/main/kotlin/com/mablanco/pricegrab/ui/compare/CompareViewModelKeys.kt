package com.mablanco.pricegrab.ui.compare

import androidx.lifecycle.SavedStateHandle
import com.mablanco.pricegrab.core.model.QuantityUnit

internal object CompareViewModelKeys {
    const val OFFER_COUNT = "offerCount"
    const val UNDO_OFFER_COUNT = "undoOfferCount"
    const val UNDO_DEADLINE = "undoDeadline"

    fun price(i: Int): String = "price$i"
    fun quantity(i: Int): String = "quantity$i"
    fun quantityUnit(i: Int): String = "quantityUnit$i"

    fun undoPrice(i: Int): String = "undoPrice$i"
    fun undoQuantity(i: Int): String = "undoQuantity$i"
    fun undoQuantityUnit(i: Int): String = "undoQuantityUnit$i"
}

internal const val UNDO_LIFETIME_MS = 10_000L

private val BLOCKED_INPUT_CHARS: Set<Char> = setOf('-', '+', 'e', 'E')

/**
 * Drops characters the numeric fields never accept (signs, exponent markers,
 * whitespace). Applied both to typed input and to every string read back
 * from [SavedStateHandle].
 */
internal fun sanitizeNumericInput(value: String): String =
    value.filter { c -> c !in BLOCKED_INPUT_CHARS && !c.isWhitespace() }

// MainActivity is exported, and AndroidX seeds SavedStateHandle defaults from
// the launch intent's extras, so any app can put arbitrary values under these
// keys. Every read below is type-checked and re-validated instead of trusted.
private fun readString(savedStateHandle: SavedStateHandle, key: String): String? =
    savedStateHandle.get<Any?>(key) as? String

private fun readInt(savedStateHandle: SavedStateHandle, key: String): Int? =
    savedStateHandle.get<Any?>(key) as? Int

internal fun readQuantityUnit(savedStateHandle: SavedStateHandle, key: String): QuantityUnit {
    val stored: String? = readString(savedStateHandle, key)
    return stored?.let { runCatching { QuantityUnit.valueOf(it) }.getOrNull() }
        ?: QuantityUnit.Gram
}

internal fun readOffersFromSavedState(savedStateHandle: SavedStateHandle): List<OfferSlotState> {
    val count = (readInt(savedStateHandle, CompareViewModelKeys.OFFER_COUNT) ?: MIN_OFFERS)
        .coerceIn(MIN_OFFERS, MAX_OFFERS)
    return List(count) { i ->
        OfferSlotState(
            priceRaw = sanitizeNumericInput(
                readString(savedStateHandle, CompareViewModelKeys.price(i)) ?: "",
            ),
            quantityRaw = sanitizeNumericInput(
                readString(savedStateHandle, CompareViewModelKeys.quantity(i)) ?: "",
            ),
            quantityUnit = readQuantityUnit(savedStateHandle, CompareViewModelKeys.quantityUnit(i)),
        )
    }
}

internal fun persistOffersToSavedState(
    savedStateHandle: SavedStateHandle,
    offers: List<OfferSlotState>,
) {
    savedStateHandle[CompareViewModelKeys.OFFER_COUNT] = offers.size
    offers.forEachIndexed { i, slot ->
        savedStateHandle[CompareViewModelKeys.price(i)] = slot.priceRaw
        savedStateHandle[CompareViewModelKeys.quantity(i)] = slot.quantityRaw
        savedStateHandle[CompareViewModelKeys.quantityUnit(i)] = slot.quantityUnit.name
    }
    for (i in offers.size until MAX_OFFERS) {
        savedStateHandle.remove<String>(CompareViewModelKeys.price(i))
        savedStateHandle.remove<String>(CompareViewModelKeys.quantity(i))
        savedStateHandle.remove<String>(CompareViewModelKeys.quantityUnit(i))
    }
}

internal fun clearUndoFromSavedState(savedStateHandle: SavedStateHandle) {
    savedStateHandle.remove<Int>(CompareViewModelKeys.UNDO_OFFER_COUNT)
    savedStateHandle.remove<Long>(CompareViewModelKeys.UNDO_DEADLINE)
    for (i in 0 until MAX_OFFERS) {
        savedStateHandle.remove<String>(CompareViewModelKeys.undoPrice(i))
        savedStateHandle.remove<String>(CompareViewModelKeys.undoQuantity(i))
        savedStateHandle.remove<String>(CompareViewModelKeys.undoQuantityUnit(i))
    }
}

internal fun persistUndoToSavedState(
    savedStateHandle: SavedStateHandle,
    snapshot: PreResetSnapshot,
    deadline: Long,
) {
    savedStateHandle[CompareViewModelKeys.UNDO_OFFER_COUNT] = snapshot.slots.size
    savedStateHandle[CompareViewModelKeys.UNDO_DEADLINE] = deadline
    snapshot.slots.forEachIndexed { i, slot ->
        savedStateHandle[CompareViewModelKeys.undoPrice(i)] = slot.priceRaw
        savedStateHandle[CompareViewModelKeys.undoQuantity(i)] = slot.quantityRaw
        savedStateHandle[CompareViewModelKeys.undoQuantityUnit(i)] = slot.quantityUnit.name
    }
    for (i in snapshot.slots.size until MAX_OFFERS) {
        savedStateHandle.remove<String>(CompareViewModelKeys.undoPrice(i))
        savedStateHandle.remove<String>(CompareViewModelKeys.undoQuantity(i))
        savedStateHandle.remove<String>(CompareViewModelKeys.undoQuantityUnit(i))
    }
}

internal fun restoreUndoStateFromSavedState(savedStateHandle: SavedStateHandle): UndoState? {
    val deadline = savedStateHandle.get<Any?>(CompareViewModelKeys.UNDO_DEADLINE) as? Long ?: return null
    val now = System.currentTimeMillis()
    // A deadline further out than one undo lifetime was not written by us.
    if (deadline <= now || deadline > now + UNDO_LIFETIME_MS) return null
    val snapshot = readUndoSnapshotFromSavedState(savedStateHandle) ?: return null
    return UndoState(snapshot, deadline)
}

internal fun readUndoSnapshotFromSavedState(savedStateHandle: SavedStateHandle): PreResetSnapshot? {
    val count = readInt(savedStateHandle, CompareViewModelKeys.UNDO_OFFER_COUNT) ?: return null
    if (count !in MIN_OFFERS..MAX_OFFERS) return null

    val slots = (0 until count).map { i ->
        val price = readString(savedStateHandle, CompareViewModelKeys.undoPrice(i))
        val quantity = readString(savedStateHandle, CompareViewModelKeys.undoQuantity(i))
        if (price == null || quantity == null) {
            return@map null
        }
        OfferSlotSnapshot(
            priceRaw = sanitizeNumericInput(price),
            quantityRaw = sanitizeNumericInput(quantity),
            quantityUnit = readQuantityUnit(
                savedStateHandle,
                CompareViewModelKeys.undoQuantityUnit(i),
            ),
        )
    }
    if (slots.any { it == null }) return null
    return PreResetSnapshot(slots.filterNotNull())
}
