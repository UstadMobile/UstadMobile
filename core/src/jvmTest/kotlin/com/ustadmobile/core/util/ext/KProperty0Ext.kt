package com.ustadmobile.core.util.ext

import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

/**
 * Returns true if a lazy property reference has been initialized, or if the property is not lazy.
 *
 * As per: https://gist.github.com/tw-Frey/4d75d7438eb78c1ac7106d23abe1a66b
 */
val KProperty0<*>.isLazyInitialized: Boolean
    get() {
        if (this !is Lazy<*>) return true

        // Prevent IllegalAccessException from JVM access check on private properties.
        val originalAccessLevel = isAccessible
        isAccessible = true
        val isLazyInitialized = (getDelegate() as Lazy<*>).isInitialized()
        // Reset access level.
        isAccessible = originalAccessLevel
        return isLazyInitialized
    }
