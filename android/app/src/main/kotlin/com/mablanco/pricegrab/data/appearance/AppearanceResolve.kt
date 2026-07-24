package com.mablanco.pricegrab.data.appearance

/** Minimum SDK that supports Material You dynamic color schemes. */
const val DYNAMIC_COLOR_MIN_SDK: Int = 31

/**
 * Pure resolution of [AppearancePreferences] into theme inputs.
 *
 * [ResolvedAppearance.useDynamicColor] is true only when the user opted
 * into Material You **and** the device SDK is at least [DYNAMIC_COLOR_MIN_SDK].
 */
fun resolveAppearance(
    prefs: AppearancePreferences,
    systemDark: Boolean,
    sdkInt: Int,
): ResolvedAppearance = ResolvedAppearance(
    darkTheme = when (prefs.mode) {
        AppearanceMode.System -> systemDark
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
    },
    useDynamicColor = prefs.materialYouEnabled && sdkInt >= DYNAMIC_COLOR_MIN_SDK,
)
