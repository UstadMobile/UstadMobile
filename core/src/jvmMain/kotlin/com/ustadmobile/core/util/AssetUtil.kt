package com.ustadmobile.core.util

import kotlinx.io.InputStream

actual fun getAssetFromResource(path: String, context: Any): InputStream? {
    return Any::class.java.getResourceAsStream(path)
}


