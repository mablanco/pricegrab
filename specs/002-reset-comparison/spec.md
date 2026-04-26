# Feature Specification: Reset / Start a New Comparison

**Feature Branch**: `002-reset-comparison`
**Created**: 2026-04-26
**Status**: Draft
**Input**: User description: "Add a reset/restart action to the Unit-Price Comparison Screen so the shopper can clear the four input fields and the result in a single tap and start a new comparison, with an Undo affordance to recover from accidental taps."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Start a new comparison without leaving the screen (Priority: P1)

A shopper has just compared two products and obtained a result. They reach a
different shelf and want to compare two completely different products. Today,
the only way to start over is to tap each of the four fields and clear the
text manually, which is slow and error-prone in a one-handed grocery context.
With this feature, a single explicit "reset" action on the screen returns the
form to its initial empty state — all four input fields cleared, the result
hidden — so the next comparison starts immediately.

**Why this priority**: Closes the basic comparison loop the v1 MVP left open.
Without this story, the app's repeated-use case is friction-heavy enough that
many shoppers will resort to backgrounding and re-launching the app. With it,
the app supports its real-world usage pattern (multiple comparisons in one
shopping trip) natively.

**Independent Test**: A tester completes a comparison (enters all four
values, sees a result), triggers the reset action, and verifies that all four
input fields are empty, the result area is hidden or shows the same neutral
prompt as on a cold launch, and the screen is ready to accept a new
comparison — without any other behaviour of the app being involved.

**Acceptance Scenarios**:

1. **Given** all four fields contain valid values and a comparison result is
   on screen,
   **When** the shopper triggers the reset action,
   **Then** all four input fields become empty, the result area returns to
   its empty-state appearance, and the next field the shopper would naturally
   enter (Price A) is ready to receive input.
2. **Given** only some of the four fields contain values and the result area
   is showing the neutral "complete the missing fields" prompt,
   **When** the shopper triggers the reset action,
   **Then** the partially-entered fields are cleared and the screen state is
   indistinguishable from a cold launch.
3. **Given** the form is already empty (cold launch state),
   **When** the shopper triggers the reset action,
   **Then** no observable change occurs and the screen does not display a
   misleading "cleared" confirmation.
4. **Given** any non-empty form state,
   **When** the shopper triggers the reset action with TalkBack enabled,
   **Then** TalkBack announces that the comparison has been reset, in the
   active locale.

---

### User Story 2 — Recover from an accidental reset (Priority: P2)

The reset action is a destructive operation: with one tap the shopper loses
all four values they had typed in. In a noisy supermarket environment with
one-handed phone use, accidental taps are common. To make the reset action
safe to expose prominently, the app MUST give the shopper a brief, clearly
visible opportunity to undo it and restore everything they had before the
reset.

**Why this priority**: Without an undo affordance, the reset action is
either too risky to expose prominently (which defeats the purpose of P1) or
must be hidden behind a confirmation dialog (which adds friction to every
deliberate reset, the common case). An ephemeral undo is the standard
Material Design 3 pattern for safe destructive actions and is what this
spec adopts.

**Independent Test**: A tester completes a comparison, triggers reset,
observes the transient undo affordance, taps it, and verifies that the four
input values and the comparison result are restored exactly as they were
before the reset — without any other behaviour of the app being involved.

**Acceptance Scenarios**:

1. **Given** a non-empty form state with a comparison result on screen,
   **When** the shopper triggers the reset action and then activates the
   undo affordance before it disappears,
   **Then** the four input values and the result on screen are restored
   exactly to their pre-reset state, byte-for-byte for the displayed text.
2. **Given** the undo affordance is visible after a reset,
   **When** the shopper waits for the affordance to time out without
   activating it,
   **Then** the affordance disappears, the form remains empty, and there is
   no visual residue of the previous state on the screen.
3. **Given** the undo affordance is visible after a reset,
   **When** the shopper starts typing a new value into Price A while the
   affordance is still visible,
   **Then** the undo affordance is dismissed (because committing to new
   input has implicitly accepted the reset), and the new value is what
   appears in Price A.
4. **Given** the undo affordance is visible after a reset,
   **When** the device rotates,
   **Then** the affordance survives the rotation with its remaining lifetime
   intact and the undo action still restores the pre-reset state.

---

### Edge Cases

- **Reset on already-empty form**: The reset action MUST be either visibly
  disabled or a silent no-op when there is nothing to clear. It MUST NOT
  display an undo affordance in this case (there is nothing to undo).
- **Reset during in-progress IME composition**: If the shopper taps reset
  while a numeric keypad is open and a character is mid-composition, the
  IME state MUST be cleared cleanly and the field MUST end up empty
  regardless of which composition stage was active.
- **Reset followed by app backgrounding**: If the shopper triggers reset and
  then sends the app to the background before the undo affordance times
  out, the undo affordance MUST NOT be expected to survive the
  backgrounding. The pre-reset state is lost as soon as the app process is
  considered subject to OS reclamation.
- **Reset followed by a configuration change**: Across rotation, theme
  change, locale change in the OS settings, the reset state and the undo
  affordance lifetime MUST be preserved.
- **Reset with the OS "confirm destructive actions" accessibility setting
  enabled**: Out of scope for v1. The undo affordance is the primary safety
  net; we do not surface an additional confirmation dialog. (See
  Assumptions.)
- **Repeated rapid resets**: If the shopper triggers reset multiple times
  in quick succession, only the latest reset's undo state matters. Older
  pre-reset states are not stacked or recoverable.
- **Reset reachable while result is being computed**: Calculation in v1 is
  synchronous (kicked off on input change), so this race is not expected,
  but the spec calls it out: a reset issued while a result is being
  produced MUST cancel the produced result and end up in the empty state.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Unit-Price Comparison Screen MUST expose a single reset
  control that, when activated, clears the four input fields (Price A,
  Quantity A, Price B, Quantity B) and hides any visible comparison result.
- **FR-002**: The reset control's exact placement on the screen
  [NEEDS CLARIFICATION: top app bar trailing action vs floating action button
  vs inline text button — to be decided in /speckit.plan based on Material 3
  conventions and screen-density trade-offs]; whatever placement is chosen,
  it MUST satisfy the constitutional touch-target and contrast requirements.
- **FR-003**: The reset control MUST present a non-empty content
  description in the active locale ("Reset comparison" / "Reiniciar
  comparación" or equivalent) for TalkBack.
- **FR-004**: When the form contains at least one non-empty field, the
  reset control MUST be enabled. When the form is in the cold-launch empty
  state (all four fields empty, no result on screen), the reset control
  MUST be either visibly disabled or hidden — i.e., MUST NOT appear active.
  [NEEDS CLARIFICATION: "disabled" vs "hidden" — disabled is more
  predictable for accessibility but takes screen space; to confirm in
  /speckit.plan.]
- **FR-005**: Activating the reset control MUST atomically:
  1. Set all four input fields to empty.
  2. Hide or neutralise the result area (per FR-006 of feature 001).
  3. Move keyboard focus to the field the shopper would type into next on
     a fresh comparison (Price A).
  4. Trigger a TalkBack announcement that the comparison has been reset.
- **FR-006**: After a reset performed on a non-empty form, the screen MUST
  display an undo affordance. The affordance MUST: (a) clearly state that
  the comparison was reset; (b) offer an explicit "Undo" action; (c)
  disappear automatically after a finite lifetime
  [NEEDS CLARIFICATION: exact duration — Material 3 default for a
  destructive Snackbar with action is in the 5–10 s range; to fix in
  /speckit.plan].
- **FR-007**: Activating the undo affordance MUST restore the four input
  values and the visible comparison result to exactly what they were
  immediately before the reset, character-for-character in the displayed
  text. The undo affordance MUST then disappear.
- **FR-008**: The undo affordance MUST be dismissed (its lifetime
  considered consumed without restoring) when any of the following occurs
  before its automatic timeout:
  1. The shopper begins typing into any of the four input fields.
  2. The undo action itself is activated (in which case the restore
     happens, FR-007).
  3. The app is backgrounded (process subject to OS reclamation; pre-reset
     state is no longer guaranteed recoverable).
- **FR-009**: The reset control, the reset announcement, the undo
  affordance, and its action label MUST all be available in English and
  Spanish, selected from the active locale.
- **FR-010**: All UI introduced by this feature (reset control and undo
  affordance) MUST satisfy the constitutional accessibility requirements:
  contrast ≥ AA, font scale up to 200%, touch target ≥ 48dp, focus order
  reachable by D-pad / keyboard, content description on every interactive
  element.
- **FR-011**: This feature MUST NOT cause the screen to require any
  network access, persistence, or data collection. The pre-reset state
  used by undo lives in memory only, for the lifetime of the undo
  affordance.

### Key Entities

- **Pre-Reset Snapshot**: A transient in-memory record of the four input
  values and the displayed comparison result captured at the moment a
  non-empty reset is triggered. Lifetime: from reset until the undo
  affordance is dismissed (FR-008). Not persisted, not serialised across
  process death.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A shopper coming from a completed comparison can return the
  screen to the empty state in 1 tap. Measured: starting from a screen with
  four valid inputs and a visible result, exactly 1 tap on the reset
  control returns the screen to its cold-launch appearance.
- **SC-002**: 100% of automated tests covering the reset action confirm
  that, after reset, all four input fields are empty and the result area
  is in its empty/neutral state, regardless of the pre-reset state.
- **SC-003**: 100% of automated tests covering the undo action confirm
  that, after activating undo within the affordance's lifetime, the four
  input values and the result area are restored to their pre-reset values,
  character-for-character.
- **SC-004**: A TalkBack-only tester (eyes closed, no visual feedback) can
  perform a reset and verbally confirm the screen has been cleared, in
  both English and Spanish, in 100% of test runs.
- **SC-005**: With the system font scale at 200%, the reset control and
  the undo affordance remain fully readable and tappable, with no truncated
  labels and no occluded touch areas.
- **SC-006**: No functionality introduced by this feature requires network
  access, and turning the device to airplane mode has no observable effect
  on reset or undo.
- **SC-007**: The end-to-end "reset → undo → state restored" cycle survives
  rotation in 100% of test runs, with the undo affordance still
  functional after rotation.

## Assumptions

- The reset action operates only on the Unit-Price Comparison Screen as
  defined by feature 001; this feature does not introduce a navigation or
  multi-screen reset semantics. All future references to "reset" or "the
  reset action" elsewhere in the product MUST be specified separately.
- The undo affordance is implemented as a transient surface (Material 3
  Snackbar pattern) rather than a confirmation dialog. The choice trades
  pre-action friction for post-action recoverability and is consistent
  with Material 3 guidance for safe destructive actions. A confirmation
  dialog is explicitly out of scope for v1.
- The Android OS-level "confirm destructive actions" accessibility setting,
  if introduced or honored in a future Android release, will be revisited
  in a follow-up feature; v1 of this feature does not surface an
  app-level confirmation dialog gated on it.
- No persistence is introduced by this feature. The pre-reset snapshot
  required by undo lives in memory only and is lost across app process
  death. This keeps the feature consistent with feature 001's
  no-persistence guarantee (FR-014 of feature 001).
- The reset action does not log, transmit, or otherwise leak the values
  the shopper entered. This is enforced by the constitution's Privacy &
  Platform Constraints and is reaffirmed here for clarity.
- The exact placement of the reset control (top app bar vs FAB vs other)
  is deferred to /speckit.plan, where Material 3 guidance and the screen's
  current vertical density will inform the choice. The spec is written so
  that all functional behaviour is independent of the placement decision.
- The undo affordance's lifetime is bounded but its exact value is
  deferred to /speckit.plan. The spec only requires that it is finite,
  long enough to read and react to, and short enough not to clutter the
  screen indefinitely.
- The feature is additive to feature 001 and does not modify the
  comparison logic itself. All FR-001..FR-014 of feature 001 remain in
  force unchanged.
