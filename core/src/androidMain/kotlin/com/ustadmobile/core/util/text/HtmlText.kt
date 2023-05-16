package com.ustadmobile.core.util.text

import android.os.Build
import android.text.Html
import android.text.Spanned


@Suppress("DEPRECATION")
fun String.htmlTextToSpanned() : Spanned{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}
