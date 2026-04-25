# Cutting a PriceGrab release

PriceGrab releases are signed APKs published as
[GitHub Releases](https://github.com/mablanco/pricegrab/releases). The
release pipeline lives in
[`.github/workflows/android-ci.yml`](../.github/workflows/android-ci.yml)
and is triggered by pushing a `v*` tag to `main`.

This document covers the **one-time** keystore + secrets setup, plus the
**per-release** checklist.

---

## 1. Generate the upload keystore (one-time)

The keystore signs every release. Once it exists, **never lose it**:
losing the key means future updates cannot be installed on top of an
existing install. Treat the keystore file and its passwords like a
production secret.

```bash
keytool -genkeypair -v \
  -storetype PKCS12 \
  -keystore pricegrab-release.jks \
  -alias pricegrab \
  -keyalg RSA -keysize 4096 \
  -validity 10000 \
  -dname "CN=PriceGrab, O=Marco Antonio Blanco, C=ES"
```

Pick a long passphrase and save it in a password manager.

> **PKCS12 only uses one password.** A keystore conceptually has two
> passwords — the **store password**, which protects the file itself,
> and the **key password**, which protects each individual entry inside.
> The legacy JKS format allowed them to differ; PKCS12, the modern
> default, encrypts the container and the keys with the same secret.
> When `keytool` prompts twice, give the same value both times. We still
> upload it as two separate GitHub secrets in §2 so that the Gradle
> signing config keeps a clean separation if we ever migrate to a format
> that supports two distinct passwords.

> Keep the resulting `pricegrab-release.jks` outside the repo. The
> repo's `.gitignore` already blocks `*.jks` and `*.keystore`, but the
> safest place is a private vault.

## 2. Upload the keystore as a GitHub secret (one-time)

Encode the keystore as base64 so it survives the secret editor:

```bash
base64 -w0 pricegrab-release.jks > pricegrab-release.jks.b64
```

In **Settings → Secrets and variables → Actions → New repository
secret**, create:

| Secret name                    | Value                                                            |
|--------------------------------|------------------------------------------------------------------|
| `SIGNING_KEYSTORE_BASE64`      | The contents of `pricegrab-release.jks.b64`.                     |
| `SIGNING_KEYSTORE_PASSWORD`    | The passphrase chosen in step 1.                                 |
| `SIGNING_KEY_ALIAS`            | `pricegrab` (or whatever alias was used in step 1).              |
| `SIGNING_KEY_PASSWORD`         | The same passphrase as `SIGNING_KEYSTORE_PASSWORD` (PKCS12).     |

The CI job in `android-ci.yml` decodes `SIGNING_KEYSTORE_BASE64` to a
temporary `release.jks` and exports `PRICEGRAB_KEYSTORE`,
`PRICEGRAB_KEYSTORE_PASSWORD`, `PRICEGRAB_KEY_ALIAS`, and
`PRICEGRAB_KEY_PASSWORD` — those are the four env vars that
[`android/app/build.gradle.kts`](../android/app/build.gradle.kts) reads
inside its `signingConfigs.release { … }` block.

## 3. Per-release checklist

For every public release:

1. **Bump the version** in `android/app/build.gradle.kts`:
   - `versionCode` — strictly increasing integer (1, 2, 3, …).
   - `versionName` — semver string visible to users (`0.1.0`).
2. **Add a changelog** for the new `versionCode` under
   [`fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`](../fastlane/metadata/android/en-US/changelogs)
   and the matching `es-ES/changelogs/<versionCode>.txt`. Keep each
   under **500 characters**; F-Droid truncates beyond that.
3. **Open a release PR** with the version + changelog edits. Wait for
   CI to go green and merge it into `main`.
4. **Tag** the merge commit on `main` and push:

   ```bash
   git checkout main
   git pull --ff-only
   git tag -s v0.1.0 -m "Release 0.1.0"
   git push origin v0.1.0
   ```

5. The `Signed release APK` job in `android-ci.yml` will:
   - Decode the keystore from `SIGNING_KEYSTORE_BASE64`.
   - Run `:app:assembleRelease` with the four `PRICEGRAB_*` env vars.
   - Attach the resulting APK to a new GitHub Release at the tag.

   Pre-release tags (`v0.1.0-rc1`, `v0.1.0-beta1`, `v0.1.0-alpha1`)
   are automatically marked as pre-releases by the workflow.

6. Once the Release page exists, smoke-test the published APK:
   `adb install pricegrab-v0.1.0.apk` on a device that has not had the
   debug build installed (or uninstall the debug build first — debug
   and release are signed differently so they cannot be replaced
   in-place). Verify the version shown in *Settings → Apps → PriceGrab*.

## 4. Recovering from a misfire

If the release CI fails after the tag is already pushed, the safest path
is:

1. Delete the **release** on GitHub (not the tag, unless you also need
   to re-cut the tag from a different commit).
2. Fix the underlying issue on `main` via a normal PR.
3. Re-tag with the **same** `versionCode`/`versionName` only if no APK
   ever made it to a real user; otherwise bump and tag again.

> Never overwrite a tag that has already produced an installable APK.
> F-Droid and any device that fetched the original APK will reject the
> overwritten one as untrusted.

## 5. F-Droid

The submission playbook lives in its own document:
[`docs/fdroid.md`](./fdroid.md). It contains the certificate
fingerprint, the ready-to-copy `metadata/com.mablanco.pricegrab.yml`
recipe, and the GitLab merge-request workflow. From the release
side, the only ongoing obligation is to keep
[`fastlane/metadata/android/{en-US,es-ES}/changelogs/<versionCode>.txt`](../fastlane/metadata/android/en-US/changelogs)
up to date and to **never** rotate the upstream signing key without
coordinating an `AllowedAPKSigningKeys` update with F-Droid first
(the buildserver will reject any APK signed by an unlisted key).
