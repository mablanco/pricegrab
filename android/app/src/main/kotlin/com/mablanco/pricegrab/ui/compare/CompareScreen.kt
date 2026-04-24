package com.mablanco.pricegrab.ui.compare

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme

/**
 * US1 screen: enter two `(price, quantity)` offers and see which one is
 * cheaper per unit.
 *
 * This stateful entry point reads a [CompareViewModel] from the current
 * [androidx.lifecycle.ViewModelStore] and delegates presentation to the
 * stateless [CompareScreen] overload below, keeping UI logic testable without
 * a full activity lifecycle.
 */
@Composable
fun CompareScreen(
    modifier: Modifier = Modifier,
    viewModel: CompareViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CompareScreen(
        state = state,
        onPriceAChange = viewModel::onPriceAChange,
        onQuantityAChange = viewModel::onQuantityAChange,
        onPriceBChange = viewModel::onPriceBChange,
        onQuantityBChange = viewModel::onQuantityBChange,
        modifier = modifier,
    )
}

@Composable
fun CompareScreen(
    state: CompareUiState,
    onPriceAChange: (String) -> Unit,
    onQuantityAChange: (String) -> Unit,
    onPriceBChange: (String) -> Unit,
    onQuantityBChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        Text(
            text = stringResource(R.string.compare_heading),
            style = MaterialTheme.typography.headlineSmall,
        )

        OfferCard(
            title = stringResource(R.string.offer_a_title),
            priceRaw = state.priceARaw,
            priceError = state.priceAError,
            quantityRaw = state.quantityARaw,
            quantityError = state.quantityAError,
            onPriceChange = onPriceAChange,
            onQuantityChange = onQuantityAChange,
            testTagPrefix = TEST_TAG_OFFER_A,
        )

        OfferCard(
            title = stringResource(R.string.offer_b_title),
            priceRaw = state.priceBRaw,
            priceError = state.priceBError,
            quantityRaw = state.quantityBRaw,
            quantityError = state.quantityBError,
            onPriceChange = onPriceBChange,
            onQuantityChange = onQuantityBChange,
            testTagPrefix = TEST_TAG_OFFER_B,
        )

        ResultCard(outcome = state.outcome)
    }
}

@Composable
private fun OfferCard(
    title: String,
    priceRaw: String,
    priceError: InputError?,
    quantityRaw: String,
    quantityError: InputError?,
    onPriceChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    testTagPrefix: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(SCREEN_PADDING),
            verticalArrangement = Arrangement.spacedBy(FIELD_SPACING),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)

            LabeledNumberField(
                value = priceRaw,
                onValueChange = onPriceChange,
                labelRes = R.string.price_label,
                contentDescription = stringResource(R.string.cd_price_field, title),
                error = priceError,
                imeAction = ImeAction.Next,
                testTag = "${testTagPrefix}_price",
            )

            LabeledNumberField(
                value = quantityRaw,
                onValueChange = onQuantityChange,
                labelRes = R.string.quantity_label,
                contentDescription = stringResource(R.string.cd_quantity_field, title),
                error = quantityError,
                imeAction = ImeAction.Done,
                testTag = "${testTagPrefix}_quantity",
            )
        }
    }
}

@Composable
private fun LabeledNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    contentDescription: String,
    error: InputError?,
    imeAction: ImeAction,
    testTag: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let {
            { Text(stringResource(it.messageRes())) }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription },
    )
}

@Composable
private fun ResultCard(outcome: ComparisonOutcome?) {
    val message = stringResource(outcome.headlineRes())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TEST_TAG_RESULT)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = message
            },
    ) {
        Row(
            modifier = Modifier.padding(SCREEN_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FIELD_SPACING),
        ) {
            if (outcome is ComparisonOutcome.AWins || outcome is ComparisonOutcome.BWins) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag(TEST_TAG_RESULT_TEXT),
            )
        }
    }
}

@StringRes
private fun InputError.messageRes(): Int = when (this) {
    InputError.NotANumber -> R.string.error_not_a_number
    InputError.NegativePrice -> R.string.error_negative_price
    InputError.NonPositiveQuantity -> R.string.error_non_positive_quantity
}

@StringRes
private fun ComparisonOutcome?.headlineRes(): Int = when (this) {
    null -> R.string.result_placeholder
    ComparisonOutcome.Tie -> R.string.result_tie
    is ComparisonOutcome.AWins -> R.string.result_a_wins
    is ComparisonOutcome.BWins -> R.string.result_b_wins
}

// ---- Test tags (constants so tests can reference them) ----------------------

const val TEST_TAG_OFFER_A: String = "offerA"
const val TEST_TAG_OFFER_B: String = "offerB"
const val TEST_TAG_RESULT: String = "result"
const val TEST_TAG_RESULT_TEXT: String = "result_text"

// ---- Layout constants -------------------------------------------------------

private val SCREEN_PADDING = 16.dp
private val SECTION_SPACING = 16.dp
private val FIELD_SPACING = 12.dp

// ---- Previews ---------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun CompareScreenEmptyPreview() {
    PriceGrabTheme {
        CompareScreen(
            state = CompareUiState(),
            onPriceAChange = {},
            onQuantityAChange = {},
            onPriceBChange = {},
            onQuantityBChange = {},
        )
    }
}

@Preview(showBackground = true, name = "A wins")
@Composable
private fun CompareScreenAWinsPreview() {
    PriceGrabTheme {
        CompareScreen(
            state = CompareUiState(
                priceARaw = "2.50",
                quantityARaw = "500",
                priceBRaw = "4.00",
                quantityBRaw = "800",
                outcome = ComparisonOutcome.AWins(
                    perUnitDelta = java.math.BigDecimal("0.0"),
                    percentDelta = java.math.BigDecimal("0"),
                ),
            ),
            onPriceAChange = {},
            onQuantityAChange = {},
            onPriceBChange = {},
            onQuantityBChange = {},
        )
    }
}
