package com.mablanco.pricegrab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * App-wide spacing rhythm used by the Compare screen and any future surface.
 *
 * Material 3 ships a typography and color system but deliberately leaves the
 * spacing scale to the app. Hand-rolling `dp` literals at every call site
 * drifts; centralising them in a [Spacing] data class plus a
 * [staticCompositionLocalOf] gives the same uniformity Material 3 gives for
 * type and color.
 *
 * The five tokens map to the use-cases observed on the Compare screen:
 * - [s]:    gap inside a Row (text + icon, e.g. brandmark + title).
 * - [m]:    breathing room between a label and its input.
 * - [l]:    card-to-card vertical gap, inside-card padding.
 * - [xl]:   hero-card top margin (separates result from the input area).
 * - [xxl]:  reserved for section breaks (top-bar to first card on screens
 *           that want extra prominence). The Compare screen does not
 *           currently use [xxl] but defining it keeps the scale closed
 *           rather than expanding ad-hoc.
 *
 * All values are pinned in [SpacingDefaultsTest] so a drift fails fast.
 */
data class Spacing(
    val s: Dp = 4.dp,
    val m: Dp = 8.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

/**
 * `staticCompositionLocalOf` (rather than `compositionLocalOf`) is correct
 * here because the spacing scale never changes at runtime; readers do not
 * need recomposition when the value is "updated" because it is, in
 * practice, immutable.
 */
val LocalSpacing = staticCompositionLocalOf { Spacing() }

/**
 * Convenience accessor so call sites read [MaterialTheme.spacing.l] in the
 * same shape they read [MaterialTheme.colorScheme.primary] or
 * [MaterialTheme.typography.headlineSmall], keeping the API surface uniform
 * with stock Material 3.
 */
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
