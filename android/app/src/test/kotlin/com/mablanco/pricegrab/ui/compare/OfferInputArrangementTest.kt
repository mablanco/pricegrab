package com.mablanco.pricegrab.ui.compare

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Feature 006: pure mapping from system font scale to offer-field arrangement.
 */
class OfferInputArrangementTest {

    @Test
    fun defaultFontScaleUsesCompactSingleRow() {
        assertEquals(
            OfferInputArrangement.CompactSingleRow,
            arrangementFor(1.0f),
        )
    }

    @Test
    fun justBelowThresholdUsesCompactSingleRow() {
        assertEquals(
            OfferInputArrangement.CompactSingleRow,
            arrangementFor(1.299f),
        )
    }

    @Test
    fun thresholdAndAboveUsesAdaptiveTwoRow() {
        assertEquals(
            OfferInputArrangement.AdaptiveTwoRow,
            arrangementFor(1.3f),
        )
        assertEquals(
            OfferInputArrangement.AdaptiveTwoRow,
            arrangementFor(2.0f),
        )
    }
}
