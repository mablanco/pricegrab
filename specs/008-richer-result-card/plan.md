# Implementation Plan: Richer Result Card (Visible Absolute + Percent Savings)

**Branch**: `008-richer-result-card` | **Date**: 2026-07-24
**Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/008-richer-result-card/spec.md`

## Summary

Surface **percent savings** on the compare hero whenever there is a unique
winner, alongside the absolute per-unit savings already shown (vs the
second-cheapest offer). Today `ResultPresenter` already formats
`percentDelta`, but `ResultRegion` / `HeroResultCard` only render the
absolute line — percent is computed and unused (and absent from TalkBack).
This feature restores aisle glanceability: absolute + percent both visible
and announced, without changing comparison math, offer labels, or
navigation.

Technical approach: extend localized savings string templates to include
the percent argument; render absolute as the primary savings line and
percent as a second visible line on the hero; fold both into the polite
live-region summary. Keep `PriceComparator` / `ComparisonOutcome` /
rounding rules untouched. Ship via planning → implementation →
release-prep (next free PR letter **AD**).

## Technical Context

**Language/Version**: Kotlin 2.0.21 — same toolchain as features 001–007.

**Primary Dependencies**: None new. Existing Jetpack Compose Material 3,
`ResultPresenter`, `LocaleNumberFormatter`, string resources.

**Storage**: N/A — presentation-only; no persistence beyond existing
Compare/`SavedStateHandle` form state.

**Testing**:

- JVM unit: extend `ResultPresenterTest` only if presentation helpers
  change; prefer keeping presenter as-is (already returns
  `percentDelta`). Add a small pure formatter test if savings-line
  assembly moves out of the composable.
- Compose UI / instrumented:
  - Update `CompareScreenSavingsTest` so unique-winner cases assert
    **visible** percent (canonical 20% and free-offer 100%).
  - Tie / empty / incompatible-units still hide all savings figures.
  - TalkBack / live-region summary includes absolute + percent for a
    winner (extend accessibility test if one exists; otherwise add).
  - 200% font scale smoke: both savings lines remain readable (layout
    test or manual quickstart).
- Manual: light/dark (incl. Settings appearance), `en`/`es`, two- and
  three-offer happy paths, aisle glance check (SC-006).

**Target Platform**: Android, `minSdk` 24, `targetSdk` 35, `compileSdk` 35
— unchanged.

**Project Type**: mobile-app (single Android application, no backend) —
unchanged.

**Performance Goals**: No new I/O or deps; recomposition only when outcome
changes. Cold-start budget unchanged (constitution IV).

**Constraints**:

- Fully offline; no new permissions; F-Droid compatible.
- ES + EN for all new/changed result strings.
- Non-color cues for winner remain (check / tie glyph).
- Typical phone portrait: richer hero must not force loss of either
  savings figure at default or 200% font (wrapping OK; clipping meaning
  not OK).
- Do not change who wins, second-cheapest savings base, or 3-offer cap.

**Scale/Scope**: Touch `CompareScreenResult.kt` (or successor),
`strings.xml` / `values-es`, instrumented savings (+ a11y) tests,
previews. Estimated ~50–120 LOC production + ~80–150 LOC tests. No
versionCode bump in the implementation PR — release prep targets
**0.1.11** (stay on 0.1.x). Do **not** bump to 0.2.0 for this feature.

**Agent reminder (do not bump now)**: When a future change would warrant a
**MINOR** SemVer bump (0.2.0+), **prompt Marco** before choosing the
version — do not invent a minor bump unilaterally.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gates derived from `.specify/memory/constitution.md` v1.0.1.

| Principle / section | Gate | Plan evidence | Status |
|---------------------|------|---------------|--------|
| I. Modern Mobile UX | M3, single-purpose primary flow, non-color cues, clear empty/invalid states | Compare stays primary; hero still text + icon; percent is companion copy only; tie/empty/incompatible stay quiet about savings. | ✅ |
| II. Accessibility (NON-NEGOTIABLE) | TalkBack, 200% font, WCAG AA, non-color cues | Live-region includes absolute + percent; 200% layout must keep both figures readable; contrast via existing onSurface / onSurfaceVariant tokens. | ✅ |
| III. Internationalization (ES/EN) | Strings in resources, locale-aware numbers | New percent templates in `values` + `values-es`; numerals via existing `ResultPresenter` / locale formatter. | ✅ |
| IV. Offline-First Performance | No network, ≤2s cold start, APK &lt; 15 MB, config-change preserve | UI-only; no deps; ViewModel state unchanged. | ✅ |
| V. Test-First Quality | Red→Green→Refactor; CI gates | Instrumented savings assertions updated first; unit coverage for any extracted formatter; Compare regression suite stays green. | ✅ |
| Privacy & Platform | No trackers, F-Droid-compatible | No storage/network/permissions. | ✅ |

**Post-design re-check**: Passes. Layout choice, string contract, and
a11y summary documented in [research.md](./research.md),
[data-model.md](./data-model.md), and
[contracts/result-savings-display.md](./contracts/result-savings-display.md).

## Project Structure

### Documentation (this feature)

```text
specs/008-richer-result-card/
├── plan.md                 # This file
├── research.md             # Phase 0
├── data-model.md           # Phase 1
├── quickstart.md           # Phase 1 manual verification
├── contracts/
│   └── result-savings-display.md
├── checklists/
│   └── requirements.md
├── spec.md
└── tasks.md                # /speckit.tasks (not this command)
```

### Source Code (touch list)

```text
android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareScreenResult.kt       # MODIFY — percent line + a11y summary
├── CompareScreenPreviews.kt     # MODIFY — show percent in previews
└── ResultPresenter.kt           # KEEP — already exposes percentDelta

android/app/src/main/res/values/strings.xml
android/app/src/main/res/values-es/strings.xml

android/app/src/androidTest/kotlin/.../compare/
├── CompareScreenSavingsTest.kt  # MODIFY — assert visible percent
└── (a11y test as needed)

android/app/src/test/kotlin/.../compare/
└── ResultPresenterTest.kt       # KEEP / minor if helpers extracted
```

**Structure Decision**: Stay inside the existing single-module Android app
(`android/app`). No new Gradle modules. Domain `core/calc` and
`ComparisonOutcome` math **untouched**.

## Complexity Tracking

> No constitution violations. Table left empty intentionally.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Implementation phases (preview for `/speckit.tasks`)

1. **Tests first**: failing instrumented assertions for visible percent on
   winner (20% and 100% fixtures); tie still hides savings; a11y summary
   includes percent.
2. **Strings**: ES/EN templates for percent companion line (and any
   combined a11y phrasing if needed).
3. **UI**: `HeroResultCard` / `ResultRegion` render absolute + percent;
   live-region joins headline + both savings parts.
4. **Previews + regression**: update Compose previews; run Compare
   suites; manual quickstart.
5. **Release prep** (separate PR): version **0.1.11**, changelogs,
   status/fdroid docs.

## PR cadence

| Letter | Branch / PR | Contents |
|--------|-------------|----------|
| **AD** | `008-richer-result-card` (this branch) | Spec + plan + research + contracts + tasks (planning only; no app source) |
| **AE** | `feat/0xx-richer-result-card-impl` | Implementation + tests |
| **AF** | `chore/0xx-…` release-prep | Version, changelogs, status docs |

Global ledger: update `specs/001-unit-price-comparison/tasks.md` when
opening PR AD (planning commit / follow-up on this branch).
