package com.mablanco.pricegrab.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// PriceGrab Material 3 typography hierarchy.
//
// Feature 003 pins explicit weight and size assignments (instead of accepting
// the per-component Material 3 defaults wholesale) so the Compare screen has
// a deliberate, reusable hierarchy. The platform default font (Roboto Flex)
// is kept as-is — shipping a custom font would push the cold-start budget and
// APK size, both scarce on a utility app (see plan.md §"Typography" and
// research.md entry 7).
//
// Surface assignments (single source of truth for any future surface that
// wants the same hierarchy):
//
//   Surface                              Token              Weight
//   ──────────────────────────────────── ────────────────── ──────
//   Top-bar title ("PriceGrab")          titleLarge         Medium
//   Offer card label ("Offer A/B")       titleMedium        Medium
//   Field label ("Price", "Quantity")    labelLarge         Regular
//   Helper / error text                  bodySmall          Regular
//   Hero result headline                 headlineSmall      Bold
//   Hero result body (savings line)      bodyLarge          Regular
//
// Roles not listed above inherit the Material 3 default `Typography()` so the
// rest of the surface keeps stock M3 behaviour without us re-deriving it.
internal val PriceGrabTypography: Typography = Typography().run {
    copy(
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Medium),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.Medium),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.Normal),
        bodySmall = bodySmall.copy(fontWeight = FontWeight.Normal),
        bodyLarge = bodyLarge.copy(fontWeight = FontWeight.Normal),
        headlineSmall = headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            // Slightly tighter line height than the Material 3 default so the
            // hero result headline reads as a single emphatic block rather
            // than two airy lines on small viewports.
            lineHeight = HEADLINE_SMALL_LINE_HEIGHT_SP.sp,
        ),
    )
}

// Material 3's stock `headlineSmall.lineHeight` is 32.sp; the bold variant
// fills more vertical space optically, so we trim 2.sp to keep the hero card
// compact at 200% font scale without breaking the type hierarchy.
private const val HEADLINE_SMALL_LINE_HEIGHT_SP = 30
