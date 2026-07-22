# Implementation Plan: Compact Offer Input Row

**Branch**: `006-compact-offer-row` | **Date**: 2026-07-22
**Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/006-compact-offer-row/spec.md`

## Summary

Redesign each offer card so **price**, **quantity**, and **unit** sit on one
compact horizontal row at the default system font scale, with numeric fields
sized for short aisle values. When font scale is large (including **200%**),
switch to an **adaptive** two-row arrangement (price; then quantity + unit)
so labels and controls never truncate or overlap. Comparison math, multi-offer
controls, reset/undo, and result hero stay unchanged — presentation only.

Technical approach: extract offer-field arrangement into a small Compose
helper driven by `LocalDensity.current.fontScale`; keep existing test tags and
ViewModel APIs; extend large-font instrumented coverage to assert the adaptive
arrangement; ship via the usual planning → implementation → release-prep
cadence (next free PR letter **X**).

## Technical Context

**Language/Version**: Kotlin 2.0.21 — same toolchain as features 001–005.

**Primary Dependencies**: no additions. Material 3 Compose BOM already
provides `OutlinedTextField`, `ExposedDropdownMenuBox`, `Card`, and
`Row`/`Column` used today in `OfferCard`.

**Storage**: N/A — no new `SavedStateHandle` keys; existing slot persistence
unchanged. Layout arrangement is derived at composition time from font scale
(not persisted).

**Testing**:

- No new JVM domain tests expected (no `core/` math or parsing changes).
- Compose UI / instrumented:
  - Default font scale: price, quantity, and unit of an offer share one
    horizontal row (structural assertion via bounds or a test tag on the
    row container — see [contracts/offer-row-layout.md](./contracts/offer-row-layout.md)).
  - `fontScale = 2f` (extend `CompareScreenLargeFontTest`): all three
    controls still displayed; adaptive arrangement active (not the compact
    single-row geometry).
  - Existing multi-offer, units, savings, reset/undo, a11y, and config-change
    tests remain green without tag renames.
- Manual: TalkBack on both arrangements; cold launch with 2 and 3 offers at
  default scale for scroll/density; 200% system font in Settings.

**Target Platform**: Android, `minSdk` 24, `targetSdk` 35, `compileSdk` 35 —
unchanged.

**Project Type**: mobile-app (single Android application, no backend) —
unchanged.

**Performance Goals**: Arrangement choice is O(1) read of `fontScale` per
composition; no new I/O, coroutines, or network. Cold start budget unchanged
(constitution IV).

**Constraints**:

- Fully offline; no new permissions.
- Touch targets ≥ 48×48 dp in both arrangements (constitution I / II).
- ES + EN strings remain complete; prefer reusing existing labels/CDs.
- F-Droid Mode B / reproducible builds unaffected (UI-only).
- Portrait-first; landscape/tablet redesign out of scope.

**Scale/Scope**: Primarily `OfferCard` / `LabeledNumberField` /
`QuantityUnitSelector` layout in `ui/compare/`. Optional tiny pure helper for
arrangement selection (unit-testable). Estimated ~80–150 LOC production +
~100–200 LOC test. No versionCode bump in the implementation PR — release
prep chooses the next version (likely **0.1.9**).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gates derived from `.specify/memory/constitution.md` v1.0.1.

| Principle / section | Gate | Plan evidence | Status |
|---------------------|------|---------------|--------|
| I. Modern Mobile UX | M3, ≥48dp touch, no scroll on typical portrait for primary flow, non-color cues, single-purpose | Compact single row recovers vertical space with 2–3 offers; M3 fields retained; unit selector keeps ≥48dp; result cues unchanged. | ✅ |
| II. Accessibility (NON-NEGOTIABLE) | TalkBack, 200% font, WCAG AA, non-color cues, focus order | Adaptive two-row at large `fontScale` (incl. 200%); existing CDs kept; extend large-font + a11y instrumented tests; manual TalkBack smoke. | ✅ |
| III. Internationalization (ES/EN) | Strings in resources, locale-aware numbers | No new user-facing copy expected; existing `en`/`es` labels and CDs reused; locale tests remain green. | ✅ |
| IV. Offline-First Performance | No network, ≤2s cold start, APK &lt; 15 MB | UI-only; no dependencies or permissions. | ✅ |
| V. Test-First Quality | Red→Green→Refactor; CI gates | Arrangement tests written first (default compact + 200% adaptive); existing suites must stay green. | ✅ |
| Privacy & Platform | No trackers, F-Droid-compatible | Unchanged distribution/privacy surface. | ✅ |

**Post-design re-check**: Passes. Adaptive breakpoint and error placement
documented in [research.md](./research.md); no principle violations requiring
Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/006-compact-offer-row/
├── plan.md                 # This file
├── research.md             # Phase 0
├── data-model.md           # Phase 1 (presentation arrangement only)
├── quickstart.md           # Phase 1 manual verification
├── contracts/
│   └── offer-row-layout.md # Phase 1 UI / test contract
├── checklists/
│   └── requirements.md
├── spec.md
└── tasks.md                # /speckit.tasks (not this command)
```

### Source Code (touch list)

```text
android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareScreen.kt              # OfferCard row layout + arrangement switch
├── CompareScreenUnits.kt         # minor width/alignment tweaks if needed
└── (optional) OfferInputArrangement.kt  # fontScale → arrangement helper

android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareScreenLargeFontTest.kt # assert adaptive arrangement @ 200%
├── (new or extended) compact-row geometry / tag tests @ default scale
└── existing *Test.kt             # must remain green (tags stable)
```

**Structure Decision**: Stay inside the existing single-module Android app
(`android/app`). No new Gradle modules. Domain (`core/`) untouched.

## Complexity Tracking

> No constitution violations. Table left empty intentionally.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Implementation phases (preview for `/speckit.tasks`)

1. **Tests first**: failing assertions for single-row at default scale and
   adaptive layout at `fontScale = 2f`.
2. **Arrangement helper + OfferCard refactor**: compact `Row` with weighted
   price/quantity + fixed unit; switch to two-row when font scale ≥ threshold.
3. **Polish**: error `supportingText` / alignment so compact row does not
   clip; keep IME Next/Done order price → quantity → done.
4. **Regression**: full compare instrumented suite + manual TalkBack /
   three-offer density check.
5. **Release prep** (separate PR after impl merges): versionName/versionCode,
   fastlane changelogs, `docs/project-status.md`, `docs/fdroid.md` as needed.

## PR cadence

| Letter | Branch / PR | Contents |
|--------|-------------|----------|
| **X** | `006-compact-offer-row` (this branch) | Spec + plan + research + contracts + tasks (planning only; no app source) |
| **Y** | `feat/0xx-compact-offer-row-impl` | Implementation + tests |
| **Z** | `chore/0xx-…` release-prep | Version, changelogs, status docs |

Global ledger: update `specs/001-unit-price-comparison/tasks.md` when
opening PR X.
