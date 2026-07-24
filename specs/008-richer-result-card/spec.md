# Feature Specification: Richer Result Card (Visible Absolute + Percent Savings)

**Feature Branch**: `008-richer-result-card`
**Created**: 2026-07-24
**Status**: Ready for tasks (plan complete 2026-07-24)
**Input**: User description: "Richer result card for the supermarket aisle:
make both absolute per-unit savings and percent savings clearly visible in
the comparison hero so a shopper deciding between different pack sizes or
brands can see how much cheaper the winner is at a glance. Percent must not
remain TalkBack-only or computed-but-hidden. Offer labels, share/export,
and pack-vs-loose modes stay out of scope."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — See how much cheaper at a glance (Priority: P1)

A shopper in one supermarket holds two (or three) products of different
size or brand. They enter prices and quantities and get a winner. Today the
hero clearly names the cheaper offer and shows absolute savings (e.g. save
X per kg vs the next offer), but the relative savings (percent less) are
not visible on screen even though the app already computes them. After this
feature, the same shopper sees **both** the absolute savings and the
percent savings without enabling accessibility tools or reading fine print
elsewhere. They can decide in a couple of seconds whether the cheaper
option is “a little” or “a lot” better.

**Why this priority**: This is the core value of the feature and matches the
aisle use case (same store, different products). Absolute savings alone
can feel abstract; percent gives an instant sense of magnitude.

**Independent Test**: Enter a known two-offer case where Offer B is 20%
cheaper per unit → hero shows winner + absolute savings **and** a visible
percent figure consistent with 20% → change inputs to a tie → percent and
absolute savings both disappear → change to a free-offer win → percent
shows 100 (or equivalent localized “100%”) together with absolute savings.

**Acceptance Scenarios**:

1. **Given** two same-dimension offers where one is uniquely cheaper,
   **When** the comparison result appears, **Then** the hero shows the
   winner headline, the absolute per-unit savings (vs the second-cheapest
   offer, named as today), **and** the percent savings as visible text
   (not only as an accessibility announcement).
2. **Given** three same-dimension offers with a unique cheapest,
   **When** the result appears, **Then** absolute and percent savings
   both reflect the gap vs the **second-cheapest** offer (same rule as
   today), and both are visible on the hero.
3. **Given** a unique winner, **When** the shopper looks at the hero at
   arm’s length, **Then** absolute savings and percent savings are both
   readable without opening menus, settings, or accessibility overlays.
4. **Given** a unique winner in `en` and again in `es`, **When** the
   result appears, **Then** both savings figures use fully localized
   copy and locale-appropriate number formatting (including decimal
   separators).

---

### User Story 2 — Quiet result when there is nothing to save (Priority: P1)

When offers tie on unit price, or when the form is incomplete / invalid so
there is no comparison outcome, the hero must not invent or leave stale
savings figures. The richer card only adds prominence when there is a
real unique winner.

**Why this priority**: Avoids misleading aisle decisions and preserves
trust in the result region.

**Independent Test**: Equal unit prices → tied message, no absolute and no
percent savings visible. Clear or partially fill fields → no winner hero
savings. Incompatible units → incompatible-units message, no savings
figures.

**Acceptance Scenarios**:

1. **Given** two or more offers with equal unit prices, **When** the
   result is a tie, **Then** the tied message appears and neither
   absolute nor percent savings are shown.
2. **Given** incomplete or unparsable offers so there is no outcome,
   **When** the shopper looks at the result region, **Then** the
   placeholder (or existing empty-state copy) appears and no savings
   figures are shown.
3. **Given** offers with incompatible quantity dimensions, **When** the
   incompatible-units state is active, **Then** that error is shown and
   no absolute or percent savings are shown.

---

### User Story 3 — Accessibility and large text still work (Priority: P2)

A shopper using TalkBack, or with system font scale up to 200%, still gets
a coherent announcement and a usable layout. The visible percent is also
included in the accessibility summary so sighted and non-sighted users
hear the same magnitude story. Large font must not clip the richer hero
or force loss of either savings figure on a typical phone in portrait.

**Why this priority**: Accessibility is a release blocker per the
constitution; this story is the safety net around US1’s new visible text.

**Independent Test**: TalkBack announces winner + absolute + percent in
one polite summary when there is a winner. At 200% font scale, both
savings figures remain fully readable (no truncation of meaning, no
overlap). Color alone is never the only cue that one offer won.

**Acceptance Scenarios**:

1. **Given** a unique winner, **When** TalkBack focuses the result
   region, **Then** the announcement includes the winner, the absolute
   savings, and the percent savings.
2. **Given** a tie or empty result, **When** TalkBack focuses the result
   region, **Then** the announcement matches the visible state and does
   not mention savings figures.
3. **Given** system font scale at 200%, **When** a unique winner is
   shown, **Then** headline, absolute savings, and percent savings remain
   readable without overlap or clipped meaning on a typical phone in
   portrait.
4. **Given** light and dark appearance (including user Settings
   overrides), **When** the richer hero is shown, **Then** text contrast
   for the new/changed savings copy meets WCAG 2.1 AA.

---

### Edge Cases

- Free winning offer (100% savings vs a paid second-cheapest): both
  absolute and percent remain visible; percent communicates “fully free
  vs the next option.”
- Very small relative savings that round to a displayed 0% with the
  existing one-decimal rounding rule: still show the percent figure
  consistently with today’s rounding (do not invent a new precision
  rule in this feature).
- Very large absolute deltas or long localized strings: layout must not
  hide the percent or the absolute line; prefer wrapping over truncation
  of meaning.
- Configuration change (rotation, locale change, process recreation while
  fields are filled): result content stays consistent with the same inputs;
  no stale percent from a previous comparison.
- Three offers where two tie for cheapest: existing comparison rules
  apply unchanged; this feature only changes how a **Winner** savings
  presentation is shown, not who wins.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: When the comparison outcome is a unique winner, the result
  hero MUST show both (a) absolute per-unit savings and (b) percent
  savings as **visible** text on screen.
- **FR-002**: Absolute and percent savings MUST continue to measure the
  gap versus the **second-cheapest** offer (including naming that offer
  in the absolute-savings copy as today). Calculation rules MUST NOT
  change.
- **FR-003**: Percent savings MUST use the same rounding/presentation
  rules already established for percent (at most one decimal place,
  locale-aware numerals). The UI MUST supply any percent symbol via
  localized string templates (ES/EN).
- **FR-004**: Absolute savings MUST remain dimension-aware as today
  (per kg / per L / per piece) with localized copy.
- **FR-005**: On tie, empty/incomplete input, or incompatible units, the
  UI MUST NOT show absolute or percent savings.
- **FR-006**: The accessibility summary for a unique winner MUST include
  winner, absolute savings, and percent savings so TalkBack users receive
  the same magnitude information as sighted users.
- **FR-007**: The richer hero MUST remain usable at system font scale up
  to at least 200% without overlap or loss of either savings figure’s
  meaning.
- **FR-008**: All new or changed user-facing strings MUST exist in both
  English and Spanish with no hardcoded UI copy.
- **FR-009**: Winner identity MUST still be communicated with text plus a
  non-color cue (existing icon/shape pattern); this feature MUST NOT
  rely on color alone to convey “cheaper” or “how much.”
- **FR-010**: The compare flow MUST remain the primary surface; this
  feature MUST NOT add new screens, settings entries, or navigation.

### Key Entities

- **Comparison result (winner)**: The unique cheapest offer among entered
  same-dimension offers, with absolute and percent savings versus the
  second-cheapest offer.
- **Savings presentation**: The pair of shopper-facing figures — absolute
  per display-unit savings and percent less — shown together on the hero
  when a winner exists.
- **Result hero**: The elevated result region on the compare screen that
  presents winner headline and savings; empty/tie/error states stay
  quiet about savings.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In 100% of unique-winner test cases used for QA, a sighted
  reviewer can identify both absolute and percent savings from the hero
  without TalkBack or developer tools.
- **SC-002**: For the canonical 20%-cheaper fixture (and the free-offer
  100% fixture), displayed percent matches the already-established
  rounded percent string in both `en` and `es`.
- **SC-003**: Tie, empty, and incompatible-units states show **zero**
  visible savings figures in 100% of those checks.
- **SC-004**: TalkBack’s result announcement for a unique winner includes
  absolute and percent savings in 100% of accessibility smoke runs.
- **SC-005**: At 200% font scale on a typical phone in portrait, both
  savings figures remain fully readable (no clipped meaning) in light and
  dark appearance.
- **SC-006**: A shopper can read winner + absolute + percent and decide
  which product to keep in under 3 seconds in manual aisle-style walkthroughs
  (two-offer and three-offer happy paths).
- **SC-007**: Existing comparison behaviors (who wins, vs second-cheapest,
  units, reset/undo, settings appearance) show no functional regressions in
  the automated and smoke suites for this release.

## Assumptions

- The primary context is **in-store** comparison of different pack sizes or
  brands in the **same** establishment — not cross-store shopping.
- Absolute savings stay the primary “how much money per unit” statement;
  percent is the companion magnitude cue. Exact typography/layout is left
  to planning as long as both are clearly visible (e.g. second line,
  parenthetical, or equally scannable pair).
- Percent is already computed by the product; this feature is about
  **surfacing and hierarchy**, not new math.
- Offer titles remain the existing Offer A/B/C labels; free-text product
  or store names are intentionally deferred.
- Release stays on **0.1.x** (likely **0.1.11** at release-prep). Agents
  must **prompt Marco** before any **MINOR** bump (0.2.0+).
- No network, analytics, accounts, or on-disk history are introduced.

## Out of Scope

- Free-text offer labels (store or brand names typed by the user).
- Copy/share/export of the result (clipboard or system share sheet).
- Pack-vs-loose / “how many will you buy?” purchase-quantity framing.
- Changing comparison formulas, ranking, or the three-offer cap.
- Showing a full ranked list of all offers’ unit prices.
- Currency symbols, FX, or new unit systems (oz, lb, …).
- Favorites/templates, widgets, splash branding, custom fonts.
- Landscape / tablet / foldable redesign beyond keeping the richer hero
  readable in the current portrait-first layout.
- Google Play or any constitution-forbidden network features.
