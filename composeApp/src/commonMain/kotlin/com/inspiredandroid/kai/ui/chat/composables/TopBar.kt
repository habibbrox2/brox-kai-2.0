package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.BackIcon
import com.inspiredandroid.kai.data.ServiceEntry
import com.inspiredandroid.kai.prefersReducedMotion
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.BorderSoft
import com.inspiredandroid.kai.ui.ChatDarkBackground
import com.inspiredandroid.kai.ui.KaiShapes
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.QuickOptionGreen
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.chat.ChatActions
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.settings.SettingsModel
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.ic_service_openrouter
import kai.composeapp.generated.resources.ic_settings
import kai.composeapp.generated.resources.ic_volume_off
import kai.composeapp.generated.resources.ic_volume_up
import kai.composeapp.generated.resources.interactive_back_content_description
import kai.composeapp.generated.resources.interactive_mode_content_description
import kai.composeapp.generated.resources.sandbox_content_description
import kai.composeapp.generated.resources.settings_content_description
import kai.composeapp.generated.resources.toggle_speech_output_content_description
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.marc_apps.tts.TextToSpeechInstance
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

/**
 * Chat workspace header.
 * Displays OpenRouter logo + "Brox Kai 2.0" + "AI Agent" badge,
 * active model selector, and navigation / tool buttons.
 */
@Composable
internal fun TopBar(
    textToSpeech: TextToSpeechInstance? = null,
    isSpeechOutputEnabled: Boolean,
    isSpeaking: Boolean,
    actions: ChatActions,
    onNavigateToSettings: () -> Unit,
    isSandboxAvailable: Boolean,
    isSandboxOpen: Boolean,
    isShellExecuting: Boolean,
    onToggleSandbox: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    services: ImmutableList<ServiceEntry> = persistentListOf(),
    models: ImmutableList<SettingsModel> = persistentListOf(),
    isLoadingModels: Boolean = false,
    /** Interactive UI mode: the model answers with an interactive screen inside this chat. */
    isInteractiveMode: Boolean = false,
    onToggleInteractiveMode: (() -> Unit)? = null,
    /** Removes the last exchange — only offered while interactive mode is on. */
    onGoBack: (() -> Unit)? = null,
) {
    val topBarBg = ChatDarkBackground
    val topBarBorder = BorderStroke(1.dp, BorderSoft.copy(alpha = 0.5f))
    val topBarShape = KaiShapes.panel

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 20.dp)
            .defaultMinSize(minHeight = ChatSpacing.xxxl + 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ChatSpacing.lg, vertical = ChatSpacing.sm)
                .clip(topBarShape)
                .background(topBarBg, topBarShape)
                .border(topBarBorder, topBarShape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ChatSpacing.md, vertical = ChatSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Leading cluster: drawer toggle + interactive mode toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ChatSpacing.xs),
                ) {
                    if (onOpenDrawer != null) {
                        IconButton(
                            modifier = Modifier.handCursor(),
                            onClick = onOpenDrawer,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open navigation",
                                tint = TextPrimary,
                            )
                        }
                    }
                    onToggleInteractiveMode?.let { toggle ->
                        IconToggleButton(
                            checked = isInteractiveMode,
                            onCheckedChange = { toggle() },
                            modifier = Modifier.handCursor(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = stringResource(Res.string.interactive_mode_content_description),
                                tint = if (isInteractiveMode) {
                                    AccentPrimary
                                } else {
                                    TextPrimary
                                },
                            )
                        }
                    }
                }

                // Center: OpenRouter logo + "Brox Kai 2.0" + "AI Agent" badge + model selector
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_service_openrouter),
                            contentDescription = "OpenRouter",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Brox Kai 2.0",
                            style = KaiTypography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                        )
                        Box(
                            modifier = Modifier
                                .clip(KaiShapes.pill)
                                .background(QuickOptionGreen.copy(alpha = 0.15f))
                                .border(
                                    1.dp,
                                    QuickOptionGreen.copy(alpha = 0.5f),
                                    KaiShapes.pill,
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "AI Agent",
                                style = KaiTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                color = QuickOptionGreen,
                            )
                        }
                    }
                    if (services.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
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

                // Trailing cluster: go back, sandbox, speech, settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ChatSpacing.xs),
                ) {
                    onGoBack?.let { goBack ->
                        IconButton(
                            modifier = Modifier.handCursor(),
                            onClick = goBack,
                        ) {
                            Icon(
                                imageVector = BackIcon,
                                contentDescription = stringResource(Res.string.interactive_back_content_description),
                                tint = TextPrimary,
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
                        modifier = Modifier.handCursor(),
                        onClick = onNavigateToSettings,
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_settings),
                            contentDescription = stringResource(Res.string.settings_content_description),
                            tint = TextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SandboxToggleButton(
    isSandboxOpen: Boolean,
    isShellExecuting: Boolean,
    onToggleSandbox: () -> Unit,
) {
    val prefersReducedMotion = prefersReducedMotion()
    val flashAlpha = remember { Animatable(0f) }
    LaunchedEffect(isShellExecuting) {
        if (isShellExecuting && !prefersReducedMotion) {
            flashAlpha.snapTo(0.4f)
            flashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            )
        }
    }
    val primary = AccentPrimary
    val checkedContainer = primary.copy(alpha = 0.2f)
    val flashContainer = primary.copy(alpha = flashAlpha.value)
    IconToggleButton(
        checked = isSandboxOpen,
        onCheckedChange = { onToggleSandbox() },
        modifier = Modifier.handCursor(),
        colors = IconButtonDefaults.iconToggleButtonColors(
            containerColor = flashContainer,
            checkedContainerColor = if (flashAlpha.value > 0f) flashContainer else checkedContainer,
            checkedContentColor = AccentPrimary,
        ),
    ) {
        Icon(
            imageVector = Icons.Filled.Dns,
            contentDescription = stringResource(Res.string.sandbox_content_description),
            tint = if (isSandboxOpen) {
                AccentPrimary
            } else {
                TextPrimary
            },
        )
    }
}

@Composable
private fun SpeechToggleButton(
    textToSpeech: TextToSpeechInstance,
    isSpeechOutputEnabled: Boolean,
    isSpeaking: Boolean,
    actions: ChatActions,
) {
    IconButton(
        modifier = Modifier.handCursor(),
        onClick = {
            if (isSpeechOutputEnabled && isSpeaking) {
                actions.setIsSpeaking(false, "")
                textToSpeech.stop()
            }
            actions.toggleSpeechOutput()
        },
    ) {
        Icon(
            imageVector = if (isSpeechOutputEnabled) {
                vectorResource(Res.drawable.ic_volume_up)
            } else {
                vectorResource(Res.drawable.ic_volume_off)
            },
            contentDescription = stringResource(Res.string.toggle_speech_output_content_description),
            tint = TextPrimary,
        )
    }
}
