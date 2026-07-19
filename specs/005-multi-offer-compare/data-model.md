# Data Model: Compare Up to Three Offers

## OfferSlotState (UI)

| Field | Type | Notes |
|-------|------|-------|
| priceRaw | String | |
| quantityRaw | String | |
| quantityUnit | QuantityUnit | default Gram |
| priceError | InputError? | |
| quantityError | InputError? | |

List size always in **2..3**.

## ComparisonOutcome (core)

```kotlin
sealed class ComparisonOutcome {
    data object Tie : ComparisonOutcome()
    data class Winner(
        val slotIndex: Int, // 0..n-1 among *input list to comparator*
        val perUnitDelta: BigDecimal,
        val percentDelta: BigDecimal,
    ) : ComparisonOutcome()
}
```

UI maps `slotIndex` through the list of **parsed** offers back to the
visible letter (A/B/C) via the slot’s position in `CompareUiState.offers`
(only parsed slots are passed to `compareMany`; the ViewModel must pass
indices relative to the full UI list — prefer passing `IndexedValue` or
pairs `(uiIndex, Offer)` so `Winner.slotIndex` is the UI index).

**Preferred**: `compareMany(offers: List<IndexedOffer>)` where
`IndexedOffer(uiIndex, offer)` and `Winner.slotIndex = uiIndex`.

## ComparisonGate

- `Incomplete` — fewer than 2 successful parses
- `IncompatibleUnits` — any two parsed offers differ in `Dimension`
- `Ready(ComparisonOutcome)` — otherwise

## PreResetSnapshot

```kotlin
data class PreResetSnapshot(
    val slots: List<OfferSlotSnapshot>, // size 2..3
)
data class OfferSlotSnapshot(
    val priceRaw: String,
    val quantityRaw: String,
    val quantityUnit: QuantityUnit,
)
```

## SavedStateHandle keys

- `offerCount` (Int, 2..3)
- For i in 0..2: `price$i`, `quantity$i`, `quantityUnit$i`
- Undo mirrors with `undoOfferCount` + `undoPrice$i` etc. + deadline

## Constants

- `MIN_OFFERS = 2`, `MAX_OFFERS = 3`
