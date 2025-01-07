package com.ustadmobile.test.http

import io.ktor.http.Url
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

class TestServerControllerMain {

    companion object {

        @Suppress("MemberVisibilityCanBePrivate")
        const val DEFAULT_PORT = 8075

        /**
         * First parameter, if any parameters are present, must be the url of the testserver controller
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val testServerControllerUrl = Url(args.firstOrNull() ?: "http://localhost:$DEFAULT_PORT/")

            val environmentArgs = buildList {
                add("-port=${testServerControllerUrl.port}")
                add("-P:siteHost=${testServerControllerUrl.host}")

                if(args.size > 1)
                    addAll(args.drop(1))
            }.toTypedArray()


            embeddedServer(Netty, commandLineEnvironment(environmentArgs)).start(wait = true)
        }
    }

}