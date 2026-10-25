package com.inspiredandroid.kai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.BorderSoft
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.KaiShapes
import com.inspiredandroid.kai.ui.SurfaceRaised
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.handCursor

/**
 * Chip with full control over appearance — no hidden minimum-interactive-size
 * padding from Material's FilterChip. 38.dp tall at the default font scale, and
 * taller when the label needs the room.
 */
@Composable
fun KaiChip(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val containerColor = if (selected) SurfaceRaised else CardFill
    val contentColor = when {
        !enabled -> TextSecondary.copy(alpha = 0.38f)
        selected -> TextPrimary
        else -> TextSecondary
    }
    val borderColor = when {
        !enabled -> BorderSoft.copy(alpha = 0.38f)
        selected -> SurfaceRaised
        else -> BorderSoft
    }
    val shape = KaiShapes.xs
    val border = BorderStroke(1.dp, borderColor)
    val sizeModifier = modifier.defaultMinSize(minHeight = 48.dp)

    val cardModifier = Modifier
        .clip(shape)
        .background(containerColor, shape)
        .border(border, shape)

    val chipContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp)
                .padding(horizontal = ChatSpacing.lg, vertical = ChatSpacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }

    if (onClick != null) {
        Box(
            modifier = sizeModifier
                .handCursor()
                .then(cardModifier)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            chipContent()
        }
    } else {
        Box(
            modifier = sizeModifier.then(cardModifier),
            contentAlignment = Alignment.Center,
        ) {
            chipContent()
        }
    }
}
