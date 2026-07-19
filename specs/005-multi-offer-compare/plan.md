# Implementation Plan: Compare Up to Three Offers

**Branch**: `005-multi-offer-compare` | **Date**: 2026-07-19
**Spec**: [spec.md](./spec.md)

## Summary

Refactor Compare from fixed A/B fields to a list of 2..3 offer slots with
`+`/`−`, rank by unit price, and show winner vs second-cheapest. Bundle
launcher icon vertical nudge and ship **v0.1.8**.

## Technical Context

Same Kotlin / Compose / Material 3 stack as 001–004. No new dependencies.

## Phase 1 — Domain + ViewModel

1. Redesign `ComparisonOutcome` → `Tie` | `Winner(slotIndex, deltas)`.
2. `PriceComparator.compareMany` + thin `compare(a,b)`.
3. Update `ComparisonGate`, `ResultPresenter`, all JVM tests.
4. `OfferSlotState` list in `CompareUiState`; SavedState keys for N slots.
5. ViewModel: indexed setters, `addOffer`, `removeOffer`, reset/undo.

## Phase 2 — UI

1. Compose: list of OfferCards; `+` / `−`; heading strings.
2. Hero result uses parameterized winner title.
3. Instrumented tests for add/remove and 3-offer scenario.

## Phase 3 — A11y / Reset polish

1. Content descriptions for add/remove.
2. Reset/undo instrumented coverage with three slots.

## Phase 4 — Release

1. versionCode 9 / 0.1.8; changelogs; `ART_OFFSET_Y_PX` nudge + regen icons.
2. `docs/fdroid.md` sync; tag `v0.1.8` after manual QA.

## File touch list (primary)

- `core/model/ComparisonOutcome.kt`
- `core/calc/PriceComparator.kt` (+ tests)
- `ui/compare/ComparisonGate.kt`, `CompareUiState.kt`, `CompareViewModel*.kt`
- `ui/compare/CompareScreen.kt`, `ResultPresenter.kt`
- `res/values{,-es}/strings.xml`
- androidTest compare screens
- `branding/regenerate-icons.py` + mipmaps (Phase 4)
- `android/app/build.gradle.kts`, fastlane changelogs, `docs/fdroid.md`

## Manual verification (v0.1.8)

1. Cold launch: 2 cards; + adds C; cannot add fourth.
2. Three mass offers → correct winner + per kg vs second.
3. C blank → A vs B still works.
4. g vs ml → incompatible error.
5. Reset → 2 empty; Undo restores 3.
6. TalkBack: +/− and result.
7. Icon circular mask: even top/bottom air after nudge.
