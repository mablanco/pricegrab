# Implementation Plan: Unit-Price Comparison Screen

**Branch**: `001-unit-price-comparison` | **Date**: 2026-04-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-unit-price-comparison/spec.md`

## Summary

Deliver the MVP screen that lets a shopper enter price and quantity for two
offers and immediately see which one is cheaper per unit, by how much in
absolute terms, and by how much in percentage terms. The screen must work
fully offline, in Spanish and English, with first-class accessibility
(TalkBack + 200% font scale) and without collecting any user data.

Technical approach: native Android app written in **Kotlin**, UI in
**Jetpack Compose** with **Material 3**, state managed by a single
`CompareViewModel` using Compose's unidirectional data flow, pure Kotlin
calculation core (`PriceComparator`) unit-tested with JUnit + kotlin.test,
and Compose UI tests for the happy-path and accessibility smoke tests in both
locales. Single-module Gradle app under `android/`. Build reproducible with a
pinned toolchain (Gradle wrapper + Kotlin/AGP/Compose versions in
`gradle/libs.versions.toml`). Distribution via signed APKs attached to
GitHub Releases by a GitHub Actions workflow; codebase kept F-Droid-ready
(no Google Mobile Services, no Firebase, no proprietary SDKs).

## Technical Context

**Language/Version**: Kotlin 2.0.21 (latest stable at plan time; pinned in `libs.versions.toml`)
**Primary Dependencies**:

- Jetpack Compose BOM (2025.01.00 or latest stable) — drives `ui`, `material3`, `ui-tooling-preview`, `ui-test-junit4`.
- `androidx.activity:activity-compose`
- `androidx.lifecycle:lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`
- `androidx.core:core-ktx`
- `androidx.appcompat:appcompat` (only if strictly needed for configuration-change preservation; otherwise omitted)
- No dependency injection framework (app is too small to warrant Hilt/Dagger).
- No third-party analytics, crash reporting, or networking libraries. Ever.

**Storage**: None. The app is stateless across launches. In-flight state is held
in the ViewModel via `SavedStateHandle` + Compose `rememberSaveable` to survive
configuration changes and process death.

**Testing**:

- Unit: JUnit 5 + `kotlin.test` for the pure-Kotlin `core/` module (calculator, formatter).
  Coverage measured with JaCoCo. Target ≥ 90% on `core/calc/` per constitution V.
- Compose UI: `androidx.compose.ui:ui-test-junit4` with `createAndroidComposeRule`.
- Instrumented: at least one end-to-end test per locale (`en`, `es`), one accessibility
  smoke test (TalkBack content descriptions + result announcement), one configuration-change
  test (rotation preserves inputs and result).
- Static analysis: Android Lint (baseline empty) + **detekt** with the standard ruleset.

**Target Platform**: Android.

- `minSdk` = 24 (Android 7.0 Nougat). Covers ≥ 96% of active devices per Google's
  distribution dashboard; justified in place of 21/23 because AndroidX + Compose
  are smoother on 24+.
- `targetSdk` = 35 (Android 15, latest stable).
- `compileSdk` = 35.

**Project Type**: mobile-app (single Android application, no backend).

**Performance Goals**:

- Cold start to interactive ≤ 2 s on a Pixel 4a-class device (per constitution IV).
- Calculation appears instantaneous (< 16 ms frame budget, so we stay on the main thread).
- 60 fps scroll/animation on a single-screen layout.
- Release APK size ≤ 5 MB (well under the constitutional 15 MB ceiling).

**Constraints**:

- Fully offline. No `INTERNET` permission in the manifest.
- No runtime permissions.
- Reproducible builds: toolchain pinned, no dynamic versions, Kotlin `-Xjvm-default=all`
  and deterministic build settings applied.
- F-Droid compatible: no Google Mobile Services, no Firebase, no closed-source
  dependencies; `gradle-wrapper.jar` is the only committed JAR.
- Zero data collection (no analytics, no telemetry, no attribution).

**Scale/Scope**: 1 screen, 4 inputs, 1 result area, 2 locales. Estimated upper
bound ~2000 LOC production + tests.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

Gates derived from `.specify/memory/constitution.md` v1.0.1.

| Principle / section                          | Gate                                                                                                   | Plan evidence                                                                                                                                                                                                 | Status |
|----------------------------------------------|--------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| I. Modern Mobile UX                          | Material 3, ≥48dp touch, decimal keyboard, no scroll in main flow, non-color result cue                | Compose + Material 3 components; `TextField` with `KeyboardOptions(keyboardType = Decimal)`; single screen in portrait; result shows icon+text+position in addition to color                                  |   ✅   |
| II. Accessibility (NON-NEGOTIABLE)           | TalkBack labels, 200% font scale, WCAG AA contrast, non-color-only result, D-pad nav                   | Compose `semantics { contentDescription = ... }` on every interactive element; result node uses `liveRegion = Polite`; sizes in `sp`; Material 3 default theme meets AA; instrumented accessibility test      |   ✅   |
| III. Internationalization (ES/EN)            | All strings in resources, locale-aware parsing/formatting, both locales tested                         | `res/values/strings.xml` + `res/values-es/strings.xml`; `NumberFormat.getInstance(Locale.getDefault())` for parse/format; `CompareScreenLocaleTest` runs happy path in both locales                           |   ✅   |
| IV. Offline-First Performance & Reliability  | No network in core flow, cold start ≤ 2 s, APK ≤ 15 MB, no ANR, inputs preserved on config change      | No networking dependency; `android:usesCleartextTraffic="false"` (defensive); R8 enabled for release; state in `ViewModel` + `rememberSaveable`; configuration-change test                                    |   ✅   |
| V. Test-First Quality (NON-NEGOTIABLE)       | TDD, ≥ 90% coverage on calculator, Espresso/Compose UI happy path ES + EN, CI blocks merge on red      | Calculator built test-first; JaCoCo verifies 90% floor on `core/calc/**`; CI runs unit + instrumented tests on every PR and blocks merge on failure                                                           |   ✅   |
| Privacy & Platform Constraints               | No analytics/tracking SDKs, minimal permissions, latest stable target SDK, F-Droid compatible          | Zero third-party analytics; no runtime permissions; `targetSdk` 35; no GMS/Firebase; `minSdk` 24 covers ≥ 95% of devices                                                                                       |   ✅   |
| Distribution (v1.0.1)                        | Signed APKs on GitHub Releases; reproducible build; no non-free blobs                                  | GitHub Actions workflow signs APK with a secret-stored keystore and uploads to Release on tag push; build settings deterministic; only `gradle-wrapper.jar` committed                                          |   ✅   |
| Development Workflow & Quality Gates         | Spec-Driven Development; feature branch; PR with spec reference; lint + tests green                    | Current feature is branch `001-unit-price-comparison`; this plan PR includes spec; CI required status checks will be enabled once the workflow exists                                                         |   ✅   |

**Outcome**: all gates pass on initial check. No entries needed in Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-unit-price-comparison/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── price-comparator.md   # Calculation-module contract
├── checklists/
│   └── requirements.md  # Spec quality checklist (from /speckit.specify)
└── tasks.md             # Created later by /speckit.tasks
```

### Source Code (repository root)

```text
android/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml      # Single source of truth for pinned versions
│   └── wrapper/
│       ├── gradle-wrapper.properties
│       └── gradle-wrapper.jar  # Only permitted committed JAR
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin/com/mablanco/pricegrab/
│       │   │   ├── MainActivity.kt
│       │   │   ├── PriceGrabApp.kt               # Compose app root
│       │   │   ├── ui/
│       │   │   │   ├── theme/                    # Material 3 theme
│       │   │   │   └── compare/
│       │   │   │       ├── CompareScreen.kt      # Composable
│       │   │   │       ├── CompareViewModel.kt
│       │   │   │       └── CompareUiState.kt
│       │   │   └── core/
│       │   │       ├── model/
│       │   │       │   ├── Offer.kt
│       │   │       │   └── ComparisonOutcome.kt
│       │   │       ├── calc/
│       │   │       │   └── PriceComparator.kt    # Pure Kotlin, test-first
│       │   │       └── format/
│       │   │           └── LocaleNumberFormatter.kt
│       │   └── res/
│       │       ├── values/strings.xml            # Default (en)
│       │       ├── values-es/strings.xml         # Spanish
│       │       ├── drawable/
│       │       └── mipmap-*/                      # Launcher icons
│       ├── test/            # JVM unit tests
│       │   └── kotlin/com/mablanco/pricegrab/
│       │       ├── core/calc/PriceComparatorTest.kt
│       │       └── core/format/LocaleNumberFormatterTest.kt
│       └── androidTest/     # Instrumented tests
│           └── kotlin/com/mablanco/pricegrab/
│               ├── CompareScreenHappyPathTest.kt
│               ├── CompareScreenLocaleTest.kt
│               ├── CompareScreenConfigChangeTest.kt
│               └── CompareScreenAccessibilityTest.kt
├── detekt.yml
└── .editorconfig

.github/
└── workflows/
    └── android-ci.yml        # Build, lint, detekt, unit + instrumented tests, release-on-tag

fastlane/                     # (created only when F-Droid metadata is finalized)
└── metadata/android/
    ├── en-US/
    └── es-ES/
```

**Structure Decision**: Single-module Android app under `android/`. Separation
of concerns inside `com.mablanco.pricegrab`:

- `core/` holds framework-agnostic code (pure Kotlin, JVM-testable, no Android imports).
- `ui/` holds the Compose layer (screen, ViewModel, UI state, theme).

This keeps the calculation logic trivially unit-testable on the JVM (no emulator
needed), mirrors the constitution's ≥90% coverage requirement on the calculator,
and makes the path to F-Droid inclusion simple (standard Gradle layout, no
weird module topology).

The `android/` prefix isolates the native project from Spec Kit assets at the
repo root (`specs/`, `.specify/`, `.cursor/`), which keeps the repo tidy if a
non-Android auxiliary project (e.g., an F-Droid manifest generator) ever
appears.

## Complexity Tracking

> Fill ONLY if Constitution Check has violations that must be justified.

No violations. Table intentionally empty.
