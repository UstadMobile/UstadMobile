package com.ustadmobile.core.impl

import kotlinx.browser.window

//userAgent is used by js code
@Suppress("UNUSED_VARIABLE")
actual fun getOs(): String {
    val userAgent = window.navigator.userAgent
    val platform = window.navigator.platform
    val macosPlatforms = listOf("Macintosh", "MacIntel", "MacPPC", "Mac68K")
    val windowsPlatforms = listOf("Win32", "Win64", "Windows", "WinCE")
    val iosPlatforms = listOf("iPhone", "iPad", "iPod")
    return when {
        macosPlatforms.indexOf(platform) != -1 -> "Mac OS"
        iosPlatforms.indexOf(platform) != -1 -> "iOS"
        windowsPlatforms.indexOf(platform) != -1 -> "Windows"
        js("/Android/.test(userAgent)") as Boolean -> "Android"
        else -> "Linux"
    }
}

actual fun getOsVersion(): String = "${getOs()}-unknown"