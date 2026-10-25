package com.inspiredandroid.kai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.BorderSoft
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.DangerAlert
import com.inspiredandroid.kai.ui.KaiShapes
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.SuccessGreen
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.WarningAmber

/**
 * Small status indicator pill used for tags, status labels, and counts.
 *
 * @param label Display text
 * @param variant Color variant: default, success, warning, error, or accent
 * @param modifier Compose modifier
 */
@Composable
fun KaiBadge(
    label: String,
    variant: BadgeVariant = BadgeVariant.Default,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor, borderColor) = badgeColors(variant)

    Box(
        modifier = modifier
            .border(BorderStroke(0.5.dp, borderColor), KaiShapes.pill)
            .defaultMinSize(minHeight = 24.dp)
            .clip(KaiShapes.pill)
            .background(bgColor),
    ) {
        Text(
            text = label,
            style = KaiTypography.labelSmall,
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

private fun badgeColors(
    variant: BadgeVariant,
): Triple<Color, Color, Color> = when (variant) {
    BadgeVariant.Success -> Triple(SuccessGreen, TextPrimary, SuccessGreen.copy(alpha = 0.4f))
    BadgeVariant.Warning -> Triple(WarningAmber, TextPrimary, WarningAmber.copy(alpha = 0.4f))
    BadgeVariant.Error -> Triple(DangerAlert, TextPrimary, DangerAlert.copy(alpha = 0.4f))
    BadgeVariant.Accent -> Triple(AccentPrimary, TextPrimary, AccentPrimary.copy(alpha = 0.4f))
    BadgeVariant.Default -> Triple(CardFill, TextSecondary, BorderSoft)
}

/** Color variants for KaiBadge. */
enum class BadgeVariant {
    Default,
    Success,
    Warning,
    Error,
    Accent,
}
