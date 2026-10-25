package com.inspiredandroid.kai.ui.chat.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.chat.activities.ActivityDetails
import com.inspiredandroid.kai.ui.chat.activities.ActivityStatus
import com.inspiredandroid.kai.ui.chat.activities.ActivityType
import com.inspiredandroid.kai.ui.chat.activities.AgentActivity
import com.inspiredandroid.kai.ui.AccentPrimary
import com.inspiredandroid.kai.ui.CardFill
import com.inspiredandroid.kai.ui.DangerAlert
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.KaiShapes
import com.inspiredandroid.kai.ui.KaiTypography
import com.inspiredandroid.kai.ui.TextPrimary
import com.inspiredandroid.kai.ui.TextSecondary

@Composable
fun AgentActivityTimeline(
    activities: List<AgentActivity>,
    modifier: Modifier = Modifier,
    showSummary: Boolean = true,
) {
    if (activities.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showSummary && activities.isNotEmpty()) {
            val running = activities.count { it.status == ActivityStatus.RUNNING }
            val completed = activities.count { it.status == ActivityStatus.COMPLETED }
            val failed = activities.count { it.status == ActivityStatus.FAILED }
            Text(
                text = buildString {
                    append("Agent activity")
                    if (running > 0) append(" • $running running")
                    if (completed > 0) append(" • $completed done")
                    if (failed > 0) append(" • $failed failed")
                },
                style = KaiTypography.labelMedium,
                color = TextSecondary,
            )
        }

        activities.forEach { activity ->
            AgentActivityItem(activity = activity)
        }
    }
}

@Composable
private fun AgentActivityItem(
    activity: AgentActivity,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(activity.isExpanded) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(KaiShapes.row)
            .background(CardFill)
            .clickable(enabled = activity.isExpandable) {
                if (activity.isExpandable) expanded = !expanded
            }
            .handCursor(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActivityStatusIndicator(status = activity.status, type = activity.type)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = activity.title,
                    style = KaiTypography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                activity.description?.let { desc ->
                    Text(
                        text = desc,
                        style = KaiTypography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (activity.isExpandable) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && activity.isExpandable,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActivityDetailsView(details = activity.details)
            }
        }
    }
}

@Composable
private fun ActivityStatusIndicator(
    status: ActivityStatus,
    type: ActivityType,
    modifier: Modifier = Modifier,
) {
    val (color, _) = when (status) {
        ActivityStatus.PENDING -> TextSecondary to ""
        ActivityStatus.RUNNING -> AccentPrimary to ""
        ActivityStatus.COMPLETED -> AccentPrimary to ""
        ActivityStatus.FAILED -> DangerAlert to ""
        ActivityStatus.CANCELLED -> TextSecondary to ""
        ActivityStatus.DENIED -> DangerAlert to ""
        ActivityStatus.TIMEOUT -> DangerAlert to ""
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
private fun ActivityDetailsView(
    details: ActivityDetails,
    modifier: Modifier = Modifier,
) {
    when (details) {
        is ActivityDetails.ToolCall -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                details.arguments?.let { args ->
                    Text(
                        text = "Arguments:",
                        style = KaiTypography.labelSmall,
                        color = TextSecondary,
                    )
                    Text(
                        text = args,
                        style = KaiTypography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KaiShapes.xs)
                            .background(CardFill)
                            .padding(8.dp),
                    )
                }
                details.result?.let { result ->
                    Text(
                        text = "Result:",
                        style = KaiTypography.labelSmall,
                        color = TextSecondary,
                    )
                    Text(
                        text = result.take(500) + if (result.length > 500) "..." else "",
                        style = KaiTypography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KaiShapes.xs)
                            .background(CardFill)
                            .padding(8.dp),
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                details.error?.let { error ->
                    Text(
                        text = "Error: $error",
                        style = KaiTypography.bodySmall,
                        color = DangerAlert,
                    )
                }
            }
        }

        is ActivityDetails.ShellCommand -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$ ${details.command}",
                    style = KaiTypography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KaiShapes.xs)
                        .background(CardFill)
                        .padding(8.dp),
                )
                details.workingDirectory?.let { cwd ->
                    Text(
                        text = "cwd: $cwd",
                        style = KaiTypography.labelSmall,
                        color = TextSecondary,
                    )
                }
                details.stdout?.let { stdout ->
                    if (stdout.isNotEmpty()) {
                        Text(
                            text = stdout,
                            style = KaiTypography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KaiShapes.xs)
                                .background(CardFill)
                                .padding(8.dp),
                            maxLines = 15,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                details.stderr?.let { stderr ->
                    if (stderr.isNotEmpty()) {
                        Text(
                            text = stderr,
                            style = KaiTypography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = DangerAlert,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KaiShapes.xs)
                                .background(CardFill)
                                .padding(8.dp),
                            maxLines = 10,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                details.exitCode?.let { code ->
                    Text(
                        text = "Exit code: $code",
                        style = KaiTypography.labelSmall,
                        color = if (code == 0) AccentPrimary else DangerAlert,
                    )
                }
            }
        }

        is ActivityDetails.Skill -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Skill: ${details.skillName} (${details.skillId})",
                    style = KaiTypography.bodySmall,
                    color = TextPrimary,
                )
                details.description?.let { desc ->
                    Text(
                        text = desc,
                        style = KaiTypography.bodySmall,
                        color = TextSecondary,
                    )
                }
                details.stage?.let { stage ->
                    Text(
                        text = "Stage: $stage",
                        style = KaiTypography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        is ActivityDetails.FileOperation -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${details.operation}: ${details.path}",
                    style = KaiTypography.bodySmall,
                    color = TextPrimary,
                )
                details.changeSummary?.let { summary ->
                    Text(
                        text = summary,
                        style = KaiTypography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        is ActivityDetails.Permission -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Action: ${details.action}",
                    style = KaiTypography.bodySmall,
                    color = TextPrimary,
                )
                Text(
                    text = "Target: ${details.target}",
                    style = KaiTypography.bodySmall,
                    color = TextPrimary,
                )
                details.scope?.let { scope ->
                    Text(
                        text = "Scope: $scope",
                        style = KaiTypography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        is ActivityDetails.Generic -> {
            details.content?.let { content ->
                Text(
                    text = content,
                    style = KaiTypography.bodySmall,
                    color = TextPrimary,
                )
            }
        }
    }
}
