# Research: Richer Result Card (Feature 008)

## 1. What is broken / missing today

**Decision**: Treat this as a **presentation gap**, not a calculation gap.

**Findings**:

- `PriceComparator` and `ComparisonOutcome.Winner` already carry
  `percentDelta` (vs second-cheapest).
- `ResultPresenter.present(...)` already returns
  `SavingsPresentation(perUnitDelta, percentDelta)` with one-decimal
  rounding and locale numerals.
- `ResultRegion` builds `savingsLine` from **absolute only**
  (`result_savings_per_kg` / `_per_L` / `_per_piece`) and ignores
  `savings.percentDelta`.
- The polite live-region summary is `"$headline. $savingsLine"`, so
  TalkBack also omits percent (contrary to older comments that claimed
  percent lived in a11y only).

**Rationale**: Spec FR-001/FR-006 ask to surface existing data, not invent
new math.

**Alternatives considered**: Recomputing percent in the UI — rejected
(duplication risk). Changing comparator formulas — out of scope.

## 2. Layout: how to make percent glanceable

**Decision**: Keep the **absolute savings as the primary body line**
(unchanged copy pattern: “Save X per kg vs Offer A”). Add a **second
visible body line** for percent using a dedicated localized template,
e.g. en `"%1$s%% less"` / es `"%1$s%% menos"` (exact wording finalized in
implementation to match tone of existing result strings).

Typography suggestion (plan-level, not binding pixels):

| Role | Style token | Color token |
|------|-------------|-------------|
| Winner / tie headline | `headlineSmall` | `onSurface` |
| Absolute savings | `bodyLarge` | `onSurfaceVariant` |
| Percent savings | `bodyLarge` (or `titleMedium` if QA wants more punch) | `onSurfaceVariant` |

**Rationale**:

- Matches spec assumption: absolute stays primary; percent is companion.
- A second line is harder to miss in the aisle than a parenthetical
  buried at the end of a long absolute sentence (especially with
  “vs Offer X” and long localized numbers).
- Height cost is small after feature 006 compacted offer rows; wrapping
  at 200% font is preferred over truncation.

**Alternatives considered**:

| Option | Why not (for 008) |
|--------|-------------------|
| Restore single string `"Save X … (Y% less)"` from feature 001 | Densest; percent easy to miss; harder to assert separately in UI tests |
| Percent-only emphasis / drop absolute | Violates FR-001; absolute is the money-per-unit cue |
| Badge / chip / progress bar for percent | Extra chrome; risks color-only cues; overkill for M3 hero |
| Show full unit-price table for all offers | Out of scope (ranked list) |

## 3. String & formatting contract

**Decision**:

- Absolute templates stay dimension-specific (`per kg` / `per L` /
  `per piece`) with `perUnitDelta` + versus-offer title args — **no
  formula change**.
- Percent uses `ResultPresenter`’s already-rounded `percentDelta`
  string; UI adds the percent sign via resource template (never hardcode
  `%` in Kotlin for user-visible copy if the template can own it).
- Do **not** invent new rounding: keep max one decimal, half-up, strip
  trailing zeros (existing presenter behavior). Showing a rounded `"0"`
  percent when the true delta is tiny remains acceptable (spec edge
  case).

**Rationale**: FR-002–FR-004; reuse battle-tested formatter tests.

**Alternatives considered**: Move percent formatting into string
resources only — rejected; numerals must stay locale-aware in code.
Change tiny-percent policy to hide `"0%"` — deferred; would need product
sign-off beyond 008.

## 4. Accessibility summary

**Decision**: For a unique winner, live-region /
`contentDescription` becomes:

```text
"<headline>. <absolute savings line>. <percent savings line>"
```

(Localized punctuation/joins as appropriate; both savings parts must be
present whenever they are visible.)

Tie / empty / incompatible: unchanged quiet behavior — no savings
clauses.

Icon remains decorative (`contentDescription = null` /
invisible-to-user pattern as today); announcement comes from the result
region summary + visible texts.

**Rationale**: FR-006 / SC-004; sighted and TalkBack users get the same
magnitude story.

**Alternatives considered**: Percent only in TalkBack — explicitly
rejected by the product decision that motivated 008. Separate live
regions per line — unnecessary noise.

## 5. Test strategy

**Decision**:

1. Update `CompareScreenSavingsTest.bWinsShowsAbsoluteAndPercentSavings`
   (rename if needed) to assert the **visible** percent node/text for the
   20% fixture — not only absolute.
2. Assert free-offer path shows visible **100** percent companion.
3. Keep `tieHidesSavingsRow` (extend to assert percent node absent too).
4. Extend or add an a11y instrumented check that the result region’s
   semantics include percent for a winner.
5. Leave `PriceComparatorTest` and core math alone unless a regression
   appears (should not).

**Rationale**: Constitution V; SC-001–SC-003, SC-004, SC-007.

## 6. Versioning & PR letters

**Decision**: Stay on **0.1.x**; release-prep targets **0.1.11**
(`versionCode` 12). Planning PR letter **AD**, implementation **AE**,
release-prep **AF**. Prompt Marco before any 0.2.0 discussion.

**Rationale**: Presentation enhancement on the existing compare surface;
patch line is appropriate per project status version policy cue.

## 7. Out of scope (reconfirmed)

Offer labels, share/copy, pack-vs-loose, 4+ offers, currency/FX, new
units, favorites, widgets, splash, landscape redesign — unchanged from
spec Out of Scope. No Settings entries for this feature.
