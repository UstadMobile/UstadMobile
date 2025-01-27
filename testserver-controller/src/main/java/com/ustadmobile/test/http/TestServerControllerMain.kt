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
         * KTOR config parameter key for the URL of the testserver controller. Should be the full
         * url e.g. http://localhost:8075/. Normally passed as a command line flag e.g.
         * -P:url=http://localhost:8075/
         */
        const val PARAM_NAME_URL = "url"

        /**
         * The host (IP address or host name) that will be used for the learning space url that the
         * software under test will connect to
         */
        const val PARAM_NAME_LEARNINGSPACE_HOST = "learningSpaceHost"

        /**
         *
         */
        const val PARAM_NAME_LEARNINGSPACE_PORTRANGE = "portRange"

        @JvmStatic
        fun main(args: Array<String>) {
            val testServerControllerUrl = Url(
                args.firstOrNull { it.startsWith("-P:$PARAM_NAME_URL=") }
                    ?.substringAfter("=") ?: "http://localhost:$DEFAULT_PORT/"
            )

            val environmentArgs = buildList {
                addAll(args)
                add("-port=${testServerControllerUrl.port}")
                if(!args.any { it.startsWith("-P:$PARAM_NAME_URL=") })
                    add("-P:$PARAM_NAME_URL=$testServerControllerUrl")
            }.toTypedArray()


            embeddedServer(Netty, commandLineEnvironment(environmentArgs)).start(wait = true)
        }
    }

}