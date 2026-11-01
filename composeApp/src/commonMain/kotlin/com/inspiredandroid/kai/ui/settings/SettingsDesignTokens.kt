package com.inspiredandroid.kai.ui.settings

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.BorderSoft
import com.inspiredandroid.kai.ui.BorderStrong
import com.inspiredandroid.kai.ui.BorderSubtle
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.CardSurface
import com.inspiredandroid.kai.ui.DangerAlert
import com.inspiredandroid.kai.ui.SuccessGreen
import com.inspiredandroid.kai.ui.SurfaceElevated
import com.inspiredandroid.kai.ui.SurfaceRaised
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.TextTertiary
import com.inspiredandroid.kai.ui.WarningAmber

/**
 * Shared design tokens for the Brox Kai settings control center.
 * Updated for 2027 Minimal design system.
 */
object SettingsSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 28.dp
    val xxxl: Dp = 32.dp

    val minTouchTarget: Dp = 48.dp
    val sectionGap: Dp = 20.dp
    val cardGap: Dp = 16.dp
}

object SettingsShapes {
    val card = RoundedCornerShape(18.dp)
    val section = RoundedCornerShape(22.dp)
    val row = RoundedCornerShape(14.dp)
    val pill = RoundedCornerShape(999.dp)
    val input = RoundedCornerShape(14.dp)
    val button = RoundedCornerShape(999.dp)
    val panel = RoundedCornerShape(24.dp)
}

object SettingsStatusColors {
    val connected = SuccessGreen
    val checking = WarningAmber
    val error = DangerAlert
    val unknown = TextTertiary
    val attention = WarningAmber
    val glow = AccentPrimary
}

object SettingsSurfaces {
    /** Page background - dark theme */
    @Composable
    fun page(): Color = Color(0xFF0F172A)

    /** Glassmorphic card background with blur effect */
    @Composable
    fun card(): Color = CardFill

    /** Card border - subtle neural border */
    @Composable
    fun border(): Color = BorderSubtle

    /** Raised surface for floating elements */
    @Composable
    fun raised(): Color = SurfaceRaised

    /** Elevated surface for panels */
    @Composable
    fun elevated(): Color = SurfaceElevated

    /** Primary accent color for interactive elements */
    @Composable
    fun accent(): Color = AccentPrimary

    /** Text colors */
    @Composable
    fun textPrimary(): Color = TextPrimary

    @Composable
    fun textSecondary(): Color = TextSecondary

    @Composable
    fun textTertiary(): Color = TextTertiary

    /** Danger/destructive color */
    @Composable
    fun danger(): Color = DangerAlert
}
