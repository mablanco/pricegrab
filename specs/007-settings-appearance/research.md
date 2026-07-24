# Research: Settings Appearance (Feature 007)

## 1. Preference storage: DataStore Preferences

**Decision**: Persist appearance preferences with
`androidx.datastore:datastore-preferences` (single preferences DataStore
file, e.g. `appearance_preferences`).

**Rationale**:

- First-class `Flow` API integrates cleanly with
  `collectAsStateWithLifecycle` for instant theme updates without
  `SharedPreferences.OnSharedPreferenceChangeListener` boilerplate.
- Survives process death and reboot; local-only (constitution Privacy).
- Open-source AndroidX; F-Droid compatible; small APK impact vs
  SharedPreferences alone.

**Alternatives considered**:

- **SharedPreferences**: zero new dependency, but callback/Flow bridging
  is more error-prone for Compose theme root. Rejected for this feature.
- **Proto DataStore**: typed schema overkill for two keys. Rejected.
- **Room / files**: unnecessary complexity. Rejected.

## 2. Theme mode application (System / Light / Dark)

**Decision**: Store `AppearanceMode` enum (`System`, `Light`, `Dark`) with
default **`System`**. Resolve effective dark flag in Compose:

```text
effectiveDark =
  when (mode) {
    System → isSystemInDarkTheme()
    Light  → false
    Dark   → true
  }
```

Pass `darkTheme = effectiveDark` into `PriceGrabTheme`. Do **not** require
activity recreate for theme changes; recomposition is sufficient for the
Compose tree. Optionally call
`AppCompatDelegate.setDefaultNightMode(...)` only if edge-to-edge /
system bar contrast needs it during implementation QA; prefer
Compose-only first.

**Rationale**: Matches FR-002–FR-005; preserves today’s default behavior
for fresh installs; keeps Compare ViewModel state across preference
changes.

**Alternatives considered**:

- **Activity recreate on every change**: simpler mental model, risks
  losing in-progress UI and feels slower (SC-002). Rejected.
- **Only System vs Dark** (no Light): incomplete vs spec. Rejected.

## 3. Material You (dynamic color) gate

**Decision**:

| Condition | Color scheme |
|-----------|--------------|
| `materialYouEnabled == false` (default) | Brand `LightColors` / `DarkColors` from feature 003 |
| `materialYouEnabled == true` **and** `SDK_INT >= 31` | `dynamicLightColorScheme` / `dynamicDarkColorScheme` |
| `materialYouEnabled == true` **but** `SDK_INT < 31` | Brand palette; Settings control **disabled** / unavailable |

Support detection for UI: `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`
(API 31). Persist the user’s Material You boolean even on unsupported
devices so an OS upgrade can honor it later (spec edge case).

**Rationale**: Restores constitution I’s dynamic-color expectation as
**opt-in**, keeps FR-007 default off (brand identity / store screenshots
predictable), and satisfies FR-010 unavailable UX.

**Alternatives considered**:

- **Default Material You on** (constitution literal): rejected by product
  decision in spec + 003 brand rationale; would surprise existing users.
- **Always on when API ≥ 31, no toggle**: contradicts FR-006/FR-007.
- **minSdk raise to 31**: unnecessary; brand path works on API 24–30.

## 4. Navigation: lightweight two-destination state

**Decision**: Do **not** add Navigation Compose. In `PriceGrabApp` (or a
tiny root composable), hold:

```text
sealed interface AppScreen { Compare; Settings }
```

- Compare top bar: Settings `IconButton` (gear) → `Settings`.
- Settings top bar: Back → `Compare`.
- System back / predictive back: same as Settings back.
- `CompareViewModel` remains Activity-scoped so offer inputs survive.

**Rationale**: Only two screens; avoids a new navigation dependency and
keeps APK/test surface small. FR-001 / FR-012 satisfied.

**Alternatives considered**:

- **Navigation Compose**: fine for deeper trees later; overkill now.
- **Separate Activity for Settings**: heavier; complicates theme
  continuity. Rejected.
- **Bottom sheet Settings**: harder for TalkBack / 200% font / future
  settings categories. Rejected for v1.

## 5. Settings entry point placement

**Decision**: Add a Settings action to `CompareTopBar` **leading or
trailing actions** alongside Reset. Prefer trailing: Settings (always
enabled) then Reset (enabled when dirty), or Settings as the leftmost
action if Reset crowding is an issue — finalize in implementation so both
≥48 dp and TalkBack order is logical (Settings before Reset is acceptable).

**Rationale**: Discoverable without cluttering the compare form; matches
common Android patterns; keeps Compare as launch destination.

**Alternatives considered**: Overflow menu only (worse discoverability);
FAB (competes with result/hero). Rejected.

## 6. Settings control patterns

**Decision**:

- **Theme**: single-selection control — Material 3
  `SegmentedButton` row **or** radio list under an “Appearance” /
  “Theme” section header. Prefer radio list if segmented labels truncate
  at 200% font / `es` strings; otherwise segmented is fine for three
  short labels.
- **Material You**: `Switch` with title + supporting text; when
  unsupported, `enabled = false` and supporting text explains
  unavailability (ES/EN).

**Rationale**: Clear selected state for TalkBack (FR-014); readable at
200% (FR-015).

## 7. Cold start / first frame

**Decision**: Until DataStore emits, use **defaults** (`System`, Material
You off) — identical to today’s shipped look. After emission, apply stored
values. Avoid a blocking splash wait.

**Rationale**: Prevents blank/janky startup; SC-001 defaults match fresh
install; returning users may briefly show default then correct theme
(~one frame) — acceptable. If QA shows a visible flash for Dark-preferring
users, consider `runBlocking` first read only in `Application` /
`MainActivity.onCreate` (document if adopted).

## 8. Contrast (SC-008)

**Decision**: Brand light/dark remain the audited baseline from feature
003. For Material You: manual check on one reference device with a
non-pathological wallpaper (light and dark appearance). No CI contrast
automation in this feature (still deferred per project-status backlog).

**Rationale**: Dynamic palettes are unbounded; constitution II still
requires a human smoke check before release.

## 9. Dependency & release

**Decision**: Add only `datastore-preferences` (version pinned in
`libs.versions.toml`). Implementation PR does **not** bump
`versionCode`. Release-prep after merge targets **0.1.10** (stay on
0.1.x; not 0.2.0) and updates fastlane + `docs/project-status.md`.

**Rationale**: Matches 002–006 cadence (planning → impl → release-prep).
Marco resolved the open 0.2.0 vs 0.1.10 question toward patch. Next
planning PR letter **AA**. Agents must **prompt Marco** before any
future **MINOR** bump (0.2.0+); do not bump version in this feature’s
impl PR.

## 10. Scope freeze

**Decision**: Appearance-only Settings. No units memory, currency,
history, splash branding, custom fonts, or per-screen themes. Do not
change brand seed `#2F5C73` unless a documented contrast failure forces a
minimal token tweak.

**Rationale**: Spec Out of Scope + Assumptions.
