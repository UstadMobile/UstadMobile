package com.ustadmobile.core.util.network

import java.io.IOException
import java.net.ServerSocket

/**
 * Find a free TCP port
 *
 * @return a random free TCP port
 */
fun findFreePort(): Int {
    return try {
        ServerSocket(0).use { socket ->
            socket.localPort
        }
    } catch (e: IOException) {
        throw RuntimeException(e)
    }
}
