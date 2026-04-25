# F-Droid submission playbook

This document is the canonical place for everything related to listing
PriceGrab on [F-Droid](https://f-droid.org/). It contains the
ready-to-copy build recipe, the upstream signing-key fingerprint, and
the GitLab merge-request workflow. Keep it in sync whenever the build
changes (Gradle plugin bumps, signing-key rotations, repo layout
changes, …).

The release-cutting playbook lives in [`release.md`](./release.md).
This file picks up *after* a tag has shipped on GitHub Releases.

## 1. One-time decisions

Locked in for v0.1.0:

| Decision | Value | Why |
|----------|-------|-----|
| Distribution model | F-Droid builds and signs the APK with its own key, AND `AllowedAPKSigningKeys` pins our upstream key. | Users who already installed the upstream APK from GitHub Releases can keep updating from F-Droid without uninstall/reinstall, because either signature is accepted. Reproducible builds is a longer rabbit hole we deferred. |
| Source location | `https://github.com/mablanco/pricegrab.git` | Public repository under the personal `mablanco` GitHub account. |
| License | MIT | F-Droid accepts MIT as a [free license](https://www.gnu.org/licenses/license-list.html). |
| Author identity | `Marco Antonio Blanco <marcoantonio.blanco@protonmail.com>` | Public identity used in the F-Droid listing. ProtonMail address is intentionally separate from any work email. |
| Categories | `Money` (primary). | Closest fit in F-Droid's [category list](https://gitlab.com/fdroid/fdroidserver/-/blob/master/fdroidserver/data/categories.yml); the app helps with everyday purchasing decisions. |
| Anti-features | None expected. | App is offline, has no telemetry, no tracking, no ads, no proprietary deps, no network calls of any kind. |

## 2. Upstream signing-key fingerprint

The v0.1.0 APK published in GitHub Releases is signed with this
certificate:

```text
SHA-256 (cert) = 70:a9:70:9c:e5:a4:82:96:68:d9:d5:04:11:b9:59:bb:90:ad:2e:19:d0:2e:20:69:ad:0f:f3:52:81:81:a9:fb
```

For F-Droid the fingerprint must be lowercase and **without** the colon
separators, exactly as it appears in the YAML below:

```text
70a9709ce5a4829668d9d50411b959bb90ad2e19d02e2069ad0ff3528181a9fb
```

If you ever need to re-extract this value (for example after a
key rotation), the canonical command is:

```bash
APK=/path/to/PriceGrab-<versionName>.apk
$ANDROID_HOME/build-tools/<latest>/apksigner verify --print-certs "$APK" \
  | grep -i 'SHA-256.*certificate' \
  | sed 's/[^0-9a-f]//gi' | tr 'A-F' 'a-f'
```

> Never overwrite or rotate this key without first opening a
> coordinated MR against `fdroiddata` to update
> `AllowedAPKSigningKeys`. A signature mismatch on an already-shipped
> package is rejected by every device that installed an earlier
> version, *and* by F-Droid's buildserver.

## 3. Build recipe (`metadata/com.mablanco.pricegrab.yml`)

This is the file that will live in
[`fdroiddata`](https://gitlab.com/fdroid/fdroiddata) under
`metadata/com.mablanco.pricegrab.yml`. Copy verbatim into the GitLab
fork:

```yaml
Categories:
  - Money
License: MIT
AuthorName: Marco Antonio Blanco
AuthorEmail: marcoantonio.blanco@protonmail.com
WebSite: https://github.com/mablanco/pricegrab
SourceCode: https://github.com/mablanco/pricegrab
IssueTracker: https://github.com/mablanco/pricegrab/issues

AutoName: PriceGrab

RepoType: git
Repo: https://github.com/mablanco/pricegrab.git

Builds:
  - versionName: 0.1.0
    versionCode: 1
    disable: signingConfigs.getByName("release") was unconditional and crashed F-Droid's build (which strips signingConfigs); fixed from v0.1.1 onwards.
    commit: v0.1.0
    subdir: android/app
    gradle:
      - yes

  - versionName: 0.1.1
    versionCode: 2
    commit: v0.1.1
    subdir: android/app
    gradle:
      - yes

AllowedAPKSigningKeys: 70a9709ce5a4829668d9d50411b959bb90ad2e19d02e2069ad0ff3528181a9fb

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.1.1
CurrentVersionCode: 2
```

**Why these fields.**

- `subdir: android/app` — points to the directory that contains the
  application module's `build.gradle.kts`. F-Droid walks up to the
  Gradle root (`android/`, where `settings.gradle.kts` and `gradlew`
  live) automatically. Confirmed working against fdroidserver in MR
  [!37136](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/37136).
- `gradle: [yes]` — uses the default release variant; we have no
  product flavours.
- `disable:` on the v0.1.0 entry preserves the historical fact that
  v0.1.0 was published upstream while making sure F-Droid's
  buildserver doesn't keep re-attempting a known-broken build. New
  installs from F-Droid start at v0.1.1.
- `AllowedAPKSigningKeys` pinning is what lets users with the upstream
  GitHub APK keep updating from F-Droid without an uninstall.
- `AutoUpdateMode: Version` together with `UpdateCheckMode: Tags`
  means: every time a new `vX.Y.Z` tag lands in the upstream repo,
  fdroidserver picks it up automatically and adds a fresh `Builds`
  entry — no manual MR per release. The schema only accepts `None`,
  `Version`, or `Version +<suffix>`; older docs that mention the
  `v%v` placeholder are obsolete.
- The store metadata (title, descriptions, screenshots, changelogs)
  is **not** restated here. F-Droid auto-discovers it from
  `fastlane/metadata/android/{en-US,es-ES}/` at the repo root, which
  is exactly where this repo keeps it.

## 4. F-Droid build-server gotchas the upstream code must respect

The F-Droid buildserver applies *automatic, undocumented* patches to
the source tree before invoking Gradle. Two of them have already
bitten this project; future contributors must keep the upstream code
resilient to both:

1. **`signingConfigs { create("release") { … } }` is stripped.** Any
   reference downstream (typically inside `buildTypes.release`) must
   handle the case where the named signing config does not exist:

   ```kotlin
   val releaseSigningConfig = signingConfigs.findByName("release")
   if (releaseSigningConfig?.storeFile != null) {
       signingConfig = releaseSigningConfig
   }
   ```

   Using `signingConfigs.getByName("release")` will throw on F-Droid
   because the lookup happens after the strip.

2. **`gradle/wrapper/gradle-wrapper.jar` is removed** and replaced by
   F-Droid's own `gradlew-fdroid` wrapper, which downloads a fresh
   gradle distribution per the `distributionUrl` in
   `gradle-wrapper.properties`. Anything that hashes or executes the
   committed `gradle-wrapper.jar` (custom CI checks, build-script
   plugins) must be skippable when the file is absent.

If either invariant ever has to be relaxed, document the reason and
the workaround here so we never re-debug it from scratch.

### If `issuebot` complains about `subdir: android/app`

For now `android/app` works. If a future restructure breaks it, the
fall-back layouts to try are, in order:

1. `subdir: android` — run gradle from the gradle root and let it
   assemble all subprojects (we only have `:app`, so this is fine).
2. Restructure the upstream repo so the gradle root is at the
   repository root (no `android/` wrapper). This is intrusive and
   would touch every CI/script reference.

Document whichever option ends up landing in `fdroiddata` so the next
maintainer doesn't have to re-derive it.

## 5. Submission workflow

1. **Create a GitLab account** (free, required: F-Droid only accepts
   MRs through `gitlab.com/fdroid/fdroiddata`, not GitHub PRs or
   email). New accounts must complete identity verification before
   any pipeline runs, otherwise the first push will look like it
   succeeded but `issuebot` will never trigger.
2. **Fork** [`fdroid/fdroiddata`](https://gitlab.com/fdroid/fdroiddata)
   into your personal GitLab namespace.
3. **Create a branch** in the fork named after the package:
   `add-com.mablanco.pricegrab`.
4. **Add the file** `metadata/com.mablanco.pricegrab.yml` with the
   exact contents from §3.
5. **Commit** with the conventional title F-Droid uses:
   `New app: com.mablanco.pricegrab`.
6. **Push** and **open a Merge Request** from the fork against
   `fdroid/fdroiddata`'s `master` branch.
7. Wait for [`issuebot`](https://gitlab.com/fdroid/issuebot) to run
   automated checks (`fdroid lint`, `fdroid rewritemeta`, `schema
   validation`, `fdroid build`, `check apk`, …). Iterate on the YAML
   and the upstream code until it passes.
8. A human F-Droid maintainer reviews and merges. **First-time
   submissions can take days to weeks**; that is expected and not a
   sign anything is wrong.

We deliberately skip running the local Docker validation step
(`fdroid build`, `fdroid lint`) — `issuebot` runs the same checks in
CI, and the local Docker images are large and slow to set up. If a
specific MR keeps failing CI for a non-obvious reason, that is when
running the local toolchain pays off.

## 6. Ongoing release-time obligations

After every new tag (`vX.Y.Z`) lands on `main`:

1. Make sure
   [`fastlane/metadata/android/{en-US,es-ES}/changelogs/<versionCode>.txt`](../fastlane/metadata/android/en-US/changelogs)
   exists for the new build, with at most ~500 characters of plain
   text per locale.
2. Push the tag from `main`. The release workflow attaches the signed
   APK to GitHub Releases.
3. Verify `apksigner verify --print-certs` against the published APK
   and confirm the SHA-256 matches §2. If it does **not**, stop and
   open a coordination MR before the next F-Droid build cycle.
4. Do nothing else. With `AutoUpdateMode: Version` and
   `UpdateCheckMode: Tags`, F-Droid's bot picks the release up
   automatically within ~24 hours.

## 7. References

- [F-Droid Inclusion Policy](https://f-droid.org/docs/Inclusion_Policy/)
- [Build Metadata Reference](https://f-droid.org/docs/Build_Metadata_Reference/)
- [Submitting to F-Droid Quick Start Guide](https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/)
- [Reproducible Builds](https://f-droid.org/docs/Reproducible_Builds/) — possible follow-up once the project stabilises.
- [`fdroid/fdroiddata` repository](https://gitlab.com/fdroid/fdroiddata)
- [`fdroid/issuebot` repository](https://gitlab.com/fdroid/issuebot)
