# Data Model: Richer Result Card (Feature 008)

Domain compare entities and calculation results are **unchanged**. This
feature only changes how a winner’s savings are **presented** on the
hero.

See [contracts/result-savings-display.md](./contracts/result-savings-display.md)
for behavioural / UI contracts.

## Unchanged: `ComparisonOutcome`

```text
sealed ComparisonOutcome {
  Tie
  Winner(slotIndex, secondSlotIndex, perUnitDelta, percentDelta)
}
```

| Field | Meaning | 008 impact |
|-------|---------|------------|
| `slotIndex` | Winning offer index | Unchanged |
| `secondSlotIndex` | Second-cheapest (savings baseline) | Unchanged |
| `perUnitDelta` | Absolute unit-price gap (base units) | Unchanged |
| `percentDelta` | Relative savings vs second-cheapest | **Now displayed** |

Invariants from features 001/004/005 remain: same-dimension offers only;
savings vs second-cheapest; free winner → `percentDelta == 100` when
second is paid.

## Unchanged: `SavingsPresentation`

Produced by `ResultPresenter` (pure Kotlin):

| Field | Type | Notes |
|-------|------|-------|
| `perUnitDelta` | `String` | Locale-formatted absolute savings in **display** units |
| `percentDelta` | `String` | Locale-formatted percent, ≤1 decimal, no `%` glyph |

008 consumes **both** fields in the UI (today only `perUnitDelta` is used).

## New presentation concept: `HeroSavingsCopy` (logical)

Not necessarily a new Kotlin type — may remain local variables in
`ResultRegion`. Documented for clarity:

| Field | When present | Source |
|-------|--------------|--------|
| `headline` | Any non-null outcome | Existing winner / tie strings |
| `absoluteLine` | `Winner` + known dimension | Existing `result_savings_per_*` templates |
| `percentLine` | `Winner` + known dimension | **New** percent template + `percentDelta` |
| `a11ySummary` | Always for result region | Join of visible parts (see contract) |

**Validation / elision rules**:

1. `Tie` → headline only; `absoluteLine` and `percentLine` null.
2. `outcome == null` → placeholder; no savings lines.
3. Incompatible units → error copy; no savings lines.
4. `Winner` with null dimension → no savings lines (same as today).
5. If `absoluteLine` is shown, `percentLine` MUST also be shown (and
   vice versa) for a `Winner` — no half-rich state.

## Unchanged entities (reference)

| Entity | Role |
|--------|------|
| `Offer` / `OfferSlotState` / `QuantityUnit` / `Dimension` | Inputs + display-unit scale |
| `PriceComparator` | Ranking + deltas |
| `CompareUiState` / `CompareViewModel` | Form + outcome; no new fields |
| Appearance preferences (007) | Theme only; no result coupling |

No migrations; no persisted result history.
