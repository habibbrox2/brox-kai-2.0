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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.getBackgroundDispatcher
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.BorderSoft
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.KaiShapes
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.dynamicui.FrozenSubmission
import com.inspiredandroid.kai.ui.dynamicui.toSpeakableText
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.markdown.MarkdownContent
import com.inspiredandroid.kai.ui.markdown.parseMarkdown
import com.inspiredandroid.kai.ui.rememberCopyToClipboard
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.bot_message_copy_content_description
import kai.composeapp.generated.resources.bot_message_flag_content_description
import kai.composeapp.generated.resources.bot_message_regenerate_content_description
import kai.composeapp.generated.resources.bot_message_speech_content_description
import kai.composeapp.generated.resources.bot_message_thinking_expand_content_description
import kai.composeapp.generated.resources.bot_message_thinking_label
import kai.composeapp.generated.resources.chat_cancel_edit_content_description
import kai.composeapp.generated.resources.chat_edit_submission_content_description
import kai.composeapp.generated.resources.ic_copy
import kai.composeapp.generated.resources.ic_flag
import kai.composeapp.generated.resources.ic_refresh
import kai.composeapp.generated.resources.ic_stop
import kai.composeapp.generated.resources.ic_volume_up
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
    val botBubbleShape = androidx.compose.foundation.shape.RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = 4.dp,
        bottomEnd = 18.dp,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = com.inspiredandroid.kai.ui.chat.ChatSpacing.lg, vertical = com.inspiredandroid.kai.ui.chat.ChatSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 48.dp, max = 680.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = botBubbleShape,
                    ambientColor = androidx.compose.ui.graphics.Color(0x330E0F12),
                    spotColor = androidx.compose.ui.graphics.Color(0x330E0F12),
                )
                .clip(botBubbleShape)
                .background(com.inspiredandroid.kai.ui.WarmPaperBubble)
                .border(
                    width = 1.dp,
                    color = androidx.compose.ui.graphics.Color(0x1F0E0F12),
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
                            style = KaiTypography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = com.inspiredandroid.kai.ui.DarkText,
                        )
                    }
                }
                val nonBlankSegments = remember(reasoningSegments) {
                    reasoningSegments.filter { it.isNotBlank() }.toImmutableList()
                }
                if (nonBlankSegments.isNotEmpty()) {
                    ReasoningBlockquote(
                        segments = nonBlankSegments,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (message.isNotEmpty()) {
                    val answerTopPadding = if (nonBlankSegments.isNotEmpty()) 8.dp else 2.dp
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides com.inspiredandroid.kai.ui.DarkText,
                    ) {
                        SelectionContainer {
                            MarkdownContent(
                                document = document,
                                isInteractive = effectiveInteractive,
                                onUiCallback = kaiUiCallback,
                                frozen = effectiveFrozen,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = answerTopPadding, end = if (message.isNotEmpty()) 24.dp else 0.dp),
                                onOpenInEditor = onOpenInEditor,
                            )
                        }
                    }
                }
                if (onRegenerate != null) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .clickable { onRegenerate() }
                            .handCursor()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.bot_message_regenerate_content_description),
                            tint = com.inspiredandroid.kai.ui.DarkText.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Regenerate",
                            style = KaiTypography.labelSmall,
                            color = com.inspiredandroid.kai.ui.DarkText.copy(alpha = 0.8f),
                        )
                    }
                }
            }
            if (message.isNotEmpty()) {
                androidx.compose.material3.IconButton(
                    onClick = { copyToClipboard(message) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .handCursor(),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_copy),
                        contentDescription = stringResource(Res.string.bot_message_copy_content_description),
                        tint = com.inspiredandroid.kai.ui.DarkText.copy(alpha = 0.6f),
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
        if (frozen != null && onResubmit != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
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
    if (message.isEmpty()) return
    var thumbsUp by remember { mutableStateOf(false) }
    var thumbsDown by remember { mutableStateOf(false) }
    val componentScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    MessageActionRow(
        modifier = Modifier.padding(horizontal = ChatSpacing.lg),
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
                        } catch (ignore: TextToSpeechSynthesisInterruptedError) {
                            // Expected interruption - no action needed
                        } catch (e: Exception) {
                            // Handle TTS errors gracefully
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

    // Claude Code Style Collapsible Execution Log:
    // border-left: 2px solid #06B6D4, subtle monospace status text, expandable
    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(0.dp, 8.dp, 8.dp, 0.dp))
            .background(androidx.compose.ui.graphics.Color(0xFF0A0E14))
            .border(
                androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF1E293B)),
                androidx.compose.foundation.shape.RoundedCornerShape(0.dp, 8.dp, 8.dp, 0.dp),
            ),
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
                    .background(androidx.compose.ui.graphics.Color(0xFF06B6D4), androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.size(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(Res.string.bot_message_thinking_expand_content_description),
                tint = androidx.compose.ui.graphics.Color(0xFF06B6D4),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = if (segments.size > 1) "Thinking (${segments.size} steps)" else stringResource(Res.string.bot_message_thinking_label),
                style = KaiTypography.labelSmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
            )
            if (!expanded && preview.isNotEmpty()) {
                Text(
                    text = " · $preview",
                    style = KaiTypography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = androidx.compose.ui.graphics.Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (segment in segments) {
                    SelectionContainer {
                        Text(
                            text = segment,
                            color = androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                            style = KaiTypography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = KaiTypography.bodySmall.lineHeight * 1.25f,
                        )
                    }
                }
            }
        }
    }
}
