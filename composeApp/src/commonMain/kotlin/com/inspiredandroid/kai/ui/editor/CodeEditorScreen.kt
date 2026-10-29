package com.inspiredandroid.kai.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.BackgroundPrimary
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.rememberCopyToClipboard

/**
 * Lightweight code editor workspace (Plan 7, Phase 7). Opened from a code block's
 * "Open in editor" action. Presents the code in an editable monospace field with
 * copy support; a full file-tree / terminal workspace is a follow-up.
 */
@Composable
fun CodeEditorScreen(
    code: String,
    language: String?,
    onExit: () -> Unit,
) {
    var text by remember { mutableStateOf(code) }
    val copyToClipboard = rememberCopyToClipboard()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onExit, modifier = Modifier.handCursor()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close editor",
                        tint = TextPrimary,
                    )
                }
                Text(
                    text = "Code Editor",
                    style = KaiTypography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                if (!language.isNullOrBlank()) {
                    Text(
                        text = language,
                        style = KaiTypography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                IconButton(onClick = { copyToClipboard(text) }, modifier = Modifier.handCursor()) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = TextPrimary,
                    )
                }
            }
            val scroll = rememberScrollState()
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
                    .semantics {
                        contentDescription = "Code editor, enter code here"
                    },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    fontFeatureSettings = "tnum",
                ),
                cursorBrush = SolidColor(AccentPrimary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None,
                ),
                decorationBox = { innerTextField ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Enter code here...",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary.copy(alpha = 0.4f),
                                    fontFeatureSettings = "tnum",
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .align(androidx.compose.ui.Alignment.TopStart),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}
