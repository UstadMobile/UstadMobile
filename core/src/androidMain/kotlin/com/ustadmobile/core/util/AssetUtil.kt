package com.ustadmobile.core.util

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


actual fun getAssetFromResource(path: String, context: Any): InputStream? {
    var path = path
    if (path.startsWith("/")) {
        path = path.substring(1)
    }
    return ((context as Context).assets.open(path))
}