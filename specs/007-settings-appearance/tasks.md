---
description: "Task list for feature 007 — Settings appearance (theme & Material You)"
---

# Tasks: Settings Appearance (Theme & Material You)

**Input**: Design documents from `/specs/007-settings-appearance/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md,
contracts/appearance-preferences.md, quickstart.md

**Tests**: REQUIRED. Constitution Principle V and plan.md mandate
test-first (Red → Green → Refactor). Preference / resolution unit tests and
Settings instrumented tests MUST fail before production wiring lands.

**Organization**: Tasks grouped by user story from `spec.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on
  incomplete tasks).
- **[Story]**: `[US1]` / `[US2]` / `[US3]`. Setup, Foundational, and Polish
  phases carry no story label.
- Paths are relative to the git root (`pricegrab/`).

---

## Phase 1: Setup (Shared infrastructure)

**Purpose**: Point Spec Kit at 007, add DataStore dependency, create
package scaffolding. No Settings UI yet.

- [x] T001 [P] Append PR **AA** / **AB** / **AC** cadence notes for feature
      007 to `specs/001-unit-price-comparison/tasks.md` (global letter
      ledger) — done in planning commit.
- [x] T002 [P] Confirm `docs/project-status.md` Active Spec Kit pointer is
      `specs/007-settings-appearance`, cadence row 007 → **0.1.10**, and
      Settings backlog item marked in progress — done in planning commit.
- [x] T003 [P] Pin `androidx.datastore:datastore-preferences` in
      `android/gradle/libs.versions.toml` and add
      `implementation(libs.androidx.datastore.preferences)` (or equivalent
      catalog alias) in `android/app/build.gradle.kts`.
- [x] T004 [P] Create package directories for
      `android/app/src/main/kotlin/com/mablanco/pricegrab/data/appearance/`
      and `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/settings/`
      (placeholder `.gitkeep` or first empty files only if needed for
      structure; prefer real types in Phase 2).

**Checkpoint**: Dependency resolves; feature pointer / ledger coherent;
implementation can start after PR AA merges (or on `feat/…-impl` from
`main`).

---

## Phase 2: Foundational (Preferences model + resolve + theme API)

**Purpose**: Pure preference model, DataStore repository, theme resolution,
and `PriceGrabTheme` API that all stories share. Blocks Settings UI work
that depends on persisted prefs.

**⚠️ CRITICAL**: No Compare → Settings navigation or Material You toggle UI
until Phase 2 unit tests for defaults / `resolve()` are green. Theme
parameter API may land here before screens exist.

- [x] T005 [P] Add JVM unit tests for `AppearanceMode` string mapping
      (missing/invalid → `System`; `system`/`light`/`dark`) and default
      `AppearancePreferences` (`System`, `materialYouEnabled = false`) in
      `android/app/src/test/kotlin/com/mablanco/pricegrab/data/appearance/AppearancePreferencesTest.kt`
      (fail until T007).
- [x] T006 [P] Add JVM unit tests for `resolve(prefs, systemDark, sdkInt)`
      covering System/Light/Dark × `useDynamicColor` only when
      `materialYouEnabled && sdkInt >= 31` in
      `android/app/src/test/kotlin/com/mablanco/pricegrab/data/appearance/AppearanceResolveTest.kt`
      (fail until T008).
- [x] T007 [P] Implement `AppearanceMode`, `AppearancePreferences`, and
      preference key constants in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/data/appearance/AppearancePreferences.kt`
      (and `AppearancePreferencesKeys.kt` if split) per `data-model.md` /
      `contracts/appearance-preferences.md`; make T005 green.
- [x] T008 Implement pure `resolve(...)` → `ResolvedAppearance` in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/data/appearance/AppearanceResolve.kt`
      (or same package as T007); make T006 green.
- [x] T009 Implement `AppearancePreferencesRepository` (DataStore file
      `appearance_preferences`, `Flow` read, `setMode` /
      `setMaterialYouEnabled` writes) in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/data/appearance/AppearancePreferencesRepository.kt`.
- [x] T010 Extend `PriceGrabTheme` in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/theme/Theme.kt`
      to accept `darkTheme` + `useDynamicColor`; use brand schemes when
      dynamic is off/unsupported; use
      `dynamicLightColorScheme` / `dynamicDarkColorScheme` when
      `useDynamicColor` is true (API 31+). Update KDoc to reflect opt-in
      Material You (feature 007) vs 003 default-off brand.

**Checkpoint**: Mapping + resolve unit-tested; repository compiles; theme
API ready for root wiring. No Settings screen required yet.

---

## Phase 3: User Story 1 — Choose light, dark, or system theme (Priority: P1) 🎯 MVP

**Goal**: Shopper opens Settings from Compare, selects System / Light /
Dark; app updates immediately without restart; preference survives
process death; Compare inputs survive open/close Settings.

**Independent Test**: Fresh install → follows system → Settings → Light
(forced light) → Dark → System → force-stop → last choice restored;
Compare values preserved after round-trip.

### Tests for User Story 1 ⚠️

> Write FIRST; ensure FAIL until navigation + theme controls exist.

- [x] T011 [P] [US1] Add Compose instrumented test: Settings reachable via
      `settings_open` tag; theme tags `theme_system` / `theme_light` /
      `theme_dark`; selecting Light/Dark/System updates selection — in
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/settings/SettingsThemeTest.kt`.
- [x] T012 [P] [US1] Add instrumented test that Compare offer inputs survive
      Settings open → theme change → back (FR-012 / SC-004) in
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/settings/SettingsPreservesCompareTest.kt`
      (or same file as T011 if cleaner).

### Implementation for User Story 1

- [x] T013 [US1] Introduce lightweight `AppScreen` (`Compare` | `Settings`)
      navigation state in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/PriceGrabApp.kt`
      (no Navigation Compose); system/UI back returns to Compare;
      `CompareViewModel` remains Activity-scoped.
- [x] T014 [US1] Wire preference `Flow` into `PriceGrabApp`: collect
      defaults until DataStore emits; call `resolve` + `PriceGrabTheme`
      with effective `darkTheme` / `useDynamicColor` (Material You may
      still always resolve false until US2 UI writes the flag).
- [x] T015 [US1] Add Settings entry affordance (gear / settings action,
      tag `settings_open`, ≥48 dp) on Compare top bar in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreen.kt`.
- [x] T016 [US1] Implement Settings screen shell + theme single-selection
      control (radio list or segmented; prefer radio if 200%/`es`
      truncates) with tags from contract, writing `setMode` on change, in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/settings/SettingsScreen.kt`
      (+ thin `SettingsViewModel.kt` if helpful).
- [x] T017 [US1] Add EN/ES strings for Settings title, theme section, and
      System/Light/Dark labels in
      `android/app/src/main/res/values/strings.xml` and
      `android/app/src/main/res/values-es/strings.xml`.
- [x] T018 [US1] Make T011/T012 green; smoke that theme applies app-wide
      without Activity restart.

**Checkpoint**: US1 MVP — theme override works end-to-end with persistence
and Compare state preserved. Material You control may be absent or stubbed
off.

---

## Phase 4: User Story 2 — Optionally use Material You colors (Priority: P1)

**Goal**: On capable devices, toggle Material You on/off; default off
(brand palette); on unsupported devices, control disabled with clear copy;
preference persists; brand used when off or unsupported.

**Independent Test**: API 31+ → enable → dynamic palette → disable →
brand; force-stop keeps preference. API &lt; 31 → cannot enable; brand stays.

### Tests for User Story 2 ⚠️

> Write FIRST; ensure FAIL until switch + dynamic wiring land.

- [x] T019 [P] [US2] Add instrumented test for Material You switch
      (`material_you_switch`): default off; toggle on/off when available —
      in
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/settings/SettingsMaterialYouTest.kt`.
- [x] T020 [P] [US2] Add JVM or instrumented coverage for unavailable gate
      (sdk &lt; 31 → switch disabled / cannot enable; `useDynamicColor`
      false even if stored true) in
      `android/app/src/test/kotlin/com/mablanco/pricegrab/data/appearance/AppearanceResolveTest.kt`
      and/or Settings instrumented tests with a test double / shadow SDK
      gate.

### Implementation for User Story 2

- [x] T021 [US2] Add Material You `Switch` + supporting text (enabled only
      when `SDK_INT >= 31`) on Settings; persist via
      `setMaterialYouEnabled` in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/settings/SettingsScreen.kt`.
- [x] T022 [US2] Confirm root theme path uses dynamic schemes when
      `resolve(...).useDynamicColor` is true in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/theme/Theme.kt`
      / `PriceGrabApp.kt`; brand otherwise.
- [x] T023 [P] [US2] Add EN/ES strings for Material You title, description,
      and unavailable-on-this-device copy in
      `android/app/src/main/res/values/strings.xml` and
      `android/app/src/main/res/values-es/strings.xml`.
- [x] T024 [US2] Make T019/T020 green.

**Checkpoint**: US1 + US2 both independently verifiable; defaults remain
System + Material You off.

---

## Phase 5: User Story 3 — Reach Settings without losing the compare flow (Priority: P2)

**Goal**: Settings is discoverable, bilingual, TalkBack-friendly, and
usable at 200% font scale; back always restores Compare with inputs.

**Independent Test**: Compare → Settings → ES/EN labels → TalkBack names +
state → fontScale 2.0 no truncation/overlap → back to Compare.

### Tests for User Story 3 ⚠️

- [x] T025 [P] [US3] Add large-font instrumented smoke (`fontScale = 2.0`)
      asserting Settings remains scrollable and primary labels/controls
      are displayed without overlap in
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/settings/SettingsLargeFontTest.kt`.
- [x] T026 [P] [US3] Add semantics / content-description assertions (or
      documented manual checklist tied to tags) for theme options +
      Material You state announcements in
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/settings/SettingsA11yTest.kt`
      (merge with T025 if one file is cleaner).

### Implementation for User Story 3

- [x] T027 [US3] Audit Settings + Compare entry for ≥48 dp targets,
      meaningful semantics, and scrollable layout at large font in
      `SettingsScreen.kt` / Compare top-bar action; fix any gaps from
      US1/US2.
- [x] T028 [P] [US3] Verify all Settings user-visible strings exist in both
      `values` and `values-es` (no hardcoded UI text); fix any missing
      translations.
- [x] T029 [US3] Make T025/T026 green; confirm back / predictive back returns
      to Compare with form state intact (regression on T012).

**Checkpoint**: US3 a11y / i18n / discoverability acceptance scenarios pass.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Regression, docs alignment, release-prep tasks deferred to PR
**AC**.

- [x] T030 [P] Run Compare regression suite (unit + instrumented) and fix
      any breakage from theme/root changes; keep existing offer test tags.
- [x] T031 [P] Walk `specs/007-settings-appearance/quickstart.md` manually
      (or note blockers in PR AB); include SC-008 contrast smoke for brand
      + one dynamic palette when a device is available.
- [x] T032 [P] Update `docs/project-status.md` In-progress / feature notes
      if drift after impl (still **no** versionCode bump on PR AB).
- [ ] T033 Release-prep **only on PR AC** (not AB): bump to **0.1.10** /
      versionCode **11**, fastlane changelogs en/es, `docs/fdroid.md` +
      `docs/project-status.md`. **Do not** start AC until Marco asks;
      **prompt Marco** before any future MINOR (0.2.0+) bump.

**Checkpoint**: PR AB ready for review (T003–T032). Tag only after AC +
manual QA on `main`.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies.
- **Phase 2 (Foundational)**: After T003 (DataStore). Blocks US1–US3
  production wiring that needs prefs/theme API.
- **Phase 3 (US1)**: After Phase 2. MVP.
- **Phase 4 (US2)**: After US1 Settings shell exists (T016) so the switch
  has a home; resolve/dynamic theme API from Phase 2.
- **Phase 5 (US3)**: After US1 (+ US2 controls for full TalkBack coverage).
- **Phase 6 (Polish)**: After US1–US3; T033 only on release-prep PR AC.

### User Story Dependencies

| Story | Depends on | Independently testable by |
|-------|------------|---------------------------|
| US1 Theme mode | Phase 2 | SettingsThemeTest + preserve-compare + force-stop manual |
| US2 Material You | US1 Settings shell + Phase 2 resolve/theme | SettingsMaterialYouTest + unavailable gate |
| US3 Reach / a11y | US1 entry (+ US2 for MY semantics) | LargeFont + a11y tests + ES/EN check |

### Parallel Opportunities

- T001 ∥ T002 (already done).
- T003 ∥ T004.
- T005 ∥ T006 (unit tests).
- T007 ∥ T008 after tests sketched (same package OK sequentially if
  preferred).
- T011 ∥ T012 (instrumented).
- T019 ∥ T020.
- T023 ∥ T021 (strings vs switch UI).
- T025 ∥ T026.
- T030 ∥ T031 ∥ T032 during polish.

---

## Parallel Example: User Story 1

```bash
# Tests first (expect FAIL until Settings + theme wiring exist):
# - SettingsThemeTest: tags + Light/Dark/System selection
# - SettingsPreservesCompareTest: inputs survive round-trip

# Then implement:
# - AppScreen navigation in PriceGrabApp.kt
# - Preference collect + PriceGrabTheme(darkTheme, useDynamicColor)
# - Compare settings_open affordance
# - SettingsScreen theme control + EN/ES strings
```

---

## Implementation Strategy

### MVP First (US1 only)

1. Phase 1 dependency + Phase 2 model/repo/theme API green.
2. Phase 3 navigation + Settings theme control + T011/T012 green.
3. **STOP and VALIDATE** on a phone: Light/Dark/System, no restart,
   Compare values preserved.
4. Continue US2 → US3 → polish before marking PR AB ready for review.

### PR mapping

| Letter | Scope |
|--------|--------|
| **AA** | This branch: spec, plan, research, data-model, contracts, quickstart, tasks, status/ledger docs. **No** `android/` source. |
| **AB** | `feat/0xx-settings-appearance-impl`: T003–T032 (implementation + tests). No version bump. |
| **AC** | Release-prep: T033 → **0.1.10** / versionCode 11 + tag after Marco QA. |

### Incremental delivery

1. Planning PR AA merge (or continue follow-ups on this branch until merge).
2. Impl PR AB: US1 → US2 → US3 → polish/regression.
3. Release-prep PR AC → tag `v0.1.10` from `main` after manual QA.

---

## Notes

- Stay on **0.1.x** (**0.1.10** at release-prep). Agents must **prompt
  Marco** before proposing a **MINOR** bump (0.2.0+).
- Do not add Navigation Compose for two screens.
- Do not change `core/` comparison math or brand seed `#2F5C73` unless a
  documented contrast failure forces a minimal token tweak.
- Preserve existing Compare test tags (`{offerA|B|C}_price|_quantity|_unit`,
  etc.).
- Commit in English Conventional Commits; chat with Marco in Spanish.
- Constitution: I (opt-in dynamic color), II a11y, III ES/EN, IV offline
  DataStore, V test-first.
