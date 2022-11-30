package com.ustadmobile.test.http

import com.ustadmobile.lib.util.SysPathUtil
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import java.util.concurrent.TimeUnit


@Suppress("BlockingMethodInNonBlockingContext")
fun Application.testServerManager() {

    var serverProcess: Process? = null

    Runtime.getRuntime().addShutdownHook(Thread {
        serverProcess?.destroy()
    })

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    install(CallLogging)

    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    install(Routing) {
        get("/start") {
            serverProcess?.also {
                it.destroy()
                it.waitFor(5, TimeUnit.SECONDS)
            }

            val serverArgs = call.application.environment.config
                .propertyOrNull("ktor.testServer.command")?.getString()?.split(Regex("\\s+"))
                    ?.toMutableList()
                ?: throw IllegalArgumentException("No testServer command specified in configuration")

            //If the command is not an absolute path or relative path, then look in the PATH variable
            if(!(serverArgs[0].startsWith(".") || serverArgs[0].startsWith("/"))) {
                serverArgs[0] = SysPathUtil.findCommandInPath(serverArgs[0])?.absolutePath
                    ?: throw IllegalArgumentException("Could not find server command in PATH ${serverArgs[0]}")
            }

            serverProcess = ProcessBuilder(serverArgs)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            call.respondText(
                text = "<html><body>OK: ran ${serverArgs.joinToString( " ")}</body></html>",
                contentType = ContentType.Text.Html
            )
        }

        get("/markSuccessful") {
            serverProcess?.also {
                it.destroy()
                it.waitFor()
            }

            serverProcess = null
            call.respond(HttpStatusCode.OK, "OK")
        }

    }
}
