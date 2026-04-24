package com.mablanco.pricegrab.ui.compare

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/**
 * Verifies FR-009 / Principle III: user-entered numbers are parsed using the
 * current locale's decimal separator. In es-ES the separator is a comma, so
 * "2,50" must be recognized as a valid price (not rejected as "not a number").
 *
 * We change the JVM's default Locale before the activity starts; the
 * `CompareViewModel` reads `Locale.getDefault()` on each keystroke, so this is
 * enough to test the parser path. Android string resources are not
 * re-localized here (that would require per-app locale APIs) — strings stay
 * in the device's configured language, which is fine for this assertion.
 */
@RunWith(AndroidJUnit4::class)
class CompareScreenLocaleEsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun commaDecimalSeparatorIsAccepted() {
        composeRule.onNodeWithTag("offerA_price").performTextInput("2,50")
        composeRule.onNodeWithTag("offerA_quantity").performTextInput("500")
        composeRule.onNodeWithTag("offerB_price").performTextInput("4,00")
        composeRule.onNodeWithTag("offerB_quantity").performTextInput("1000")

        val expected = composeRule.activity.getString(R.string.result_b_wins)
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }

    companion object {
        private lateinit var previousLocale: Locale

        @BeforeClass
        @JvmStatic
        fun setSpanishLocale() {
            previousLocale = Locale.getDefault()
            Locale.setDefault(Locale.forLanguageTag("es-ES"))
        }

        @AfterClass
        @JvmStatic
        fun restoreLocale() {
            Locale.setDefault(previousLocale)
        }
    }
}
