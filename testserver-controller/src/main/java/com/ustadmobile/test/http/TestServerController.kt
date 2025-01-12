package com.ustadmobile.test.http

import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
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
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


const val ADB_RECORD_PARAM = "adbRecord"

const val DEVICE_SERIAL_PARAM = "device"

const val TESTNAME_PARAM = "testName"

const val TEST_FILE_NAME_PARAM = "test-file-name"

const val DEST_PARAM = "dest"

enum class RunMode {
    CYPRESS, MAESTRO
}

const val TESTCONTROLLER_PATH = "testcontroller"


@Suppress("BlockingMethodInNonBlockingContext", "unused", "SdCardPath")
fun Application.testServerController() {

    var adbRecordProcess: Process? = null

    val mode = RunMode.CYPRESS

    val adbPath = SysPathUtil.findCommandInPath(
        commandName = "adb",
        extraSearchPaths = System.getenv("ANDROID_HOME") ?: "",
    )

    var adbVideoName: String? = null

    var currentSerial: String? = null

    var currentReverseProxyPort: Int = 0

    val okHttpClient = OkHttpClient.Builder()
        .followRedirects(false) //Following redirect would break reverse proxy
        .build()

    val resultDir = environment.config.propertyOrNull("resultDir")?.getString()?.let {
        File(it)
    } ?: File(".")

    val controllerUrl = environment.config.property(PARAM_NAME_URL).getString()
    val controllerUrlObj = URL(controllerUrl)

    if(adbPath == null || !adbPath.exists()) {
        throw IllegalStateException("ERROR: ADB path does not exist")
    }

    fun adbPullFile(
        deviceSerial: String,
        fromPath: String,
        destFile: File,
        deleteAfter: Boolean = false
    ) {
        destFile.parentFile.takeIf { !it.exists() }?.mkdirs()

        log.info("Pulling file from device $deviceSerial $fromPath -> ${destFile.absolutePath}")
        ProcessBuilder(listOf(adbPath.absolutePath, "-s", deviceSerial, "pull",
                fromPath, destFile.absolutePath))
            .start()
            .also {
                it.waitFor(20, TimeUnit.SECONDS)
            }
        if(deleteAfter) {
            log.info("Delete $fromPath from $deviceSerial")
            ProcessBuilder(listOf(adbPath.absolutePath, "-s", deviceSerial, "shell", "rm", fromPath))
                .start()
                .also {
                    it.waitFor(20, TimeUnit.SECONDS)
                }
        }
    }

    fun stopRecording() {
        if(adbRecordProcess != null) {
            ProcessBuilder(listOf(adbPath.absolutePath, "-s", (currentSerial ?: "err"), "shell", "kill",
                "-SIGINT", "$(pidof screenrecord)"))
                .start()
                .also {
                    it.waitFor(20, TimeUnit.SECONDS)
                }

            adbRecordProcess?.waitFor(20, TimeUnit.SECONDS)
            val destFile = File(File(resultDir, adbVideoName ?: "err"),
                "screenrecord.mp4")
            adbPullFile(currentSerial ?: "err", "/sdcard/$adbVideoName.mp4",
                destFile, true)
            adbRecordProcess = null
        }
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

    var serverProcess: Process? = null

    Runtime.getRuntime().addShutdownHook(Thread {
        stopRecording()
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

    /*
     * Required because NPM start-server uses a HEAD request to check if the server is ready.
     */
    install(AutoHeadResponse)

    install(CallLogging)

    install(ContentNegotiation) {
        json()
    }

    @Suppress("KotlinConstantConditions")
    if(mode == RunMode.CYPRESS) {
        intercept(ApplicationCallPipeline.Setup) {
            val requestUri = call.request.uri
            if(!requestUri.startsWith("/$TESTCONTROLLER_PATH") && serverProcess != null) {
                //reverse proxy it
                val destUrl = Url("http://${controllerUrlObj.host}:$currentReverseProxyPort$requestUri")
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
                            "<a href=\"start\">Start or restart server now</a>" +
                            "</body></html>",
                    contentType = ContentType.Text.Html,
                )
            }

            /**
             * Start the test server and Android ADB screen recording as needed.
             *
             * API usage:
             *
             * GET start?recordAdbDevice=<serial>&testName=<test_name>
             *
             * Params:
             *  recordAdbDevice: the serial of the device to record (as per adb devices command)
             *  testName: name of the test about to start - used to determine the directory to save video output
             */
            get("start") {
                try {
                    val requestDeviceSerial = call.request.queryParameters[DEVICE_SERIAL_PARAM] ?: ""
                    val adbRecordEnabled = call.request.queryParameters[ADB_RECORD_PARAM]?.toBoolean() ?: false
                    val config = call.application.environment.config
                    val clearPgJdbcUrl = config.propertyOrNull("ktor.testServer.clearPgUrl")?.getString()
                    val clearPgUser = config.propertyOrNull("ktor.testServer.clearPgUser")?.getString()
                    val clearPgPass = config.propertyOrNull("ktor.testServer.clearPgPass")?.getString()



                    var response = SimpleDateFormat.getDateTimeInstance().format(Date()) + "<br/>"
                    serverProcess?.also {
                        it.destroy()
                        it.waitFor(5, TimeUnit.SECONDS)
                        response += "Stopped server: pid #${serverProcess?.pid()}<br/>"
                        serverProcess = null
                    }

                    adbRecordProcess?.also {
                        stopRecording()
                        adbRecordProcess = null
                    }

                    if(clearPgJdbcUrl != null && clearPgUser != null && clearPgPass != null) {
                        clearPostgresDb(clearPgJdbcUrl, clearPgUser, clearPgPass)
                    }

                    currentSerial = requestDeviceSerial
                    adbVideoName = call.request.queryParameters[TESTNAME_PARAM]
                        ?: System.currentTimeMillis().toString()

                    val port = findFreePort().also {
                        currentReverseProxyPort = it
                    }

                    val siteUrl = if(mode == RunMode.CYPRESS) {
                        controllerUrl
                    }else {
                        "http://${controllerUrlObj.host}:$port/"
                    }

                    val dataDir = File(baseDataDir, sanitizeDbNameFromUrl(siteUrl))
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

                    val serverArgsWithSiteUrl = serverArgs +
                            "-P:ktor.ustad.siteUrl=$siteUrl" +
                            "-P:ktor.deployment.port=$port" +
                            "-P:ktor.ustad.datadir=${dataDir.absolutePath}"

                    serverProcess = ProcessBuilder(serverArgsWithSiteUrl)
                        .directory(serverDir)
                        .redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start()

                    response += "Started server process PID #${serverProcess?.pid()} " +
                            "${serverArgsWithSiteUrl.joinToString( " ")} " +
                            "(workingDir=${serverDir.absolutePath}<br/>"

                    if(adbRecordEnabled) {
                        ProcessBuilder(
                            listOf(adbPath.absolutePath, "-s", requestDeviceSerial, "shell",
                                "screencap", "/sdcard/$adbVideoName.png")
                        ).start().waitFor(5, TimeUnit.SECONDS)
                        val screenshotDestFile = File(File(resultDir, adbVideoName ?: "err"),
                            "screenrecord-poster.png")
                        adbPullFile(requestDeviceSerial, "/sdcard/$adbVideoName.png",
                            screenshotDestFile, deleteAfter = true)

                        val recordArgs = listOf(adbPath.absolutePath, "-s", requestDeviceSerial,
                            "shell", "screenrecord", "/sdcard/$adbVideoName.mp4")
                        adbRecordProcess = ProcessBuilder(recordArgs)
                            .redirectOutput(ProcessBuilder.Redirect.PIPE)
                            .redirectError(ProcessBuilder.Redirect.PIPE)
                            .start()

                        response += "Started video recording: ${recordArgs.joinToString(separator = " ")} " +
                                "PID ${adbRecordProcess?.pid()} <br/>"
                        application.log.info("Started video recording: ${recordArgs.joinToString(separator = " ")} " +
                                "PID ${adbRecordProcess?.pid()}")
                    }


                    call.response.header("cache-control", "no-cache")

                    val urlToWaitFor = URL(URL(siteUrl), "umapp/")

                    okHttpClient.waitForUrl(urlToWaitFor.toString())

                    call.respond(
                        ServerInfo(
                            url = siteUrl,
                            port = Url(siteUrl).port,
                            extraInfo = response,
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
                serverProcess?.also {
                    it.destroy()
                    it.waitFor()
                }
                stopRecording()
                call.response.header("cache-control", "no-cache")
                call.respond(HttpStatusCode.OK, "OK")
            }

            /**
             * Clear the Downloads directory of the device (to avoid running out of space and make
             * sure that the uploaded content for a given test is visible at the top of the list).
             *
             * /cleardownloads?device=<serial>
             */
            get("/cleardownloads") {
                val deviceSerial = call.request.queryParameters[DEVICE_SERIAL_PARAM]

                val adbCommand = SysPathUtil.findCommandInPath("adb")
                    ?: throw IllegalStateException("Cannot find adb in path")

                val process = ProcessBuilder(listOf(adbCommand.absolutePath,
                    "-s", deviceSerial, "shell", "rm", "-r", "/sdcard/Download/*"))
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

            /**
             * Push file from the test content directory to the device Downloads directory using adb
             *
             * /pushcontent?device=<serial>&test-file-name=file-name.ext&dest=/sdcard/Pictures
             *
             * dest parameter is optional. The argument MUST be url encoded.
             *
             * test-file-name should be the name of a file found in the test files directory (
             * test-end-to-end/test-files/content )
             */
            get("/pushcontent") {
                val deviceSerial = call.request.queryParameters[DEVICE_SERIAL_PARAM]
                val fileName = call.request.queryParameters[TEST_FILE_NAME_PARAM]
                    ?: throw IllegalArgumentException("No filename specified")
                val pushDest = call.request.queryParameters[DEST_PARAM] ?: "/sdcard/Download"
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
                    "-s", deviceSerial, "push", contentFile.absolutePath, pushDest))
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
}
