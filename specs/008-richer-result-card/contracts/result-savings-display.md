# Contract: Result savings display (hero)

Behavioural contract for feature **008**. Calculation contracts in
`specs/001-…` and `specs/005-…` remain authoritative for math.

## When savings appear

| UI state | Headline | Absolute line | Percent line |
|----------|----------|---------------|--------------|
| Empty / incomplete | Placeholder | — | — |
| Incompatible units | Error copy | — | — |
| `Tie` | Tied message | — | — |
| `Winner` + known dimension | “Offer X is cheaper” | Required | Required |
| `Winner` + unknown dimension | Winner headline | — | — |

Absolute and percent MUST appear together for a displayable winner
(no absolute-only or percent-only hero).

## Content rules

### Absolute line (existing)

```text
template(dimension, perUnitDelta, versusOfferTitle)
→ result_savings_per_kg | result_savings_per_L | result_savings_per_piece
```

- `perUnitDelta`: from `ResultPresenter` (display units).
- `versusOfferTitle`: localized Offer A/B/C for `secondSlotIndex`.
- Meaning: savings **vs second-cheapest** (feature 005).

### Percent line (new)

```text
template(percentDelta) → e.g. en "%1$s%% less" / es "%1$s%% menos"
```

- `percentDelta`: from `ResultPresenter` (no `%` in the value string).
- Resource owns the percent sign and wording.
- Same rounding as today (≤1 decimal, half-up, trailing zeros stripped).

### Accessibility summary

```text
Winner:  "<headline>. <absoluteLine>. <percentLine>"
Tie:     "<headline>"
Empty:   "<placeholder>"
Incompat:"<incompatible message>"
```

Live region remains polite on the result region container.

## Test tags

| Element | Tag (existing unless noted) |
|---------|-----------------------------|
| Result region | `result` (`TEST_TAG_RESULT`) |
| Hero card | `heroResult` |
| Winner / tie headline | `result_text` |
| Absolute savings | `result_savings` |
| Percent savings | `result_savings_percent` (**new**) |

Instrumented winner tests MUST assert both `result_savings` and
`result_savings_percent` (or equivalent text matchers) are displayed.

## Non-goals (contract boundaries)

- No change to `percentDelta` formula or second-cheapest selection.
- No offer renaming / free-text labels.
- No share/copy actions on the hero.
- No Settings toggle for “show percent”.
