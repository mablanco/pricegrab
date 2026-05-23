# Data Model: Quantity Units for Offer Comparison (Feature 004)

Extends feature 001's domain model with real quantity units, dimension
awareness, and a comparison gate for incompatible measures. All new
framework-agnostic types live in `com.mablanco.pricegrab.core.model`
unless noted.

See also [`contracts/quantity-units.md`](./contracts/quantity-units.md) for
behavioural contracts and canonical test cases.

## `Dimension`

The physical **kind** of measure. Two offers are comparable only when
both `QuantityUnit`s map to the same dimension.

```kotlin
enum class Dimension {
    Mass,
    Volume,
    Count,
}
```

**Display properties** (used by `ResultPresenter`, not by `PriceComparator`):

| Dimension | Savings string key suffix | `displayUnitScale` — multiply base `perUnitDelta` for display |
|-----------|---------------------------|---------------------------------------------------------------|
| `Mass`    | per kg                    | `1000` (base is g, display is kg)                             |
| `Volume`  | per L                     | `1000` (base is ml, display is L)                             |
| `Count`   | per piece                 | `1`                                                           |

## `QuantityUnit`

One selectable unit per offer quantity field.

```kotlin
enum class QuantityUnit {
    Gram,
    Kilogram,
    Millilitre,
    Litre,
    Piece,
    ;

    val dimension: Dimension
    /** Multiply the shopper-entered quantity to express it in base units. */
    val toBaseMultiplier: BigDecimal
}
```

**Mapping**

| Constant      | UI code | Dimension | Base unit | `toBaseMultiplier` |
|---------------|---------|-----------|-----------|--------------------|
| `Gram`        | g       | Mass      | g         | 1                  |
| `Kilogram`    | kg      | Mass      | g         | 1000               |
| `Millilitre`  | ml      | Volume    | ml        | 1                  |
| `Litre`       | L       | Volume    | ml        | 1000               |
| `Piece`       | pcs     | Count     | pcs       | 1                  |

**Defaults**

- `QuantityUnit.DEFAULT = Gram` — used for cold launch, after Reset, and
  when `SavedStateHandle` has no stored unit key (FR-002).

**Persistence in UI layer**

- Stored in `SavedStateHandle` as the enum **name** string
  (`"Gram"`, `"Kilogram"`, …) for stability across app versions.
- Dropdown closed-state label uses the short UI code from string resources
  (`unit_code_g`, …); menu items use localized full names
  (`unit_name_gram`, …).

## `Offer` (extended)

A single side of the comparison (A or B).

**Fields**

| Field           | Type            | Required | Validation                                      |
|-----------------|-----------------|----------|-------------------------------------------------|
| `price`         | `BigDecimal`    | yes      | `price.signum() >= 0`                           |
| `quantity`      | `BigDecimal`    | yes      | `quantity.signum() > 0`                         |
| `quantityUnit`  | `QuantityUnit`  | yes      | defaults to `QuantityUnit.Gram` when omitted in tests only; production parser always sets explicitly |

**Derived**

| Derived                 | Type         | Definition                                                                 |
|-------------------------|--------------|----------------------------------------------------------------------------|
| `quantityInBaseUnits`   | `BigDecimal` | `quantity × quantityUnit.toBaseMultiplier`                                 |
| `unitPrice`             | `BigDecimal` | `price.divide(quantityInBaseUnits, MathContext.DECIMAL64)` — price **per base unit** (€/g, €/ml, or €/pc) |

**Invariants**

- Immutable data class; same purity guarantees as feature 001.
- `unitPrice` depends on normalized quantity, not the raw selector scale.
- Two offers with the same dimension compare fairly regardless of whether
  the shopper typed g vs kg or ml vs L.

**Example**

- 2.50 € / 500 g → `quantityInBaseUnits = 500`, `unitPrice = 0.005` €/g (= 5.00 €/kg display).
- 4.00 € / 1 kg → `quantityInBaseUnits = 1000`, `unitPrice = 0.004` €/g (= 4.00 €/kg display) → B wins.

## `OfferParseResult`

Unchanged sealed interface shape from feature 001. `Success(offer)` now
carries `Offer` with `quantityUnit` supplied by the caller:

```kotlin
OfferParser.parse(rawPrice, rawQuantity, quantityUnit, locale)
```

Parse failures (`InvalidPrice`, `NonPositiveQuantity`, …) are unchanged;
unit selection does not create new parse variants.

## `ComparisonOutcome` (unchanged shape)

Produced by `PriceComparator.compare(a, b)` when **both** offers are valid
**and** the caller has verified matching dimensions.

| Variant | Meaning | Payload |
|---------|---------|---------|
| `Tie` | Equal base `unitPrice` | none |
| `AWins(perUnitDelta, percentDelta)` | A cheaper per base unit | deltas in **base-unit price space** (€/g, €/ml, €/pc) |
| `BWins(perUnitDelta, percentDelta)` | B cheaper per base unit | same |

`percentDelta` semantics unchanged from feature 001. Only presentation
(`ResultPresenter`) converts `perUnitDelta` to shelf units via
`Dimension.displayUnitScale`.

## `ComparisonGate` (UI / ViewModel layer)

The ViewModel must **not** call `PriceComparator.compare` when dimensions
differ. Document as an explicit gate so tests have a named concept:

```kotlin
sealed interface ComparisonGate {
    /** Both offers parsed; dimensions match — run comparator. */
    data class Ready(val outcome: ComparisonOutcome) : ComparisonGate

    /** Both offers parsed; dimensions differ — show error, no winner. */
    data object IncompatibleUnits : ComparisonGate

    /** One or both offers invalid or incomplete — neutral placeholder. */
    data object Incomplete : ComparisonGate
}
```

**State transitions** (extends feature 001 `CompareUiState` flow):

1. User edits any raw field or unit selector → re-parse both offers.
2. If either parse fails or any numeric field blank → `Incomplete`.
3. If both succeed and `quantityUnitA.dimension != quantityUnitB.dimension`
   → `IncompatibleUnits` (`outcome = null`, dedicated error flag / message).
4. If both succeed and dimensions match → `Ready(PriceComparator.compare(...))`.

Changing a unit selector so dimensions align clears `IncompatibleUnits`
immediately when numeric inputs still parse (spec US2 AS-3).

## UI State (`CompareUiState` extensions)

```kotlin
data class CompareUiState(
  // ... existing four *Raw, four *Error, outcome, undoState from features 001–002
  val quantityUnitA: QuantityUnit = QuantityUnit.Gram,
  val quantityUnitB: QuantityUnit = QuantityUnit.Gram,
  val incompatibleUnits: Boolean = false,  // true only when gate == IncompatibleUnits
)
```

`outcome` remains `null` for both `Incomplete` and `IncompatibleUnits`;
the screen distinguishes them via `incompatibleUnits` (and non-null field
errors only on `& Incomplete path).

**`isResetEnabled` extension** (feature 002): true when any raw field is
non-blank **or** either unit differs from `QuantityUnit.Gram`.

## `PreResetSnapshot` (extended — feature 002)

```kotlin
data class PreResetSnapshot(
    val priceARaw: String,
    val quantityARaw: String,
    val priceBRaw: String,
    val quantityBRaw: String,
    val quantityUnitA: QuantityUnit,
    val quantityUnitB: QuantityUnit,
)
```

Reset captures all six values; clearing sets units to `Gram`. Undo
restores all six. Outcome is still recomputed from raw strings + units,
not stored in the snapshot.

## SavedStateHandle keys (new)

| Key               | Type     | Default on missing |
|-------------------|----------|--------------------|
| `quantityUnitA`   | `String` | `"Gram"`           |
| `quantityUnitB`   | `String` | `"Gram"`           |

Undo snapshot keys mirror the same unit strings under the existing
`undo*` prefix pattern if units are snapshotted separately, or embed in
serialized snapshot — implementation may choose either as long as undo
round-trips (see `plan.md`).

## What this model is NOT

- **Not** cross-dimension conversion (no g ↔ ml).
- **Not** persisted across cold starts beyond FR-002 default `g`.
- **Not** a change to two-offer scope or price-entry rules.
