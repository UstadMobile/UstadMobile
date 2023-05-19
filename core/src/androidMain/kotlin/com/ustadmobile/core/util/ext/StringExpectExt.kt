package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.text.htmlTextToSpanned

actual fun String.htmlToPlainText(): String {
    return htmlTextToSpanned().toString()
}

