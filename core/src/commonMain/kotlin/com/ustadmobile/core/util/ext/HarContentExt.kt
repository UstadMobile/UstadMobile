package com.ustadmobile.core.util.ext

import com.ustadmobile.core.contentformats.har.HarContent

fun HarContent.isTextContent(): Boolean{
    return mimeType?.startsWith("text/") == true ||
            mimeType?.startsWith("application/json") == true
}