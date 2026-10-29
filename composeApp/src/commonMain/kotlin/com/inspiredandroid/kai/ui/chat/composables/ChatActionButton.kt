package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.handCursor

/**
 * Reusable action button used across the chat workspace (top bar, message action
 * row, empty-state quick starts). Guarantees a 48dp touch target and a content
 * description for accessibility (Phase 5 requirement).
 */
@Composable
fun ChatActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    if (label == null) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .defaultMinSize(minWidth = ChatSpacing.minTouchTarget, minHeight = ChatSpacing.minTouchTarget)
                .handCursor(),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.38f),
            )
        }
    } else {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .defaultMinSize(minHeight = ChatSpacing.minTouchTarget)
                .handCursor(),
            contentPadding = PaddingValues(
                horizontal = ChatSpacing.md,
                vertical = ChatSpacing.sm,
            ),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(ChatSpacing.xs))
            Text(
                text = label,
                style = KaiTypography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
