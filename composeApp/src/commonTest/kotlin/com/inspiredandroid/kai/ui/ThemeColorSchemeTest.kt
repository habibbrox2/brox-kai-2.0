package com.inspiredandroid.kai.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeColorSchemeTest {

    @Test
    fun `oled token is true zero luminance black`() {
        assertEquals(Color(0xFF000000), OledBlack)
        assertEquals(OledBlack, OledColorScheme.background)
        assertEquals(OledBlack, OledColorScheme.surface)
        assertEquals(OledBlack, OledColorScheme.surfaceContainer)
        assertEquals(OledBlack, OledColorScheme.surfaceContainerLow)
        assertEquals(OledBlack, OledColorScheme.surfaceContainerLowest)
        assertTrue(OledColorScheme.isOledFlavor)
    }

    @Test
    fun `midnight uses brand navy surfaces and is not classified as oled`() {
        assertEquals(broxDeepNavy, MidnightColorScheme.background)
        assertEquals(broxNavy, MidnightColorScheme.surface)
        assertEquals(broxNavy, MidnightColorScheme.surfaceContainer)
        assertEquals(SurfaceElevated, MidnightColorScheme.surfaceContainerLow)
        assertFalse(MidnightColorScheme.isOledFlavor)
    }

    @Test
    fun `black background helper makes all base surfaces pure black`() {
        val scheme = DarkColorScheme.withBlackBackground()

        assertEquals(OledBlack, scheme.background)
        assertEquals(OledBlack, scheme.surface)
        assertEquals(OledBlack, scheme.surfaceContainer)
        assertEquals(OledBlack, scheme.surfaceContainerLow)
        assertEquals(OledBlack, scheme.surfaceContainerLowest)
        assertTrue(scheme.isOledFlavor)
    }

    @Test
    fun `standard dark theme is not classified as oled`() {
        assertFalse(DarkColorScheme.isOledFlavor)
    }
}
