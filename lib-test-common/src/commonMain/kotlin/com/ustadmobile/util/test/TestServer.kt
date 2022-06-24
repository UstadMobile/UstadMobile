package com.ustadmobile.util.test

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.get

/**
 * Utility class to assist with setting up and operating with a test server.
 *
 */
class TestServer(val serverHost: String, val controlServerPort: Int,
                             val httpClient: HttpClient) {

    var port: Int = -1

    var token: String = ""

    suspend fun allocate(): TestServer {
        val portAndKey: Pair<Int, String> = httpClient.get(
            "http://$serverHost:$controlServerPort/servers/newServer"
        ).body()
        port = portAndKey.first
        token = portAndKey.second
        return this
    }

    fun requireAllocated() = require(port != -1, {"TestServer must be allocated first!"})

    suspend fun reset(): TestServer {
        requireAllocated()
        httpClient.get("http://$serverHost:$controlServerPort/servers/$port/reset")
        return this
    }

    suspend fun throttle(bytesPerPeriod: Long, periodDuration: Long): TestServer {
        requireAllocated()
        httpClient.get("http://$serverHost:$controlServerPort/servers/$port/throttle?bytesPerPeriod=$bytesPerPeriod&periodDuration=$periodDuration")
        return this
    }

    suspend fun setNumDisconnects(numDisconnects: Int): TestServer {
        requireAllocated()
        httpClient.get("http://$serverHost:$controlServerPort/servers/$port/setNumDisconnects?numDisconnects=$numDisconnects")
        return this
    }

    suspend fun deallocate(): TestServer {
        if(port != -1) {
            httpClient.get("http://$serverHost:$controlServerPort/servers/$port/close")
            port = -1
        }

        return this
    }

}