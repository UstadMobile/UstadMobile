package com.ustadmobile.core.util

import java.io.InputStream

actual fun getAssetFromResource(path: String, context: Any): InputStream? {
    return context.javaClass.getResourceAsStream(path)
}


