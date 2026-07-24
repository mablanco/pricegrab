# Implementation Plan: Settings Appearance (Theme & Material You)

**Branch**: `007-settings-appearance` | **Date**: 2026-07-24
**Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/007-settings-appearance/spec.md`

## Summary

Add a dedicated **Settings** screen reachable from Compare so shoppers can
choose **System / Light / Dark** appearance and optionally enable **Material
You** (dynamic color) on capable devices. Defaults on a fresh install match
today’s product look: **System** theme + **Material You off** (fixed brand
palette from feature 003). Preferences persist locally across process death;
changes apply app-wide without restart; Compare form state is preserved when
opening/closing Settings.

Technical approach: persist two preferences (theme mode + Material You flag)
via **DataStore Preferences**; hoist preference Flow into `PriceGrabApp` so
`PriceGrabTheme` receives resolved `darkTheme` and `useDynamicColor`; restore
`dynamicLightColorScheme` / `dynamicDarkColorScheme` only when the user opts
in and `SDK_INT >= 31`; navigate with a lightweight two-destination Compose
state (no Navigation library). Ship via planning → implementation →
release-prep (next free PR letter **AA**).

## Technical Context

**Language/Version**: Kotlin 2.0.21 — same toolchain as features 001–006.

**Primary Dependencies** (additions justified in [research.md](./research.md)):

- `androidx.datastore:datastore-preferences` — local preference Flow.
- Existing Material 3 Compose BOM already provides
  `dynamicLightColorScheme` / `dynamicDarkColorScheme` (API 31+).
- **No** Navigation Compose for this feature (two screens only).

**Storage**: DataStore Preferences file (app-private). Keys for
`theme_mode` (`system` \| `light` \| `dark`) and `material_you_enabled`
(`Boolean`). No accounts, network, or personal data.

**Testing**:

- JVM unit: preference mapping / defaults; theme resolution
  (`AppearanceMode` + system dark → effective dark); dynamic-color gate
  (`enabled && sdk >= 31`).
- Compose UI / instrumented:
  - Settings reachable from Compare; back preserves offer inputs.
  - Theme segmented control / radio group selects System/Light/Dark;
    Material You switch on/off when available; disabled + unavailable
    copy when not.
  - Preference survives activity recreate (config change); instrumented
    DataStore may use a test file or in-memory double.
  - TalkBack / 200% font smoke on Settings; existing Compare suites stay
    green.
- Manual: force-stop persistence; wallpaper change with Material You on;
  contrast spot-check brand + one dynamic palette (light/dark).

**Target Platform**: Android, `minSdk` 24, `targetSdk` 35, `compileSdk` 35 —
unchanged. Dynamic color usable only on API 31+.

**Project Type**: mobile-app (single Android application, no backend) —
unchanged.

**Performance Goals**: Preference reads are cold-start path; first frame may
use defaults until DataStore emits (must not flash wrong theme after first
emission). No network. Theme switch recomposition only — constitution IV
cold-start budget unchanged.

**Constraints**:

- Fully offline; no new permissions; F-Droid / no proprietary SDKs.
- Material You **default off**; brand palette fallback always available.
- ES + EN strings for all Settings labels, descriptions, unavailable state.
- Touch targets ≥ 48×48 dp; WCAG AA for brand palette; representative
  dynamic-palette contrast check when Material You is on.
- Compare remains launch destination; Settings is not a deep hierarchy.

**Scale/Scope**: New `ui/settings/` surface + preference store + theme wiring
in `PriceGrabTheme` / `PriceGrabApp` / Compare top bar entry. Estimated
~250–400 LOC production + ~200–350 LOC tests. No versionCode bump in the
implementation PR — release prep targets **0.1.10** (next 0.1.x patch;
stay on the 0.1 line). Do **not** bump to 0.2.0 for this feature.

**Agent reminder (do not bump now)**: When a future change would warrant a
**MINOR** SemVer bump (0.2.0+), **prompt Marco** before choosing the
version — do not invent a minor bump unilaterally.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gates derived from `.specify/memory/constitution.md` v1.0.1.

| Principle / section | Gate | Plan evidence | Status |
|---------------------|------|---------------|--------|
| I. Modern Mobile UX | M3, ≥48dp, single-purpose primary flow, dynamic color when available, non-color cues | Settings is secondary; Compare stays primary/launch. Theme uses M3 controls. Dynamic color restored as **explicit opt-in** (default off preserves brand from 003 while satisfying “when available” via user choice). ≥48dp targets on Settings controls. | ✅ |
| II. Accessibility (NON-NEGOTIABLE) | TalkBack, 200% font, WCAG AA, non-color cues | Settings CDs + selected state; scrollable Settings at 200%; brand AA already established in 003; SC-008 dynamic contrast manual check. | ✅ |
| III. Internationalization (ES/EN) | Strings in resources, locale-aware numbers | All Settings copy in `values` + `values-es`; no hardcoded UI strings. | ✅ |
| IV. Offline-First Performance | No network, ≤2s cold start, APK &lt; 15 MB, preserve state on config change | Local DataStore only; one small Jetpack dep; Compare ViewModel retained across Settings; theme/config changes preserve offers. | ✅ |
| V. Test-First Quality | Red→Green→Refactor; CI gates | Preference/resolution unit tests + Settings instrumented tests written first; Compare regression suite must stay green. | ✅ |
| Privacy & Platform | No trackers, F-Droid-compatible, minimal permissions | Preferences are non-personal UI flags; no INTERNET; DataStore is open-source AndroidX. | ✅ |

**Complexity / principle note (not a violation)**: Constitution I mentions
dynamic color “when available.” Feature 003 deliberately opted out for brand
consistency. This feature **reconciles** both: brand remains the default;
Material You is available when the OS supports it **and** the user enables
it. No Complexity Tracking row required.

**Post-design re-check**: Passes. Persistence, navigation, and theme
resolution documented in [research.md](./research.md),
[data-model.md](./data-model.md), and
[contracts/appearance-preferences.md](./contracts/appearance-preferences.md).

## Project Structure

### Documentation (this feature)

```text
specs/007-settings-appearance/
├── plan.md                 # This file
├── research.md             # Phase 0
├── data-model.md           # Phase 1
├── quickstart.md           # Phase 1 manual verification
├── contracts/
│   └── appearance-preferences.md
├── checklists/
│   └── requirements.md
├── spec.md
└── tasks.md                # /speckit.tasks (not this command)
```

### Source Code (touch list)

```text
android/gradle/libs.versions.toml          # datastore-preferences
android/app/build.gradle.kts               # implementation(libs…)

android/app/src/main/kotlin/com/mablanco/pricegrab/
├── MainActivity.kt                        # optional: keep thin
├── PriceGrabApp.kt                        # destination state + theme wiring
├── data/appearance/
│   ├── AppearancePreferences.kt           # data class / enums
│   ├── AppearancePreferencesRepository.kt # DataStore read/write
│   └── AppearancePreferencesKeys.kt       # preference keys
├── ui/theme/Theme.kt                      # darkTheme + useDynamicColor
├── ui/compare/CompareScreen.kt            # Settings action in top bar
└── ui/settings/
    ├── SettingsScreen.kt
    └── SettingsViewModel.kt               # optional thin VM over repository

android/app/src/main/res/values/strings.xml
android/app/src/main/res/values-es/strings.xml

android/app/src/test/kotlin/.../appearance/   # mapping + resolution tests
android/app/src/androidTest/kotlin/.../settings/  # Settings UI tests
```

**Structure Decision**: Stay inside the existing single-module Android app
(`android/app`). No new Gradle modules. Domain `core/` math untouched.

## Complexity Tracking

> No constitution violations. Table left empty intentionally.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Implementation phases (preview for `/speckit.tasks`)

1. **Tests first**: failing unit tests for defaults and theme/dynamic
   resolution; failing instrumented tests for Settings entry, theme
   selection, Material You unavailable gate, back-preserves-compare.
2. **Repository + model**: DataStore Preferences + `AppearancePreferences`.
3. **Theme wiring**: extend `PriceGrabTheme`; collect prefs in
   `PriceGrabApp`.
4. **Settings UI + Compare entry**: top-bar settings affordance; Settings
   screen with theme group + Material You switch; ES/EN strings.
5. **Navigation**: two-destination state + system back; Compare ViewModel
   retained at Activity scope.
6. **Regression**: Compare suites + a11y/font smoke; manual quickstart.
7. **Release prep** (separate PR): version, changelogs, status/fdroid docs.

## PR cadence

| Letter | Branch / PR | Contents |
|--------|-------------|----------|
| **AA** | `007-settings-appearance` (this branch) | Spec + plan + research + contracts + tasks (planning only; no app source) |
| **AB** | `feat/0xx-settings-appearance-impl` | Implementation + tests |
| **AC** | `chore/0xx-…` release-prep | Version, changelogs, status docs |

Global ledger: update `specs/001-unit-price-comparison/tasks.md` when
opening PR AA (planning commit / follow-up on this branch).
