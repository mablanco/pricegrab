package com.mablanco.pricegrab.ui.compare

/**
 * How an offer card lays out price, quantity, and unit controls.
 *
 * Derived at composition time from [androidx.compose.ui.unit.Density.fontScale]
 * — never persisted. See `specs/006-compact-offer-row/research.md`.
 */
enum class OfferInputArrangement {
    /** price | quantity | unit on one row (default font scale). */
    CompactSingleRow,

    /** price on its own row; quantity | unit below (large font scale). */
    AdaptiveTwoRow,
}

/**
 * Font scales at or above this threshold use [OfferInputArrangement.AdaptiveTwoRow].
 * Matches research decision: 1.3 leaves headroom before 200% while keeping
 * default (1.0) on the compact path.
 */
internal const val COMPACT_FONT_SCALE_MAX_EXCLUSIVE: Float = 1.3f

internal fun arrangementFor(fontScale: Float): OfferInputArrangement =
    if (fontScale < COMPACT_FONT_SCALE_MAX_EXCLUSIVE) {
        OfferInputArrangement.CompactSingleRow
    } else {
        OfferInputArrangement.AdaptiveTwoRow
    }
