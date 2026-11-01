package com.inspiredandroid.kai

import com.inspiredandroid.kai.Version

private val wasmUserAgent: String = js("navigator.userAgent") as String
private val wasmLanguage: String = js("navigator.language") as String

actual fun getDeviceInfo(): Map<String, Any> {
    return mapOf(
        "platform" to currentPlatform.displayName,
        "appVersion" to Version.appVersion,
        "userAgent" to wasmUserAgent,
        "language" to wasmLanguage,
    )
}