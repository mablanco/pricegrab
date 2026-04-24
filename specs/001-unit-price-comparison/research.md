# Research: Unit-Price Comparison Screen (Feature 001)

Phase 0 research consolidating decisions that the plan depends on. Each entry
follows the **Decision / Rationale / Alternatives considered** format.

## 1. Android toolchain pinning (for reproducibility and F-Droid)

**Decision**: Pin every version in `gradle/libs.versions.toml` and commit the
Gradle Wrapper. Specifically:

- Gradle: 8.11.1
- Android Gradle Plugin (AGP): 8.7.3
- Kotlin: 2.0.21
- Compose BOM: 2025.01.00
- JDK: Temurin 17 (set via `org.gradle.java.home` in CI; documented in
  `quickstart.md`)
- `minSdk` 24, `targetSdk` 35, `compileSdk` 35
- detekt: 1.23.7

**Rationale**: F-Droid builds from source on their own infrastructure and
requires deterministic, declarative build inputs. Pinning everything via the
version catalog + committed wrapper makes the build reproducible and keeps
the Dependabot/Renovate update surface explicit. JDK 17 is the current
floor for AGP 8.x.

**Alternatives considered**:

- *Latest stable, floating versions* — rejected; breaks reproducibility and
  produces flaky F-Droid builds.
- *Version Catalog + composite build* — rejected for v1 as overkill for a
  single-module app; can be revisited if we split modules later.

## 2. Locale-aware decimal parsing

**Decision**: Parse user input with
`NumberFormat.getInstance(LocalConfiguration.current.locale)` and format
output with the same instance. Accept the locale's decimal separator
transparently; reject anything else with an inline error.

**Rationale**: `NumberFormat` is part of `java.text` and Unicode-CLDR aware,
so `es-ES` users type `2,50` and `en-US` users type `2.50` without any
code branch. It avoids the classic `Double.parseDouble("2,50")` bug. Formatting
also honors grouping conventions (`1.234,56` vs `1,234.56`).

**Alternatives considered**:

- *`Double.parseDouble` on raw text* — rejected; locale-blind, throws on `,`.
- *Custom regex parser* — rejected; reinventing Unicode decimal handling.
- *Always use `.` internally and convert* — still need `NumberFormat` to
  display results localized; simpler to use it for both directions.

## 3. Internal numeric representation

**Decision**: Use `BigDecimal` for prices and quantities in the `core/`
layer and let the UI convert `String <-> BigDecimal` via the locale-aware
formatter. Unit price computed as
`price.divide(quantity, MathContext.DECIMAL64)`.

**Rationale**: Money and per-unit prices are decimal by nature. `Double`
introduces representation error (e.g. `0.1 + 0.2 != 0.3`) that will show up
in user-visible rounding and make the calculator test suite flaky at
boundary values. `DECIMAL64` provides 16 significant decimal digits — more
than enough for any realistic supermarket input.

**Alternatives considered**:

- *`Double`* — rejected; floating-point error is unacceptable in a
  correctness-critical calculator.
- *Integer "cents" representation* — rejected; the second operand (quantity)
  is commonly not integer (e.g. 500 g is integer, but 0.5 kg is not) and the
  unit-price result is inherently decimal.

## 4. TalkBack announcement of result changes

**Decision**: Wrap the result Composable with
`Modifier.semantics { liveRegion = LiveRegionMode.Polite }` and include a
concise, fully localized `contentDescription` that summarizes the winner
and the saving (e.g. *"Product B is 20% cheaper per unit. Saves 0.001 per
unit."*).

**Rationale**: `LiveRegionMode.Polite` causes TalkBack to announce the new
result without interrupting the user's typing. The spoken description must
read naturally and must include both the winner and the magnitude, so it
is a purpose-built string, not a concatenation of UI labels.

**Alternatives considered**:

- *`LiveRegionMode.Assertive`* — rejected; interrupts mid-keystroke, which is
  disruptive while the user is still entering values.
- *Manual `AccessibilityEvent` dispatch* — rejected; deprecated in favor of
  the Compose semantics API.

## 5. State preservation across configuration changes

**Decision**: Hold UI state in a `CompareViewModel(private val handle:
SavedStateHandle)` with `MutableStateFlow<CompareUiState>`. Backed by
`SavedStateHandle` so that process death also restores inputs. Complement
with `rememberSaveable` only for purely UI-local concerns (focus, password
visibility, etc. — not applicable here).

**Rationale**: `SavedStateHandle` covers all three cases the constitution
requires (rotation, theme change, locale change, backgrounding) and is
Google's first-party recommendation. Keeping state in the ViewModel instead
of composables also makes it directly testable.

**Alternatives considered**:

- *Plain `remember` in the composable* — rejected; lost on rotation.
- *`rememberSaveable` only* — rejected; harder to unit-test without Compose
  runtime, and doesn't survive process death without `SavedStateHandle`.

## 6. Preventing negative numeric input

**Decision**: Use
`KeyboardOptions(keyboardType = KeyboardType.Decimal)`, which does **not**
expose a minus sign, and also strip any non-digit / non-separator character
in the `onValueChange` handler as a belt-and-braces measure (in case of
paste, hardware keyboard, or IME quirks). Validate again in `core/` before
computing.

**Rationale**: Layered defense. Keyboard-level prevention covers 99% of
cases and aligns with "prevention over post-hoc validation" (constitution I).
Defensive stripping covers the remaining 1%. Core-level validation covers
bugs in the UI.

**Alternatives considered**:

- *`KeyboardType.Number`* — rejected; would expose a minus sign.
- *`KeyboardType.NumberPassword`* — rejected; breaks TalkBack pronunciation
  and surprises sighted users.

## 7. GitHub Actions CI + signed APK publishing

**Decision**: A single workflow `.github/workflows/android-ci.yml` with jobs:

1. **lint** — Android Lint + detekt + ktlint-compatible formatting check.
2. **unit-test** — `./gradlew test jacocoTestReport`; upload coverage as
   artifact; fail if `core/calc/**` line coverage drops below 90%.
3. **instrumented-test** — Gradle Managed Device (GMD) on an API 34 AOSP
   ATD image running the `connectedCheck` suite; this keeps everything
   local to the runner, no Firebase Test Lab, no external service.
4. **assemble-debug** — produces a debug APK, attached as an artifact for
   every PR.
5. **release** (only on `v*` git tag pushes) — runs `assembleRelease`, signs
   the APK using a keystore injected from GitHub Actions secrets
   (`SIGNING_KEYSTORE_BASE64`, `SIGNING_KEYSTORE_PASSWORD`,
   `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`), and uploads the signed APK
   to the GitHub Release for the tag.

Jobs 1–4 run on every PR; job 5 runs only on tag pushes.

**Rationale**: GitHub-hosted runners + GMD images keep the whole pipeline
open-source-friendly and free for a public repo. No reliance on Google
proprietary services (matches F-Droid ethos). Release signing happens on CI
so the keystore never lives on a developer machine.

**Alternatives considered**:

- *Firebase Test Lab* — rejected; proprietary, requires Google account binding.
- *Self-hosted macOS runner* — rejected; we only need Linux + API 34 emulator.
- *Releases via `r0adkll/upload-google-play`* — rejected; Google Play is
  explicitly out of scope (constitution v1.0.1 Distribution).

## 8. F-Droid inclusion prerequisites

**Decision**: Keep the codebase F-Droid-ready from day one but defer the
actual F-Droid submission until after v1.0.0 ships and runs on real devices
for a couple of weeks. Readiness means:

- No proprietary dependencies, no non-free binary blobs.
- Reproducible build via pinned toolchain.
- An Android Manifest without `INTERNET` and without GMS permissions.
- A `fastlane/metadata/android/` directory pre-populated with
  title/short-description/full-description in `en-US` and `es-ES`, plus a
  changelog per release (`changelogs/<versionCode>.txt`).
- An `fdroiddata` submission recipe (`metadata/com.mablanco.pricegrab.yml`)
  will be authored as its own feature once v1.0.0 is cut.

**Rationale**: Submitting to F-Droid is a one-way door — metadata, signing
identity and build recipe must be right before the first inclusion. It is
safer to validate the build pipeline on GitHub Releases first and only then
offer it to F-Droid.

**Alternatives considered**:

- *Submit to F-Droid immediately* — rejected; the app has no device-level
  validation yet and a bad first impression is hard to recover from.
- *Use IzzyOnDroid F-Droid repo as a stepping stone* — kept as an option for
  v1.0.0 if the official F-Droid queue is slow; no commitment now.

## 9. Theming and Material 3

**Decision**: Use Material 3 with dynamic color on Android 12+ (`minSdk 24`
so dynamic color is applied conditionally via `dynamicColorScheme`) and a
hand-picked brand color scheme fallback on older versions. Both light and
dark themes supported; the system setting decides.

**Rationale**: Dynamic color is a Material You expectation in 2026 and is
cost-free when available. The fallback palette is ~6 colors total; we will
pick it against WCAG AA (≥4.5:1 for text, ≥3:1 for large text/UI chrome) using
an open-source contrast checker at design time.

**Alternatives considered**:

- *Material 2* — rejected; older, less accessible defaults, weaker
  Compose integration.
- *Custom design system* — rejected; no design capacity, and for a
  single-screen utility app it is unjustified complexity.
