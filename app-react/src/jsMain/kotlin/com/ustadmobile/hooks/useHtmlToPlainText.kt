package com.ustadmobile.hooks

import com.ustadmobile.core.util.ext.htmlToPlainText
import react.useMemo

fun useHtmlToPlainText(html: String): String {
    return useMemo(html) {
        html.htmlToPlainText()
    }
}
