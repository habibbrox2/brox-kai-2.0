package com.inspiredandroid.kai.ui.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// The Brox Kai "BK" monogram, authored in the same 512x512 space as the launcher
// icon so the animated mark is the exact artwork used everywhere else.
private const val MARK_CENTER_X = 264.5f
private const val MARK_CENTER_Y = 256f
private const val MARK_HEIGHT = 320f

private val MarkCyan = Color(0xFF00E5FF)
private val MarkBlue = Color(0xFF1E88E5)
private val GlowCyan = Color(0x4000E5FF)

private val broxMarkB: Path by lazy {
    Path().apply {
        // Outer "B"
        moveTo(104f, 96f)
        lineTo(240f, 96f)
        cubicTo(300f, 96f, 333f, 129f, 333f, 176f)
        cubicTo(333f, 207f, 317f, 230f, 291f, 244f)
        cubicTo(328f, 257f, 349f, 284f, 349f, 321f)
        cubicTo(349f, 377f, 309f, 416f, 246f, 416f)
        lineTo(104f, 416f)
        lineTo(104f, 96f)
        close()
        // Upper counter
        moveTo(176f, 158f)
        lineTo(176f, 222f)
        lineTo(232f, 222f)
        cubicTo(252f, 222f, 264f, 210f, 264f, 190f)
        cubicTo(264f, 170f, 252f, 158f, 232f, 158f)
        lineTo(176f, 158f)
        close()
        // Lower counter
        moveTo(176f, 280f)
        lineTo(176f, 354f)
        lineTo(240f, 354f)
        cubicTo(263f, 354f, 276f, 340f, 276f, 317f)
        cubicTo(276f, 294f, 262f, 280f, 239f, 280f)
        lineTo(176f, 280f)
        close()
    }
}

private val broxMarkK: Path by lazy {
    Path().apply {
        moveTo(354f, 96f)
        lineTo(422f, 96f)
        lineTo(328f, 253f)
        lineTo(425f, 416f)
        lineTo(346f, 416f)
        lineTo(270f, 287f)
        lineTo(354f, 96f)
        close()
    }
}

/**
 * The Brox Kai mark, reimagined with a premium glow and fluid motion.
 * The two halves of the BK monogram elegantly drift apart and converge
 * while breathing with a subtle pulse for a polished, futuristic look.
 */
@Composable
fun LogoAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnimation")
    
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logoDrift",
    )
    
    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logoBreath",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(size * 1.15f)
                .scale(1f + 0.02f * breath),
        ) {
            val glowAlpha = 0.15f + 0.25f * breath
            val glowRadius = this.size.minDimension * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlowCyan.copy(alpha = glowAlpha),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = glowRadius,
                ),
            )
        }
        
        Canvas(modifier = Modifier.size(size)) {
            val breathScale = 1f + 0.015f * breath
            val scale = this.size.height * 0.68f / MARK_HEIGHT * breathScale
            val centerX = this.size.width / 2f
            val centerY = this.size.height / 2f
            val driftOffset = this.size.width * 0.08f * drift
            val strokeWidth = scale * 2.5f

            fun drawHalf(path: Path, color: Color, offsetX: Float, isLeft: Boolean) {
                withTransform({
                    translate(centerX + offsetX, centerY)
                    translate(-MARK_CENTER_X, -MARK_CENTER_Y)
                    scale(scale, scale, Offset.Zero)
                }) {
                    // Subtle glow stroke
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.3f),
                        style = Stroke(width = strokeWidth),
                    )
                    // Main fill
                    drawPath(path = path, color = color)
                }
            }

            // B is the left half, K the right half; they elegantly part and reunite.
            drawHalf(broxMarkB, MarkCyan, -driftOffset, true)
            drawHalf(broxMarkK, MarkBlue, driftOffset, false)
        }
    }
}
