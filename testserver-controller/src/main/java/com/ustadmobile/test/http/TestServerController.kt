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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Suppress("BlockingMethodInNonBlockingContext", "unused")
fun Application.testServerController() {

    val serverDir = File("app-ktor-server")
    if(!serverDir.exists()) {
        println("ERROR: Server dir does not exist! testServerManager working directory MUST be the " +
                "root directory of the source code")
        throw IllegalStateException("ERROR: Server dir does not exist! testServerManager working directory MUST be the " +
                "root directory of the source code")
    }

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
        get("/") {
            var response = ""
            if(serverProcess != null) {
                response = "Running pid #${serverProcess?.pid()} (running=${serverProcess?.isAlive}<br/>"
            }else {
                response = "Server not running <br/>"
            }

            call.response.header("cache-control", "no-cache")
            call.respondText(
                text = "<html><body>" +
                        "$response <br/>" +
                        "<a href=\"/start\">Start or restart server now</a>" +
                        "</body></html>",
                contentType = ContentType.Text.Html,
            )
        }


        get("/start") {
            var response = SimpleDateFormat.getDateTimeInstance().format(Date()) + "<br/>"
            serverProcess?.also {
                it.destroy()
                it.waitFor(5, TimeUnit.SECONDS)
                response += "Stopped server: pid #${serverProcess?.pid()}<br/>"
                serverProcess = null
            }

            val dataDir = File(serverDir, "data")
            if(dataDir.exists()){
                dataDir.deleteRecursively()
                response += "Cleared data directory: ${dataDir.absolutePath} <br/>"
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
                .directory(serverDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            response += "Started server process PID #${serverProcess?.pid()} ${serverArgs.joinToString( " ")}"

            call.response.header("cache-control", "no-cache")
            call.respondText(
                text = "<html><body>$response</body></html>",
                contentType = ContentType.Text.Html
            )
        }

        get("/markSuccessful") {
            serverProcess?.also {
                it.destroy()
                it.waitFor()
            }

            serverProcess = null
            call.response.header("cache-control", "no-cache")
            call.respond(HttpStatusCode.OK, "OK")
        }

    }
}
