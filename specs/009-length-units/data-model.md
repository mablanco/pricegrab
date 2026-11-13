# Data Model: Length Units (Feature 009)

Extends [`specs/004-quantity-units/data-model.md`](../004-quantity-units/data-model.md).
Only the deltas are listed.

## `Dimension` (enum, `core/model/Dimension.kt`)

| Value | `displayUnitScale` | Display unit | Status |
|-------|--------------------|--------------|--------|
| `Mass` | 1000 | kg | unchanged |
| `Volume` | 1000 | L | unchanged |
| **`Length`** | **100** | **m** | **new** |
| `Count` | 1 | piece | unchanged |

Declaration order is not user-visible; `Length` goes after `Volume` for
readability.

## `QuantityUnit` (enum, `core/model/QuantityUnit.kt`)

Declaration order **is** the selector order (FR-001).

| # | Value | `dimension` | `toBaseMultiplier` | Code (en / es) | Name (en / es) | Status |
|---|-------|-------------|--------------------|----------------|----------------|--------|
| 1 | `Gram` | Mass | 1 | g / gr | grams / gramos | unchanged |
| 2 | `Kilogram` | Mass | 1000 | kg / kg | kilograms / kilogramos | unchanged |
| 3 | `Millilitre` | Volume | 1 | ml / ml | millilitres / mililitros | unchanged |
| 4 | `Litre` | Volume | 1000 | L / l | litres / litros | unchanged |
| 5 | **`Centimetre`** | **Length** | **1** | **cm / cm** | **centimetres / centímetros** | **new** |
| 6 | **`Metre`** | **Length** | **100** | **m / m** | **metres / metros** | **new** |
| 7 | `Piece` | Count | 1 | pcs / uds | pieces / unidades | unchanged |

`QuantityUnit.DEFAULT` stays `Gram` (FR-002).

## Derived values (unchanged formulas)

- `Offer.quantityInBaseUnits = quantity × toBaseMultiplier` → centimetres
  for length.
- `Offer.unitPrice = price / quantityInBaseUnits` (DECIMAL64) → price per cm.
- Display delta = `perUnitDelta × Length.displayUnitScale` → per m.

## Validation rules

- Two or more offers are comparable iff all `quantityUnit.dimension` are
  equal (existing `ComparisonGate`). `Centimetre` + `Metre` → comparable;
  any length unit + any non-length unit → `IncompatibleUnits`.
- Price / quantity parsing rules from feature 001 unchanged.

## State persistence

- `SavedStateHandle` keys unchanged; values are `QuantityUnit.name`
  (`"Centimetre"`, `"Metre"`). Unknown names → `Gram`.
- Reset snapshot / undo (feature 002) copies units as opaque enum values;
  no change.
