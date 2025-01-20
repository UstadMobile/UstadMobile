package com.ustadmobile.test.http

import java.io.IOException
import java.net.ServerSocket

/**
 * Find a free TCP port. Copy of function on core (by design test code is separated)
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
