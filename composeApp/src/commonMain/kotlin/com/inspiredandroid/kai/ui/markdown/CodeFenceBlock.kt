package com.inspiredandroid.kai.ui.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.ui.AccentGlow
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.BorderSoft
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.OledBlack
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.isOledFlavor
import com.inspiredandroid.kai.ui.KaiShapes
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.rememberCopyToClipboard

@Composable
internal fun CodeFenceBlock(
    language: String?,
    code: String,
    modifier: Modifier = Modifier,
    onOpenInEditor: ((String, String?) -> Unit)? = null,
    onRunInSandbox: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isOled = colorScheme.isOledFlavor
    val highlightColors = remember(colorScheme) { codeHighlightColors(colorScheme) }
    val highlighted = remember(code, language, highlightColors) {
        highlightCode(code, language, highlightColors)
    }
    val copyToClipboard = rememberCopyToClipboard()

    val surfaceBg = CardFill
    val surfaceBorder = if (isOled) {
        BorderSoft
    } else {
        BorderSoft.copy(alpha = 0.2f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(KaiShapes.card)
            .background(surfaceBg, KaiShapes.card)
            .border(androidx.compose.foundation.BorderStroke(1.dp, surfaceBorder), KaiShapes.card),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = ChatSpacing.md, end = ChatSpacing.xs, top = ChatSpacing.sm, bottom = ChatSpacing.xs),
            ) {
                Text(
                    text = language?.takeIf { it.isNotBlank() } ?: "",
                    style = KaiTypography.labelSmall,
                    color = if (isOled) AccentPrimary else TextSecondary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (onOpenInEditor != null) {
                        IconButton(
                            onClick = { onOpenInEditor(code, language) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Open in editor",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    // Run in Sandbox button
                    if (onRunInSandbox != null) {
                        TextButton(
                            onClick = onRunInSandbox,
                            modifier = Modifier
                                .height(28.dp)
                                .padding(horizontal = 8.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = AccentPrimary.copy(alpha = 0.15f),
                                contentColor = AccentPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Run in Sandbox",
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Run in Sandbox",
                                style = KaiTypography.labelSmall,
                                color = AccentPrimary,
                            )
                        }
                    }
                    IconButton(
                        onClick = { copyToClipboard(code) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy code",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            HorizontalDivider(color = surfaceBorder)
            val scroll = rememberScrollState()
            Box(Modifier.horizontalScroll(scroll).padding(ChatSpacing.md)) {
                Text(
                    text = highlighted,
                    fontFamily = FontFamily.Monospace,
                    style = KaiTypography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}
