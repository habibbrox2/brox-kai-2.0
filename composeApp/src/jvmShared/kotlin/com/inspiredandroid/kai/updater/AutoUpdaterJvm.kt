package com.inspiredandroid.kai.updater

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URI

/**
 * JVM (desktop + android) implementation of [httpGet] using HttpURLConnection.
 */
internal actual suspend fun httpGet(url: String, headers: Map<String, String>): String = withContext(Dispatchers.IO) {
    val uri = try {
        URI.create(url)
    } catch (exception: IllegalArgumentException) {
        throw HttpRequestException(cause = exception)
    }
    val requestScheme = uri.scheme ?: throw HttpRequestException()
    val requestHost = uri.host?.lowercase() ?: throw HttpRequestException()
    require(requestScheme.equals("https", ignoreCase = true) && requestHost in ALLOWED_UPDATE_HOSTS) { "Untrusted update request URL" }
    val connection = uri.toURL().openConnection() as java.net.HttpURLConnection
    try {
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "GET"
        connection.instanceFollowRedirects = true
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        val statusCode = connection.responseCode
        val finalHost = connection.url.host?.lowercase()
        if (finalHost == null || finalHost !in ALLOWED_UPDATE_HOSTS) throw HttpRequestException(statusCode = statusCode)
        if (statusCode !in 200..299) {
            runCatching { connection.errorStream?.close() }
            throw HttpRequestException(statusCode = statusCode)
        }
        if (connection.contentLengthLong > 2L * 1024L * 1024L) throw HttpRequestException(cause = IllegalStateException("Response exceeds limit"))
        val output = ByteArrayOutputStream()
        connection.inputStream.use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > 2 * 1024 * 1024) throw HttpRequestException(cause = IllegalStateException("Response exceeds limit"))
                output.write(buffer, 0, count)
            }
        }
        output.toString(Charsets.UTF_8.name())
    } catch (exception: HttpRequestException) {
        throw exception
    } catch (exception: SocketTimeoutException) {
        throw HttpRequestException(timedOut = true, cause = exception)
    } catch (exception: IOException) {
        throw HttpRequestException(cause = exception)
    } finally {
        connection.disconnect()
    }
}
private val ALLOWED_UPDATE_HOSTS = setOf("api.github.com", "github.com", "objects.githubusercontent.com", "release-assets.githubusercontent.com", "github-releases.githubusercontent.com")

/**
 * JVM implementation of [currentPlatformKey] from system properties.
 */
internal actual fun currentPlatformKey(): String {
    val os = System.getProperty("os.name", "").lowercase()
    val arch = System.getProperty("os.arch", "").lowercase()
    return when {
        os.contains("windows") -> "windows"

        os.contains("mac") || os.contains("darwin") ->
            if (arch.contains("aarch64") || arch.contains("arm64")) "macos-arm64" else "macos-x64"

        os.contains("linux") ->
            if (arch.contains("aarch64") || arch.contains("arm64")) "linux-arm64" else "linux-x64"

        else -> "none"
    }
}
