package com.inspiredandroid.kai.ui.chat.activities

import com.inspiredandroid.kai.ui.chat.History
import com.inspiredandroid.kai.ui.chat.ToolCallInfo
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Clock

object ActivityMapper {
    fun historyToActivities(
        history: List<History>,
        getToolDisplayName: suspend (String) -> String = { it },
    ): List<AgentActivity> {
        val activities = mutableListOf<AgentActivity>()
        val toolExecutingMap = mutableMapOf<String, AgentActivity>()

        for (entry in history) {
            when (entry.role) {
                History.Role.USER -> {
                    // User messages aren't "agent activities" in the same sense
                    // Skip or could add as context
                }

                History.Role.ASSISTANT -> {
                    if (entry.toolCalls != null && entry.toolCalls.isNotEmpty()) {
                        // Assistant is making tool calls
                        for (toolCall in entry.toolCalls) {
                            val activity = AgentActivity(
                                type = if (toolCall.name == "execute_shell_command") {
                                    ActivityType.SHELL_COMMAND
                                } else {
                                    ActivityType.TOOL_CALL
                                },
                                title = if (toolCall.name == "execute_shell_command") {
                                    "Executing shell command"
                                } else {
                                    "Calling tool: ${toolCall.name}"
                                },
                                status = ActivityStatus.PENDING,
                                details = if (toolCall.name == "execute_shell_command") {
                                    parseShellCommand(toolCall.arguments)
                                } else {
                                    ActivityDetails.ToolCall(
                                        toolName = toolCall.name,
                                        arguments = toolCall.arguments,
                                    )
                                },
                            )
                            activities.add(activity)
                        }
                    } else if (entry.content.isNotEmpty() && !entry.isThinking) {
                        // Regular assistant response - could be treated as completion/progress
                    }
                }

                History.Role.TOOL_EXECUTING -> {
                    val toolName = entry.content
                    val activity = AgentActivity(
                        type = if (toolName == "execute_shell_command") {
                            ActivityType.SHELL_COMMAND
                        } else {
                            ActivityType.TOOL_CALL
                        },
                        title = if (toolName == "execute_shell_command") {
                            "Executing shell command"
                        } else {
                            "Calling tool: $toolName"
                        },
                        status = ActivityStatus.RUNNING,
                        details = if (toolName == "execute_shell_command") {
                            ActivityDetails.ShellCommand(command = entry.toolName ?: "")
                        } else {
                            ActivityDetails.ToolCall(toolName = toolName)
                        },
                    )
                    toolExecutingMap[entry.id] = activity
                    activities.add(activity)
                }

                History.Role.TOOL -> {
                    // Find matching running activity and mark as completed
                    val toolName = entry.toolName ?: ""
                    // Try to find and update existing activity
                    val existingIndex = activities.indexOfLast {
                        it.status == ActivityStatus.RUNNING &&
                            (
                                (it.details is ActivityDetails.ToolCall && it.details.toolName == toolName) ||
                                    (it.details is ActivityDetails.ShellCommand && toolName == "execute_shell_command")
                                )
                    }
                    if (existingIndex >= 0) {
                        val existing = activities[existingIndex]
                        val updated = existing.copy(
                            status = if (entry.content.contains("\"success\":false") || entry.content.contains("\"error\"")) {
                                ActivityStatus.FAILED
                            } else {
                                ActivityStatus.COMPLETED
                            },
                            endTime = Clock.System.now().toEpochMilliseconds(),
                            durationMs = Clock.System.now().toEpochMilliseconds() - existing.startTime,
                            details = when (existing.details) {
                                is ActivityDetails.ToolCall -> existing.details.copy(result = entry.content)
                                is ActivityDetails.ShellCommand -> parseShellResult(existing.details, entry.content)
                                else -> existing.details
                            },
                        )
                        activities[existingIndex] = updated
                    } else {
                        // Create a completed activity if not found
                        val activity = AgentActivity(
                            type = if (toolName == "execute_shell_command") {
                                ActivityType.SHELL_COMMAND
                            } else {
                                ActivityType.TOOL_CALL
                            },
                            title = if (toolName == "execute_shell_command") {
                                "Shell command"
                            } else {
                                "Tool: $toolName"
                            },
                            status = if (entry.content.contains("\"success\":false") || entry.content.contains("\"error\"")) {
                                ActivityStatus.FAILED
                            } else {
                                ActivityStatus.COMPLETED
                            },
                            endTime = Clock.System.now().toEpochMilliseconds(),
                            details = if (toolName == "execute_shell_command") {
                                parseShellResult(ActivityDetails.ShellCommand(command = ""), entry.content)
                            } else {
                                ActivityDetails.ToolCall(toolName = toolName, result = entry.content)
                            },
                        )
                        activities.add(activity)
                    }
                }
            }
        }
        return activities
    }

    private fun parseShellCommand(argsJson: String): ActivityDetails.ShellCommand = try {
        // Simple parsing - in real implementation would use proper JSON parsing
        val commandMatch = Regex("\"command\"\\s*:\\s*\"([^\"]+)\"").find(argsJson)
        val cwdMatch = Regex("\"working_dir\"\\s*:\\s*\"([^\"]+)\"").find(argsJson)
        ActivityDetails.ShellCommand(
            command = commandMatch?.groupValues?.get(1) ?: argsJson,
            workingDirectory = cwdMatch?.groupValues?.get(1),
        )
    } catch (e: Exception) {
        ActivityDetails.ShellCommand(command = argsJson)
    }

    private fun parseShellResult(details: ActivityDetails.ShellCommand, resultJson: String): ActivityDetails.ShellCommand = try {
        val stdoutMatch = Regex("\"stdout\"\\s*:\\s*\"([^\"]*)\"").find(resultJson)
        val stderrMatch = Regex("\"stderr\"\\s*:\\s*\"([^\"]*)\"").find(resultJson)
        val exitCodeMatch = Regex("\"exitCode\"\\s*:\\s*(-?\\d+)").find(resultJson)
        details.copy(
            stdout = stdoutMatch?.groupValues?.get(1)?.replace("\\n", "\n"),
            stderr = stderrMatch?.groupValues?.get(1)?.replace("\\n", "\n"),
            exitCode = exitCodeMatch?.groupValues?.get(1)?.toIntOrNull(),
        )
    } catch (e: Exception) {
        details.copy(stdout = resultJson)
    }
}
