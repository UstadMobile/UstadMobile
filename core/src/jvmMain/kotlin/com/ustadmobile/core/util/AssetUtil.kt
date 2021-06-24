package com.ustadmobile.core.util

import java.io.InputStream
import kotlin.reflect.KClass

actual fun getAssetFromResource(path: String, context: Any, clazz: KClass<*>): InputStream? {
    return clazz.java.getResourceAsStream(path)
}


