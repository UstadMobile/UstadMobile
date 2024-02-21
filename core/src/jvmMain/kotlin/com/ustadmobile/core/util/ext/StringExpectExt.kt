package com.ustadmobile.core.util.ext

import org.jsoup.Jsoup
import java.io.File

actual fun String.htmlToPlainText(): String {
    return Jsoup.parse(this).wholeText()
}

/**
 * Require that the string ends with a path separator character (e.g. / on Linux, Mac, Android, JS,
 * \ when run on Windows on JVM)
 */
actual fun String.requireFileSeparatorSuffix(): String {
    return requirePostfix(File.separator)
}
