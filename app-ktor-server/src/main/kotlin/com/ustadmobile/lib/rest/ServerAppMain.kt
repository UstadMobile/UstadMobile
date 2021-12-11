package com.ustadmobile.lib.rest

import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.netty.handler.codec.http.HttpServerCodec

/**
 * This server app is provided to increase the acceptable length of a url. This is needed if queries
 * get very long - e.g. concatenating a long list of containerentryfile uids.
 */
class ServerAppMain {

    companion object {

        const val MAX_INITIAL_LINE_LENGTH = 32 * 1024

        const val MAX_HEADER_SIZE = 4096

        const val MAX_CHUNK_SIZE = 4096

        @JvmStatic
        fun main(args: Array<String>) {
            embeddedServer(Netty, commandLineEnvironment(args)) {
                requestReadTimeoutSeconds = 600
                responseWriteTimeoutSeconds = 600
                httpServerCodec= {
                    HttpServerCodec(MAX_INITIAL_LINE_LENGTH, MAX_HEADER_SIZE, MAX_CHUNK_SIZE)
                }
            }.start(true)
        }

    }

}