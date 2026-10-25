package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.data.Attachment
import com.inspiredandroid.kai.decodeToImageBitmap
import com.inspiredandroid.kai.ui.AccentGradientEnd
import com.inspiredandroid.kai.ui.AccentGradientStart
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.KaiShapes
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.chat.ChatSpacing
import com.inspiredandroid.kai.ui.components.LocalShowFullScreenImage
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.isOledFlavor
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.ic_file
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun UserMessage(
    message: String,
    attachments: ImmutableList<Attachment> = persistentListOf(),
) {
    val showFullScreen = LocalShowFullScreenImage.current
    val isOled = MaterialTheme.colorScheme.isOledFlavor

    SelectionContainer {
        Row(
            Modifier
                .padding(horizontal = ChatSpacing.lg, vertical = ChatSpacing.md)
                .fillMaxWidth(),
        ) {
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .clip(com.inspiredandroid.kai.ui.KaiShapes.userBubble)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                com.inspiredandroid.kai.ui.AccentGradientStart.copy(alpha = 0.95f),
                                com.inspiredandroid.kai.ui.AccentGradientEnd.copy(alpha = 0.9f),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                com.inspiredandroid.kai.ui.AccentPrimary.copy(alpha = 0.6f),
                                com.inspiredandroid.kai.ui.AccentGradientEnd.copy(alpha = 0.4f),
                            ),
                        ),
                        shape = com.inspiredandroid.kai.ui.KaiShapes.userBubble,
                    )
                    .padding(horizontal = ChatSpacing.xl, vertical = ChatSpacing.lg),
                horizontalAlignment = Alignment.End,
            ) {
                val images = attachments.filter { it.mimeType.startsWith("image/") }
                val others = attachments.filter { !it.mimeType.startsWith("image/") }
                for (att in images) {
                    val imageBitmap = remember(att.data) {
                        try {
                            decodeToImageBitmap(Base64.decode(att.data))
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Image attachment",
                            modifier = Modifier
                                .widthIn(max = 200.dp)
                                .clip(KaiShapes.xs)
                                .handCursor()
                                .clickable { showFullScreen(imageBitmap) },
                            contentScale = ContentScale.FillWidth,
                        )
                        Spacer(Modifier.height(ChatSpacing.sm))
                    }
                }
                if (others.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(ChatSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(ChatSpacing.xs),
                    ) {
                        for (att in others) {
                            key("${att.fileName ?: att.mimeType}_${att.data.take(8)}") {
                                val chipColors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isOled) {
                                        com.inspiredandroid.kai.ui.CardFill
                                    } else {
                                        CardFill
                                    },
                                    labelColor = TextPrimary,
                                )
                                SuggestionChip(
                                    onClick = {},
                                    colors = chipColors,
                                    icon = {
                                        Icon(
                                            modifier = Modifier.size(ChatSpacing.lg),
                                            painter = painterResource(Res.drawable.ic_file),
                                            contentDescription = null,
                                            tint = TextPrimary,
                                        )
                                    },
                                    label = { Text(truncateFileName(att.fileName ?: att.mimeType)) },
                                )
                            }
                        }
                    }
                    if (message.isNotEmpty()) {
                        Spacer(Modifier.height(ChatSpacing.sm))
                    }
                }
                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        color = com.inspiredandroid.kai.ui.OledBlack,
                        style = KaiTypography.bodyLarge,
                    )
                }
            }
        }
    }
}
