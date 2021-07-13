package com.ustadmobile.core.util

import android.content.Context
import java.io.InputStream
import kotlin.reflect.KClass


actual fun getAssetFromResource(path: String, context: Any, clazz: KClass<*>): InputStream? {
    var path = path
    if (path.startsWith("/")) {
        path = path.substring(1)
    }
    return ((context as Context).assets.open(path))
}