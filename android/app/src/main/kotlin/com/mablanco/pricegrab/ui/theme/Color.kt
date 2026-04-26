package com.mablanco.pricegrab.ui.theme

import androidx.compose.ui.graphics.Color

// PriceGrab brand palette — derived from the launcher icon's BG_HEX (#2F5C73,
// the steel blue sampled at the corners of branding/icon-source.png and used
// as the adaptive-icon background by branding/regenerate-icons.py).
//
// Generated deterministically by Material Color Utilities (TonalSpot scheme,
// the default used by Material Theme Builder) on 2026-04-26. All 18
// canonical text/background pairs were verified against WCAG 2.1 AA (4.5:1
// minimum for normal text); worst-case ratio is 4.94:1 (light
// onSurfaceVariant on surfaceVariant) and median is ~6.1:1 — comfortably
// above the threshold in both light and dark modes.
//
// Do not hand-edit individual values: regenerate the entire palette from the
// seed if the brand color ever changes (single source of truth =
// branding/regenerate-icons.py BG_HEX).
internal val BrandPrimary = Color(0xFF2C6580)
internal val BrandOnPrimary = Color(0xFFF4FAFF)
internal val BrandPrimaryContainer = Color(0xFFA8DEFE)
internal val BrandOnPrimaryContainer = Color(0xFF10506B)

internal val BrandSecondary = Color(0xFF4E626D)
internal val BrandOnSecondary = Color(0xFFF4FAFF)
internal val BrandSecondaryContainer = Color(0xFFD1E5F3)
internal val BrandOnSecondaryContainer = Color(0xFF415460)

internal val BrandTertiary = Color(0xFF535D85)
internal val BrandOnTertiary = Color(0xFFFAF8FF)
internal val BrandTertiaryContainer = Color(0xFFC5D0FE)
internal val BrandOnTertiaryContainer = Color(0xFF3B466C)

internal val BrandError = Color(0xFFA83836)
internal val BrandOnError = Color(0xFFFFF7F6)
internal val BrandErrorContainer = Color(0xFFFA746F)
internal val BrandOnErrorContainer = Color(0xFF6E0A12)

internal val BrandBackground = Color(0xFFF7F9FD)
internal val BrandOnBackground = Color(0xFF2C3338)
internal val BrandSurface = Color(0xFFF7F9FD)
internal val BrandOnSurface = Color(0xFF2C3338)
internal val BrandSurfaceVariant = Color(0xFFDCE3EA)
internal val BrandOnSurfaceVariant = Color(0xFF586066)
internal val BrandOutline = Color(0xFF747C82)
internal val BrandOutlineVariant = Color(0xFFABB3B9)

// Dark-theme mirrors (same TonalSpot scheme, dark = true).
internal val BrandPrimaryDark = Color(0xFFA4CCE4)
internal val BrandOnPrimaryDark = Color(0xFF1B4559)
internal val BrandPrimaryContainerDark = Color(0xFF2F576C)
internal val BrandOnPrimaryContainerDark = Color(0xFFC4E8FF)

internal val BrandSecondaryDark = Color(0xFFB5C9D7)
internal val BrandOnSecondaryDark = Color(0xFF30434E)
internal val BrandSecondaryContainerDark = Color(0xFF2B3E49)
internal val BrandOnSecondaryContainerDark = Color(0xFFAEC2D0)

internal val BrandTertiaryDark = Color(0xFFD8DEFF)
internal val BrandOnTertiaryDark = Color(0xFF444E75)
internal val BrandTertiaryContainerDark = Color(0xFFC5D0FE)
internal val BrandOnTertiaryContainerDark = Color(0xFF3B466C)

internal val BrandErrorDark = Color(0xFFFA746F)
internal val BrandOnErrorDark = Color(0xFF490006)
internal val BrandErrorContainerDark = Color(0xFF871F21)
internal val BrandOnErrorContainerDark = Color(0xFFFF9993)

internal val BrandBackgroundDark = Color(0xFF0B0F11)
internal val BrandOnBackgroundDark = Color(0xFFDFE6ED)
internal val BrandSurfaceDark = Color(0xFF0B0F11)
internal val BrandOnSurfaceDark = Color(0xFFDFE6ED)
internal val BrandSurfaceVariantDark = Color(0xFF1F272C)
internal val BrandOnSurfaceVariantDark = Color(0xFFA4ACB2)
internal val BrandOutlineDark = Color(0xFF6F767C)
internal val BrandOutlineVariantDark = Color(0xFF41494E)
