package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.util.ext.htmlToPlainText

@Composable
fun rememberHtmlToPlainText(html: String): String {
    return remember(html) {
        html.htmlToPlainText()
    }
}
