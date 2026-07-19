# Research: Compare Up to Three Offers (Feature 005)

## 1. Max offers = 3

**Decision**: Hard cap at three slots.
**Rationale**: Aisle UX; more would force list chrome and dilute focus.
**Alternatives**: Unlimited N (rejected); fixed three always visible (rejected — keeps cold launch at two).

## 2. Winner vs second-cheapest

**Decision**: Rank parsed offers by `unitPrice`; winner = lowest; savings
deltas vs second-lowest. Top-two equal → `Tie`.
**Rationale**: Matches user choice; fits existing hero card + savings line.
**Alternatives**: Full ranking list (rejected for clutter); savings vs worst (rejected — less useful).

## 3. Domain API shape

**Decision**: Add `PriceComparator.compareMany(offers: List<Offer>): ComparisonOutcome`
requiring `offers.size >= 2` and identical dimensions. Keep `compare(a,b)` as
thin wrapper calling `compareMany(listOf(a,b))` for back-compat tests.
**Rationale**: Single ranking path; dual API stays for existing call sites during migration.
**Alternatives**: Only pairwise tournament (rejected — ambiguous with 3).

## 4. Outcome model

**Decision**: Replace `AWins`/`BWins` with `Winner(slotIndex: Int, perUnitDelta, percentDelta)`
plus existing `Tie`. UI maps index 0/1/2 → Offer A/B/C strings.
**Rationale**: Scales to three without combinatorial sealed subtypes.
**Alternatives**: Keep AWins/BWins/CWins (rejected — awkward).

## 5. Blank slots

**Decision**: Gate uses only successfully parsed offers; need ≥2. Blank C
does not block A vs B.
**Rationale**: Matches “add when ready” mental model.

## 6. Reset / Undo

**Decision**: Snapshot is `List<OfferSlotSnapshot>` (2..3). Reset always
returns to two empty Gram slots.
**Rationale**: Same undo contract as feature 002, extended.

## 7. Icon + version

**Decision**: v0.1.8 / versionCode 9; `ART_OFFSET_Y_PX` more negative (~−90
to −100 after measure); do not re-tag 0.1.7 (F-Droid `not repro`).
**Rationale**: Clean release for Mode B auto-update.
