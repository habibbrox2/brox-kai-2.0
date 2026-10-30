package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import kotlinx.coroutines.delay

/**
 * Consistent ChatGPT-style message action row (Copy / Retry / Share / Speech / Thumbs).
 * Features:
 * - Instant "Copied!" checkmark feedback on copy
 * - Retry / Regenerate button (🔄)
 * - Read aloud TTS button (🔊)
 * - Thumbs up & thumbs down feedback
 * - 48dp accessible touch targets
 */
@Composable
fun MessageActionRow(
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onSpeak: (() -> Unit)? = null,
    onThumbsUp: (() -> Unit)? = null,
    onThumbsDown: (() -> Unit)? = null,
    thumbsUpSelected: Boolean = false,
    thumbsDownSelected: Boolean = false,
    onFlag: (() -> Unit)? = null,
) {
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ChatSpacing.xxs),
    ) {
        ChatActionButton(
            icon = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = if (isCopied) "Copied" else "Copy message",
            onClick = {
                onCopy()
                isCopied = true
            },
        )
        if (onRetry != null) {
            ChatActionButton(
                icon = Icons.Default.Refresh,
                contentDescription = "Regenerate / Retry",
                onClick = onRetry,
            )
        }
        if (onSpeak != null) {
            ChatActionButton(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Read aloud",
                onClick = onSpeak,
            )
        }
        if (onThumbsUp != null) {
            ChatActionButton(
                icon = if (thumbsUpSelected) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = "Thumbs up",
                onClick = onThumbsUp,
            )
        }
        if (onThumbsDown != null) {
            ChatActionButton(
                icon = if (thumbsDownSelected) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                contentDescription = "Thumbs down",
                onClick = onThumbsDown,
            )
        }
        if (onFlag != null) {
            ChatActionButton(
                icon = Icons.Default.Flag,
                contentDescription = "Report",
                onClick = onFlag,
            )
        }
        if (onShare != null) {
            ChatActionButton(
                icon = Icons.Default.Share,
                contentDescription = "Share",
                onClick = onShare,
            )
        }
    }
}
