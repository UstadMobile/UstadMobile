package com.ustadmobile.test.http

import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.test.http.TestServerControllerMain.Companion.PARAM_NAME_LEARNINGSPACE_HOST
import com.ustadmobile.test.http.TestServerControllerMain.Companion.PARAM_NAME_LEARNINGSPACE_PORTRANGE
import com.ustadmobile.test.http.TestServerControllerMain.Companion.PARAM_NAME_URL
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.*
import okhttp3.OkHttpClient
import java.io.File
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList

enum class RunMode {
    CYPRESS, MAESTRO
}

const val TESTCONTROLLER_PATH = "testcontroller"


/**
 * Note: to handle multiple emulators:
 *  1) Test script will run allocate a random port forwarding for emulator
 *  2) Server can recognize which emulator it is based on the port (use host header)
 */
@Suppress("unused", "SdCardPath")
fun Application.testServerController() {

    val mode = environment.config.propertyOrNull("mode")?.getString()?.let { runPropVal ->
        RunMode.entries.firstOrNull { it.name.equals(runPropVal, ignoreCase = true) }
    } ?: throw IllegalArgumentException(
        "Must specify runmode cypress or maestro e.g. -P:mode=cypress or -P:mode=maestro"
    )

    val adbPath = SysPathUtil.findCommandInPath(
        commandName = "adb",
        extraSearchPaths = System.getenv("ANDROID_HOME") ?: "",
    )

    val okHttpClient = OkHttpClient.Builder()
        .followRedirects(false) //Following redirect would break reverse proxy
        .build()


    val controllerUrl = environment.config.property(PARAM_NAME_URL).getString()
    val controllerUrlObj = URL(controllerUrl)
    val learningSpaceHostPropVal = environment.config.propertyOrNull(PARAM_NAME_LEARNINGSPACE_HOST)?.getString()
    val learningSpaceHostRangePropVal = environment.config
        .propertyOrNull(PARAM_NAME_LEARNINGSPACE_PORTRANGE)?.getString() ?: "$DEFAULT_FROM_PORT-$DEFAULT_UNTIL_PORT"
    val split = learningSpaceHostRangePropVal.split("-").map { it.toInt() }
    if(split.size != 2) {
        throw IllegalArgumentException("$PARAM_NAME_LEARNINGSPACE_PORTRANGE must be in the form of x-y e.g. $DEFAULT_FROM_PORT-$DEFAULT_UNTIL_PORT")
    }

    val learningSpaceFromPort = split.first()
    val learningSpaceUntilPort = split.last()

    val learningSpaceHost = when {
        mode == RunMode.CYPRESS -> InetAddress.getByName(controllerUrlObj.host)
        learningSpaceHostPropVal != null -> InetAddress.getByName(learningSpaceHostPropVal)
        else -> {
            val allNetInterfaces = NetworkInterface.getNetworkInterfaces().toList()

            allNetInterfaces.firstOrNull { netInterface ->
                !netInterface.isLoopback && netInterface.isUp && netInterface.inetAddresses.toList().any { addr ->
                    addr is Inet4Address
                }
            }?.interfaceAddresses?.firstOrNull {
                it.address !is Inet6Address
            }?.address ?: throw IllegalStateException("Could not determine site host")
        }
    }

    val runningServers: MutableList<ServerRunner> = CopyOnWriteArrayList()

    if(adbPath == null || !adbPath.exists()) {
        throw IllegalStateException("ERROR: ADB path does not exist")
    }

    val srcRootDirProp = environment.config.propertyOrNull("srcRoot")?.getString()
    val userDir = File(System.getProperty("user.dir"))

    val rootSrcDir = when {
        srcRootDirProp != null -> File(srcRootDirProp)
        userDir.name == "testserver-controller" -> userDir.parentFile
        File(userDir, "settings.gradle").exists() -> userDir
        else -> {
            val exception = IllegalStateException(
                "ERROR: Could not find the UstadMobile root source directory. If the current " +
                "working directory is not the source root directory or child thereof, then this path" +
                "must be specified using the srcRoot property e.g. P:srcRoot=path"
            )
            println(exception.message)
            throw exception
        }
    }

    val serverDir = File(rootSrcDir, "app-ktor-server")
    val testFilesDir = File(File(rootSrcDir, "test-end-to-end"), "test-files")
    val testContentDir = File(testFilesDir, "content")
    log.info("TEST FILES: ${testContentDir.absolutePath}")

    val testServerControllerDir = File(rootSrcDir, "testserver-controller")
    val testServerControllerBuildDir = File(testServerControllerDir, "build")
    val baseDataDir = File(testServerControllerBuildDir, "data")

    if(!serverDir.exists()) {
        println("ERROR: Source root directory ($rootSrcDir) does not exist! testServerManager working directory MUST be the " +
                "root directory of the source code")
        throw IllegalStateException("ERROR: Server dir does not exist! testServerManager working directory MUST be the " +
                "root directory of the source code")
    }

    fun stopAllRunningServers() {
        while(runningServers.isNotEmpty()) {
            val serverToStop = runningServers.removeAt(0)
            serverToStop.stop()
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    /*
     * Required because NPM start-server uses a HEAD request to check if the server is ready.
     */
    install(AutoHeadResponse)

    install(CallLogging)

    install(ContentNegotiation) {
        json()
    }

    if(mode == RunMode.CYPRESS) {
        intercept(ApplicationCallPipeline.Setup) {
            val requestUri = call.request.uri
            val serverToForwardTo = runningServers.lastOrNull()
            if(!requestUri.startsWith("/$TESTCONTROLLER_PATH") && serverToForwardTo != null) {
                //reverse proxy it
                val destUrl = Url("http://${controllerUrlObj.host}:${serverToForwardTo.port}$requestUri")
                call.respondReverseProxy(destUrl.toString(), okHttpClient)
                return@intercept finish()
            }
        }
    }

    install(Routing) {
        get("/") {
            /*
             * When running in SINGLE_PORT mode and a server is already running, then the interceptor
             * will intercept before reaching this endpoint.
             *
             * Responding to / ensures:
             * a) the start-server-and-test script recognizes the server is ready
             * b) developers not reading documentation fully don't see an http error and think "It's not working"
             */
            call.response.cacheControl(CacheControl.NoStore(null))
            call.respondText(
                text = "Test Controller server is running - use the /testcontroller API to start and manage " +
                        "running the ustad server (e.g. app-ktor-server) for tests as per README docs."
            )
        }

        route(TESTCONTROLLER_PATH) {
            staticFiles("test-files/content/", testContentDir)

            get("/") {
                val response = buildString {
                    append("<html><body>")
                    append("TestServerController running: Mode=${mode.name}<br/>")
                    append("Running instances (${runningServers.size})<br/>")
                    append("<ul>")
                    runningServers.forEach {
                        append("<li>PID ${it.pid} learning space url: ${it.learningSpaceUrl}</li>")
                    }
                    append("</ul>")
                    append("</body></html>")
                }

                call.response.header("cache-control", "no-cache")
                call.respondText(
                    text = response,
                    contentType = ContentType.Text.Html,
                )
            }

            /**
             * Start the test server
             *
             * API usage:
             *
             * GET start
             *
             */
            get("start") {
                try {
                    if(mode == RunMode.CYPRESS) {
                        stopAllRunningServers()
                    }

                    val serverRunner = ServerRunner(
                        mode = mode,
                        okHttpClient = okHttpClient,
                        serverDir = serverDir,
                        runServerCommand = call.application.environment.config
                            .property("ktor.testServer.command").getString(),
                        controllerUrl = controllerUrlObj,
                        learningSpaceHost = learningSpaceHost,
                        baseDataDir = baseDataDir,
                        fromPort = learningSpaceFromPort,
                        untilPort = learningSpaceUntilPort,
                    )

                    runningServers.add(serverRunner)
                    serverRunner.start()

                    call.respond(
                        ServerInfo(
                            url = serverRunner.learningSpaceUrl,
                            port = Url(serverRunner.learningSpaceUrl).port,
                            extraInfo = "Using port ${serverRunner.port} pid=${serverRunner.pid}",
                            adminUsername = "admin",
                            //This is currently set in testserver-controller/application.conf,
                            //however on learningspace branches it can be randomly generated.
                            adminPassword = "testpass",
                        )
                    )
                }catch(e: Throwable) {
                    call.respondText(
                        status = HttpStatusCode.InternalServerError,
                        text = "ERROR: ${e.message} \n ${e.stackTraceToString()}",
                        contentType = ContentType.Text.Plain,
                    )
                }
            }

            /**
             * This is called by the stop.sh script just before the server gets shut down so we can
             * properly save things as needed. Using the shutdown hook does not seem to allow video to
             * finish properly
             */
            get("/stop") {
                val learningSpaceUrlToStop = call.request.queryParameters["url"]

                if(learningSpaceUrlToStop == null) {
                    stopAllRunningServers()
                    call.respondText(
                        status = HttpStatusCode.OK,
                        contentType = ContentType.Text.Plain,
                        text = "OK - stopped all servers"
                    )
                }

                val serverToStop = runningServers.firstOrNull {
                    it.learningSpaceUrl == learningSpaceUrlToStop
                }

                call.response.header("cache-control", "no-cache")

                if(serverToStop != null) {
                    serverToStop.stop()
                    runningServers.remove(serverToStop)
                    call.respondText(
                        status = HttpStatusCode.OK,
                        contentType = ContentType.Text.Plain,
                        text = "OK - stopped"
                    )
                }else {
                    call.respondText(
                        status = HttpStatusCode.BadRequest,
                        contentType = ContentType.Text.Plain,
                        text = "Could not stop - url to stop was specified but not found $learningSpaceUrlToStop"
                    )
                }
            }
        }
    }
}
