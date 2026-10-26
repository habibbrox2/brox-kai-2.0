package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.BackIcon
import com.inspiredandroid.kai.getBackgroundDispatcher
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.chat.ChatUiState
import com.inspiredandroid.kai.ui.chat.History
import com.inspiredandroid.kai.ui.chat.activities.ActivityMapper
import com.inspiredandroid.kai.ui.chat.lastRenderedAssistant
import com.inspiredandroid.kai.ui.components.LogoAnimation
import com.inspiredandroid.kai.ui.components.VerticalScrollbarForList
import com.inspiredandroid.kai.ui.dynamicui.FrozenSubmission
import com.inspiredandroid.kai.ui.dynamicui.toSpeakableText
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.markdown.KaiUiBlock
import com.inspiredandroid.kai.ui.markdown.parseMarkdown
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.fallback_answered_by
import kai.composeapp.generated.resources.fallback_service_failed
import kai.composeapp.generated.resources.fallback_trying_next
import kai.composeapp.generated.resources.scroll_to_bottom_content_description
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.errors.TextToSpeechSynthesisInterruptedError
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlinx.collections.immutable.ImmutableList as IImmutableList

/**
 * The conversation timeline: scrollable list of user/assistant messages, tool
 * activity, streaming row, fallback/error states and the scroll-to-bottom affordance.
 *
 * Extracted from ChatModeScreen (Phase 2 of the chat UI upgrade plan) so the
 * shell can compose it independently. All business state still comes from
 * [ChatUiState]; no provider/persistence logic lives here.
 */
@Composable
fun ChatTimeline(
    uiState: ChatUiState,
    textToSpeech: TextToSpeechInstance?,
    onOpenInEditor: ((String, String?) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val componentScope = rememberCoroutineScope()

    val lastAssistantId = remember(uiState.history) { uiState.history.lastRenderedAssistant()?.id }
    // Scroll to bottom & speak only when a NEW assistant message is appended (not on clear, regenerate, etc.)
    var lastScrollId by remember { mutableStateOf(lastAssistantId) }
    LaunchedEffect(lastAssistantId) {
        if (lastScrollId == lastAssistantId) return@LaunchedEffect
        lastScrollId = lastAssistantId
        val history = uiState.history
        if (history.isNotEmpty()) {
            listState.requestScrollToItem(history.lastIndex)
            val lastMessage = history.last()
            if (uiState.isSpeechOutputEnabled && lastMessage.role == History.Role.ASSISTANT) {
                componentScope.launch(getBackgroundDispatcher()) {
                    textToSpeech?.stop()
                    uiState.actions.setIsSpeaking(true, lastMessage.id)
                    try {
                        textToSpeech?.say(lastMessage.content.toSpeakableText())
                    } catch (_: TextToSpeechSynthesisInterruptedError) {
                        // Speech was interrupted by user
                    } catch (_: Exception) {
                        // Handle TTS errors gracefully
                    } finally {
                        uiState.actions.setIsSpeaking(false, lastMessage.id)
                    }
                }
            }
        }
    }
    // Compute frozen submissions + user-to-assistant mapping without mutating state during composition
    val pairings = remember(uiState.history, uiState.isLoading) {
        val history = uiState.history
        val lastUserIdx = history.indexOfLast { it.role == History.Role.USER }

        val frozen = history
            .withIndex()
            .filter { (_, h) -> h.role == History.Role.USER }
            .mapNotNull { (i, h) ->
                val sub = h.uiSubmission ?: return@mapNotNull null
                val originId = (i - 1 downTo 0).firstNotNullOfOrNull { j ->
                    history[j].takeIf {
                        it.role == History.Role.ASSISTANT &&
                            it.content.isNotEmpty() && !it.isThinking &&
                            it.content == sub.sourceContent
                    }?.id
                } ?: (i - 1 downTo 0).firstNotNullOfOrNull { j ->
                    history[j].takeIf {
                        it.role == History.Role.ASSISTANT &&
                            it.content.isNotEmpty() && !it.isThinking
                    }?.id
                } ?: return@mapNotNull null
                originId to FrozenSubmission(
                    values = sub.values,
                    pressedEvent = sub.pressedEvent,
                    isPending = uiState.isLoading && i == lastUserIdx,
                )
            }
            .toMap()

        val userIdByAssistant = history
            .withIndex()
            .filter { (_, h) -> h.role == History.Role.USER }
            .mapNotNull { (i, h) ->
                val originId = (i - 1 downTo 0).firstNotNullOfOrNull { j ->
                    history[j].takeIf {
                        it.role == History.Role.ASSISTANT &&
                            it.content.isNotEmpty() && !it.isThinking &&
                            it.content == (h.uiSubmission?.sourceContent ?: "")
                    }?.id
                } ?: (i - 1 downTo 0).firstNotNullOfOrNull { j ->
                    history[j].takeIf {
                        it.role == History.Role.ASSISTANT &&
                            it.content.isNotEmpty() && !it.isThinking
                    }?.id
                } ?: return@mapNotNull null
                originId to h.id
            }
            .toMap()

        frozen to userIdByAssistant
    }
    val frozenByAssistantId = pairings.first
    val userIdByAssistantId = pairings.second
    val executingToolsState = rememberTimelineExecutingTools(uiState.history)

    val fallbackStatusText = uiState.fallbackStatus?.let { status ->
        val failed = stringResource(Res.string.fallback_service_failed, status.serviceName, uiErrorText(status.errorReason))
        val next = status.nextServiceName?.let { stringResource(Res.string.fallback_trying_next, it) }
        if (next != null) "$failed\n$next" else failed
    }

    val (reasoningSegmentsByAssistantId, suppressedThinkingIds) = remember(uiState.history) {
        val history = uiState.history

        val byAnswerId = history
            .withIndex()
            .filter { (_, e) ->
                e.role == History.Role.ASSISTANT &&
                    !e.isThinking &&
                    e.content.isNotEmpty()
            }
            .mapNotNull { (i, e) ->
                val thinkingPrefix = history
                    .take(i)
                    .filter { it.role == History.Role.ASSISTANT && it.isThinking && it.content.isNotEmpty() }
                    .map { it.content }
                    .reversed()

                val combined = buildList {
                    addAll(thinkingPrefix)
                    e.reasoningContent?.takeIf { it.isNotBlank() }?.let { add(it) }
                }

                if (combined.isNotEmpty()) e.id to combined.toImmutableList() else null
            }
            .toMap()

        val suppressed = history
            .withIndex()
            .filter { (_, e) ->
                e.role == History.Role.ASSISTANT && e.isThinking && e.content.isNotEmpty()
            }
            .filterIndexed { idx, (i, _) ->
                history.drop(i + 1).any { it.role == History.Role.ASSISTANT && !it.isThinking && it.content.isNotEmpty() }
            }
            .map { (_, e) -> e.id }
            .toSet()

        byAnswerId to suppressed
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index < listState.layoutInfo.totalItemsCount - 1
        }
    }

    Box(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .widthIn(max = 820.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = com.inspiredandroid.kai.ui.chat.ChatSpacing.lg,
                bottom = com.inspiredandroid.kai.ui.chat.ChatSpacing.xl,
            ),
        ) {
            items(uiState.history, key = { it.id }, contentType = { it.role }) { history ->
                when (history.role) {
                    History.Role.USER -> {
                        if (history.uiSubmission == null) {
                            UserMessage(
                                message = history.content,
                                attachments = history.attachments,
                            )
                        }
                    }

                    History.Role.ASSISTANT -> {
                        if (history.content.isNotEmpty() && !history.isThinking) {
                            val isLastAssistant = history.id == lastAssistantId
                            val frozen = frozenByAssistantId[history.id]
                            val pairedUserId = userIdByAssistantId[history.id]
                            BotMessage(
                                message = history.content,
                                textToSpeech = textToSpeech,
                                isSpeaking = uiState.isSpeaking && uiState.isSpeakingContentId == history.id,
                                setIsSpeaking = {
                                    uiState.actions.setIsSpeaking(it, history.id)
                                },
                                onRegenerate = if (isLastAssistant) uiState.actions.regenerate else null,
                                isInteractive = isLastAssistant && !uiState.isLoading && frozen == null,
                                onUiCallback = { event, data ->
                                    uiState.actions.submitUiCallback(event, data)
                                },
                                frozen = frozen,
                                onResubmit = if (pairedUserId != null && !uiState.isLoading) {
                                    { event, data -> uiState.actions.resubmit(pairedUserId, event, data) }
                                } else {
                                    null
                                },
                                reasoningSegments = reasoningSegmentsByAssistantId[history.id] ?: persistentListOf(),
                                onOpenInEditor = onOpenInEditor,
                            )
                            if (history.fallbackServiceName != null) {
                                Text(
                                    text = stringResource(Res.string.fallback_answered_by, history.fallbackServiceName),
                                    style = KaiTypography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                                )
                            }
                        } else if (history.isThinking &&
                            history.content.isNotEmpty() &&
                            history.id !in suppressedThinkingIds
                        ) {
                            BotMessage(
                                message = "",
                                textToSpeech = null,
                                isSpeaking = false,
                                setIsSpeaking = {},
                                reasoningSegments = reasoningSegmentsByAssistantId[history.id]
                                    ?: persistentListOf(history.content),
                                onOpenInEditor = onOpenInEditor,
                            )
                        }
                    }

                    History.Role.TOOL_EXECUTING -> {
                        // Rendered in WaitingResponseRow below
                    }

                    History.Role.TOOL -> {
                        // Don't show completed tool results in UI
                    }
                }
            }
            // Show agent activity timeline for tool executions and agent actions
            // Temporarily disabled to fix crash
            // val hasAgentActivity = !uiState.isLoading && uiState.history.any {
            //     it.role == History.Role.TOOL ||
            //     (it.role == History.Role.ASSISTANT && it.toolCalls?.isNotEmpty() == true)
            // }
            // if (hasAgentActivity) {
            //     item(key = "agent-activity-timeline") {
            //         val activities = remember(uiState.history) {
            //             ActivityMapper.historyToActivities(uiState.history)
            //         }
            //         if (activities.isNotEmpty()) {
            //             AgentActivityTimeline(activities = activities)
            //         }
            //     }
            // }
            val showWaitingRow = uiState.isLoading &&
                (frozenByAssistantId.values.none { it.isPending } || executingToolsState.tools.isNotEmpty())
            if (showWaitingRow) {
                item(key = "loading") {
                    WaitingResponseRow(
                        executingTools = executingToolsState.tools,
                        isStatusOnly = executingToolsState.isStatusOnly,
                        statusText = fallbackStatusText,
                    )
                }
            }
            uiState.error?.let { error ->
                item(key = "error") {
                    if (uiState.showFreeProviderSuggestions) {
                        FreeProviderSuggestionsPanel(
                            error = error,
                            retry = uiState.actions.retry,
                        )
                    }
                }
            }
            if (!uiState.isLoading && uiState.followUpSuggestions.isNotEmpty()) {
                item(key = "followups") {
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 820.dp)
                            .padding(horizontal = com.inspiredandroid.kai.ui.chat.ChatSpacing.lg, vertical = com.inspiredandroid.kai.ui.chat.ChatSpacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.followUpSuggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = { uiState.actions.ask(suggestion) },
                                label = { Text(suggestion) },
                            )
                        }
                    }
                }
            }
        }

        VerticalScrollbarForList(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )

        AnimatedVisibility(
            visible = showScrollToBottom,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = com.inspiredandroid.kai.ui.chat.ChatSpacing.lg),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            SmallFloatingActionButton(
                modifier = Modifier.handCursor(),
                onClick = {
                    componentScope.launch {
                        val totalItems = listState.layoutInfo.totalItemsCount
                        if (totalItems > 0) {
                            listState.animateScrollToItem(totalItems - 1)
                        }
                    }
                },
                containerColor = com.inspiredandroid.kai.ui.CardFill,
                contentColor = com.inspiredandroid.kai.ui.TextPrimary,
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(Res.string.scroll_to_bottom_content_description),
                    tint = com.inspiredandroid.kai.ui.AccentPrimary,
                )
            }
        }
    }
}

private data class TimelineExecutingToolsState(
    val tools: ImmutableList<Pair<String, String>>,
    val isStatusOnly: Boolean,
)

@Composable
private fun rememberTimelineExecutingTools(history: ImmutableList<History>): TimelineExecutingToolsState {
    val historyState = rememberUpdatedState(history)
    val state by remember {
        derivedStateOf {
            val executing = historyState.value.filter { it.role == History.Role.TOOL_EXECUTING }
            TimelineExecutingToolsState(
                tools = executing.map { it.id to (it.toolName ?: "tool") }.toImmutableList(),
                isStatusOnly = executing.any { it.isStatusMessage },
            )
        }
    }
    return state
}
