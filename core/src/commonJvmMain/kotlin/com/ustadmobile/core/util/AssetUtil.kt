package com.ustadmobile.core.util

import java.io.InputStream
import kotlin.reflect.KClass


/**
 * @param path path to the resource
 * @param clazz: KClazz is used here to access the resource on JVM
 *               which must be located in the same module as the KClazz
 */
expect fun getAssetFromResource(path: String, context: Any, clazz: KClass<*>): InputStream?
