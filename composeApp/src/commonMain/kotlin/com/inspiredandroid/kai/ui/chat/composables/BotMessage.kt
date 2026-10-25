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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = com.inspiredandroid.kai.ui.chat.ChatSpacing.lg, vertical = com.inspiredandroid.kai.ui.chat.ChatSpacing.md),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .clip(com.inspiredandroid.kai.ui.KaiShapes.assistantSurface)
                .background(com.inspiredandroid.kai.ui.CardFill)
                .border(
                    width = 1.dp,
                    color = com.inspiredandroid.kai.ui.BorderSoft,
                    shape = com.inspiredandroid.kai.ui.KaiShapes.assistantSurface,
                )
                .padding(horizontal = com.inspiredandroid.kai.ui.chat.ChatSpacing.xl, vertical = com.inspiredandroid.kai.ui.chat.ChatSpacing.lg),
        ) {
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
                // When reasoning is shown above, the Thinking row already provides
                // the visual gap to the answer — drop the duplicated top inset.
                val answerTopPadding = if (nonBlankSegments.isNotEmpty()) com.inspiredandroid.kai.ui.chat.ChatSpacing.sm else com.inspiredandroid.kai.ui.chat.ChatSpacing.xs
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
    val copyToClipboard = rememberCopyToClipboard()
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
    // Preview always reflects the MOST RECENT thinking segment so the user gets a
    // visual update each time a new reasoning phase starts, without expanding.
    val preview = remember(segments) {
        segments.lastOrNull()
            ?.lineSequence()
            ?.map { it.trim() }
            ?.firstOrNull { it.isNotEmpty() }
            .orEmpty()
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable { expanded = !expanded }
                .handCursor(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(Res.string.bot_message_thinking_expand_content_description),
                tint = TextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = stringResource(Res.string.bot_message_thinking_label),
                style = KaiTypography.labelMedium,
                color = TextSecondary,
            )
            if (!expanded && preview.isNotEmpty()) {
                Text(
                    text = " · $preview",
                    style = KaiTypography.bodySmall,
                    color = TextSecondary,
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
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (segment in segments) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        VerticalDivider(
                            thickness = 2.dp,
                            color = com.inspiredandroid.kai.ui.AccentPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxHeight(),
                        )
                        SelectionContainer(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = segment,
                                color = TextSecondary,
                                style = KaiTypography.bodySmall,
                                lineHeight = KaiTypography.bodySmall.lineHeight * 1.2f,
                            )
                        }
                    }
                }
            }
        }
    }
}
