package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.mediahelpers.MissingMediaProgramsException
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
            val siteUrlArgIndex = args.indexOfFirst { it == "--siteUrl" || it == "-u" }
            val siteUrlArg = if(siteUrlArgIndex >= 0) {
                args.getOrNull(siteUrlArgIndex + 1)
            }else {
                null
            }

            val environmentArgs = if(siteUrlArg != null) {
                args + arrayOf("-P:ktor.ustad.siteUrl=$siteUrlArg")
            }else {
                args
            }

            try {
                embeddedServer(Netty, commandLineEnvironment(environmentArgs)) {
                    //Increase these timeouts to allow for ServerSentEvents which keep the client waiting
                    requestReadTimeoutSeconds = 600
                    responseWriteTimeoutSeconds = 600
                    httpServerCodec= {
                        HttpServerCodec(MAX_INITIAL_LINE_LENGTH, MAX_HEADER_SIZE, MAX_CHUNK_SIZE)
                    }
                }.start(true)
            }catch(e: SiteConfigException) {
                System.err.println(e.message)
            }catch(e: MissingMediaProgramsException) {
                System.err.println("Required media programs (e.g. MediaInfo, Handbrake CLI, Sox, etc.) were not found.")
                System.err.println(e.message ?: "")
                System.err.println("See the README for more information")
            }catch(e: Throwable) {
                e.printStackTrace()
            }
        }

    }

}