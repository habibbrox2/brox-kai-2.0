package com.inspiredandroid.kai.data

class ToolPermissionManager(
    private val appSettings: AppSettings,
) {
    fun classifyToolRisk(toolName: String, args: Map<String, Any> = emptyMap()): ToolRiskLevel = when (toolName) {
        // Read-only/info gathering tools
        "web_search",
        "get_time",
        "get_current_location",
        "get_weather",
        "read_email",
        "search_emails",
        "list_emails",
        "get_scheduled_tasks",
        "get_heartbeat_config",
        "get_heartbeat_log",
        "get_memory",
        "get_conversations",
        "get_service_status",
        "get_model_info",
        "gallery_search",
        "gallery_status",
        "downloads_status",
        "get_notifications",
        -> ToolRiskLevel.READ_ONLY

        // External requests
        "fetch_url" -> ToolRiskLevel.EXTERNAL_REQUEST

        "web_search" -> ToolRiskLevel.EXTERNAL_REQUEST

        // System/shell commands
        "execute_shell_command" -> ToolRiskLevel.SYSTEM_COMMAND

        // File operations - check args for delete/write
        "write_file",
        "save_file",
        "create_file",
        -> {
            val path = args["path"]?.toString() ?: args["filename"]?.toString() ?: ""
            if (path.contains("delete") || args.containsKey("delete")) {
                ToolRiskLevel.FILE_DELETE
            } else {
                ToolRiskLevel.FILE_WRITE
            }
        }

        "delete_file",
        "remove_file",
        "delete",
        -> ToolRiskLevel.FILE_DELETE

        // Data scraping
        "scrape_url",
        "crawl",
        -> ToolRiskLevel.DATA_SCRAPING

        else -> {
            // Heuristics based on name
            val lower = toolName.lowercase()
            when {
                lower.contains("delete") || lower.contains("remove") -> ToolRiskLevel.FILE_DELETE
                lower.contains("write") || lower.contains("save") || lower.contains("create") || lower.contains("upload") -> ToolRiskLevel.FILE_WRITE
                lower.contains("fetch") || lower.contains("request") || lower.contains("http") -> ToolRiskLevel.EXTERNAL_REQUEST
                lower.contains("search") && !lower.contains("local") -> ToolRiskLevel.DATA_SCRAPING
                lower.contains("shell") || lower.contains("exec") || lower.contains("command") -> ToolRiskLevel.SYSTEM_COMMAND
                lower.contains("get") || lower.contains("list") || lower.contains("read") || lower.contains("find") || lower.contains("view") -> ToolRiskLevel.READ_ONLY
                else -> ToolRiskLevel.OTHER
            }
        }
    }

    fun shouldAskForPermission(toolName: String, args: Map<String, Any> = emptyMap()): Boolean {
        val riskLevel = classifyToolRisk(toolName, args)
        // Read-only tools run directly
        if (riskLevel == ToolRiskLevel.READ_ONLY) {
            return false
        }
        // Check stored permission
        val permission = appSettings.getToolPermission(toolName, getScope(toolName, args))
        return when (permission?.policy) {
            ToolPermissionPolicy.ALWAYS_ALLOW -> false

            ToolPermissionPolicy.ALLOW_ONCE -> {
                // Remove the once permission after using it
                // We'll handle this in the caller
                false
            }

            ToolPermissionPolicy.ASK_ALWAYS, null -> true
        }
    }

    fun getScope(toolName: String, args: Map<String, Any>): String? = when (toolName) {
        "fetch_url", "web_search", "scrape_url", "crawl" -> {
            val url = args["url"]?.toString() ?: args["query"]?.toString()
            url?.let { extractDomain(it) }
        }

        else -> null
    }

    private fun extractDomain(url: String): String = try {
        val withoutScheme = url.substringAfter("://")
        withoutScheme.substringBefore("/").substringBefore(":")
    } catch (_: Exception) {
        url
    }

    fun recordAllowOnce(toolName: String, scope: String?) {
        appSettings.setToolPermission(
            ToolPermission(
                toolName = toolName,
                scope = scope,
                policy = ToolPermissionPolicy.ALLOW_ONCE,
            ),
        )
    }

    fun recordAlwaysAllow(toolName: String, scope: String?) {
        appSettings.setToolPermission(
            ToolPermission(
                toolName = toolName,
                scope = scope,
                policy = ToolPermissionPolicy.ALWAYS_ALLOW,
            ),
        )
    }

    fun consumeAllowOnce(toolName: String, scope: String?) {
        val permission = appSettings.getToolPermission(toolName, scope)
        if (permission?.policy == ToolPermissionPolicy.ALLOW_ONCE) {
            // Remove it
            val permissions = appSettings.getToolPermissions().toMutableList()
            permissions.removeAll { it.toolName == toolName && it.scope == scope && it.policy == ToolPermissionPolicy.ALLOW_ONCE }
            appSettings.setToolPermissions(permissions)
        }
    }
}
