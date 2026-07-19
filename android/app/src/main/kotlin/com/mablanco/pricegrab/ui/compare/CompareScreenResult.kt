package com.mablanco.pricegrab.ui.compare

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.core.os.ConfigurationCompat
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.core.model.Dimension
import com.mablanco.pricegrab.ui.theme.spacing
import java.util.Locale

@Composable
internal fun ResultRegion(
    outcome: ComparisonOutcome?,
    incompatibleUnits: Boolean,
    dimension: Dimension?,
) {
    val configuration = LocalConfiguration.current
    val locale = ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()

    val placeholder = stringResource(R.string.result_placeholder)
    val incompatibleMessage = stringResource(R.string.error_incompatible_units)
    val headline = outcome?.let { outcomeHeadline(it) }
    val savings = ResultPresenter.present(outcome, dimension, locale)
    val savingsLine: String? = savings?.let { formatSavingsLine(it.perUnitDelta, dimension) }
    val a11ySummary: String = when {
        incompatibleUnits -> incompatibleMessage
        headline == null -> placeholder
        savingsLine != null -> "$headline. $savingsLine"
        else -> headline
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TEST_TAG_RESULT)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = a11ySummary
            },
    ) {
        when {
            incompatibleUnits -> {
                Text(
                    text = incompatibleMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(TEST_TAG_INCOMPATIBLE_UNITS),
                )
            }
            outcome == null -> {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                HeroResultCard(
                    outcome = outcome,
                    headline = headline ?: "",
                    savingsLine = savingsLine,
                )
            }
        }
    }
}

@Composable
private fun outcomeHeadline(outcome: ComparisonOutcome): String = when (outcome) {
    ComparisonOutcome.Tie -> stringResource(R.string.result_tied)
    is ComparisonOutcome.Winner -> stringResource(
        R.string.result_winner,
        stringResource(offerTitleRes(outcome.slotIndex)),
    )
}

@Composable
private fun formatSavingsLine(perUnitDelta: String, dimension: Dimension?): String? {
    if (dimension == null) return null
    @StringRes val templateRes = when (dimension) {
        Dimension.Mass -> R.string.result_savings_per_kg
        Dimension.Volume -> R.string.result_savings_per_L
        Dimension.Count -> R.string.result_savings_per_piece
    }
    return stringResource(templateRes, perUnitDelta)
}

@Composable
private fun HeroResultCard(
    outcome: ComparisonOutcome,
    headline: String,
    savingsLine: String?,
) {
    val spacing = MaterialTheme.spacing
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.s)
            .testTag(TEST_TAG_HERO_RESULT),
        elevation = CardDefaults.elevatedCardElevation(),
    ) {
        Row(
            modifier = Modifier.padding(spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.m),
        ) {
            HeroResultIcon(outcome)
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .testTag(TEST_TAG_RESULT_TEXT)
                        .semantics { heading() },
                )
                if (savingsLine != null) {
                    Text(
                        text = savingsLine,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(TEST_TAG_RESULT_SAVINGS),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroResultIcon(outcome: ComparisonOutcome) {
    val tint = MaterialTheme.colorScheme.primary
    when (outcome) {
        is ComparisonOutcome.Winner -> Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = tint,
        )
        ComparisonOutcome.Tie -> Icon(
            painter = painterResource(R.drawable.ic_tie_glyph),
            contentDescription = null,
            tint = tint,
        )
    }
}

@StringRes
internal fun offerTitleRes(index: Int): Int = when (index) {
    0 -> R.string.offer_a_title
    1 -> R.string.offer_b_title
    2 -> R.string.offer_c_title
    else -> error("Offer index out of range: $index")
}

internal fun offerTestTag(index: Int): String = when (index) {
    0 -> TEST_TAG_OFFER_A
    1 -> TEST_TAG_OFFER_B
    2 -> TEST_TAG_OFFER_C
    else -> error("Offer index out of range: $index")
}

internal fun resultDimension(state: CompareUiState): Dimension? =
    when (val outcome = state.outcome) {
        is ComparisonOutcome.Winner ->
            state.offers.getOrNull(outcome.slotIndex)?.quantityUnit?.dimension
        ComparisonOutcome.Tie ->
            state.offers.firstOrNull()?.quantityUnit?.dimension
        null -> null
    }
