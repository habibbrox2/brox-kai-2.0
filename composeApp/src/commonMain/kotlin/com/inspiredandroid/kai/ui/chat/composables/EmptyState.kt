package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.BorderSoft
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.SurfaceRaised
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.chat.ChatShapes
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.components.LogoAnimation
import com.inspiredandroid.kai.ui.components.animatedGradientBorder
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.isOledFlavor
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.privacy_agree_prefix
import kai.composeapp.generated.resources.privacy_policy
import org.jetbrains.compose.resources.stringResource

/**
 * Premium empty state for the chat workspace (Plan 7, Phase 2).
 *
 * Chat-first, minimal: a small animated mark, a light greeting, and a 2x2 grid
 * of quick-start cards with title + subtext. Interactive UI and Kai Build no
 * longer live here — they moved to the sidebar (Plan 7, Phase 1).
 */
@Composable
internal fun EmptyState(
    modifier: Modifier,
    isUsingSharedKey: Boolean,
    onQuickStart: ((String) -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LogoAnimation(size = ChatSpacing.xxxl)
        Spacer(Modifier.height(ChatSpacing.md))
        Text(
            text = "How can I help you today?",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = KaiTypography.headlineSmall,
            textAlign = TextAlign.Center,
            color = TextPrimary,
        )
        if (onQuickStart != null) {
            Spacer(Modifier.height(ChatSpacing.xl))
            QuickStartCards(onQuickStart = onQuickStart)
        }
        if (isUsingSharedKey) {
            Spacer(Modifier.height(ChatSpacing.xl))
            val linkColor = AccentPrimary
            val prefixText = stringResource(Res.string.privacy_agree_prefix)
            val policyText = stringResource(Res.string.privacy_policy)
            val annotatedString = remember(prefixText, policyText, linkColor) {
                androidx.compose.ui.text.buildAnnotatedString {
                    append(prefixText)
                    withLink(
                        androidx.compose.ui.text.LinkAnnotation.Url(
                            url = "https://schubert-simon.de/privacy/kai.txt",
                        ),
                    ) {
                        withStyle(style = androidx.compose.ui.text.SpanStyle(color = linkColor)) {
                            append(policyText)
                        }
                    }
                }
            }
            Text(
                annotatedString,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = KaiTypography.bodySmall,
                textAlign = TextAlign.Center,
                color = TextSecondary,
            )
        }
    }
}

private data class QuickStartItem(
    val label: String,
    val subtext: String,
    val prompt: String,
    val icon: ImageVector,
)

@Composable
private fun QuickStartCards(onQuickStart: (String) -> Unit) {
    val items = listOf(
        QuickStartItem("Write", "Drafts, emails & copy", "Help me write a short, engaging blog post about AI assistants.", Icons.Default.Edit),
        QuickStartItem("Code", "Write & debug code", "Write a Kotlin function that retries an HTTP call with exponential backoff.", Icons.Default.Code),
        QuickStartItem("Research", "Summarize & explore", "Summarize the latest trends in on-device AI in 2026.", Icons.Default.Search),
        QuickStartItem("Build", "Design a UI", "Help me design a small Compose screen with a list and a FAB.", Icons.Default.Build),
    )
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(ChatSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(ChatSpacing.sm),
        maxItemsInEachRow = 2,
    ) {
        items.forEach { item ->
            QuickStartCard(
                item = item,
                onClick = { onQuickStart(item.prompt) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickStartCard(
    item: QuickStartItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isOled = MaterialTheme.colorScheme.isOledFlavor

    val cardBg = if (isOled) {
        com.inspiredandroid.kai.ui.CardFill
    } else {
        SurfaceRaised
    }
    val cardBorder = if (isHovered) {
        androidx.compose.foundation.BorderStroke(1.dp, com.inspiredandroid.kai.ui.AccentPrimary.copy(alpha = 0.5f))
    } else if (isOled) {
        androidx.compose.foundation.BorderStroke(1.dp, com.inspiredandroid.kai.ui.BorderSoft)
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, BorderSoft.copy(alpha = 0.4f))
    }

    Box(
        modifier = modifier
            .handCursor()
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .clip(ChatShapes.quickStartCard)
            .background(cardBg, ChatShapes.quickStartCard)
            .border(cardBorder, ChatShapes.quickStartCard),
    ) {
        Row(
            modifier = Modifier.padding(ChatSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChatSpacing.sm),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isHovered) com.inspiredandroid.kai.ui.AccentPrimary else AccentPrimary,
                modifier = Modifier.size(20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.label,
                    style = KaiTypography.labelLarge,
                    color = TextPrimary,
                )
                Text(
                    text = item.subtext,
                    style = KaiTypography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}
