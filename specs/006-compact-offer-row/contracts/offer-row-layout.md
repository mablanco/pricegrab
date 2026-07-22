# Contract: Offer input row layout

## Arrangement selection

```text
arrangementFor(fontScale):
  fontScale < 1.3  → CompactSingleRow
  fontScale ≥ 1.3  → AdaptiveTwoRow
```

Call site reads `LocalDensity.current.fontScale` during composition.
Changing font scale recomposes into the other arrangement; entered
price/quantity/unit values MUST remain (ViewModel / SavedState — already
true).

## CompactSingleRow geometry

For each offer with test-tag prefix `P` (`offerA` / `offerB` / `offerC`):

| Control | Test tag | Placement |
|---------|----------|-----------|
| Price | `{P}_price` | Same horizontal row as quantity and unit |
| Quantity | `{P}_quantity` | Same row |
| Unit | `{P}_unit` | Same row; trailing; width ≥ existing unit selector |

**Invariant (default density, fontScale = 1.0)**:

```text
abs(centerY(price) - centerY(quantity)) < ROW_Y_TOLERANCE
abs(centerY(quantity) - centerY(unit)) < ROW_Y_TOLERANCE
left(price) < left(quantity) < left(unit)
```

`ROW_Y_TOLERANCE` is an implementation constant (suggested ≤ 24 dp equivalent
in px) generous enough for label/error asymmetry but tight enough to fail
today’s stacked layout.

## AdaptiveTwoRow geometry

At `fontScale = 2.0` (instrumented via `LocalDensity`):

```text
bottom(price) ≤ top(quantity) + SMALL_GAP_SLACK
left(quantity) < left(unit)
all of {price, quantity, unit} still assertIsDisplayed (scroll allowed)
```

## Interaction contract (unchanged)

| Concern | Rule |
|---------|------|
| IME | Price `ImeAction.Next` → quantity; quantity `Done` |
| Keyboard | Decimal numeric for price and quantity |
| Touch | Each control ≥ 48×48 dp |
| Semantics | Existing `cd_price_field`, `cd_quantity_field` (+ unit name), `cd_quantity_unit` |
| Add/remove | Offer C uses the same arrangement rule as A/B |
| Errors | Per-field `supportingText`; no silent failure |

## Non-goals

- No change to comparison output tags (`result`, `heroResult`, …).
- No rename of `{P}_price` / `{P}_quantity` / `{P}_unit`.
- No persistence of arrangement enum.
