# Tasks: Compare Up to Three Offers

**Input**: Design documents from `/specs/005-multi-offer-compare/`
**Tests**: REQUIRED (constitution V).

## Phase 1: Domain + ViewModel

### Tests first

- [ ] T001 [P] Update `PriceComparatorTest` for `Winner` indices; add 3-offer ranking cases (unique winner vs second; top-two tie; free offer).
- [ ] T002 [P] Update `ComparisonGate` / ViewModel unit tests for list slots, incomplete with &lt;2 parses, blank C ignored.
- [ ] T003 [P] Extend reset/undo ViewModel tests for 3-slot snapshot.
- [ ] T004 [P] Update `ResultPresenterTest` for `Winner`.

### Implementation

- [ ] T005 Redesign `ComparisonOutcome` to `Tie` | `Winner(slotIndex, perUnitDelta, percentDelta)`.
- [ ] T006 Implement `PriceComparator.compareMany`; keep `compare(a,b)` as wrapper.
- [ ] T007 Update `ComparisonGate.evaluate` for list of parse results.
- [ ] T008 Refactor `CompareUiState` to `offers: List<OfferSlotState>`; snapshot list.
- [ ] T009 Refactor `CompareViewModel` + SavedState keys for 2..3 slots; add/remove.
- [ ] T010 Update `ResultPresenter` for `Winner`.
- [ ] T011 `./gradlew :app:testDebugUnitTest :app:jacocoTestReport` green.

## Phase 2: UI (US1)

- [ ] T012 [P] [US1] Instrumented: + shows Offer C; max 3; − returns to 2.
- [ ] T013 [P] [US1] Instrumented: three offers → winner headline + per-kg savings.
- [ ] T014 [US1] Strings EN/ES: heading, Offer C, add/remove, parameterized winner.
- [ ] T015 [US1] CompareScreen: list cards, +/− controls, wire ViewModel.
- [ ] T016 [US1] HeroResultCard uses new outcome.

## Phase 3: Safety + a11y (US2/US3)

- [ ] T017 [P] [US2] Instrumented: blank C still compares A/B; incompatible with C.
- [ ] T018 [P] [US3] A11y: +/− descriptions; result live region.
- [ ] T019 [P] Reset/undo instrumented with three slots.
- [ ] T020 Detekt / compile androidTest green.

## Phase 4: Release

- [x] T021 Bump versionCode 9 / versionName 0.1.8.
- [x] T022 Changelogs en/es for code 9.
- [x] T023 `ART_OFFSET_Y_PX` nudge (~−95); regenerate icons.
- [x] T024 Update `docs/fdroid.md`; ledger PR U/V/W.
- [ ] T025 Tag v0.1.8 after manual verification (Marco).

## PR strategy

- **PR U** — `005-multi-offer-compare` planning only.
- **PR V** — `feat/023-multi-offer-impl` T001–T020.
- **PR W** — `chore/024-fdroid-doc-sync-v0.1.8` T021–T024; tag T025 outside.
