# Feature Specification: Compare Up to Three Offers

**Feature Branch**: `005-multi-offer-compare`
**Created**: 2026-07-19
**Status**: Planning complete
**Input**: User request driven by shopper feedback (Brian Dsouza): add a `+`
button so a third offer field can appear for comparing more than two items.
Also nudge the launcher icon vertically so bottom margin matches top under
circular masks. Ship as **v0.1.8** (do not re-tag v0.1.7).

## Clarifications (2026-07-19)

1. **Maximum offers**: **3** (start with 2; `+` adds Offer C; `−` removes
   when count &gt; 2). More than 3 is out of scope — keeps aisle focus.
2. **Result with 3 offers**: show the **cheapest** winner and savings
   **vs the second-cheapest** (same per kg / L / piece templates).
3. **Icon**: further upward optical shift (`ART_OFFSET_Y_PX`) in release prep.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Add a third offer from the aisle (Priority: P1)

A shopper comparing two yogurts notices a third pack on the shelf. They tap
`+`, a third offer card appears, they enter its price/quantity/unit, and
the app tells them which of the three is cheapest per kg (or L / piece)
and how much they save versus the next-best option.

**Why this priority**: Direct response to real user feedback; extends the
core compare loop without changing privacy or offline guarantees.

**Independent Test**: Cold launch → two cards → tap `+` → Offer C visible →
enter three same-dimension offers → winner is the cheapest; savings vs
second-cheapest.

**Acceptance Scenarios**:

1. **Given** cold launch, **Then** exactly two offer cards (A, B); `+`
   enabled; `−` not available (or disabled).
2. **Given** two cards, **When** the shopper taps `+`, **Then** Offer C
   appears (default unit grams) and `+` disables (at max 3).
3. **Given** three valid mass offers, **When** comparison completes,
   **Then** the cheapest offer is named as winner and savings use **per kg**
   against the second-cheapest unit price.
4. **Given** three cards, **When** the shopper removes Offer C, **Then**
   only A and B remain and the result recomputes for the pair.

---

### User Story 2 — Safe gates for incomplete / incompatible sets (Priority: P1)

Blank extra cards must not block a valid two-offer compare. Mixed dimensions
among any filled offers must not produce a false winner.

**Acceptance Scenarios**:

1. **Given** A and B filled and C blank, **Then** comparison runs on A+B only.
2. **Given** fewer than two successfully parsed offers, **Then** placeholder
   (no winner).
3. **Given** A = g and B = ml (valid numbers), **Then** incompatible-units
   error; no winner. Same if C introduces a conflicting dimension.

---

### User Story 3 — Reset / Undo and accessibility (Priority: P2)

Reset clears all slots back to **two** empty Gram defaults and offers Undo
that restores the previous slot count and field values. TalkBack announces
`+` / `−`, offer titles, and winner + savings.

**Acceptance Scenarios**:

1. Reset with three filled offers → two empty cards; Undo restores three.
2. TalkBack: `+` / `−` have localized content descriptions; result live region
   includes winner letter and shelf-unit savings.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Compare screen hosts 2..3 offer slots; labels A/B/C (ES: Oferta).
- **FR-002**: `+` adds one slot up to 3; `−` removes when size &gt; 2.
- **FR-003**: At least two parsed offers required for a result.
- **FR-004**: Blank slots ignored when ≥2 others parse.
- **FR-005**: All parsed offers must share one `Dimension` or show
  incompatible-units error.
- **FR-006**: Winner = lowest `unitPrice`; savings vs second-lowest; tie of
  top two → Tie (existing copy).
- **FR-007**: Savings display units remain per kg / per L / per piece (004).
- **FR-008**: Reset restores two Gram slots; Undo restores full snapshot
  including slot count.
- **FR-009**: Heading no longer says “two offers” only.
- **FR-010**: Release prep nudges launcher icon vertical optical center.

### Key Entities

- **OfferSlot**: raw price/quantity strings, `QuantityUnit`, field errors.
- **ComparisonOutcome** (extended): winner by index / letter, or Tie, with
  deltas vs second-cheapest.
- **PreResetSnapshot**: ordered list of slots (2..3).

## Success Criteria

- **SC-001**: Canonical 3-offer mass scenario identifies correct winner and
  per-kg savings vs second in EN/ES.
- **SC-002**: Two-offer regression suite from 001/004 still green.
- **SC-003**: Cannot add a fourth offer; can return to two via `−` or Reset.
- **SC-004**: TalkBack announces add/remove and result.
- **SC-005**: v0.1.8 ships with icon vertical nudge verified on circular mask.

## Assumptions

- Max three is enough for supermarket aisle use.
- Offer letters stay A/B/C (not 1/2/3).
- Removing prefers the last slot or explicit per-card remove of C when size=3.
- Cold start still defaults to two slots (SavedState may restore three).

## Out of Scope

- Four or more offers; density conversion; F-Droid 0.1.7 `not repro` fix
  (separate); Google Play.
