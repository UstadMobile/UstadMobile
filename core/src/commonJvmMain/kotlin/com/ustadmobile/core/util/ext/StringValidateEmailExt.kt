package com.ustadmobile.core.util.ext

actual fun String?.validEmail(): Boolean {
    return EMAIL_VALIDATION_REGEX.matches("$this")
}

@Suppress("RegExpRedundantEscape")
val EMAIL_VALIDATION_REGEX: Regex by lazy(LazyThreadSafetyMode.NONE) {
    Regex("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$")
}