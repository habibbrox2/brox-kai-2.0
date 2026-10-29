package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.prefersReducedMotion
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.KaiShapes
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextSecondary
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.tools_count
import kai.composeapp.generated.resources.waiting_brewing
import kai.composeapp.generated.resources.waiting_content_description
import kai.composeapp.generated.resources.waiting_thinking
import kai.composeapp.generated.resources.waiting_working
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun toolSummaryText(
    executingTools: ImmutableList<Pair<String, String>>,
): String? = when {
    executingTools.isEmpty() -> null
    executingTools.size == 1 -> executingTools.first().second
    else -> stringResource(Res.string.tools_count, executingTools.size)
}

@Composable
internal fun WaitingResponseRow(
    executingTools: ImmutableList<Pair<String, String>>,
    isStatusOnly: Boolean = false,
    statusText: String? = null,
) {
    val summary = statusText ?: toolSummaryText(executingTools)
    val effectiveStatusOnly = isStatusOnly || statusText != null
    val waitingCd = stringResource(Res.string.waiting_content_description)

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clipToBounds(),
    ) {
        Box(
            modifier = Modifier
                .background(
                    CardFill,
                    KaiShapes.xs,
                )
                .animateContentSize(
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                )
                .padding(12.dp)
                .semantics { contentDescription = waitingCd },
        ) {
            PulsingStatusIndicator(
                toolSummary = summary,
                isStatusOnly = effectiveStatusOnly,
                dotSize = 16.dp,
                dotColor = TextSecondary,
                textColor = TextSecondary,
                textStyle = KaiTypography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun PulsingStatusIndicator(
    toolSummary: String?,
    dotSize: Dp,
    dotColor: Color,
    textColor: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    isStatusOnly: Boolean = false,
) {
    val reducedMotion = prefersReducedMotion()

    val infiniteTransition = if (reducedMotion) null else rememberInfiniteTransition(label = "toolStatusPulse")
    val pulseScale: Float
    val pulseAlpha: Float
    if (infiniteTransition == null) {
        pulseScale = 1.0f
        pulseAlpha = 1.0f
    } else {
        pulseScale = infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        ).value
        pulseAlpha = infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        ).value
    }
    val waitingTexts = remember {
        listOf(
            Res.string.waiting_thinking,
            Res.string.waiting_working,
            Res.string.waiting_brewing,
        )
    }
    val index: Int = if (reducedMotion) {
        0
    } else {
        val progress = rememberInfiniteTransition(label = "waitingTextIndex").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, delayMillis = 0, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "textIndex",
        ).value
        (progress * waitingTexts.size).toInt().coerceIn(0, waitingTexts.size - 1)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha = pulseAlpha
                }
                .background(dotColor, CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        if (isStatusOnly && toolSummary != null) {
            Text(
                text = toolSummary,
                color = textColor,
                style = textStyle,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    (fadeIn(tween(300)) togetherWith fadeOut(tween(300)))
                        .using(SizeTransform(clip = false) { _, _ -> tween(300) })
                },
            ) { targetIndex ->
                Text(
                    text = stringResource(waitingTexts[targetIndex]),
                    color = textColor,
                    style = textStyle,
                )
            }
            if (toolSummary != null) {
                // Weighted: the animated status text ahead of this takes what it needs,
                // and without a weight the summary — the only hint of what the agent is
                // actually doing — is pushed off the right edge at large font scales.
                Text(
                    text = " · $toolSummary",
                    modifier = Modifier.weight(1f, fill = false),
                    color = textColor,
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
