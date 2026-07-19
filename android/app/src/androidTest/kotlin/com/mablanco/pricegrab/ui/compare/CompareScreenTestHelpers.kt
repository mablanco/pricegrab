package com.mablanco.pricegrab.ui.compare

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.mablanco.pricegrab.MainActivity
import com.mablanco.pricegrab.R

/**
 * Opens [testTag] and picks a unit from the dropdown by its accessibility
 * label ([unitNameRes]). Menu items expose the full unit name as content
 * description while the closed selector shows the short code, so text-based
 * matching would fail when the same code is already visible on another offer.
 */
internal fun AndroidComposeTestRule<*, MainActivity>.selectUnit(
    testTag: String,
    @StringRes unitNameRes: Int,
) {
    onNodeWithTag(testTag).performClick()
    onNodeWithContentDescription(activity.getString(unitNameRes)).performClick()
    waitForIdle()
}

/** Localized winner headline for the given offer title resource. */
internal fun Context.winnerHeadline(@StringRes offerTitleRes: Int): String =
    getString(R.string.result_winner, getString(offerTitleRes))
