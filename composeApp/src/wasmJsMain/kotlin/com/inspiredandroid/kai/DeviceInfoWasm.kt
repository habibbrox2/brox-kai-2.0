package com.inspiredandroid.kai

import com.inspiredandroid.kai.Version

actual fun getDeviceInfo(): Map<String, Any> {
    return mapOf(
        "platform" to currentPlatform.displayName,
        "appVersion" to Version.appVersion,
    )
}