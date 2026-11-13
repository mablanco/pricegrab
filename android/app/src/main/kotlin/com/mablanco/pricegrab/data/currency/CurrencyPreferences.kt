package com.mablanco.pricegrab.data.currency

/** Longest custom symbol accepted (`CHF`, `NT$`, `zł`, …). */
const val MAX_CUSTOM_SYMBOL_LENGTH: Int = 4

/**
 * Where the displayed currency symbol comes from. Persisted as a lowercase
 * string; unknown or missing values map to [Automatic] (see [fromStorage]).
 */
enum class CurrencyMode {
    Automatic,
    Custom,
    ;

    fun toStorage(): String = when (this) {
        Automatic -> STORAGE_AUTOMATIC
        Custom -> STORAGE_CUSTOM
    }

    companion object {
        const val STORAGE_AUTOMATIC: String = "automatic"
        const val STORAGE_CUSTOM: String = "custom"

        fun fromStorage(value: String?): CurrencyMode = when (value) {
            STORAGE_CUSTOM -> Custom
            else -> Automatic
        }
    }
}

/**
 * Currency choice (feature 010). [customSymbol] is kept while [mode] is
 * [CurrencyMode.Automatic] so switching back to Custom restores it.
 */
data class CurrencyPreferences(
    val mode: CurrencyMode = CurrencyMode.Automatic,
    val customSymbol: String = "",
)

fun capCustomSymbol(value: String): String = value.take(MAX_CUSTOM_SYMBOL_LENGTH)
