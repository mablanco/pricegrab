# Feature Specification: Settings Appearance (Theme & Material You)

**Feature Branch**: `007-settings-appearance`
**Created**: 2026-07-24
**Status**: Draft
**Input**: User description: "Create an options/settings page so users can
choose whether to enable Material You and whether the app follows the
light, dark, or system color scheme."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Choose light, dark, or system theme (Priority: P1)

A shopper who prefers a fixed appearance (always light in bright aisles, or
always dark at night) opens Settings from the comparison screen and picks
**Light**, **Dark**, or **System**. The whole app immediately follows that
choice. **System** (the default on a fresh install) continues to track the
device appearance setting, matching today’s behavior.

**Why this priority**: Theme override is the most common appearance need and
delivers clear value even without Material You. It is independently shippable
as an MVP.

**Independent Test**: Fresh install → Confirm app follows system appearance →
Open Settings → Choose Light → App is light regardless of system dark mode →
Choose Dark → App is dark → Choose System → App tracks the device again →
Force-stop and relaunch → Last choice is still applied.

**Acceptance Scenarios**:

1. **Given** a fresh install with no prior preference, **When** the shopper
   opens the app, **Then** appearance follows the device system setting
   (same as before this feature).
2. **Given** the shopper is on the Settings screen, **When** they select
   Light (or Dark), **Then** all app screens redraw in that mode without
   requiring a restart.
3. **Given** the shopper selected Light or Dark, **When** they later choose
   System, **Then** the app again follows the device appearance setting.
4. **Given** the shopper changed the theme preference, **When** they
   force-stop the app and reopen it, **Then** the last selected preference
   is still in effect.
5. **Given** the shopper has entered prices on the Compare screen, **When**
   they open Settings, change theme, and return, **Then** those entered
   values are still present.

---

### User Story 2 — Optionally use Material You colors (Priority: P1)

A shopper on a device that supports wallpaper-derived colors can turn
**Material You** on in Settings. When on, the app’s accent and surface
colors follow the device’s dynamic palette (still respecting the Light /
Dark / System preference). When off (the default), the app keeps the
fixed PriceGrab brand palette shipped since visual polish. On devices that
do not support Material You, the control is unavailable or clearly inactive
and the brand palette remains in use.

**Why this priority**: Restores the constitution’s Material You expectation
as an explicit user choice without forcing brand inconsistency on everyone.
Pairs with US1; both belong in the same Settings surface.

**Independent Test**: On a Material You–capable device → Settings → enable
Material You → app colors reflect the wallpaper-derived palette → disable →
brand palette returns → preference survives relaunch. On a non-capable
device → Material You cannot be enabled; brand palette stays.

**Acceptance Scenarios**:

1. **Given** a fresh install on a Material You–capable device, **When** the
   shopper has not changed appearance options, **Then** Material You is off
   and the brand palette is used.
2. **Given** Material You is available and currently off, **When** the
   shopper turns it on, **Then** the app’s colors update to the device’s
   dynamic palette without a restart, still respecting the current Light /
   Dark / System choice.
3. **Given** Material You is on, **When** the shopper turns it off, **Then**
   the brand palette is restored immediately.
4. **Given** the shopper enabled Material You, **When** they force-stop and
   reopen the app, **Then** Material You remains on.
5. **Given** a device that does not support Material You, **When** the
   shopper views Settings, **Then** they cannot enable Material You, and the
   brand palette remains in use with an understandable indication that the
   option is unavailable.

---

### User Story 3 — Reach Settings without losing the compare flow (Priority: P2)

From the comparison screen, the shopper can open Settings, adjust appearance
options, and return to Compare in one back step. Settings is clearly labeled
in both Spanish and English, usable with TalkBack, and remains readable at
large system font scale (up to at least 200%).

**Why this priority**: Discovery and accessibility of the new screen; without
this, US1/US2 exist but are hard to reach. Independent of which preference
controls are present.

**Independent Test**: From Compare → open Settings via the documented entry
point → verify ES and EN labels → exercise TalkBack on theme and Material You
controls → set font scale to 200% and confirm no truncation/overlap → navigate
back to Compare.

**Acceptance Scenarios**:

1. **Given** the shopper is on the Compare screen, **When** they activate the
   Settings entry point, **Then** the Settings screen opens.
2. **Given** the shopper is on Settings, **When** they navigate back, **Then**
   they return to Compare with previously entered values preserved.
3. **Given** the device language is Spanish (or English), **When** Settings
   is shown, **Then** all user-visible labels and descriptions are in that
   language (no mixed or hardcoded strings).
4. **Given** TalkBack is enabled, **When** focus moves across Settings
   controls, **Then** each control announces a meaningful name and current
   state (e.g. selected theme; Material You on/off or unavailable).
5. **Given** system font scale at 200%, **When** Settings is shown, **Then**
   all options remain fully readable and tappable with no truncation or
   overlap.

---

### Edge Cases

- Theme change while a comparison result is visible: result remains correct;
  only colors/surfaces update.
- Rapid toggling of Material You or theme: final selection wins; UI stays
  consistent with the persisted preference (no stuck intermediate palette).
- Material You enabled, then the shopper changes the device wallpaper: on
  next composition / return to foreground, colors reflect the new dynamic
  palette when the platform provides it.
- Material You preference stored as on while running on a device that does
  not support it (e.g. after restore / older OS): brand palette is used; the
  control shows unavailable; turning support on later (OS upgrade) may honor
  the stored preference.
- Configuration changes (rotation, locale switch) while on Settings: current
  selections remain; no reset to defaults.
- Compare form state across open/close Settings: price, quantity, unit, and
  offer count are preserved.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST provide a Settings screen reachable from the
  Compare screen.
- **FR-002**: Settings MUST let the user choose appearance mode among
  **System**, **Light**, and **Dark**.
- **FR-003**: The default appearance mode on a fresh install MUST be
  **System**.
- **FR-004**: When appearance mode is System, the app MUST follow the
  device light/dark setting. When Light or Dark is selected, the app MUST
  use that mode regardless of the device setting.
- **FR-005**: Appearance mode changes MUST apply to the whole app without
  requiring an app restart.
- **FR-006**: Settings MUST let the user turn Material You (wallpaper-derived
  dynamic color) on or off when the device supports it.
- **FR-007**: Material You MUST default to **off** on a fresh install so the
  existing brand palette remains the default look.
- **FR-008**: When Material You is on and supported, the app MUST use the
  device dynamic color palette while still honoring the selected appearance
  mode (System / Light / Dark).
- **FR-009**: When Material You is off, or when the device does not support
  it, the app MUST use the fixed brand palette.
- **FR-010**: On devices that do not support Material You, the user MUST NOT
  be able to enable it; the UI MUST make the unavailable state clear.
- **FR-011**: Appearance mode and Material You preferences MUST persist
  across process death and device reboot (local only; no account or network).
- **FR-012**: Opening Settings and returning to Compare MUST preserve all
  in-progress comparison inputs and results.
- **FR-013**: All Settings user-visible strings MUST be available in English
  and Spanish.
- **FR-014**: Settings interactive controls MUST expose meaningful labels and
  state for assistive technologies (e.g. TalkBack).
- **FR-015**: Settings MUST remain usable at system font scale up to at least
  200% without truncation, overlap, or loss of controls.
- **FR-016**: Text and meaningful icons on Settings and on themed Compare
  surfaces MUST meet WCAG 2.1 AA contrast for the active palette (brand and,
  when enabled, dynamic) in both light and dark appearance.

### Key Entities

- **Appearance preferences**: The user’s chosen appearance mode (System |
  Light | Dark) and Material You enabled flag; persisted locally; applied
  app-wide whenever the UI is shown.
- **Settings screen**: Dedicated surface listing appearance controls; not
  part of the compare form itself.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: On a fresh install, 100% of launches use System appearance and
  brand colors (Material You off) until the user changes a preference.
- **SC-002**: A user can open Settings, change appearance mode, and see the
  new mode applied across the app in under 3 seconds without restarting.
- **SC-003**: After force-stop and relaunch, the last appearance mode and
  Material You choice are still applied in 100% of manual test runs.
- **SC-004**: Returning from Settings to Compare preserves entered offer
  values in 100% of manual test runs (including after a theme change).
- **SC-005**: On a Material You–capable device, enabling and disabling
  Material You visibly switches between dynamic and brand palettes without
  leaving Settings or restarting.
- **SC-006**: On a non–Material You device, Material You cannot be turned on,
  and the brand palette remains in use in 100% of checks.
- **SC-007**: Settings completes an accessibility smoke check: TalkBack can
  focus and announce every appearance control; at 200% font scale, no
  truncation or overlap; both `en` and `es` show fully translated labels.
- **SC-008**: Contrast for primary text and meaningful controls meets WCAG
  2.1 AA in light and dark for the brand palette; when Material You is on,
  the same check passes for at least one representative dynamic palette on a
  reference device.

## Assumptions

- Settings in this feature covers **appearance only** (theme mode + Material
  You). Other future options (units defaults, favorites, etc.) are out of
  scope.
- Default Material You **off** preserves the brand decision from feature 003
  while allowing opt-in alignment with the constitution’s Material You
  guidance.
- “Material You support” means the device/OS can supply wallpaper-derived
  dynamic colors; older devices fall back to the brand palette.
- Persistence is local and offline-only; no sync, backup policy beyond normal
  OS app data, and no personal data collection.
- Navigation is a simple open Settings / return to Compare flow; no deep
  settings hierarchy.
- Compare remains the primary screen; Settings must not become the launch
  destination.
- Release for this feature stays on **0.1.x** (target **0.1.10** at
  release-prep). Agents should **prompt Marco** when a future change would
  warrant a **MINOR** version bump (0.2.0+); do not bump version during
  planning or implementation of this feature.

## Out of Scope

- Additional settings categories (units memory, currency, history, accounts).
- Android 12+ branded SplashScreen customization.
- Custom fonts or non–Material 3 type scales.
- Per-screen theme overrides (one app-wide preference only).
- Landscape / tablet / foldable redesign of Settings or Compare.
- Exporting or sharing appearance preferences.
- Google Play theming APIs or any proprietary/network-backed personalization.
- Changing the brand seed color or regenerating the fixed palette (unless a
  contrast failure forces a minimal token tweak documented in the plan).
