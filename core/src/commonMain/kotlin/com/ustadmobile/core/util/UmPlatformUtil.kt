package com.ustadmobile.core.util

import kotlinx.coroutines.CoroutineScope

expect object UmPlatformUtil {

    @Deprecated("DONT USE THIS. Use runIfNotJs to run " +
            "something on non-javascript platforms (JVM and Android)")
    var isWeb: Boolean

    /**
     * Synchronously run a block on a specific platform
      */
    @Deprecated("DONT USE THIS. The name does not reflect behavior. Use runIfNotJs to run " +
            "something on non-javascript platforms (JVM and Android)")
    fun run(block:() -> Unit)

    /**
     * Asynchronously run a block on a specific platform
     */
    @Deprecated("DONT USE THIS. The name does not reflect behavior. Use runIfNotJs to run " +
            "something on non-javascript platforms (JVM and Android)")
    suspend fun runAsync( block:suspend CoroutineScope.() -> Unit)

    fun debug()

    fun log(content: Any)

    fun runIfNotJs(block: () -> Unit)

    suspend fun runIfNotJsAsync(block:suspend () -> Unit)
}