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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.BorderSoft
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.QuickOptionGreen
import com.inspiredandroid.kai.ui.SurfaceRaised
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.UserBubbleBlue
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
 * Empty state for Brox Kai 2.0 workspace.
 * Features prominent Quick Options with Bengali + English mix,
 * green Search Web option, and larger Ask a Question / Set Reminder cards.
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
                buildAnnotatedString {
                    append(prefixText)
                    withLink(
                        LinkAnnotation.Url(
                            url = "https://schubert-simon.de/privacy/kai.txt",
                        ),
                    ) {
                        withStyle(style = SpanStyle(color = linkColor)) {
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
    val isProminent: Boolean = false,
    val accentColor: Color? = null,
)

@Composable
private fun QuickStartCards(onQuickStart: (String) -> Unit) {
    val items = listOf(
        QuickStartItem(
            label = "Ask a Question",
            subtext = "প্রশ্ন জিজ্ঞাসা করো",
            prompt = "তুমি কে এবং তোমার মডেলের নাম কি?",
            icon = Icons.Default.Edit,
            isProminent = true,
            accentColor = UserBubbleBlue,
        ),
        QuickStartItem(
            label = "Set Reminder",
            subtext = "রিমাইন্ডার সেট করো",
            prompt = "Set a reminder for me in 10 minutes",
            icon = Icons.Default.Build,
            isProminent = true,
            accentColor = UserBubbleBlue,
        ),
        QuickStartItem(
            label = "Search Web",
            subtext = "ওয়েব সার্চ করো",
            prompt = "Search the web for latest AI news",
            icon = Icons.Default.Search,
            isProminent = false,
            accentColor = QuickOptionGreen,
        ),
        QuickStartItem(
            label = "Code",
            subtext = "একটা কোড লেখো",
            prompt = "একটা কোড লেখো",
            icon = Icons.Default.Code,
            isProminent = false,
            accentColor = UserBubbleBlue,
        ),
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
    val cardColor = item.accentColor ?: UserBubbleBlue

    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = if (isHovered) {
        BorderStroke(1.dp, cardColor)
    } else {
        BorderStroke(1.dp, cardColor.copy(alpha = if (item.isProminent) 0.6f else 0.4f))
    }

    val cardPadding = if (item.isProminent) 16.dp else ChatSpacing.md
    val minHeight = if (item.isProminent) 76.dp else 60.dp

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .handCursor()
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .clip(ChatShapes.quickStartCard)
            .background(cardBg, ChatShapes.quickStartCard)
            .border(cardBorder, ChatShapes.quickStartCard),
    ) {
        Row(
            modifier = Modifier.padding(cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChatSpacing.sm),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isHovered) cardColor else cardColor.copy(alpha = 0.9f),
                modifier = Modifier.size(if (item.isProminent) 24.dp else 20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.label,
                    style = if (item.isProminent) KaiTypography.labelLarge.copy(fontWeight = FontWeight.Bold) else KaiTypography.labelLarge,
                    color = if (item.accentColor == QuickOptionGreen) QuickOptionGreen else TextPrimary,
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
