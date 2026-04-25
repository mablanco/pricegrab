package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher

/**
 * Asserts that the node has a [SemanticsProperties.ContentDescription] entry
 * which **contains** [expected] as a substring. Several tests rely on a stable
 * substring (e.g. the result headline) while the full content description
 * additionally appends savings information that we don't want to hardcode in
 * every assertion.
 */
internal fun hasContentDescriptionContaining(expected: String): SemanticsMatcher =
    SemanticsMatcher("has content description containing '$expected'") { node ->
        val config = node.config
        if (!config.contains(SemanticsProperties.ContentDescription)) {
            false
        } else {
            config[SemanticsProperties.ContentDescription].any { it.contains(expected) }
        }
    }
