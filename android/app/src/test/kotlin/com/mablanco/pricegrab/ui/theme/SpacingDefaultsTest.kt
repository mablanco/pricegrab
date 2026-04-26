package com.mablanco.pricegrab.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Foundational shape tests for the [Spacing] tokens introduced by feature 003.
 *
 * Compose's [androidx.compose.runtime.staticCompositionLocalOf] requires a
 * live composition to resolve `LocalSpacing.current`, which is exercised by
 * the instrumented `CompareScreen*Test` family in `androidTest/`. This pure
 * JVM test only pins the canonical default values of the [Spacing] data
 * class so any accidental drift (e.g. a refactor that changes one of the
 * `s/m/l/xl/xxl` constants) fails fast in unit-test CI before reaching
 * the slower instrumented tier.
 */
class SpacingDefaultsTest {

    @Test
    fun spacingDataClassExposesCanonicalDefaults() {
        val spacing = Spacing()

        assertEquals("Small spacing token (gap inside a row)", 4.dp, spacing.s)
        assertEquals("Medium spacing token (between text + icon)", 8.dp, spacing.m)
        assertEquals("Large spacing token (card-to-card / inside-card padding)", 16.dp, spacing.l)
        assertEquals("Extra-large spacing token (hero-card top margin)", 24.dp, spacing.xl)
        assertEquals("Section break (top-bar to first card)", 32.dp, spacing.xxl)
    }
}
