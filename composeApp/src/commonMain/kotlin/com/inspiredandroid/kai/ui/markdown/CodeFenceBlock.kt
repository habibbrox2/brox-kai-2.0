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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.rememberCopyToClipboard
import kotlinx.coroutines.delay

@Composable
internal fun CodeFenceBlock(
    language: String?,
    code: String,
    modifier: Modifier = Modifier,
    onOpenInEditor: ((String, String?) -> Unit)? = null,
    onRunInSandbox: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val highlightColors = remember(colorScheme) { codeHighlightColors(colorScheme) }
    val highlighted = remember(code, language, highlightColors) {
        highlightCode(code, language, highlightColors)
    }
    val copyToClipboard = rememberCopyToClipboard()
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    // Claude Code Style Colors:
    // Container: #0A0E14 with border #1E293B
    // Header Bar: #161B22 with border-bottom #1E293B
    val containerBg = Color(0xFF0A0E14)
    val headerBg = Color(0xFF161B22)
    val borderColor = Color(0xFF1E293B)
    val mutedText = Color(0xFF94A3B8)
    val lineNumberColor = Color(0xFF475569)
    val blockShape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(blockShape)
            .background(containerBg, blockShape)
            .border(androidx.compose.foundation.BorderStroke(1.dp, borderColor), blockShape),
    ) {
        Column {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .border(androidx.compose.foundation.BorderStroke(1.dp, borderColor))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                // Left side: Terminal icon + language badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = mutedText,
                    )
                    Text(
                        text = (language?.takeIf { it.isNotBlank() } ?: "code").lowercase(),
                        style = KaiTypography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = mutedText,
                    )
                }

                // Right side: Edit, Run, and Copy buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (onOpenInEditor != null) {
                        IconButton(
                            onClick = { onOpenInEditor(code, language) },
                            modifier = Modifier.size(28.dp).handCursor(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Open in editor",
                                modifier = Modifier.size(14.dp),
                                tint = mutedText,
                            )
                        }
                    }
                    if (onRunInSandbox != null) {
                        TextButton(
                            onClick = onRunInSandbox,
                            modifier = Modifier
                                .height(26.dp)
                                .padding(horizontal = 6.dp)
                                .handCursor(),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = AccentPrimary.copy(alpha = 0.15f),
                                contentColor = AccentPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Run",
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Run",
                                style = KaiTypography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AccentPrimary,
                            )
                        }
                    }
                    // Copy button with "Copied!" checkmark feedback
                    if (isCopied) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Copied",
                                modifier = Modifier.size(14.dp),
                                tint = AccentPrimary,
                            )
                            Text(
                                text = "Copied!",
                                style = KaiTypography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AccentPrimary,
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                copyToClipboard(code)
                                isCopied = true
                            },
                            modifier = Modifier.size(28.dp).handCursor(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy code",
                                modifier = Modifier.size(14.dp),
                                tint = mutedText,
                            )
                        }
                    }
                }
            }

            // Code Body with Line Numbers & Monospace Font
            val lines = remember(code) { code.lines() }
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(12.dp),
            ) {
                // Line numbers column
                Column(
                    modifier = Modifier.padding(end = 14.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    for (i in 1..lines.size) {
                        Text(
                            text = "$i",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = lineNumberColor,
                            textAlign = TextAlign.End,
                        )
                    }
                }
                // Highlighted Code text
                Text(
                    text = highlighted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFE2E8F0),
                )
            }
        }
    }
}
