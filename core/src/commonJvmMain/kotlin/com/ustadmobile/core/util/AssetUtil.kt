package com.ustadmobile.core.util

import java.io.InputStream
import kotlin.reflect.KClass

expect fun getAssetFromResource(path: String, context: Any, clazz: KClass<*>): InputStream?
