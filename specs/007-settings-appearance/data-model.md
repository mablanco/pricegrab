# Data Model: Settings Appearance (Feature 007)

Domain compare entities (`Offer`, `QuantityUnit`, `OfferSlotState`,
`ComparisonOutcome`, etc.) are **unchanged**. This feature adds local
appearance preferences and a Settings presentation surface.

See [contracts/appearance-preferences.md](./contracts/appearance-preferences.md)
for behavioural contracts.

## `AppearanceMode`

User-selected appearance mode. Persisted as a string key.

```text
enum AppearanceMode {
  System   // follow device light/dark (DEFAULT)
  Light    // force light ColorScheme
  Dark     // force dark ColorScheme
}
```

| Value | Stored string | Effective `darkTheme` |
|-------|---------------|------------------------|
| `System` | `"system"` | `isSystemInDarkTheme()` |
| `Light` | `"light"` | `false` |
| `Dark` | `"dark"` | `true` |

**Validation**: Unknown / missing stored value → `System`.

## `AppearancePreferences`

Aggregate of user appearance choices. Single source of truth for theme
wiring.

| Field | Type | Default (fresh install) | Notes |
|-------|------|-------------------------|-------|
| `mode` | `AppearanceMode` | `System` | FR-003 |
| `materialYouEnabled` | `Boolean` | `false` | FR-007 |

**Persistence**: DataStore Preferences keys (see contract). Local only;
no sync; not personal data.

**Invariants**:

1. Defaults match pre-007 behaviour: system appearance + brand palette.
2. `materialYouEnabled` may be `true` on API &lt; 31; **runtime** still
   uses brand palette and Settings shows unavailable / disabled control.
3. Changing either field must not clear Compare form state.

## Derived: `ResolvedAppearance`

Computed at composition (or in a pure function for tests). Not persisted.

```text
data ResolvedAppearance {
  darkTheme: Boolean
  useDynamicColor: Boolean   // true only if materialYouEnabled && sdk >= 31
}
```

```text
fun resolve(
  prefs: AppearancePreferences,
  systemDark: Boolean,
  sdkInt: Int,
): ResolvedAppearance =
  ResolvedAppearance(
    darkTheme = when (prefs.mode) {
      System → systemDark
      Light  → false
      Dark   → true
    },
    useDynamicColor = prefs.materialYouEnabled && sdkInt >= 31,
  )
```

## `AppScreen` (navigation state)

Not persisted. Held in Compose root memory.

```text
sealed interface AppScreen {
  Compare
  Settings
}
```

Default: `Compare`. Process death returns to Compare (normal Activity
restore); preferences still apply via DataStore. Compare inputs remain via
existing `CompareViewModel` / `SavedStateHandle`.

## Unchanged entities (reference)

| Entity | Role |
|--------|------|
| `OfferSlotState` / `CompareUiState` | Compare form; must survive Settings |
| Brand `ColorScheme` tokens in `Color.kt` / `Theme.kt` | Fallback when dynamic off or unsupported |
| `CompareViewModel` | Activity-scoped; no appearance fields |

No migrations beyond first write of DataStore defaults.
