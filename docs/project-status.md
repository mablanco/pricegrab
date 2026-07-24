# PriceGrab — project status & backlog

> Canonical agent/project memory for “where we are” and “what’s next”.
> Keep this file current when shipping a release or parking a deferred idea.
> Last updated: **2026-07-24** (feature **007** planning — Settings appearance).

## Snapshot

| Field | Value |
|-------|--------|
| App version | **0.1.9** (`versionCode` **10**) |
| Latest tag / Release | Tag **`v0.1.9`** after manual QA (prep on `main`; previous: [`v0.1.8`](https://github.com/mablanco/pricegrab/releases/tag/v0.1.8)) |
| Active Spec Kit feature pointer | `.specify/feature.json` → `specs/007-settings-appearance` (planning / in progress) |
| Planned next release (007) | **0.1.10** (stay on 0.1.x; **not** 0.2.0). Do not bump version until release-prep. |
| Distribution | **F-Droid Mode B** + **GitHub Releases**; **not** on Google Play |
| F-Droid package | https://f-droid.org/packages/com.mablanco.pricegrab/ (first publish: **v0.1.5**) |
| Owner | Marco Antonio Blanco — chat in Spanish; engineering artifacts in English |
| Repo path | `~/Repos/PriceGrab/pricegrab` (git root). Parent `~/Repos/PriceGrab` holds keystore tooling + source art only. |

## Shipped features

| # | Slug | One-liner | Tag |
|---|------|-----------|-----|
| 001 | `unit-price-comparison` | Two-offer unit-price compare → cheaper + savings | v0.1.0+ |
| 002 | `reset-comparison` | Reset clears form; Snackbar Undo ~10s | v0.1.4 |
| 003 | `visual-polish-branding` | Brandmark + steel-blue M3 (`#2F5C73`); hero result; no dynamic color | v0.1.5 |
| 004 | `quantity-units` | Units `{g,kg,ml,L,pcs}`; same-dimension only; savings per kg/L/piece | v0.1.7 |
| 005 | `multi-offer-compare` | Up to 3 offers (`+`/`−`); cheapest wins; savings **vs second-cheapest** (named in UI) | v0.1.8 |
| 006 | `compact-offer-row` | Single-row offer inputs; adaptive two-row at large font (200%) | v0.1.9 |

### versionCode map

| Code | Name | Notes |
|------|------|--------|
| 1–5 | 0.1.0–0.1.4 | MVP → Reset/Undo |
| 6 | 0.1.5 | Visual polish; **first F-Droid** |
| 7 | 0.1.6 | Icon safe-zone scale |
| 8 | 0.1.7 | Quantity units; **fdroiddata `disable: not repro`** |
| 9 | 0.1.8 | Three offers + icon optical fix (QA) |
| 10 | 0.1.9 | Compact offer row + adaptive large-font layout |

## Spec Kit cadence

Global PR letter ledger lives in `specs/001-unit-price-comparison/tasks.md`.

| Feature | Planning | Implementation | Release cut |
|---------|----------|----------------|-------------|
| 002 | L | M | N → v0.1.4 |
| 003 | O | P | Q → v0.1.5 |
| 004 | R | S | T → v0.1.7 |
| 005 | U | V | W → v0.1.8 |
| 006 | X | Y | Z → v0.1.9 |
| 007 | AA | AB | AC → v0.1.10 (planned) |

Next free letter after **AC**. Pattern: planning PR → impl PR → chore release-prep (version, changelogs, `docs/fdroid.md`, icons) → **tag from `main` after manual QA**.

**Version policy cue for agents**: Stay on **0.1.x** for 007. When a later
feature would warrant a **MINOR** bump (0.2.0+), **prompt Marco** before
choosing the version — do not invent a minor bump.

Release playbook: `docs/release.md`. F-Droid playbook: `docs/fdroid.md`.

## Icon pipeline (current)

`branding/regenerate-icons.py` ← `branding/icon-source.png` → mipmaps + fastlane icons.

| Constant | Value (post v0.1.8 QA #43; unchanged in v0.1.9) |
|----------|----------------------------|
| `ART_SCALE` | `0.72` |
| `ART_OFFSET_X_PX` | `-17` |
| `ART_OFFSET_Y_PX` | `-45` |

History (short): 0.86 → 0.82 → 0.76 → 0.72 scale; Y nudge planned ~−95 sat too high → QA settled −45. Always verify under **circular** launcher masks. Prefer bundling icon tweaks in release-prep, not micro-releases.

## Open / operational

1. **F-Droid v0.1.7 `not repro`** — fdroiddata marks build disabled. **Do not re-tag 0.1.7.** Recovery path is clean **v0.1.8** / **v0.1.9** auto-update (`UpdateCheckMode: Tags`). If Mode B fails, investigate with `diffoscope` (see `docs/fdroid.md`).
2. **Constitution header** still has obsolete `TODO(PROJECT_SCAFFOLD)` (Android + GHA already shipped) — housekeeping only; do not invent new scaffold work.
3. **GitHub Issues** — empty as of 2026-07-19; backlog lives here + Out of Scope sections in specs.
4. **Tag `v0.1.9`** — only after manual QA of compact row + 200% font (see `specs/006-compact-offer-row/quickstart.md`).

## Product backlog (deferred)

Captured from `specs/*/spec.md` and `research.md` Out of Scope / deferred notes. Not scheduled; pick via a new Spec Kit feature when Marco prioritizes.

### Comparison & units
- **4+ offers** — hard cap is 3; more needs list chrome / scroll redesign (`005`).
- **Density conversion (g↔ml)** — never guess; keep incompatible-units error (`004`/`005`).
- **US / custom units** (oz, fl oz, lb, …) — later i18n/units feature (`004`).
- **Currency symbol / FX** — bare decimals only (`001`/`004`).
- **“How many will you buy?” N-input** — purchase-qty framing deferred (`001`).
- **Remember last selected unit** — only if Marco asks (`004`).

### Persistence & accounts
- No on-disk history of past comparisons, no accounts, no cloud sync (`001` data-model).

### UX / branding polish
- Android 12+ **SplashScreen** branding (`003`).
- ~~**Settings** screen / theme override / Material You toggle~~ — **in progress** as feature **007** (`specs/007-settings-appearance`; PR cadence AA → AB → AC → planned **v0.1.10**).
- **Custom font** (APK/cold-start risk) (`003`).
- Extra **motion / Lottie** beyond M3 defaults (`003`).
- **Landscape / tablet / foldable** redesign (portrait-first today) (`003`).
- Formal **STYLE.md** brand governance (inline docs + icon script enough for now) (`003`).
- **WCAG contrast in CI** when tooling matures (`003`).

### Reset / undo
- OS “confirm destructive actions” dialog before Reset (`002` — Undo is the safety net).
- Per-field clear affordance (`002`).
- Undo across process death; multi-level undo stack (`002`).

### Distribution policy (constitution)
- **Google Play** — out unless constitution amended.
- **Network / analytics / trackers** — forbidden without constitution amendment.
- Signing key rotation requires coordinating `AllowedAPKSigningKeys` with F-Droid first.

### Ideas (discussed, not yet Spec Kit features)

Parked from post-v0.1.8 planning chat. Still need `/speckit.specify` before
implementation; none are scheduled.

- **Favorites / templates** — saved presets (e.g. “milk 1 L”, “rice 1 kg”) that
  prefill price + quantity + unit.
- **Copy result to clipboard** — shareable plain-text summary (e.g. WhatsApp).
- **Home-screen widget or shortcut** — open straight into an empty comparison.
- **Pack vs loose mode** — pack price / N pieces vs unit price (buy-N variant).
- **Free-text offer labels** — shopper names (“Mercadona”, “Carrefour”) instead
  of only Offer A/B/C.
- **Richer result card** — make both absolute savings and percent more visible
  in the hero (percent is partly a11y-only today).
- **Export / share** — share sheet for text or a result screenshot.

## Authoritative sources (priority)

1. `.specify/memory/constitution.md`
2. `.cursor/rules/project-conventions.mdc`
3. This file (`docs/project-status.md`) + `docs/release.md` + `docs/fdroid.md`
4. Active feature under `specs/<nnn-slug>/` (see `.specify/feature.json`)
5. PR letter ledger: `specs/001-unit-price-comparison/tasks.md`
6. Icon constants: `branding/regenerate-icons.py`

## Agent operating reminders

- Never push/merge `main`; only Marco merges PRs.
- Feature branches `<type>/<short-slug>`; Conventional Commits in **English**.
- Chat with Marco in **Spanish** by default; specs/plans/commits/code comments in **English**.
- User-facing strings and store metadata: **ES + EN**.
- Never overwrite a tag that produced an installable APK; bump version instead.
- Before proposing a **MINOR** SemVer bump (0.2.0+), **ask Marco**; 007 release stays **0.1.10**.
