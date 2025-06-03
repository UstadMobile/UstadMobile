package com.ustadmobile.test.http

import java.io.IOException
import java.net.ServerSocket
import kotlin.random.Random

const val DEFAULT_FROM_PORT = 1_025

const val DEFAULT_UNTIL_PORT = 65_534

/**
 * Find a free TCP port within a specific range
 *
 * @return a random free TCP port
 */
fun findFreePort(
    from: Int = DEFAULT_FROM_PORT,
    until: Int = DEFAULT_UNTIL_PORT,
    numAttempts: Int = 20
): Int {
    for(i in 1..numAttempts) {
        val portToTry = if(from == until) from else Random.nextInt(from, until)

        return try {
            ServerSocket(portToTry).use { socket ->
                socket.localPort
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    throw IllegalStateException("Could not find a free port in range $from to $until after $numAttempts attempts")
}



