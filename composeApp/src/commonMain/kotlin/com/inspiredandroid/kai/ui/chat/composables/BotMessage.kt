package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.getBackgroundDispatcher
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.DarkText
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.WarmPaperBubble
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.dynamicui.FrozenSubmission
import com.inspiredandroid.kai.ui.dynamicui.toSpeakableText
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.markdown.MarkdownContent
import com.inspiredandroid.kai.ui.markdown.parseMarkdown
import com.inspiredandroid.kai.ui.rememberCopyToClipboard
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.bot_message_thinking_expand_content_description
import kai.composeapp.generated.resources.bot_message_thinking_label
import kai.composeapp.generated.resources.chat_cancel_edit_content_description
import kai.composeapp.generated.resources.chat_edit_submission_content_description
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.errors.TextToSpeechSynthesisInterruptedError
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BotMessage(
    message: String,
    textToSpeech: TextToSpeechInstance?,
    isSpeaking: Boolean,
    setIsSpeaking: (Boolean) -> Unit,
    onRegenerate: (() -> Unit)? = null,
    isInteractive: Boolean = false,
    onUiCallback: ((event: String, data: Map<String, String>) -> Unit)? = null,
    frozen: FrozenSubmission? = null,
    onResubmit: ((event: String, data: Map<String, String>) -> Unit)? = null,
    reasoningSegments: ImmutableList<String> = persistentListOf(),
    onOpenInEditor: ((String, String?) -> Unit)? = null,
    isFirstMessage: Boolean = false,
) {
    val document = remember(message) { parseMarkdown(message) }
    var isEditing by remember(frozen) { mutableStateOf(false) }
    val effectiveFrozen = if (isEditing && frozen != null) frozen.copy(pressedEvent = null) else frozen
    val effectiveInteractive = if (frozen != null) (onResubmit != null && isEditing) else isInteractive
    val kaiUiCallback: (String, Map<String, String>) -> Unit = if (onResubmit != null) {
        { event, data ->
            isEditing = false
            onResubmit(event, data)
        }
    } else {
        onUiCallback ?: { _, _ -> }
    }

    val copyToClipboard = rememberCopyToClipboard()
    val botBubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = 4.dp,
        bottomEnd = 18.dp,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ChatSpacing.lg, vertical = ChatSpacing.sm),
    ) {
        // Message Content Box
        Box(
            modifier = Modifier
                .widthIn(min = 48.dp, max = 680.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = botBubbleShape,
                    ambientColor = Color(0x330E0F12),
                    spotColor = Color(0x330E0F12),
                )
                .clip(botBubbleShape)
                .background(WarmPaperBubble)
                .border(
                    width = 1.dp,
                    color = Color(0x1F0E0F12),
                    shape = botBubbleShape,
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isFirstMessage) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp),
                    ) {
                        Text("🤖", style = KaiTypography.titleSmall)
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "Brox Kai 2.0",
                            style = KaiTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DarkText,
                        )
                    }
                }

                // ChatGPT-style Collapsible Thinking Process
                val nonBlankSegments = remember(reasoningSegments) {
                    reasoningSegments.filter { it.isNotBlank() }.toImmutableList()
                }
                if (nonBlankSegments.isNotEmpty()) {
                    ReasoningBlockquote(
                        segments = nonBlankSegments,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Markdown Message Body
                if (message.isNotEmpty()) {
                    val answerTopPadding = if (nonBlankSegments.isNotEmpty()) 8.dp else 2.dp
                    CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides DarkText,
                    ) {
                        SelectionContainer {
                            MarkdownContent(
                                document = document,
                                isInteractive = effectiveInteractive,
                                onUiCallback = kaiUiCallback,
                                frozen = effectiveFrozen,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = answerTopPadding),
                                onOpenInEditor = onOpenInEditor,
                            )
                        }
                    }
                }
            }

            // Edit / Resubmit button when frozen
            if (frozen != null && onResubmit != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CardFill)
                        .handCursor()
                        .clickable { isEditing = !isEditing },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = stringResource(
                            if (isEditing) Res.string.chat_cancel_edit_content_description else Res.string.chat_edit_submission_content_description,
                        ),
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary,
                    )
                }
            }
        }

        // ChatGPT-style Clean Action Row below response (Copy, Retry, Speak, Thumbs, Flag)
        if (message.isNotEmpty()) {
            var thumbsUp by remember { mutableStateOf(false) }
            var thumbsDown by remember { mutableStateOf(false) }
            val componentScope = rememberCoroutineScope()
            val uriHandler = LocalUriHandler.current

            MessageActionRow(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                onCopy = { copyToClipboard(message) },
                onSpeak = if (textToSpeech != null) {
                    {
                        componentScope.launch(getBackgroundDispatcher()) {
                            textToSpeech.stop()
                            if (isSpeaking) {
                                setIsSpeaking(false)
                            } else {
                                setIsSpeaking(true)
                                try {
                                    textToSpeech.say(text = message.toSpeakableText())
                                } catch (_: TextToSpeechSynthesisInterruptedError) {
                                    // Expected interruption
                                } catch (_: Exception) {
                                    // Handle gracefully
                                }
                                setIsSpeaking(false)
                            }
                        }
                    }
                } else {
                    null
                },
                onThumbsUp = {
                    thumbsUp = !thumbsUp
                    if (thumbsUp) thumbsDown = false
                },
                onThumbsDown = {
                    thumbsDown = !thumbsDown
                    if (thumbsDown) thumbsUp = false
                },
                thumbsUpSelected = thumbsUp,
                thumbsDownSelected = thumbsDown,
                onRetry = onRegenerate,
                onFlag = {
                    uriHandler.openUri("https://form.jotform.com/250014908169355")
                },
            )
        }
    }
}

/**
 * ChatGPT / DeepSeek / Claude style Thinking Process block.
 * Features:
 * - Glowing thought badge (🧠 Thought process)
 * - Monospace styling with dark canvas (#0B0F17)
 * - Single-line preview when collapsed
 * - Smooth expand/collapse transition
 */
@Composable
private fun ReasoningBlockquote(
    segments: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val preview = remember(segments) {
        segments.lastOrNull()
            ?.lineSequence()
            ?.map { it.trim() }
            ?.firstOrNull { it.isNotEmpty() }
            .orEmpty()
    }

    val blockShape = RoundedCornerShape(10.dp)
    val accentCyan = Color(0xFF06B6D4)
    val containerBg = Color(0xFF0B0F17)
    val borderColor = Color(0xFF1E293B)
    val textSlate = Color(0xFFCBD5E1)
    val textMuted = Color(0xFF64748B)

    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clip(blockShape)
            .background(containerBg)
            .border(1.dp, borderColor, blockShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .handCursor()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 16.dp)
                    .background(accentCyan, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.size(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(Res.string.bot_message_thinking_expand_content_description),
                tint = accentCyan,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = if (segments.size > 1) "Thinking (${segments.size} steps)" else stringResource(Res.string.bot_message_thinking_label),
                style = KaiTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                fontFamily = FontFamily.Monospace,
                color = textSlate,
                fontSize = 12.sp,
            )
            if (!expanded && preview.isNotEmpty()) {
                Text(
                    text = " · $preview",
                    style = KaiTypography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    fontSize = 11.sp,
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070A10))
                    .border(
                        width = 1.dp,
                        color = borderColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (segment in segments) {
                    SelectionContainer {
                        Text(
                            text = segment,
                            color = textSlate,
                            style = KaiTypography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }
    }
}
