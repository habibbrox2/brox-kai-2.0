package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.BackIcon
import com.inspiredandroid.kai.data.ServiceEntry
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.chat.ChatActions
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.settings.SettingsModel
import com.inspiredandroid.kai.ui.theme.DesignTokens
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.ic_settings
import kai.composeapp.generated.resources.interactive_back_content_description
import kai.composeapp.generated.resources.interactive_mode_content_description
import kai.composeapp.generated.resources.logo
import kai.composeapp.generated.resources.settings_content_description
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.marc_apps.tts.TextToSpeechInstance
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

/**
 * Brox Kai 2.0 — 2027 Premium AI Agent Workspace HUD (Heads-Up Display).
 *
 * Implements the floating top bar specification from docs/ui-upgrade-plan-7-premium-chat.md.
 * Provides left cluster (drawer + BK monogram + brand), center cluster (pulsing agent orb + model picker),
 * and right cluster (terminal/sandbox toggle + TTS speech toggle + settings).
 */
@Composable
internal fun AgentHud(
    textToSpeech: TextToSpeechInstance? = null,
    isSpeechOutputEnabled: Boolean = false,
    isSpeaking: Boolean = false,
    actions: ChatActions,
    onNavigateToSettings: () -> Unit,
    isSandboxAvailable: Boolean = false,
    isSandboxOpen: Boolean = false,
    isShellExecuting: Boolean = false,
    isGenerating: Boolean = false,
    onToggleSandbox: () -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null,
    services: ImmutableList<ServiceEntry> = persistentListOf(),
    models: ImmutableList<SettingsModel> = persistentListOf(),
    isLoadingModels: Boolean = false,
    isInteractiveMode: Boolean = false,
    onToggleInteractiveMode: (() -> Unit)? = null,
    onGoBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val hudBackground = DesignTokens.SurfaceElevated
    val hudBorder = BorderStroke(1.dp, DesignTokens.BorderSubtle)
    val hudShape = RoundedCornerShape(DesignTokens.RadiusHud)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 4.dp)
            .defaultMinSize(minHeight = 56.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ChatSpacing.md)
                .clip(hudShape)
                .background(hudBackground, hudShape)
                .border(hudBorder, hudShape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ChatSpacing.sm, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // ─── Left Cluster: Drawer toggle + BK monogram + Title ───
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (onOpenDrawer != null) {
                        IconButton(
                            modifier = Modifier.size(36.dp).handCursor(),
                            onClick = onOpenDrawer,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open navigation",
                                tint = DesignTokens.PrimaryText,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    // Official Brox Kai Logo from Logo Pack
                    Icon(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "Brox Kai Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(26.dp),
                    )

                    Spacer(Modifier.width(2.dp))

                    Text(
                        text = "Brox Kai 2.0",
                        style = KaiTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.PrimaryText,
                        maxLines = 1,
                    )

                    onToggleInteractiveMode?.let { toggle ->
                        IconToggleButton(
                            checked = isInteractiveMode,
                            onCheckedChange = { toggle() },
                            modifier = Modifier.size(36.dp).handCursor(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = stringResource(Res.string.interactive_mode_content_description),
                                tint = if (isInteractiveMode) DesignTokens.PrimaryCyan else DesignTokens.TertiaryText,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                // ─── Center Cluster: Agent Status Orb + Model Selector ───
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f, fill = false).padding(horizontal = 4.dp),
                ) {
                    AgentStatusOrb(
                        isGenerating = isGenerating,
                        isShellExecuting = isShellExecuting,
                        isSpeaking = isSpeaking,
                    )

                    Spacer(Modifier.width(8.dp))

                    if (services.isNotEmpty()) {
                        ServiceSelector(
                            services = services,
                            onSelectService = actions.selectService,
                            models = models,
                            isLoadingModels = isLoadingModels,
                            onSelectModel = actions.selectModel,
                            onLoadModels = actions.loadModels,
                            showLabel = false,
                        )
                    }
                }

                // ─── Right Cluster: Interactive Back + Sandbox + TTS + Settings ───
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    onGoBack?.let { goBack ->
                        IconButton(
                            modifier = Modifier.size(36.dp).handCursor(),
                            onClick = goBack,
                        ) {
                            Icon(
                                imageVector = BackIcon,
                                contentDescription = stringResource(Res.string.interactive_back_content_description),
                                tint = DesignTokens.PrimaryText,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    if (isSandboxAvailable) {
                        SandboxToggleButton(
                            isSandboxOpen = isSandboxOpen,
                            isShellExecuting = isShellExecuting,
                            onToggleSandbox = onToggleSandbox,
                        )
                    }

                    if (textToSpeech != null) {
                        SpeechToggleButton(textToSpeech, isSpeechOutputEnabled, isSpeaking, actions)
                    }

                    IconButton(
                        modifier = Modifier.size(36.dp).handCursor(),
                        onClick = onNavigateToSettings,
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_settings),
                            contentDescription = stringResource(Res.string.settings_content_description),
                            tint = DesignTokens.PrimaryText,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pulsing Agent Status Orb indicating runtime execution states:
 * - Idle: calm cyan breathing glow
 * - Generating / Thinking: active agent violet pulse
 * - Shell / Tool running: amber energy glow
 * - Speaking: cyan voice-wave pulse
 */
@Composable
fun AgentStatusOrb(
    isGenerating: Boolean,
    isShellExecuting: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isGenerating || isShellExecuting) 650 else 1800,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val orbColor = when {
        isShellExecuting -> DesignTokens.Warning
        isGenerating -> DesignTokens.AgentViolet
        isSpeaking -> DesignTokens.PrimaryCyan
        else -> DesignTokens.BrandCyan
    }

    Box(
        modifier = modifier.size(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Outer pulsing aura
        Box(
            modifier = Modifier
                .size(16.dp * pulseScale)
                .clip(CircleShape)
                .background(orbColor.copy(alpha = 0.28f)),
        )
        // Core status bead
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(orbColor),
        )
    }
}

