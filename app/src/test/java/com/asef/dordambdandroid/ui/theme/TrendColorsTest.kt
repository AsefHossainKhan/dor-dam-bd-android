package com.asef.dordambdandroid.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

// SPEC: spec-2911cf — a price rise shows red, a price fall shows green.
class TrendColorsTest {

    @Test
    fun lightTheme_upIsRed_downIsGreen() {
        val colors = extendedColorsFor(darkTheme = false)
        assertEquals(Color(0xFFDC2626), colors.trendUp)
        assertEquals(Color(0xFF16A34A), colors.trendDown)
    }

    @Test
    fun darkTheme_upIsRed_downIsGreen() {
        val colors = extendedColorsFor(darkTheme = true)
        assertEquals(Color(0xFFF87171), colors.trendUp)
        assertEquals(Color(0xFF4ADE80), colors.trendDown)
    }
}
