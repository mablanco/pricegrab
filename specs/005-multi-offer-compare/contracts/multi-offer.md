# Contract: Multi-offer price comparison

## PriceComparator.compareMany

**Input**: `List<Pair<Int, Offer>>` (uiIndex → offer), size ≥ 2, all same
`Dimension` (else `IllegalArgumentException`).

**Output**:

| Case | Result |
|------|--------|
| All unitPrices equal (top cluster) | `Tie` when min price shared by ≥2 at the minimum; if only one unique minimum, that one wins even if others tie for second |
| Unique cheapest | `Winner(uiIndex, perUnitDelta, percentDelta)` vs **second-cheapest** unit price |
| Two+ share the absolute minimum | `Tie` |

**Deltas** (same formulas as pairwise today):

- `perUnitDelta = second.unitPrice - winner.unitPrice` (absolute)
- `percentDelta = perUnitDelta / second.unitPrice * 100` (100 if second is free and winner free? — if winner is free and second &gt; 0, percent = 100)

`compare(a,b)` delegates to `compareMany(listOf(0 to a, 1 to b))` mapping
`Winner(0)`/`Winner(1)` for compatibility during migration; tests may
assert on `Winner` indices instead of AWins/BWins after refactor.

## ComparisonGate.evaluate

Given parse results parallel to UI slots:

1. Collect successes with ui indices.
2. If count &lt; 2 → Incomplete.
3. If any dimension mismatch among successes → IncompatibleUnits.
4. Else Ready(compareMany(...)).

## ResultPresenter

`present(outcome, dimension, locale)`:

- `Tie` → no savings line (unchanged).
- `Winner` → format `perUnitDelta` with dimension scale (kg/L/piece).

Headline strings: `result_winner` with `%1$s` = localized offer title
(Offer A/B/C).

## Reset / Undo

- Reset → two empty Gram slots; snapshot previous list.
- Undo → restore list length and all fields.

## UI controls

- `+` visible/enabled iff `offers.size < 3`
- `−` on Offer C (or last) enabled iff `offers.size > 2`
