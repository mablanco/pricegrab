package com.mablanco.pricegrab.ui.compare

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Currency symbol shown on money amounts (feature 010). Empty means no
 * symbol, which is also the default for screens rendered outside
 * [com.mablanco.pricegrab.PriceGrabAppContent].
 */
val LocalCurrencySymbol = staticCompositionLocalOf { "" }
