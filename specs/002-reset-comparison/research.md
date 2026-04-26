# Research: Reset / Start a New Comparison (Feature 002)

Phase 0 research consolidating the decisions that the plan depends on.
Each entry follows the **Decision / Rationale / Alternatives considered**
format. Decisions resolve the three `[NEEDS CLARIFICATION]` markers in
[`spec.md`](./spec.md) (FR-002, FR-004, FR-006).

## 1. Placement of the Reset control (resolves FR-002)

**Decision**: Place a single `IconButton` as the trailing action of a
Material 3 `TopAppBar` (`CenterAlignedTopAppBar` for symmetry with the
future wordmark in feature 003). The icon is `Icons.Filled.Restore`
(rounded counter-clockwise arrow, the conventional "reset to initial"
glyph). The button carries a non-empty `contentDescription` populated
from a localized string.

**Rationale**:

- Material 3 places destructive / global screen actions in the trailing
  slot of the top app bar. This is the convention shoppers familiar
  with any Material app will instinctively scan first.
- The top app bar already needs to exist for feature 003 (visual refresh
  + wordmark). Introducing it here lets us land the Scaffold + TopAppBar
  shell early without entangling it with a visual rewrite, satisfying
  the Spec Kit principle of small, single-purpose features.
- A FAB would compete with the result card for visual prominence and
  collide with the keyboard on a one-handed grocery use case (FAB
  position is bottom-end, where the IME would push it).
- An inline `TextButton` under the form would be either always visible
  (dead space when the form is empty) or only visible after a
  comparison runs (a "result-y" position that misleads first-time
  users), and it would not cleanly accommodate feature 003's wordmark.
- The top app bar IconButton's default touch target is 48dp, which
  satisfies the constitution's accessibility requirement without
  custom sizing.

**Alternatives considered**:

- *FAB (`Icons.Filled.Restore`)* — rejected; collides with IME, breaks
  the "single hero on screen is the result" UX intent, and the FAB
  semantic role is "primary action", which Reset is not.
- *Inline `TextButton` ("Clear comparison") below the form* — rejected;
  forces a layout decision (always visible vs conditionally visible)
  that creates either dead space or magical disappearing affordances.
- *Hardware "back" gesture as reset* — rejected; back already exits the
  screen / app on Android, and overloading it would surprise users.

## 2. Disabled vs hidden state when the form is empty (resolves FR-004)

**Decision**: Render the Reset `IconButton` *visibly disabled* (default
Material 3 disabled tint, `enabled = false`, no ripple) when all four
inputs are empty *and* there is no active `UndoState`. Keep the button
in the same screen position; do not hide it.

**Rationale**:

- A control whose presence depends on screen state is harder to
  discover and harder to learn. With "visibly disabled", the user sees
  the affordance from the first launch, learns its location, and as
  soon as they type anything the button activates — a natural
  "earn-as-you-go" affordance.
- In the top app bar slot, the IconButton's footprint is already
  reserved; making it appear and disappear would shift any future
  trailing actions left/right on the app bar, which is jarring.
- For TalkBack, a permanently-present-but-disabled button is more
  predictable than a button that comes and goes from the focus order.
  Material 3 honours `enabled = false` by exposing
  `disabled = true` in the accessibility node, so TalkBack reads the
  state correctly.

**Alternatives considered**:

- *Hide the button entirely when empty* — rejected; harder to discover,
  shifts neighbouring controls, complicates focus order. Would require
  extra TalkBack hints to communicate that "Reset" exists when the
  form is non-empty.
- *Always enabled, no-op when empty* — rejected; disagrees with FR-004
  ("MUST NOT appear active") and exposes a misleading "did the tap do
  something?" question.

## 3. Undo affordance: pattern, lifetime, and dismissal (resolves FR-006)

**Decision**: Use a Material 3 `Snackbar` shown via the `Scaffold`'s
`SnackbarHost` with **`SnackbarDuration.Long`** (≈10 s). The Snackbar
text reads "Comparison cleared" / "Comparación borrada". The action
label reads "Undo" / "Deshacer". Tapping the action calls
`viewModel.undoReset()`.

The Snackbar lifetime is also short-circuited by:

- The user starting to type in any of the four input fields →
  `dismissUndo()` is called from inside `onPriceAChange` /
  `onQuantityAChange` / `onPriceBChange` / `onQuantityBChange` before
  the new value is applied. The Snackbar fades out via
  `SnackbarHostState.currentSnackbarData?.dismiss()`.
- The host activity reaching `Lifecycle.State.STOPPED` (app backgrounded
  long enough that the OS could reclaim the process). The activity's
  `lifecycleScope` collects state via `repeatOnLifecycle(STARTED)`,
  and a `DisposableEffect` on `STOPPED` calls `dismissUndo()`.

**Rationale**:

- Material 3 explicitly recommends a Snackbar with an action for *safe
  undo of destructive operations*. This is the textbook fit.
- `SnackbarDuration.Long` (~10 s) is the upper end of the Material 3
  recommended range for actionable Snackbars; it gives a one-handed
  shopper enough time to read, react, and tap. The lower
  `SnackbarDuration.Short` (~4 s) is intended for non-actionable
  acknowledgements and is too brief here.
- The Material 3 `Snackbar` is automatically wrapped in a polite live
  region and is announced by TalkBack with its message and the action
  label, satisfying FR-005.4 and FR-009 without extra code.
- Persisting the Undo lifetime as an *epoch-ms deadline* in
  `SavedStateHandle` (rather than a remaining duration) is what makes
  AS-2.4 ("survives rotation with remaining lifetime intact") cheap:
  on rotation we read the deadline, compute `remaining = deadline -
  now()`, and re-show the Snackbar for `remaining` ms. If `remaining
  ≤ 0` we drop the UndoState silently.

**Alternatives considered**:

- *Confirmation dialog before reset* — rejected; adds friction to the
  common case (deliberate reset) just to protect against the rare case
  (accidental tap). Material 3 explicitly discourages this for safe-
  undo scenarios.
- *`SnackbarDuration.Indefinite` until manual dismissal* — rejected;
  clutters the screen for users who consciously don't want Undo.
- *Restart the Snackbar timer on rotation* — rejected (see deadline
  rationale above); would let a user game an indefinite undo by
  rotating repeatedly.
- *Custom non-Snackbar surface (e.g., persistent banner)* — rejected;
  requires re-implementing accessibility, motion, and theming work
  that Snackbar already provides.

## 4. SavedStateHandle keys and process-death survival

**Decision**: Persist `UndoState` through `SavedStateHandle` using
three keys: `KEY_UNDO_PRICE_A`, `KEY_UNDO_QUANTITY_A`, `KEY_UNDO_PRICE_B`,
`KEY_UNDO_QUANTITY_B`, `KEY_UNDO_DEADLINE_EPOCH_MS`. Presence of the
deadline key indicates an active UndoState; absence means no active
Undo.

**Rationale**:

- `SavedStateHandle` is the same mechanism feature 001 already uses for
  the four raw fields, so the persistence story stays uniform.
- Splitting the snapshot into individual keys (rather than a single
  `Bundle`) avoids the need for a custom `Parcelable` and keeps the
  values inspectable from `adb shell dumpsys activity` / Studio's
  Layout Inspector when debugging.
- Process death after a reset is a rare edge case; per spec FR-008.3
  the affordance is *not* expected to survive process death. The
  deadline check on restore (`if deadline < now then null`) implements
  this naturally — if the OS keeps the saved state long enough that
  the deadline has expired, the UndoState is silently dropped on the
  next process resume.

**Alternatives considered**:

- *`@Parcelize` data class persisted as a single `Bundle`* — rejected;
  trivially more code and a Parcelable migration risk in future
  refactors, with no observable benefit.
- *Don't persist UndoState at all* — rejected; would lose the
  affordance on rotation (a config change), violating AS-2.4.

## 5. Reset announcement to TalkBack (FR-005.4)

**Decision**: The Snackbar itself is the announcement vehicle. Material
3's `SnackbarHost` is implemented as a polite live region; TalkBack
announces the message ("Comparison cleared") on appearance. We do *not*
add a separate `LiveRegionMode.Polite` node elsewhere on the screen for
the same announcement, because that would double-speak.

For the no-Snackbar branch (a Reset issued on an already-empty form,
which is a no-op per AS-1.3), no TalkBack announcement is emitted: the
button is disabled in that state, so the user cannot reach this branch
through normal interaction. Defensively, the ViewModel's `reset()`
short-circuits when the form is empty without raising any event.

**Rationale**:

- One announcement per action. Material 3 gives this for free; adding
  a parallel polite live-region label would create a "double" hearing
  effect that is well-known to annoy daily TalkBack users.
- The Snackbar action is reachable via TalkBack focus and is read with
  its label ("Undo") and its role ("button"). No extra
  `contentDescription` is required; Material 3 sets it from the
  Snackbar's `actionLabel`.

**Alternatives considered**:

- *Custom polite live region in the result area* — rejected; double
  announcement.
- *Assertive announcement* — rejected; interrupts the user mid-action.

## 6. Focus management after Reset (FR-005.3)

**Decision**: On Reset, request keyboard focus on the Price A input
field via a `FocusRequester` exposed to the Composable. Do *not*
auto-open the IME (soft keyboard) — that is decided by the focus + the
field's `KeyboardOptions`, and an auto-opened IME on Reset would feel
overly aggressive in a one-handed grocery context.

**Rationale**:

- Returning focus to the natural "next field to type" is the
  expectation for any "start over" action. Without it the user has to
  hunt for the first field, which is exactly the friction this feature
  exists to remove.
- Letting the IME open *only when the user actually taps the focused
  field* keeps Reset itself a quiet, gesture-only action — important
  on devices with hardware keyboards or D-pad navigation.

**Alternatives considered**:

- *Auto-open the IME on Reset* — rejected; jarring on devices without a
  soft keyboard, and overrides the user's intent to look at the screen
  before typing.
- *Don't move focus at all* — rejected; user has to re-tap into the
  field they just emptied, defeating the purpose of the one-tap loop.

## 7. Out-of-scope items and deferrals

To keep this feature small, the following are explicitly deferred:

- **Reset confirmation when the OS-level "confirm destructive actions"
  accessibility setting is enabled** — handled by spec Assumption #3,
  deferred to a follow-up feature.
- **A "clear single field" affordance** (e.g., trailing `Clear` icon
  inside each `OutlinedTextField`) — distinct UX, would clutter the
  form, deferred indefinitely.
- **Persisting the Undo state across process death** — explicitly
  out-of-scope per spec FR-008.3.
- **Undo history (multiple stacked snapshots)** — spec is intentionally
  scoped to the single most recent reset. Stacked undo is a richer
  feature and would belong to a separate spec.
