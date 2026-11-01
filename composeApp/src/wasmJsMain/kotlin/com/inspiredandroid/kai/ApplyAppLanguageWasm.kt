package com.inspiredandroid.kai

actual fun applyAppLanguage(languageCode: String) {
    js("document.documentElement.lang = '$languageCode'")
}