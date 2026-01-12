package com.cacaosd.droidmind.feature.automation_runner.composable

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

private const val GOLDEN_ANGLE = 137.507764

fun colorForIndex(
    index: Int,
    saturation: Float = 0.75f,
    lightness: Float = 0.6f
): Color {
    val hue = ((index * GOLDEN_ANGLE) % 360.0).toFloat()
    return Color.hsl(
        hue = abs(hue),
        saturation = saturation,
        lightness = lightness
    )
}
