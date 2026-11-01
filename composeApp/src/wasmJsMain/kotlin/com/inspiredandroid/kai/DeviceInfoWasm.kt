package com.inspiredandroid.kai

import com.inspiredandroid.kai.Version

actual fun getDeviceInfo(): Map<String, Any> {
    return mapOf(
        "platform" to currentPlatform.displayName,
        "appVersion" to Version.appVersion,
        "userAgent" to (js("navigator.userAgent") as? String ?: "unknown"),
        "language" to (js("navigator.language") as? String ?: "unknown"),
    )
}