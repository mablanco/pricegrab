# Contract: Quantity Units & Extended Price Comparison

Pins behaviour for feature 004. **Extends** (does not replace)
[`specs/001-unit-price-comparison/contracts/price-comparator.md`](../../001-unit-price-comparison/contracts/price-comparator.md).
When this document and the feature 001 contract disagree on unit-aware
behaviour, **this document wins** for feature 004 onward.

## Locations

```
android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/QuantityUnit.kt
android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/Dimension.kt
android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/Offer.kt
android/app/src/main/kotlin/com/mablanco/pricegrab/core/format/OfferParser.kt
android/app/src/main/kotlin/com/mablanco/pricegrab/core/calc/PriceComparator.kt
android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareViewModel.kt
android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/ResultPresenter.kt
```

Pure Kotlin for everything under `core/`; no Android imports in `core/**`.

## `OfferParser` signature (extended)

```kotlin
object OfferParser {
    fun parse(
        rawPrice: String,
        rawQuantity: String,
        quantityUnit: QuantityUnit,
        locale: Locale,
    ): OfferParseResult
}
```

All validation rules from feature 001 apply unchanged. On success,
`Offer(price, quantity, quantityUnit)` is returned.

## `PriceComparator` (unchanged signature, extended inputs)

```kotlin
object PriceComparator {
    fun compare(a: Offer, b: Offer): ComparisonOutcome
}
```

### Preconditions (extended)

Caller MUST supply two valid `Offer` instances satisfying feature 001
rules **and**:

- `a.quantityUnit.dimension == b.quantityUnit.dimension`.

Violating the dimension rule is a **caller** error. The comparator MAY
throw `IllegalArgumentException` with message containing both dimensions
as a defensive guard, but the ViewModel MUST prevent reaching this in
normal UI flow (see Comparison gate below).

All other preconditions (non-negative price, positive quantity) unchanged.

### Postconditions (unchanged)

Winner decision uses **base-unit** `Offer.unitPrice`:

- Mass: € per gram internally.
- Volume: € per millilitre internally.
- Count: € per piece internally.

`perUnitDelta` and `percentDelta` in `AWins` / `BWins` are computed in
**base-unit price space**, identical formulas to feature 001 but applied
to normalized `unitPrice`.

## Comparison gate (ViewModel contract)

Before calling `PriceComparator.compare`:

```kotlin
fun evaluateComparison(
    a: Offer,
    b: Offer,
): ComparisonGate
```

| Condition | Result |
|-----------|--------|
| Either offer not successfully parsed | `ComparisonGate.Incomplete` |
| `a.quantityUnit.dimension != b.quantityUnit.dimension` | `ComparisonGate.IncompatibleUnits` |
| Dimensions match | `ComparisonGate.Ready(PriceComparator.compare(a, b))` |

**UI obligations when `IncompatibleUnits`:**

- Do **not** render winner headline or savings line.
- Show localized `error_incompatible_units` in the result region (same
  polite live region as feature 003; no hero card elevation for errors).
- Do **not** call TalkBack with a winner announcement.

## `ResultPresenter` (extended)

```kotlin
object ResultPresenter {
    fun present(
        outcome: ComparisonOutcome?,
        dimension: Dimension?,
        locale: Locale,
    ): SavingsPresentation?
}
```

Returns `null` for `null` outcome, `Tie`, or `IncompatibleUnits`
(`dimension == null`).

When non-null:

- `displayDelta = perUnitDelta × dimension.displayUnitScale` (see
  [`data-model.md`](../data-model.md)).
- Format `displayDelta` with existing `LocaleNumberFormatter` rules
  (up to six fraction digits, trailing zeros stripped).
- `percentDelta` unchanged from feature 001 presenter.

The **screen** selects the savings template from `dimension`:

| Dimension | String resource (EN) |
|-----------|----------------------|
| `Mass`    | `result_savings_per_kg` — "Save %1$s per kg" |
| `Volume`  | `result_savings_per_L` — "Save %1$s per L" |
| `Count`   | `result_savings_per_piece` — "Save %1$s per piece" |

Replace usage of legacy `result_savings` ("per unit") in the hero card
body when dimension is known.

## Required unit tests — domain & comparator

Extend or add tests **before** implementation ships. Inputs use
`(priceA, qtyA, unitA, priceB, qtyB, unitB)` unless noted.

| # | Inputs | Expected | Purpose |
|---|--------|----------|---------|
| U1 | `2.50/500/g, 4.00/1/kg` | `BWins`; base Δ = 0.001 €/g; display Δ = 1.00 €/kg | Canonical spec SC-001 |
| U2 | `1.20/500/ml, 2.00/1/L` | `BWins`; display savings per L | Volume normalization |
| U3 | `3.00/6/pcs, 5.00/12/pcs` | `BWins`; display Δ scaled ×1 | Count |
| U4 | `2.00/100/g, 4.00/200/g` | `Tie` | Same dimension, equal base unit price |
| U5 | `2.50/500/g, 4.00/1000/g` | Same as feature 001 case #1 when both `Gram` | Backward compatibility |
| U6 | `0/5/g, 1/5/g` | `AWins`, pct=100 | Free offer unchanged |
| U7 | Precondition: compare g-offer vs ml-offer directly | throws `IllegalArgumentException` | Defensive comparator guard (optional if gate is strictly in VM) |
| U8 | Gate: parsed g vs parsed ml | `IncompatibleUnits`, no `compare` call | Cross-dimension block |

Feature 001 canonical cases **#1–#11** MUST remain green when all quantities
use `QuantityUnit.Gram` with the same numeric values as before.

## Required instrumented tests (summary)

- Default `g` on cold launch for A and B.
- U1 scenario end-to-end: hero card shows "per kg" / localized equivalent.
- g vs ml → incompatible message, no winner; fix unit → result appears.
- Reset clears units to `g`; Undo restores prior units.
- TalkBack quantity semantics include unit name.

## Coverage gate

Unchanged: `core/calc/**` line coverage ≥ 90% in CI. New conversion logic
in `Offer` lives under `core/model/` — cover with dedicated unit tests;
JaCoCo scope for the gate remains `core/calc/**` only unless the project
later expands the gate (out of scope for 004).

## Reset / Undo contract (extends feature 002)

| Action | Unit behaviour |
|--------|----------------|
| `resetComparison()` on non-empty form | Set `quantityUnitA` and `quantityUnitB` to `Gram`; persist in `SavedStateHandle` |
| `resetComparison()` snapshot | Include both units from pre-reset state |
| `undoReset()` | Restore snapshotted units exactly |
| User edits unit while Undo Snackbar visible | Dismiss undo (unchanged FR-008.1) |

## Versioning note

This contract applies from **v0.1.7** (`versionCode` 8) onward. Releases
≤ v0.1.6 behave as feature 001 (dimensionless quantities).
