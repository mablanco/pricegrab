---
description: "Task list for feature 001 — unit-price comparison screen"
---

# Tasks: Unit-Price Comparison Screen

**Input**: Design documents from `/specs/001-unit-price-comparison/`
**Prerequisites**: plan.md (required), spec.md (required), research.md,
data-model.md, contracts/price-comparator.md, quickstart.md

**Tests**: INCLUDED. The spec (FR-011) and the constitution (Principle V,
NON-NEGOTIABLE) both require tests, so every story below explicitly lists
its test tasks and they must be written **before** the corresponding
implementation tasks (TDD).

**Organization**: tasks are grouped by user story from `spec.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on
  incomplete tasks).
- **[Story]**: `[US1]` or `[US2]`. Setup, Foundational and Polish phases
  carry no story label.
- File paths are absolute within the repository.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization — produces a buildable, lintable,
testable Android module that ships a placeholder screen. No feature logic.

- [ ] T001 Create Gradle wrapper at `android/gradlew`, `android/gradlew.bat`, `android/gradle/wrapper/gradle-wrapper.jar`, `android/gradle/wrapper/gradle-wrapper.properties` (Gradle 8.11.1).
- [ ] T002 Create version catalog at `android/gradle/libs.versions.toml` with pinned toolchain, Compose BOM, lifecycle, test and static-analysis versions.
- [ ] T003 Create root Gradle scripts at `android/settings.gradle.kts` (pluginManagement + repositories + `include(":app")`) and `android/build.gradle.kts` (top-level plugin aliases only).
- [ ] T004 Create `android/gradle.properties` with JVM args, AndroidX flag, non-transitive R class, Kotlin official code style.
- [ ] T005 Create `android/app/build.gradle.kts` wiring AGP + Kotlin + Compose compiler + detekt + JaCoCo, declaring `namespace = "com.mablanco.pricegrab"`, `minSdk` 24, `targetSdk` 35, `compileSdk` 35, Compose BOM dependencies, unit + Compose UI test dependencies, a `signingConfigs.release` sourced from environment variables, and a `jacocoTestReport` task covering `testDebugUnitTest`.
- [ ] T006 Create `android/app/proguard-rules.pro` (start minimal; only add keep rules when R8 strips something needed).
- [ ] T007 Create `android/app/src/main/AndroidManifest.xml` with no runtime permissions, no `INTERNET`, a `dataExtractionRules` / `fullBackupContent` reference, `MainActivity` as launcher.
- [ ] T008 [P] Create the placeholder launcher icon at `android/app/src/main/res/drawable/ic_launcher.xml` (vector; replace with final art before v1.0.0).
- [ ] T009 [P] Create `android/app/src/main/res/xml/backup_rules.xml` (empty) and `android/app/src/main/res/xml/data_extraction_rules.xml` (empty cloud-backup + device-transfer).
- [ ] T010 [P] Create bridging XML theme at `android/app/src/main/res/values/themes.xml` (parent `Theme.Material.Light.NoActionBar`, transparent system bars).
- [ ] T011 [P] Create `android/app/src/main/res/values/strings.xml` with only `app_name` + placeholder-screen message.
- [ ] T012 [P] Create `android/app/src/main/res/values-es/strings.xml` mirroring T011 in Spanish.
- [ ] T013 [P] Create Material 3 theme scaffolding: `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/theme/Color.kt`, `Theme.kt` (light + dark + dynamic color on API ≥ 31), `Type.kt`.
- [ ] T014 Create `android/app/src/main/kotlin/com/mablanco/pricegrab/MainActivity.kt` as an edge-to-edge `ComponentActivity` that sets `PriceGrabApp()` as content.
- [ ] T015 Create `android/app/src/main/kotlin/com/mablanco/pricegrab/PriceGrabApp.kt` with `PriceGrabTheme { Scaffold { placeholder text from strings } }` + `@Preview`.
- [ ] T016 Create `android/app/src/test/kotlin/com/mablanco/pricegrab/ScaffoldSmokeTest.kt` — a trivial JUnit 4 passing test so the unit-test task is never empty. Deleted or replaced when Phase 2 tests land.
- [ ] T017 [P] Create `android/detekt.yml` (builds upon default config; relaxes `MaxLineLength` to 120 and allows PascalCase function names for Composables).
- [ ] T018 [P] Create `android/.editorconfig` (4-space indent for Kotlin, 2-space for YAML/XML/TOML, LF endings, final newline).
- [ ] T019 Create `.github/workflows/android-ci.yml` with jobs: `build` (lint + detekt + unit tests + JaCoCo + assembleDebug; uploads APK & reports), `instrumented-tests` (gated by `run-instrumented` label until Phase 3; runs `connectedDebugAndroidTest` on `reactivecircus/android-emulator-runner@v2` API 34 ATD), and `release` (tag-triggered, decodes `SIGNING_KEYSTORE_BASE64`, runs `assembleRelease`, uploads signed APK to the tag's GitHub Release).

**Checkpoint**: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:detekt` all green on CI. Placeholder screen launches on a device/emulator.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Pure-Kotlin domain + calculation core. Test-first. Required by
both US1 and US2 before any UI work.

**⚠️ CRITICAL**: no user-story work begins until this phase passes.

### Tests (write first, watch them fail)

- [ ] T020 [P] Write `android/app/src/test/kotlin/com/mablanco/pricegrab/core/calc/PriceComparatorTest.kt` containing the eleven-case canonical suite from `specs/001-unit-price-comparison/contracts/price-comparator.md` (cases 1–11). Suite MUST fail to compile initially (classes don't exist yet) → fail → turn green after T024–T026.
- [ ] T021 [P] Write `android/app/src/test/kotlin/com/mablanco/pricegrab/core/format/LocaleNumberFormatterTest.kt` asserting parse/format round-trips for `en-US` and `es-ES` (commas vs dots, grouping, large numbers, locale-sensitive decimal separator).
- [ ] T022 [P] Write `android/app/src/test/kotlin/com/mablanco/pricegrab/core/format/OfferParserTest.kt` asserting every `OfferParseResult` variant (`Success`, `InvalidPrice`, `InvalidQuantity`, `NegativePrice`, `NonPositiveQuantity`).

### Implementation

- [ ] T023 [P] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/Offer.kt` (immutable; `price: BigDecimal`, `quantity: BigDecimal`; `unitPrice` as a derived computed property using `MathContext.DECIMAL64`; `require` validation).
- [ ] T024 [P] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/ComparisonOutcome.kt` (sealed interface: `Tie`, `AWins(perUnitDelta, percentDelta)`, `BWins(perUnitDelta, percentDelta)`).
- [ ] T025 [P] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/core/format/LocaleNumberFormatter.kt` wrapping `NumberFormat.getInstance(locale)`; exposes `parse(String, Locale): BigDecimal?` and `format(BigDecimal, Locale, fractionDigits): String`.
- [ ] T026 [US0] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/core/format/OfferParser.kt` mapping a `(priceRaw, qtyRaw, Locale)` triple to an `OfferParseResult` (uses T025 + T023).
- [ ] T027 Create `android/app/src/main/kotlin/com/mablanco/pricegrab/core/calc/PriceComparator.kt` per the contract in `specs/001-unit-price-comparison/contracts/price-comparator.md`; throws `IllegalArgumentException` on precondition violation.
- [ ] T028 Run `./gradlew :app:testDebugUnitTest :app:jacocoTestReport` and confirm all tests pass locally.
- [ ] T029 Add a JaCoCo coverage-gate task `jacocoCoverageVerification` in `android/app/build.gradle.kts` with a `minimum = 0.90` rule scoped to classes under `com/mablanco/pricegrab/core/calc/**`; wire it into `:app:check`.
- [ ] T030 Delete `android/app/src/test/kotlin/com/mablanco/pricegrab/ScaffoldSmokeTest.kt` now that real tests exist.

**Checkpoint**: calculator green, formatter green, coverage gate green, CI still green.

---

## Phase 3: User Story 1 — Identify Cheaper Offer (Priority: P1) 🎯 MVP

**Goal**: A shopper enters the price and quantity for two offers and the
screen tells them which one is cheaper per unit, with a non-color cue and
an accessible announcement. No quantitative savings shown yet (US2 adds that).

**Independent test**: open the app, enter the two example pairs from
`spec.md` Acceptance Scenario A1 (or A4), observe that the correct offer is
highlighted and named as cheaper; rotate the device and confirm state
survives; enable TalkBack and confirm the result is announced; switch the
device language to Spanish and confirm the UI and decimal separator follow.

### Tests for User Story 1 (write first, watch them fail)

- [ ] T031 [P] [US1] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenIdentifyWinnerTest.kt` — happy path: enter A=(2.50, 500), B=(4.00, 1000); assert that the result node shows the "Offer B is cheaper" string (non-color cue present: icon + text + position).
- [ ] T032 [P] [US1] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenTieTest.kt` — same unit price both sides → result announces a tie.
- [ ] T033 [P] [US1] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenValidationTest.kt` — quantity = 0 in either offer suppresses the winner and shows the "quantity must be greater than zero" error on that field.
- [ ] T034 [P] [US1] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenLocaleEsTest.kt` — `@Config` with Spanish locale; enter `2,50` and `4,00` via locale-aware input; assert localized result string.
- [ ] T035 [P] [US1] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenConfigChangeTest.kt` — enter both offers, rotate via `activityRule.scenario.recreate()`, assert all four fields and the result are preserved.
- [ ] T036 [P] [US1] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenAccessibilityTest.kt` — assert every interactive node has a non-empty `contentDescription`; result node is a polite live region; touch targets ≥ 48 dp.

### Implementation for User Story 1

- [ ] T037 [P] [US1] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareUiState.kt` (data class + `InputError` enum as in `data-model.md`).
- [ ] T038 [US1] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareViewModel.kt` that holds a `MutableStateFlow<CompareUiState>`, uses `SavedStateHandle` for all four `*Raw` fields, parses via `OfferParser`, computes via `PriceComparator`, and exposes a `state: StateFlow<CompareUiState>`.
- [ ] T039 [US1] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreen.kt` — composable with two (price, quantity) input groups (`OutlinedTextField`, `KeyboardType.Decimal`, labelled `contentDescription`), inline error text, and a result card that shows a localized "Offer X is cheaper" message with an icon and no reliance on color alone. Wrap the result in `Modifier.semantics { liveRegion = Polite; contentDescription = localized summary }`.
- [ ] T040 [US1] Extend `android/app/src/main/res/values/strings.xml` and `android/app/src/main/res/values-es/strings.xml` with every user-visible string for US1 (field labels, hints, errors, winner messages, tie message, accessibility descriptions).
- [ ] T041 [US1] Wire `CompareScreen` into `PriceGrabApp.kt` behind the theme; remove the placeholder message.
- [ ] T042 [US1] Run `./gradlew :app:connectedDebugAndroidTest` locally on an API 34 emulator and ensure all US1 instrumented tests are green. Remove the Phase 1 instrumented-tests gate by deleting the `run-instrumented` label check in `.github/workflows/android-ci.yml`.
- [ ] T043 [US1] Update `README.md` screenshot block (text-only for now, actual screenshot added in a future polish PR) noting that the MVP is available in the repo.

**Checkpoint**: MVP is demo-able. US1 passes independently of US2. CI green, instrumented tests green, coverage gate intact.

---

## Phase 4: User Story 2 — Quantify Savings (Priority: P2)

**Goal**: The result card now shows *by how much* the cheaper offer is
cheaper, both as an absolute per-unit delta in locale-appropriate format and
as a percentage rounded to one decimal place. The TalkBack announcement is
enriched accordingly.

**Independent test**: repeat Acceptance Scenario A1 from `spec.md` and
confirm that the result now reads something like "Offer B is cheaper — saves
0.001 per unit (20% less)." Rotate to confirm the numbers are preserved.
TalkBack announces the savings phrase.

### Tests for User Story 2 (write first, watch them fail)

- [ ] T044 [P] [US2] Extend `CompareScreenIdentifyWinnerTest.kt` (or add `CompareScreenSavingsTest.kt`) to assert the absolute and percent savings text is visible and announced for a known-winner scenario (2.50/500 vs 4.00/1000 → 0.001 per unit, 20%).
- [ ] T045 [P] [US2] Add an instrumented test asserting that when one offer is free and the other is not, the screen shows a `100%` savings and a `Δ = (other unit price)` text.
- [ ] T046 [P] [US2] Add a unit test on `CompareUiState` mapping (or on a small `ResultPresenter`) asserting the locale-specific rendering of the percentage and the per-unit delta.

### Implementation for User Story 2

- [ ] T047 [US2] Extract a small pure-Kotlin `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/ResultPresenter.kt` that turns a `ComparisonOutcome` + `Locale` into displayable strings (winner headline, absolute savings, percent savings). Unit-tested under `android/app/src/test/kotlin/...ResultPresenterTest.kt`.
- [ ] T048 [US2] Update `CompareScreen.kt` to render the savings row below the winner headline for `AWins` / `BWins`; hide the row for `Tie`; keep the existing non-color cue.
- [ ] T049 [US2] Update the polite live-region `contentDescription` to include "saves X per unit, Y% less" in both locales.
- [ ] T050 [US2] Add new strings in `values/strings.xml` and `values-es/strings.xml`: savings line template, per-unit-delta format, percent-format, tie message reused from US1.
- [ ] T051 [US2] Run `./gradlew :app:connectedDebugAndroidTest` and confirm all T031–T046 pass.

**Checkpoint**: US1 + US2 both work end-to-end; spec's `SC-001 … SC-007`
measurable criteria should now be demonstrable on device.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: improvements that affect multiple stories or cross the Android
boundary (F-Droid, release, docs).

- [ ] T052 [P] Add `fastlane/metadata/android/en-US/title.txt`, `short_description.txt`, `full_description.txt` plus a first `changelogs/1.txt`.
- [ ] T053 [P] Add `fastlane/metadata/android/es-ES/` mirror of T052.
- [ ] T054 [P] Add at least one real screenshot under `fastlane/metadata/android/{en-US,es-ES}/images/phoneScreenshots/` (captured via instrumented test or manually).
- [ ] T055 Measure cold start on a Pixel 4a-class device; if it exceeds the 2 s budget from `plan.md`, file a follow-up issue and tune `MainActivity`.
- [ ] T056 Measure release APK size; confirm ≤ 5 MB. If not, enable resource shrinking verification and investigate.
- [ ] T057 Create a keystore for the release, upload the base64-encoded file as the `SIGNING_KEYSTORE_BASE64` GitHub secret, plus `SIGNING_KEYSTORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`; push a `v0.1.0` tag and verify the `release` CI job produces a signed APK on the GitHub Release.
- [ ] T058 Walk `specs/001-unit-price-comparison/quickstart.md` end-to-end on a clean clone; fix any drift.
- [ ] T059 Run `./gradlew :app:lint :app:detekt` one last time with zero new findings; baseline any legitimate legacy warnings in `app/lint-baseline.xml` if needed.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies.
- **Foundational (Phase 2)**: depends on Setup completion. Blocks US1 and US2.
- **User Story 1 (Phase 3)**: depends on Foundational. Independent of US2.
- **User Story 2 (Phase 4)**: depends on Foundational *and* US1 (US2 extends the US1 screen).
- **Polish (Phase 5)**: depends on US1 and US2 being demo-able.

### Within a phase

- Tests marked `[P]` can be written in parallel before their implementation.
- All `[P]` implementation tasks can proceed in parallel (different files).
- Tasks without `[P]` must wait for the immediately preceding non-`[P]` task
  in the same phase.

### Parallel opportunities

Within Setup, T008–T013 and T017–T018 are `[P]`. Within Foundational,
T020–T022 (tests) can all be written in parallel before any of
T023–T027. Within US1, all six test files (T031–T036) and `CompareUiState`
(T037) are `[P]`.

---

## Implementation Strategy (PRs)

To keep PRs small and reviewable, land the phases as three separate pull
requests on dedicated feature branches:

### PR A — `feat/002-android-scaffold-and-ci`

Covers **Phase 1** + `tasks.md`. Delivers a buildable, lintable, testable
Android module with a placeholder screen and a green CI pipeline. No feature
logic. This is what lives on this branch right now.

### PR B — `feat/003-us1-compare-cheaper`

Covers **Phase 2 + Phase 3** (Foundational + US1). Test-first calculator,
formatter and parser, then the MVP Compose screen. Flips the instrumented-
tests CI job to always-on.

### PR C — `feat/004-us2-quantify-savings`

Covers **Phase 4**. UI-only extension showing absolute + percent savings.

### (optional) PR D — `chore/005-release-polish`

Covers **Phase 5**. Fastlane metadata for F-Droid, first signed release via
CI tag push, performance and APK-size verification.

### Notes

- Each PR must keep the main branch green (lint, detekt, unit tests, JaCoCo
  coverage gate, and from PR B onward, instrumented tests).
- Commit after each task or logical group; keep the commit message in
  English, Conventional Commits prefix (`feat:`, `test:`, `chore:`, …).
- Stop at each phase checkpoint and validate before moving on.
- If a `[NEEDS CLARIFICATION]` surfaces while executing a task, STOP and
  raise it with Marco rather than inventing an answer.
