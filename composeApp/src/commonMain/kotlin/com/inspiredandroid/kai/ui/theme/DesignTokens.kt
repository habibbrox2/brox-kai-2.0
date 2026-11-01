package com.inspiredandroid.kai.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Brox Kai v2.0 — 2027 Premium AI Agent Workspace Design Tokens.
 *
 * Implements the design specifications defined in docs/ui-upgrade-plan-7-premium-chat.md
 * and aligns with the Brox Kai Brand & Logo Pack.
 */
object DesignTokens {
    // ─── Core Surfaces (Deep Obsidian / OLED) ───
    val Background: Color = Color(0xFF090A0F)
    val BackgroundOled: Color = Color(0xFF000000)
    val TerminalBackground: Color = Color(0xFF0B0D11)
    val Surface: Color = Color(0x0AFFFFFF) // rgba(255, 255, 255, 0.04)
    val SurfaceElevated: Color = Color(0xFF171A23)
    val SurfaceRaised: Color = Color(0xFF1E2330)

    // ─── Borders ───
    val BorderSubtle: Color = Color(0x14FFFFFF) // rgba(255, 255, 255, 0.08)
    val BorderSoft: Color = Color(0x26FFFFFF)   // rgba(255, 255, 255, 0.15)
    val BorderStrong: Color = Color(0x40FFFFFF) // rgba(255, 255, 255, 0.25)

    // ─── Brand Colors (Brox Kai v2.0 Official Logo Pack) ───
    val BrandNavy: Color = Color(0xFF0B2B5B)
    val BrandCyan: Color = Color(0xFF00D1E8)
    val BrandBlue: Color = Color(0xFF168BDB)

    // ─── Accents ───
    val PrimaryCyan: Color = Color(0xFF00D7E8)
    val AgentViolet: Color = Color(0xFF8A63F6)
    val AccentPrimary: Color = PrimaryCyan
    val AccentAgent: Color = AgentViolet

    // ─── Semantic Typography Colors ───
    val PrimaryText: Color = Color(0xFFFFFFFF)
    val SecondaryText: Color = Color(0xFFCBD5E1)
    val TertiaryText: Color = Color(0xFF94A3B8)
    val MutedText: Color = Color(0xFF64748B)

    // ─── Status Colors ───
    val Success: Color = Color(0xFF10B981)
    val Warning: Color = Color(0xFFF59E0B)
    val Error: Color = Color(0xFFEF4444)
    val Info: Color = Color(0xFF3B82F6)

    // ─── Spacing Dimensions ───
    val SpacingXxs: Dp = 2.dp
    val SpacingXs: Dp = 4.dp
    val SpacingSm: Dp = 8.dp
    val SpacingMd: Dp = 12.dp
    val SpacingLg: Dp = 16.dp
    val SpacingXl: Dp = 20.dp
    val SpacingXxl: Dp = 24.dp
    val SpacingXxxl: Dp = 32.dp

    // ─── Corner Radii ───
    val RadiusCard: Dp = 16.dp
    val RadiusHud: Dp = 24.dp
    val RadiusPill: Dp = 999.dp
    val RadiusSheet: Dp = 20.dp
}

/**
 * CompositionLocal providing access to [DesignTokens] throughout the composable tree.
 */
val LocalDesignTokens = staticCompositionLocalOf { DesignTokens }
