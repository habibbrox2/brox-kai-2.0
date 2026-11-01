package com.inspiredandroid.kai.mcp

import com.inspiredandroid.kai.httpClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class McpClient(
    private val url: String,
    private val headers: Map<String, String>,
    private val allowPrivateIps: Boolean = false,
) {
    init {
        // Fail fast: validate URL at construction time
        val urlResult = McpUrlValidator.validate(url, allowPrivateIps)
        if (!urlResult.isValid) {
            throw McpException("Invalid MCP server URL: ${urlResult.error}")
        }
        val headerResult = McpUrlValidator.validateHeaders(headers)
        if (!headerResult.isValid) {
            throw McpException("Invalid MCP server headers: ${headerResult.error}")
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
        encodeDefaults = true
        explicitNulls = false
    }

    private val client: HttpClient = httpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 10_000
        }
        // Disable automatic redirect following — we validate each redirect target manually
        followRedirects = false
    }
    private var sessionId: String? = null
    private var requestId = 0

    private fun nextId(): Int = ++requestId

    private suspend fun sendRequest(method: String, params: kotlinx.serialization.json.JsonElement? = null): JsonRpcResponse {
        // Connection-time validation (mitigates DNS rebinding; not a guarantee)
        val urlResult = McpUrlValidator.validate(url, allowPrivateIps)
        if (!urlResult.isValid) {
            throw McpException("MCP server URL validation failed: ${urlResult.error}")
        }

        val request = JsonRpcRequest(
            id = nextId(),
            method = method,
            params = params,
        )
        val requestBody = json.encodeToString(JsonRpcRequest.serializer(), request)

        // Manual redirect handling (max 3 hops)
        var currentUrl = url
        var redirectCount = 0
        val maxRedirects = 3

        while (true) {
            val response = client.post(currentUrl) {
                contentType(ContentType.Application.Json)
                header("Accept", "application/json, text/event-stream")
                sessionId?.let { header("Mcp-Session-Id", it) }
                this@McpClient.headers.keys.forEach { key ->
                    header(key, this@McpClient.headers[key]!!)
                }
                setBody(requestBody)
            }

            // Track session ID from response
            response.headers["Mcp-Session-Id"]?.let { sessionId = it }

            // Handle redirects manually
            if (response.status == HttpStatusCode.MovedPermanently ||
                response.status == HttpStatusCode.Found ||
                response.status == HttpStatusCode.SeeOther ||
                response.status == HttpStatusCode.TemporaryRedirect ||
                response.status == HttpStatusCode.PermanentRedirect
            ) {
                redirectCount++
                if (redirectCount > maxRedirects) {
                    throw McpException("Too many redirects (max $maxRedirects)")
                }
                val location = response.headers["Location"]
                    ?: throw McpException("Redirect without Location header")
                // Validate redirect target
                val redirectResult = McpUrlValidator.validate(location, allowPrivateIps)
                if (!redirectResult.isValid) {
                    throw McpException("Redirect target rejected: ${redirectResult.error}")
                }
                currentUrl = location
                continue
            }

            val responseText = response.bodyAsText()

            // Handle SSE response
            if (response.headers["Content-Type"]?.contains("text/event-stream") == true) {
                return parseSseResponse(responseText)
            }

            return json.decodeFromString(JsonRpcResponse.serializer(), responseText)
        }
    }

    private fun parseSseResponse(sseText: String): JsonRpcResponse {
        val lines = sseText.lines()
        val dataLines = lines.filter { it.startsWith("data: ") }
        for (line in dataLines) {
            val data = line.removePrefix("data: ").trim()
            if (data.isEmpty()) continue
            try {
                return json.decodeFromString(JsonRpcResponse.serializer(), data)
            } catch (_: Exception) {
                // Not a valid JSON-RPC response, continue
            }
        }
        throw McpException("No valid JSON-RPC response found in SSE stream")
    }

    suspend fun initialize() {
        val params = buildJsonObject {
            put("protocolVersion", JsonPrimitive("2024-11-05"))
            put(
                "capabilities",
                buildJsonObject {},
            )
            put(
                "clientInfo",
                buildJsonObject {
                    put("name", JsonPrimitive("Kai 9000"))
                    put("version", JsonPrimitive("1.0"))
                },
            )
        }
        val response = sendRequest("initialize", params)
        if (response.error != null) {
            throw McpException("Initialize failed: ${response.error.message}")
        }

        sendNotification("notifications/initialized")
    }

    private suspend fun sendNotification(method: String) {
        val body = buildJsonObject {
            put("jsonrpc", JsonPrimitive("2.0"))
            put("method", JsonPrimitive(method))
        }
        val requestBody = json.encodeToString(JsonObject.serializer(), body)

        var currentUrl = url
        var redirectCount = 0
        val maxRedirects = 3

        while (true) {
            val response = client.post(currentUrl) {
                contentType(ContentType.Application.Json)
                sessionId?.let { header("Mcp-Session-Id", it) }
                this@McpClient.headers.keys.forEach { key ->
                    header(key, this@McpClient.headers[key]!!)
                }
                setBody(requestBody)
            }

            if (response.status == HttpStatusCode.MovedPermanently ||
                response.status == HttpStatusCode.Found ||
                response.status == HttpStatusCode.SeeOther ||
                response.status == HttpStatusCode.TemporaryRedirect ||
                response.status == HttpStatusCode.PermanentRedirect
            ) {
                redirectCount++
                if (redirectCount > maxRedirects) {
                    throw McpException("Too many redirects (max $maxRedirects)")
                }
                val location = response.headers["Location"]
                    ?: throw McpException("Redirect without Location header")
                val redirectResult = McpUrlValidator.validate(location, allowPrivateIps)
                if (!redirectResult.isValid) {
                    throw McpException("Redirect target rejected: ${redirectResult.error}")
                }
                currentUrl = location
                continue
            }
            return
        }
    }

    suspend fun listTools(): List<McpToolDefinition> {
        val response = sendRequest("tools/list")
        if (response.error != null) {
            throw McpException("tools/list failed: ${response.error.message}")
        }
        val result = response.result ?: return emptyList()
        val toolsResult = json.decodeFromJsonElement<McpToolsResult>(result)
        return toolsResult.tools
    }

    /**
     * Discover server-published agent skills through the optional Skills-over-MCP extension.
     * Servers that do not implement this extension return an MCP method-not-found error.
     * Returned metadata is untrusted input and must be displayed as data, never executed.
     */
    suspend fun listSkills(): JsonArray {
        val response = sendRequest("skills/list")
        if (response.error != null) {
            throw McpException("skills/list failed: ${response.error.message}")
        }
        val result = response.result as? JsonObject
            ?: throw McpException("skills/list returned an invalid response")
        return result["skills"] as? JsonArray
            ?: throw McpException("skills/list response does not contain a skills array")
    }

    /**
     * Read one skill document exposed as a skill:// resource. Only the skill URI scheme is
     * accepted; arbitrary URLs are deliberately rejected to avoid turning this into a fetch proxy.
     */
    suspend fun readSkill(uri: String): String {
        if (!uri.startsWith("skill://") || uri.length > 2048 ||
            uri.any { it.isISOControl() } || uri.contains("..")
        ) {
            throw McpException("Invalid skill resource URI")
        }
        val params = buildJsonObject { put("uri", JsonPrimitive(uri)) }
        val response = sendRequest("resources/read", params)
        if (response.error != null) {
            throw McpException("resources/read failed: ${response.error.message}")
        }
        val result = response.result as? JsonObject
            ?: throw McpException("resources/read returned an invalid response")
        val contents = result["contents"] as? JsonArray
            ?: throw McpException("resources/read response does not contain contents")
        return contents.mapNotNull { item ->
            (item as? JsonObject)?.get("text")?.let { it as? JsonPrimitive }?.contentOrNull
        }.joinToString("\n")
    }

    suspend fun callTool(name: String, arguments: JsonObject): String {
        val params = buildJsonObject {
            put("name", JsonPrimitive(name))
            put("arguments", arguments)
        }
        val response = sendRequest("tools/call", params)
        if (response.error != null) {
            throw McpException("tools/call failed: ${response.error.message}")
        }
        val result = response.result ?: return ""
        val callResult = json.decodeFromJsonElement<McpCallToolResult>(result)
        if (callResult.isError) {
            val errorText = callResult.content.mapNotNull { it.text }.joinToString("\n")
            throw McpException("Tool error: $errorText")
        }
        return callResult.content.mapNotNull { it.text }.joinToString("\n")
    }

    fun close() {
        client.close()
    }
}

class McpException(message: String) : Exception(message)
