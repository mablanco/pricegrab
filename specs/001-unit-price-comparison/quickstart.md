# Quickstart: Feature 001 — Unit-Price Comparison Screen

How to get the Android app running locally, how to run the test suite, and
how to produce a signed release APK. This file will remain useful beyond
feature 001 as the project quickstart.

## 1. Prerequisites

- **JDK 17** (Temurin recommended). Verify: `java -version`.
- **Android Studio** Ladybug (2024.2.1) or newer, *or* a standalone
  command-line workflow with the Android SDK Command-Line Tools, platform
  tools, and an SDK platform for API 35.
- **Git**.
- Approximately **6 GB** free disk (Android SDK + emulator image).

## 2. Clone the repo

```bash
git clone git@github.com:mablanco/pricegrab.git
cd pricegrab
```

## 3. Open and build

### With Android Studio (recommended)

1. Open Android Studio → *Open an existing project* → select the
   `android/` directory inside the repo.
2. On first open, let Gradle sync. All versions are pinned in
   `android/gradle/libs.versions.toml`; Android Studio will offer to
   download the exact matching components.
3. Run target: `app` → select an API 34+ emulator or a physical device →
   *Run*.

### From the command line

```bash
cd android
./gradlew assembleDebug
./gradlew installDebug   # requires a connected device / running emulator
```

The first `./gradlew` invocation will download the Gradle wrapper and
produce a local `~/.gradle` cache; it takes ~3–5 minutes on a first run,
< 30 seconds after that.

## 4. Run the tests

### Unit tests (fast, JVM only)

```bash
cd android
./gradlew test
./gradlew jacocoTestReport      # coverage HTML at app/build/reports/jacoco/
```

Constitutional gate: **≥ 90 % line coverage on `core/calc/**`** — CI fails
the build if coverage drops below this floor.

### Instrumented tests (slower, needs a device/emulator)

```bash
cd android
./gradlew connectedCheck
```

Covers at minimum:

- Happy-path comparison, once per supported locale (`en`, `es`).
- Configuration-change preservation (rotation keeps inputs + result).
- Accessibility smoke test (every interactive node has a
  `contentDescription`; the result node is a polite live region).

### Static analysis

```bash
cd android
./gradlew lint detekt
```

## 5. Change the language while the app is running

- On device/emulator: *Settings → System → Languages & input → Languages*.
- Move Spanish above English (or vice versa) and re-enter PriceGrab.
- All user-visible strings and the decimal separator should switch
  instantly. State is preserved.

## 6. Produce a signed release APK (locally)

> **Do not commit your keystore.** `*.jks` and `*.keystore` are in
> `.gitignore`. Keep the file outside the repo (a private password
> manager / vault is the right place).

The Gradle release `signingConfig` reads four environment variables —
exactly the same names the CI job uses — so a local signed build is just:

```bash
export PRICEGRAB_KEYSTORE=/absolute/path/to/pricegrab-release.jks
export PRICEGRAB_KEYSTORE_PASSWORD=...
export PRICEGRAB_KEY_ALIAS=pricegrab
export PRICEGRAB_KEY_PASSWORD=...

cd android
./gradlew :app:assembleRelease
```

If `PRICEGRAB_KEYSTORE` is unset, `:app:assembleRelease` still builds an
unsigned APK; this is intentional so contributors can verify the release
build pipeline without owning the production key.

The APK is written to `android/app/build/outputs/apk/release/`.

## 7. Release to GitHub Releases (via CI)

Production releases are cut automatically by
`.github/workflows/android-ci.yml` on tag push. The full step-by-step
playbook (keystore generation, GitHub secret setup, version bump,
tagging, recovery) lives in [`docs/release.md`](../../docs/release.md).
The short version, once secrets are configured, is:

```bash
# On main, after a release-worthy merge:
git tag -s v0.1.0 -m "Release 0.1.0"
git push origin v0.1.0
```

The `Signed release APK` job decodes `SIGNING_KEYSTORE_BASE64` from
GitHub Actions secrets, exports it as `PRICEGRAB_KEYSTORE`, signs the
APK with the matching `SIGNING_KEYSTORE_PASSWORD`, `SIGNING_KEY_ALIAS`,
and `SIGNING_KEY_PASSWORD`, and attaches the result to the auto-created
GitHub Release.

## 8. F-Droid

The repo already ships the layout F-Droid expects under
[`android/fastlane/metadata/android/{en-US,es-ES}/`](../../android/fastlane/metadata/android/):
title, short description, full description, and per-`versionCode`
changelogs. Phone screenshots still need to be captured on a real or
emulated device — see
[`android/fastlane/metadata/android/en-US/images/phoneScreenshots/README.md`](../../android/fastlane/metadata/android/en-US/images/phoneScreenshots/README.md).

Actual submission to F-Droid is deferred until after v0.1.0 ships on
GitHub Releases. When that time comes, the missing artifact is the
`fdroiddata` build recipe at `metadata/com.mablanco.pricegrab.yml` in
the F-Droid data repo; that work will land as its own feature spec.

## 9. Troubleshooting

| Symptom                                                          | Fix                                                                                                     |
|------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| Gradle sync fails with "Unsupported Java"                        | Ensure `JAVA_HOME` points to JDK 17. Android Studio: *Settings → Build Tools → Gradle → Gradle JDK*.   |
| `connectedCheck` hangs indefinitely                              | Ensure exactly one device is connected: `adb devices`. Cold-boot the emulator if in "offline" state.    |
| Comparison result shows stale values after rotation              | Regression. Verify `SavedStateHandle` is used correctly in `CompareViewModel`; open an issue.           |
| TalkBack does not announce the result                            | Verify the result node has `liveRegion = LiveRegionMode.Polite` and a non-empty `contentDescription`.   |
| `./gradlew test` passes locally but CI fails on coverage         | Add tests for the missing lines in `core/calc/**`; do not lower the 90 % floor to make CI happy.        |
