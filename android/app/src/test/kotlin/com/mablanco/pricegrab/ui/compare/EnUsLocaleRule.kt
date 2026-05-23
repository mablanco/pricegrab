package com.mablanco.pricegrab.ui.compare

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.Locale

/** Forces en-US for ViewModel tests that type dot-decimal raw strings. */
class EnUsLocaleRule : TestWatcher() {
    private var previous: Locale? = null

    override fun starting(description: Description) {
        previous = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("en-US"))
    }

    override fun finished(description: Description) {
        previous?.let { Locale.setDefault(it) }
    }
}
