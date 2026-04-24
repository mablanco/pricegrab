<!--
SYNC IMPACT REPORT
==================
Version history:
  - 1.0.0 (2026-04-24, initial ratification):
      * Ratified five principles (Modern Mobile UX; Accessibility NON-NEGOTIABLE;
        Internationalization ES/EN; Offline-First Performance & Reliability;
        Test-First Quality NON-NEGOTIABLE).
      * Added sections: Privacy & Platform Constraints, Development Workflow &
        Quality Gates, Governance.
  - 1.0.1 (2026-04-24, PATCH — clarification of distribution policy):
      * Privacy & Platform Constraints: removed reference to Google Play's
        target-API window. Added explicit Distribution policy (signed APKs on
        GitHub Releases; F-Droid inclusion as a compatibility target).
      * No principle added, removed, or materially changed.

Templates requiring updates (latest amendment):
  - .specify/templates/plan-template.md     ✅ aligned (Constitution Check section is dynamically
                                              derived from this file at /speckit.plan time)
  - .specify/templates/spec-template.md     ✅ aligned (no principle-specific sections to add)
  - .specify/templates/tasks-template.md    ✅ aligned (test-first guidance already present;
                                              i18n / accessibility surface as cross-cutting tasks)
  - .specify/templates/checklist-template.md ✅ aligned
  - README.md                               ✅ updated (adds Distribution section in ES/EN)

Follow-up TODOs:
  - TODO(PROJECT_SCAFFOLD): Generate the Android project (Kotlin + Jetpack Compose)
    and wire a GitHub Actions workflow that builds, signs, and publishes the APK
    to GitHub Releases on tagged commits.
-->

# PriceGrab Constitution

PriceGrab is an Android application that helps shoppers compare two products with different
prices and quantities and instantly see which one is cheaper per unit. This constitution
defines the non-negotiable principles and quality gates for the native Android
implementation.

## Core Principles

### I. Modern Mobile UX

The product is single-purpose and MUST stay laser-focused on the unit-price comparison flow.

- The primary screen MUST let a user enter Price A, Quantity A, Price B, Quantity B and obtain
  a comparison result without scrolling on a typical phone in portrait orientation.
- UI MUST follow Material Design 3 (Material You) guidelines: dynamic color when available,
  consistent typography scale, elevation, and motion.
- All numeric inputs MUST open the decimal numeric keyboard and accept the locale's decimal
  separator (`,` for `es-ES`, `.` for `en-US`).
- Touch targets MUST be at least 48dp × 48dp. Interactive elements MUST have visible focus
  and pressed states.
- The result MUST be communicated with both text and a non-color cue (icon, position, or
  prefix) so the comparison outcome is obvious without relying on color.
- Empty, partial, and invalid input states MUST display explicit, actionable messages — never
  a silent failure or a generic crash.

**Rationale**: An earlier prototype of this app solved the math but offered a dated UI.
The rewrite's value proposition is a delightful, modern, friction-free comparison.

### II. Accessibility (NON-NEGOTIABLE)

Accessibility is a release blocker, not a polish item.

- Every interactive control MUST expose a meaningful `contentDescription` / semantics label
  for TalkBack.
- The app MUST honor the system font scale up to at least 200% without truncation, overlap,
  or loss of functionality.
- Color contrast for text and meaningful icons MUST meet WCAG 2.1 AA (≥4.5:1 for normal
  text, ≥3:1 for large text and UI components) in both light and dark themes.
- Information conveyed by color (e.g., "A is cheaper") MUST also be conveyed by text and an
  icon or shape.
- Keyboard / D-pad navigation MUST reach every interactive control in a logical order.
- Each release candidate MUST be smoke-tested with TalkBack enabled and with the largest
  system font scale before merging to the release branch.

**Rationale**: A grocery aisle is a stressful, often poorly-lit environment used by people
with diverse abilities; accessibility directly improves the core use case.

### III. Internationalization (ES/EN)

Spanish (`es`) and English (`en`) are first-class, supported on day one.

- ALL user-visible strings MUST live in `res/values/strings.xml` (default = `en`) and
  `res/values-es/strings.xml`. Hardcoded user-facing strings in code or layouts are forbidden.
- Numeric input, parsing, and result formatting MUST be locale-aware (use `NumberFormat` /
  `DecimalFormat` with the current `Locale`, never `Double.parseDouble` on raw user input).
- Pluralization MUST use Android `<plurals>` resources, not string concatenation.
- Adding a new locale MUST require only a new `values-<locale>` directory and translated
  strings — no code changes.
- Both locales MUST be exercised in instrumented tests (at minimum: happy path + decimal
  separator handling).

**Rationale**: The audience is bilingual from inception; baking i18n into the foundation is
far cheaper than retrofitting it.

### IV. Offline-First Performance & Reliability

The core comparison flow MUST work fully offline and feel instantaneous.

- The core feature MUST NOT issue any network request. Any future feature that needs network
  MUST be additive and degrade gracefully when offline.
- Cold start to interactive MUST be ≤ 2 seconds on a mid-tier device (e.g., Pixel 4a class).
- Calculation MUST occur on the main thread only when guaranteed O(1); otherwise off-thread.
  The UI MUST never trigger an ANR.
- The release APK / AAB MUST stay under 15 MB without resorting to obfuscated dependency
  bloat. Justify any dependency added.
- Configuration changes (rotation, theme switch, locale switch) MUST preserve user-entered
  values.

**Rationale**: People use this app inside supermarkets where connectivity is unreliable and
attention windows are short.

### V. Test-First Quality (NON-NEGOTIABLE)

Tests are written before the production code that makes them pass.

- The unit-price calculation logic MUST have unit tests covering: equal price-per-unit,
  A cheaper, B cheaper, zero/negative inputs, very large numbers, and locale-specific
  decimal parsing. Target ≥ 90% line coverage on the calculation module.
- At least one Espresso (or Compose UI) instrumented test MUST cover the end-to-end happy
  path in both `en` and `es`.
- The Red → Green → Refactor cycle is mandatory: a new behavior starts with a failing test.
- CI MUST run unit tests, instrumented tests (on an emulator), lint, and a static analysis
  pass (e.g., detekt / Android Lint) on every pull request. A red build blocks merge.
- Bug fixes MUST be accompanied by a regression test that fails before the fix.

**Rationale**: A wrong "which is cheaper" answer destroys user trust permanently; the math
must be provably correct and stay correct as the codebase evolves.

## Privacy & Platform Constraints

- **No data collection**: The app MUST NOT collect, persist, or transmit personal data,
  analytics, advertising identifiers, or telemetry without an explicit, documented future
  amendment to this constitution.
- **No third-party trackers**: SDKs whose primary function is analytics, advertising, or
  attribution are forbidden.
- **Minimal permissions**: The app MUST request the smallest possible set of Android
  permissions. As of v1.0.0 the core feature requires zero runtime permissions and no
  `INTERNET` permission.
- **Target the latest stable Android SDK** for `targetSdk`. `minSdk` SHOULD cover
  ≥ 95% of active devices and MUST be justified in the plan when raised.
- **Distribution**: releases are published as signed APKs on GitHub Releases. The
  codebase MUST stay compatible with F-Droid inclusion requirements:
  - No proprietary dependencies or Google-Play-only services (no Firebase,
    no Google Mobile Services, no closed-source SDKs).
  - Builds MUST be reproducible from a clean clone with a pinned toolchain
    (fixed Gradle / AGP / Kotlin / SDK versions).
  - No non-free binary blobs in the repository; `gradle-wrapper.jar` is the
    only permitted committed JAR.
- **Localization completeness**: A release MUST NOT ship with untranslated user-visible
  strings in any officially supported locale (`en`, `es`).

## Development Workflow & Quality Gates

- **Spec-Driven Development**: Every non-trivial change MUST flow through the Spec Kit
  pipeline: `/speckit.specify` → `/speckit.clarify` (when ambiguous) → `/speckit.plan` →
  `/speckit.tasks` → `/speckit.implement`.
- **Branch model**: Feature work happens on `###-feature-name` branches created by
  `/speckit.specify`. Direct commits to `main` are forbidden except for hotfixes documented
  in the commit message.
- **Pull request requirements**: Every PR MUST (1) reference its spec, (2) state which
  principles it touches, (3) ship green CI, and (4) include accessibility and i18n
  considerations in its description when UI changes.
- **Versioning**: The app version follows `MAJOR.MINOR.PATCH` (semver):
  - MAJOR: removal/replacement of a user-facing feature, breaking data migration.
  - MINOR: new user-visible feature, new supported locale.
  - PATCH: bug fixes, internal refactors, dependency updates.
- **Definition of Done** for any user-facing change:
  1. Unit + instrumented tests written and passing.
  2. Accessibility checks (TalkBack + max font scale) executed manually.
  3. Both `en` and `es` strings present.
  4. Lint / static analysis clean.
  5. Spec, plan, and tasks documents updated under `specs/<feature>/`.

## Governance

- This constitution supersedes ad-hoc practices, personal preferences, and external
  conventions when they conflict.
- **Amendments** require: (a) a written proposal in a PR that modifies this file,
  (b) a version bump per the rules below, (c) a Sync Impact Report at the top of this file,
  and (d) updates to any dependent template flagged as ⚠ pending in the previous report.
- **Constitution versioning**:
  - MAJOR: backward-incompatible governance change or principle removal/redefinition.
  - MINOR: new principle or section, or materially expanded guidance.
  - PATCH: clarifications, wording, typo fixes, non-semantic refinements.
- **Compliance review**: Reviewers MUST verify principle compliance on every PR. Any
  deliberate deviation MUST be recorded in the plan's "Complexity Tracking" table with
  a justification.
- **Runtime guidance**: Day-to-day agent and developer guidance lives in
  `.cursor/rules/specify-rules.mdc` and (once created) `README.md`. Those documents are
  subordinate to this constitution.

**Version**: 1.0.1 | **Ratified**: 2026-04-24 | **Last Amended**: 2026-04-24
