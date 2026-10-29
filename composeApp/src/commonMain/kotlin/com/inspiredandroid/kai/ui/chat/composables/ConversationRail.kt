package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.chat.ChatShapes
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.chat.ChatUiState
import com.inspiredandroid.kai.ui.chat.ConversationSummary
import com.inspiredandroid.kai.ui.handCursor
import kotlinx.collections.immutable.ImmutableList

/**
 * Desktop conversation rail shown on expanded windows (Phase 2 of the chat UI
 * upgrade plan). Lists saved conversations and lets the user switch or start a
 * new one. Presentation only; loading/deletion go through [ChatUiState.actions].
 */
@Composable
fun ConversationRail(
    uiState: ChatUiState,
    filteredConversations: ImmutableList<ConversationSummary>,
    onNewChat: () -> Unit,
    onConversationSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ChatSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(ChatSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChatSpacing.xs),
        ) {
            Text(
                text = "Conversations",
                style = KaiTypography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onNewChat, modifier = Modifier.handCursor()) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New conversation",
                    tint = AccentPrimary,
                )
            }
        }
        Spacer(Modifier.height(ChatSpacing.xs))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(ChatSpacing.xxs),
        ) {
            items(filteredConversations, key = { it.id }) { conversation ->
                val selected = conversation.id == uiState.currentConversationId
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .handCursor()
                        .clickable(onClick = { onConversationSelected(conversation.id) })
                        .clip(ChatShapes.quickStartCard)
                        .background(
                            if (selected) AccentPrimary.copy(alpha = 0.15f) else CardFill,
                            ChatShapes.quickStartCard,
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(ChatSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ChatSpacing.xs),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = if (selected) TextPrimary else TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = conversation.title.ifBlank { "Untitled" },
                            style = KaiTypography.bodySmall,
                            color = if (selected) TextPrimary else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
