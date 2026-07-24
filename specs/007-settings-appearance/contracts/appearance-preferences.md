# Contract: Appearance preferences & Settings UI

## Preference keys (DataStore)

| Key | Type | Default | Maps to |
|-----|------|---------|---------|
| `theme_mode` | `String` | `"system"` | `AppearanceMode` |
| `material_you_enabled` | `Boolean` | `false` | Material You toggle |

File name suggestion: `appearance_preferences`.

### Read path

```text
Flow<AppearancePreferences>
  missing theme_mode     → System
  invalid theme_mode     → System
  missing material_you_* → false
```

### Write path

```text
setMode(AppearanceMode)           // persists immediately
setMaterialYouEnabled(Boolean)    // persists immediately
```

Writes MUST be durable across force-stop and reboot (FR-011).

## Theme resolution

```text
resolve(prefs, systemDark, sdkInt) → (darkTheme, useDynamicColor)

useDynamicColor ≡ prefs.materialYouEnabled ∧ sdkInt ≥ 31
```

`PriceGrabTheme(darkTheme, useDynamicColor)`:

| `useDynamicColor` | Scheme |
|-------------------|--------|
| `false` | Brand light/dark schemes (feature 003) |
| `true` | `dynamicLightColorScheme` / `dynamicDarkColorScheme` for current context |

Changing prefs MUST recompose the whole app without process restart
(FR-005, FR-008).

## Settings UI contract

### Entry / exit

| Action | Result |
|--------|--------|
| Compare → Settings affordance | `AppScreen.Settings`; Compare inputs retained |
| Settings → Back (UI or system) | `AppScreen.Compare`; same inputs + result |
| Cold launch | Always `Compare` as first screen |

### Test tags (suggested)

| Control | Tag |
|---------|-----|
| Settings entry (Compare top bar) | `settings_open` |
| Settings screen root | `settings_screen` |
| Theme System | `theme_system` |
| Theme Light | `theme_light` |
| Theme Dark | `theme_dark` |
| Material You switch | `material_you_switch` |

### Semantics

- Each theme option announces name + selected/unselected.
- Material You announces name + on/off, or unavailable when disabled.
- All labels from `strings.xml` / `values-es` (FR-013, FR-014).

### Material You unavailable (API &lt; 31)

```text
material_you_switch.enabled = false
supporting / helper text = localized “unavailable on this device” (or equivalent)
brand palette remains in use
user cannot turn the switch on
```

Stored `material_you_enabled == true` on API &lt; 31 still yields brand
palette at runtime (`useDynamicColor = false`).

### Accessibility / layout

- Touch targets ≥ 48×48 dp.
- At `fontScale = 2.0`, Settings remains scrollable; no truncated primary
  labels or overlapping controls (FR-015).
- Contrast: brand AA in light/dark; when dynamic on, manual check on a
  reference device (SC-008).

## Non-goals

- Syncing preferences to cloud or backup policy beyond OS defaults.
- Per-screen theme overrides.
- Changing brand seed color as part of this contract.
