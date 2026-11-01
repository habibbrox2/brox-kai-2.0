package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextSecondary
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.ic_flag
import org.jetbrains.compose.resources.painterResource

@Composable
fun ToolConfirmationCard(
    toolName: String,
    toolCallId: String,
    arguments: String,
    onDeny: () -> Unit,
    onAllowOnce: () -> Unit,
    onAllowSession: () -> Unit = {},
    onAlwaysAllow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayName = toolName.replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }

    val targetInfo = extractTargetInfo(toolName, arguments)

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFFF59E0B).copy(alpha = 0.3f),
                spotColor = Color(0xFFF59E0B).copy(alpha = 0.3f),
            )
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                RoundedCornerShape(12.dp),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_flag),
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tool Request: $displayName",
                    style = KaiTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (targetInfo.isNotEmpty()) {
                    Text(
                        text = targetInfo,
                        style = KaiTypography.labelSmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onDeny,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            ) {
                Text("Deny", style = KaiTypography.labelMedium)
            }
            Button(
                onClick = onAllowOnce,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary.copy(alpha = 0.8f),
                ),
            ) {
                Text("Allow once", style = KaiTypography.labelMedium)
            }
            Button(
                onClick = onAllowSession,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1),
                ),
            ) {
                Text("Allow session", style = KaiTypography.labelMedium)
            }
            Button(
                onClick = onAlwaysAllow,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                ),
            ) {
                Text("Always allow", style = KaiTypography.labelMedium)
            }
        }
    }
}

private fun extractTargetInfo(toolName: String, arguments: String): String = try {
    when (toolName) {
        "fetch_url" -> {
            val urlMatch = Regex("\"url\"\\s*:\\s*\"([^\"]+)\"").find(arguments)
            val url = urlMatch?.groupValues?.get(1) ?: ""
            if (url.isNotEmpty()) "Fetch: $url" else ""
        }

        "web_search" -> {
            val queryMatch = Regex("\"query\"\\s*:\\s*\"([^\"]+)\"").find(arguments)
            val query = queryMatch?.groupValues?.get(1) ?: ""
            if (query.isNotEmpty()) "Search: $query" else ""
        }

        "execute_shell_command" -> {
            val cmdMatch = Regex("\"command\"\\s*:\\s*\"([^\"]+)\"").find(arguments)
            val cmd = cmdMatch?.groupValues?.get(1) ?: ""
            if (cmd.isNotEmpty()) "Command: $cmd" else "Shell command"
        }

        else -> {
            // Try to find url or common params
            val urlMatch = Regex("\"url\"\\s*:\\s*\"([^\"]+)\"").find(arguments)
            if (urlMatch != null) {
                "Target: ${urlMatch.groupValues[1]}"
            } else {
                ""
            }
        }
    }
} catch (_: Exception) {
    ""
}
