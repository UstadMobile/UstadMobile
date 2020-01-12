package com.ustadmobile.lib.util

private val sanitizeRegex = Regex("\\W")

/**
 * This is primarily here for test methods so that the active database can be bound in JDBC
 */
fun sanitizeDbNameFromUrl(url: String): String = url.removePrefix("https://")
        .removePrefix("http://")
        .replace(sanitizeRegex, "_")