package com.ustadmobile.core.util

import kotlinx.coroutines.CoroutineScope

expect object UmPlatformUtil {

    var isWeb: Boolean

    /**
     * Synchronously run a block on a specific platform
      */
    fun run(block:() -> Unit)

    /**
     * Asynchronously run a block on a specific platform
     */
    suspend fun runAsync( block:suspend CoroutineScope.() -> Unit)

    fun debug()

    fun log(content: Any)
}