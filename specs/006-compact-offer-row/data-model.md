# Data Model: Compact Offer Input Row (Feature 006)

Presentation-only. Domain entities from features 001–005 (`Offer`,
`QuantityUnit`, `OfferSlotState`, `ComparisonOutcome`, etc.) are
**unchanged**.

See [contracts/offer-row-layout.md](./contracts/offer-row-layout.md) for
behavioural/UI contracts.

## `OfferInputArrangement`

Derived at composition time from system font scale. Not stored in
`SavedStateHandle` or ViewModel state.

```text
enum OfferInputArrangement {
  CompactSingleRow   // price | quantity | unit
  AdaptiveTwoRow     // price
                     // quantity | unit
}
```

| Arrangement | When | Layout |
|-------------|------|--------|
| `CompactSingleRow` | `fontScale < 1.3` | One `Row`: weighted price + weighted quantity + fixed-width unit |
| `AdaptiveTwoRow` | `fontScale >= 1.3` | `Column`: full-width price; then `Row` of quantity + unit (today’s second-row shape) |

**Mapping function** (pure, unit-testable if extracted):

```text
fun arrangementFor(fontScale: Float): OfferInputArrangement =
    if (fontScale < 1.3f) CompactSingleRow else AdaptiveTwoRow
```

## Unchanged entities (reference)

| Entity | Role |
|--------|------|
| `OfferSlotState` | Raw price/quantity strings, `QuantityUnit`, field errors |
| `CompareUiState` | List of 2..3 slots + outcome + undo |
| `QuantityUnit` / `Dimension` | Unit picker options and compatibility |

No new fields, keys, or migrations.
