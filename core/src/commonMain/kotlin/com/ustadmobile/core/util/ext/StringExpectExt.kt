package com.ustadmobile.core.util.ext

/**
 * Strip the HTML tags from the given string. Uses Html on Android, Jsoup on JVM, DOM on Javascript
 *
 * @receiver String containing HTML
 * @return plain text with tags stripped out
 */
expect fun String.htmlToPlainText(): String

/**
 * Require that the string ends with a file separator character (e.g. / on Linux, Mac, Android, JS,
 * \ when run on Windows on JVM)
 */
expect fun String.requireFileSeparatorSuffix(): String
