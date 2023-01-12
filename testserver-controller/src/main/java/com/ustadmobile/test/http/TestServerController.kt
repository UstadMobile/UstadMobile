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


@Suppress("BlockingMethodInNonBlockingContext", "unused", "SdCardPath")
fun Application.testServerController() {

    val serverDir = File("app-ktor-server")
    val testContentDir = File(File("test-end-to-end", "test-files"), "content")

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

        get("/cleardownloads") {
            val deviceSerial = call.request.queryParameters["device"]

            val adbCommand = SysPathUtil.findCommandInPath("adb")
                ?: throw IllegalStateException("Cannot find adb in path")

            val process = ProcessBuilder(listOf(adbCommand.absolutePath,
                    "-s", deviceSerial, "shell", "rm", "/sdcard/Download/*"))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            process.waitFor(5, TimeUnit.SECONDS)

            call.response.header("cache-control", "no-cache")
            call.respondText(
                text = "Cleared download directory /sdcard/Download",
                contentType = ContentType.Text.Plain,
            )
        }

        get("/pushcontent") {
            val deviceSerial = call.request.queryParameters["device"]
            val fileName = call.request.queryParameters["test-file-name"]
                ?: throw IllegalArgumentException("No filename specified")
            val contentFile = File(testContentDir, fileName)

            val adbCommand = SysPathUtil.findCommandInPath("adb")
                ?: throw IllegalStateException("Cannot find adb in path")

            call.response.header("cache-control", "no-cache")

            if(!contentFile.exists()) {
                call.respondText(
                    status = HttpStatusCode.NotFound,
                    text = "No such file: $contentFile",
                    contentType = ContentType.Text.Plain,
                )
                return@get
            }

            val process = ProcessBuilder(listOf(adbCommand.absolutePath,
                "-s", deviceSerial, "push", contentFile.absolutePath, "/sdcard/Download"))
                .directory(serverDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            process.waitFor(5, TimeUnit.SECONDS)

            call.respondText(
                text = "Pushed content to $deviceSerial ${contentFile.absolutePath} -> /sdcard/Download",
                contentType = ContentType.Text.Plain
            )
        }

    }
}
