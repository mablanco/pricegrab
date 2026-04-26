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

Locked in as of v0.1.4 (Mode B switch landed in v0.1.2; v0.1.4 is
the first version F-Droid will actually publish — see §5 chronology
for how we got here from the original v0.1.0 plan):

| Decision | Value | Why |
|----------|-------|-----|
| Distribution model | **F-Droid Mode B — reproducible builds, exclusive developer-signed APK.** F-Droid rebuilds the APK from source, verifies it byte-for-byte against the upstream-signed APK on GitHub Releases, and distributes the upstream binary unchanged. F-Droid never signs PriceGrab with its own key. | Reviewer guidance after the initial submission. Eliminates dual-signature confusion: every channel ships exactly one APK, signed by the upstream developer key. Pinned via `Binaries:` + `AllowedAPKSigningKeys` (see §3). Requires the upstream build to be byte-reproducible — see §4 gotcha 3. |
| Source location | `https://github.com/mablanco/pricegrab.git` | Public repository under the personal `mablanco` GitHub account. |
| License | MIT | F-Droid accepts MIT as a [free license](https://www.gnu.org/licenses/license-list.html). |
| Author identity | `Marco Antonio Blanco <marcoantonio.blanco@protonmail.com>` | Public identity used in the F-Droid listing. ProtonMail address is intentionally separate from any work email. |
| Categories | `Money` (primary). | Closest fit in F-Droid's [category list](https://gitlab.com/fdroid/fdroidserver/-/blob/master/fdroidserver/data/categories.yml); the app helps with everyday purchasing decisions. |
| Anti-features | None expected. | App is offline, has no telemetry, no tracking, no ads, no proprietary deps, no network calls of any kind. |

## 2. Upstream signing-key fingerprint

Every APK published in GitHub Releases (v0.1.0, v0.1.1, v0.1.2,
v0.1.3, v0.1.4, …) is signed with the same certificate. Its SHA-256 is the value pinned in
`AllowedAPKSigningKeys` and must never change without a coordinated
update to the F-Droid recipe:

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
SourceCode: https://github.com/mablanco/pricegrab
IssueTracker: https://github.com/mablanco/pricegrab/issues

AutoName: PriceGrab

RepoType: git
Repo: https://github.com/mablanco/pricegrab.git

Binaries: https://github.com/mablanco/pricegrab/releases/download/v%v/app-release.apk

Builds:
  - versionName: 0.1.4
    versionCode: 5
    commit: ec6c8a0947e8c3213378cf970de6afebf3c51fbc
    subdir: android/app
    gradle:
      - yes

AllowedAPKSigningKeys: 70a9709ce5a4829668d9d50411b959bb90ad2e19d02e2069ad0ff3528181a9fb

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.1.4
CurrentVersionCode: 5
```

**Why these fields.**

- `WebSite:` is **omitted** on purpose. The
  [Build Metadata Reference](https://f-droid.org/en/docs/Build_Metadata_Reference/#WebSite)
  requires `WebSite` to be a *different* URL from `SourceCode` (a
  product page or marketing site, not the repo), and PriceGrab does
  not have one. F-Droid's reviewer enforced this on MR
  [!37136](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/37136)
  by removing the `WebSite:` line directly in the MR; this doc is
  the upstream twin of that change so the next sync stays clean.
  When/if a marketing page lands, set `WebSite:` to that page —
  never to the GitHub repo URL again.
- `Binaries:` points at the upstream-signed APK on GitHub Releases.
  `v%v` is `fdroidserver`'s substitution for `versionName`. In Mode B,
  F-Droid downloads the upstream APK from this URL after rebuilding
  from source and compares the two byte-for-byte; on success it
  publishes the upstream APK (not its rebuild) to keep the developer
  signature intact.
- `AllowedAPKSigningKeys` pins the SHA-256 of the developer signing
  certificate from §2. Combined with `Binaries:`, this is what makes
  the recipe Mode B: F-Droid will never publish an F-Droid-signed
  fallback for this package.
- `Builds[0].commit:` uses the **full 40-character SHA-1** of the
  v0.1.4 commit, not the tag name. F-Droid prefers immutable commit
  hashes over mutable tags; this convention is also what
  `AutoUpdateMode` itself writes when it auto-generates entries for
  future tags, so the style stays consistent across manual and
  auto-generated `Builds` blocks.
- `subdir: android/app` — points to the directory that contains the
  application module's `build.gradle.kts`. F-Droid walks up to the
  Gradle root (`android/`, where `settings.gradle.kts` and `gradlew`
  live) automatically. Confirmed working against fdroidserver in MR
  [!37136](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/37136).
- `gradle: [yes]` — uses the default release variant; we have no
  product flavours.
- `AutoUpdateMode: Version` together with `UpdateCheckMode: Tags`
  means: every time a new `vX.Y.Z` tag lands in the upstream repo,
  fdroidserver picks it up automatically and adds a fresh `Builds`
  entry — no manual MR per release. The schema only accepts `None`,
  `Version`, or `Version +<suffix>`; older docs that mention the
  `v%v` placeholder are obsolete.
- The store metadata (title, descriptions, screenshots, changelogs,
  **store icon**) is **not** restated here. F-Droid auto-discovers it
  from `fastlane/metadata/android/{en-US,es-ES}/` at the repo root,
  which is exactly where this repo keeps it. The 512×512 store icon
  lives at `…/<locale>/images/icon.png` and is regenerated
  deterministically by `branding/regenerate-icons.py` from the same
  `branding/icon-source.png` that produces the launcher mipmaps —
  one source of truth for both the in-app icon and the F-Droid list
  thumbnail. (Required by F-Droid review on MR !37136, 2026-04-26.)
- v0.1.0 through v0.1.3 are intentionally **absent** from
  `Builds:`. v0.1.0/v0.1.1 were initially submitted as `disable:`d
  entries (with inline notes on why each was unbuildable /
  non-reproducible), but the F-Droid reviewer asked us to drop them:
  F-Droid only wants to track versions it can actually publish. v0.1.2
  was buildable and reproducible (it's the version where Mode B
  landed), but we never shipped its `Builds:` entry to F-Droid — we
  jumped straight from "submission opened" to v0.1.3 because v0.1.3
  is the first version with the final launcher icon. v0.1.3 itself
  was on track to become the first published version (see §5
  chronology step 6) until v0.1.4 landed during review with the
  Reset/Undo feature; we then bumped the recipe one more notch so the
  first F-Droid publication includes that feature too. Existing
  GitHub-Releases users (v0.1.0 through v0.1.3) keep updating via
  F-Droid once it lists v0.1.4, because `AllowedAPKSigningKeys`
  accepts the same upstream key, so no upgrade path is broken.

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

3. **AGP `vcsInfo` embedding is non-reproducible** and must be
   disabled in `release` builds for Mode B to work. AGP 8.3+ writes
   the local git revision and project path into
   `META-INF/version-control-info.textproto` by default. Two
   checkouts of the same source on different machines / paths
   necessarily produce different bytes there, so the upstream-signed
   APK and F-Droid's rebuild can never match byte-for-byte. We turn
   this off in `app/build.gradle.kts`:

   ```kotlin
   release {
       // ...
       vcsInfo.include = false
   }
   ```

   Landed in v0.1.2. Re-enabling `vcsInfo` in the future would mean
   abandoning Mode B and falling back to F-Droid signing the APK with
   its own key.

If any of these three invariants ever has to be relaxed, document
the reason and the workaround here so we never re-debug it from
scratch.

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

### Current submission state

| Item | Value |
|------|-------|
| Merge Request | [`fdroid/fdroiddata!37136`](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/37136) |
| Submitting branch | `add-com.mablanco.pricegrab` on [`mabnavarrete/fdroiddata`](https://gitlab.com/mabnavarrete/fdroiddata) |
| Distribution model | Mode B (reproducible builds, exclusive developer-signed APK) |
| First buildable / publishable tag | `v0.1.4` (versionCode 5), pinned by full SHA `ec6c8a0947e8c3213378cf970de6afebf3c51fbc` |
| Status | Iterating on reviewer feedback; awaiting next CI pipeline run |

What it took to get to the current state, in chronological order:

1. `schema validation` rejected `AutoUpdateMode: Version v%v` —
   modern fdroidserver only accepts `None`, `Version`, or
   `Version +<suffix>`. Fixed by switching to `AutoUpdateMode: Version`.
2. `fdroid build` rejected the unconditional `signingConfigs.getByName("release")`
   call in `app/build.gradle.kts`. Fixed in [PR #12](https://github.com/mablanco/pricegrab/pull/12)
   for the v0.1.1 tag (see §4 gotcha 1).
3. `fdroid rewritemeta` rejected an over-long `disable:` value on the
   then-still-present v0.1.0 entry; fixed by shortening the note. Now
   obsolete: there are no `disable:` entries left after step 5 below.
4. **Mode A → Mode B switch** after the reviewer asked us to add
   `Binaries:` and `AllowedAPKSigningKeys` so F-Droid can publish the
   upstream-signed APK rather than its own. v0.1.2 added with
   `vcsInfo.include = false` (see §4 gotcha 3) so the upstream build
   becomes byte-reproducible. Landed in
   [PR #14](https://github.com/mablanco/pricegrab/pull/14).
5. **Reviewer asked to drop v0.1.0 / v0.1.1 entries** entirely (no
   value in tracking versions F-Droid will never publish) and to
   replace `commit: v0.1.2` with the full SHA-1 of that tag. Both
   applied directly in the GitLab fork.
6. **Bumped target tag from v0.1.2 to v0.1.3** so the first version
   F-Droid actually publishes is the one with the final launcher
   icon. Same Mode B story (reproducible, signed with the same
   upstream key); only the `Builds:` block, `CurrentVersion` and
   `CurrentVersionCode` move forward by one. Landed in
   [PR #17](https://github.com/mablanco/pricegrab/pull/17).
7. **Bumped target tag from v0.1.3 to v0.1.4** while the upstream MR
   was still in review, so the Reset/Undo feature
   ([PR #20](https://github.com/mablanco/pricegrab/pull/20))
   ships in the first F-Droid publication too. Same Mode B contract
   — toolchain, signing key and reproducibility constraints are
   unchanged from step 6 — so the upstream MR edit is a single bump
   of `Builds[0]` (commit, versionName, versionCode), `CurrentVersion`
   and `CurrentVersionCode`, with no need to reopen earlier reviewer
   feedback. Recipe sync landed in
   [PR #21](https://github.com/mablanco/pricegrab/pull/21).
8. **Reviewer feedback on the v0.1.4 review pass** (2026-04-26):
   (a) `WebSite:` had to be removed — F-Droid's
   [Build Metadata Reference](https://f-droid.org/en/docs/Build_Metadata_Reference/#WebSite)
   only allows `WebSite:` when it is **distinct** from `SourceCode:`,
   and PriceGrab has no marketing page yet. Reviewer applied the
   removal directly in the MR; this repo's twin landed in
   [PR #24](https://github.com/mablanco/pricegrab/pull/24).
   (b) The fastlane tree was missing the 512×512 store icon at
   `fastlane/metadata/android/<locale>/images/icon.png`. Generated
   deterministically from `branding/icon-source.png` by the existing
   `branding/regenerate-icons.py` pipeline (extended in the same PR
   to also emit the fastlane icon, so the launcher mipmaps and the
   fastlane store icon stay in lock-step from a single source of
   truth). No tag bump needed: the fastlane tree is metadata only,
   not part of the APK, so reproducibility for v0.1.4 is unaffected.

The actual byte-for-byte reproducibility verification happens *after*
this MR merges, on F-Droid's main build farm via `fdroid publish`. If
verification fails there, the iteration loop is: read the
`diffoscope` output, fix the upstream code, ship `v0.1.3+`, and
update the recipe's `Builds:` block with the new SHA. The
`AllowedAPKSigningKeys` value does not need to change as long as the
upstream signing key is the same.

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
- [Reproducible Builds](https://f-droid.org/docs/Reproducible_Builds/) — the framework that defines Mode B; required reading for anyone debugging an `fdroid publish` verification failure.
- [`fdroid/fdroiddata` repository](https://gitlab.com/fdroid/fdroiddata)
- [`fdroid/issuebot` repository](https://gitlab.com/fdroid/issuebot)
