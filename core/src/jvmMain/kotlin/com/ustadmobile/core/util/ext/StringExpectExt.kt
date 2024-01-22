package com.ustadmobile.core.util.ext

import org.jsoup.Jsoup

actual fun String.htmlToPlainText(): String {
    return Jsoup.parse(this).wholeText()
}
