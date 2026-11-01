package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.theme.DesignTokens
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.logo
import kai.composeapp.generated.resources.privacy_agree_prefix
import kai.composeapp.generated.resources.privacy_policy
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * 2027 Premium AI Agent Workspace Home / Welcome State.
 *
 * Implements Screen 1 from docs/ui-upgrade-plan-7-premium-chat.md.
 * Features central BK Monogram branding with ambient violet/cyan radial aura,
 * title, and responsive quick-start action chips.
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
        // ─── Ambient Glow Brand Presentation ───
        BroxKaiHeroBrand()

        Spacer(Modifier.height(ChatSpacing.lg))

        Text(
            text = "Brox Kai 2.0",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = KaiTypography.displaySmall,
            textAlign = TextAlign.Center,
            color = DesignTokens.PrimaryText,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )

        Spacer(Modifier.height(ChatSpacing.xs))

        Text(
            text = "Premium AI Agent Workspace • প্রস্তুত আপনার সহায়তায়",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = KaiTypography.titleMedium,
            textAlign = TextAlign.Center,
            color = DesignTokens.SecondaryText,
        )

        if (onQuickStart != null) {
            Spacer(Modifier.height(ChatSpacing.xl))
            QuickStartCards(onQuickStart = onQuickStart)
        }

        if (isUsingSharedKey) {
            Spacer(Modifier.height(ChatSpacing.xl))
            val linkColor = DesignTokens.PrimaryCyan
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
                color = DesignTokens.TertiaryText,
            )
        }
    }
}

/**
 * BK Monogram with smooth ambient radial aura glow.
 */
@Composable
private fun BroxKaiHeroBrand() {
    val infiniteTransition = rememberInfiniteTransition()
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp),
    ) {
        // Radial ambient glow
        Box(
            modifier = Modifier
                .size(108.dp * glowScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DesignTokens.BrandCyan.copy(alpha = 0.25f),
                            DesignTokens.AgentViolet.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        // Monogram Icon Box
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(DesignTokens.RadiusCard))
                .background(DesignTokens.SurfaceElevated)
                .border(BorderStroke(1.dp, DesignTokens.BorderSubtle), RoundedCornerShape(DesignTokens.RadiusCard)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Brox Kai Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(54.dp),
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
            accentColor = DesignTokens.PrimaryCyan,
        ),
        QuickStartItem(
            label = "Resume Project / RPG",
            subtext = "প্রজেক্ট চালনা করো",
            prompt = "Continue with my current project and summarize progress",
            icon = Icons.Default.Code,
            isProminent = true,
            accentColor = DesignTokens.AgentViolet,
        ),
        QuickStartItem(
            label = "Sandbox Check",
            subtext = "স্যান্ডবক্স কমান্ড রান করো",
            prompt = "Check sandbox environment and available tools",
            icon = Icons.Default.Terminal,
            isProminent = false,
            accentColor = DesignTokens.BrandCyan,
        ),
        QuickStartItem(
            label = "Daily Reminders",
            subtext = "রিমাইন্ডার ও কাজ",
            prompt = "Set a reminder for me in 15 minutes",
            icon = Icons.Default.Build,
            isProminent = false,
            accentColor = DesignTokens.Success,
        ),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(ChatSpacing.sm),
    ) {
        items.forEach { item ->
            QuickStartCard(
                item = item,
                onClick = { onQuickStart(item.prompt) },
                modifier = Modifier.widthIn(min = 200.dp, max = 260.dp),
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
    val cardAccent = item.accentColor ?: DesignTokens.PrimaryCyan
    val cardBg = DesignTokens.SurfaceElevated
    val cardShape = RoundedCornerShape(DesignTokens.RadiusCard)

    val cardBorder = if (isHovered) {
        BorderStroke(1.dp, cardAccent)
    } else {
        BorderStroke(1.dp, DesignTokens.BorderSubtle)
    }

    val cardPadding = if (item.isProminent) 16.dp else ChatSpacing.md
    val minHeight = if (item.isProminent) 76.dp else 60.dp

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .handCursor()
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .clip(cardShape)
            .background(cardBg, cardShape)
            .border(cardBorder, cardShape),
    ) {
        Row(
            modifier = Modifier.padding(cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChatSpacing.sm),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isHovered) cardAccent else cardAccent.copy(alpha = 0.85f),
                modifier = Modifier.size(if (item.isProminent) 24.dp else 20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.label,
                    style = if (item.isProminent) KaiTypography.labelLarge.copy(fontWeight = FontWeight.Bold) else KaiTypography.labelLarge,
                    color = DesignTokens.PrimaryText,
                )
                Text(
                    text = item.subtext,
                    style = KaiTypography.bodySmall,
                    color = DesignTokens.SecondaryText,
                )
            }
        }
    }
}
