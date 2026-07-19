package com.mablanco.pricegrab.ui.compare

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme
import java.math.BigDecimal

@Preview(showBackground = true)
@Composable
private fun CompareScreenEmptyPreview() {
    PriceGrabTheme {
        CompareScreen(
            state = CompareUiState(),
            onPriceChange = { _, _ -> },
            onQuantityChange = { _, _ -> },
            onUnitChange = { _, _ -> },
            onAddOffer = {},
            onRemoveOffer = {},
            onResetClick = {},
            onUndoClick = {},
            onUndoDismissed = {},
        )
    }
}

@Preview(showBackground = true, name = "Winner with savings")
@Composable
private fun CompareScreenWinnerPreview() {
    PriceGrabTheme {
        CompareScreen(
            state = CompareUiState(
                offers = listOf(
                    OfferSlotState(priceRaw = "2.50", quantityRaw = "500"),
                    OfferSlotState(priceRaw = "4.00", quantityRaw = "800"),
                ),
                outcome = ComparisonOutcome.Winner(
                    slotIndex = 0,
                    secondSlotIndex = 1,
                    perUnitDelta = BigDecimal("0.001"),
                    percentDelta = BigDecimal("20"),
                ),
            ),
            onPriceChange = { _, _ -> },
            onQuantityChange = { _, _ -> },
            onUnitChange = { _, _ -> },
            onAddOffer = {},
            onRemoveOffer = {},
            onResetClick = {},
            onUndoClick = {},
            onUndoDismissed = {},
        )
    }
}

@Preview(showBackground = true, name = "Three offers")
@Composable
private fun CompareScreenThreeOffersPreview() {
    PriceGrabTheme {
        CompareScreen(
            state = CompareUiState(
                offers = listOf(
                    OfferSlotState(priceRaw = "2.50", quantityRaw = "500"),
                    OfferSlotState(priceRaw = "4.00", quantityRaw = "1000"),
                    OfferSlotState(priceRaw = "3.00", quantityRaw = "750"),
                ),
                outcome = ComparisonOutcome.Winner(
                    slotIndex = 1,
                    secondSlotIndex = 0,
                    perUnitDelta = BigDecimal("0.001"),
                    percentDelta = BigDecimal("20"),
                ),
            ),
            onPriceChange = { _, _ -> },
            onQuantityChange = { _, _ -> },
            onUnitChange = { _, _ -> },
            onAddOffer = {},
            onRemoveOffer = {},
            onResetClick = {},
            onUndoClick = {},
            onUndoDismissed = {},
        )
    }
}
