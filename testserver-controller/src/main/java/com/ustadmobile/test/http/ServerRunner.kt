package com.ustadmobile.test.http

import com.ustadmobile.lib.util.SysPathUtil
import okhttp3.OkHttpClient
import java.io.File
import java.net.InetAddress
import java.net.URL

class ServerRunner(
    val mode: RunMode,
    private val okHttpClient: OkHttpClient,
    private val serverDir: File,
    private val controllerUrl: URL,
    private val learningSpaceHost: InetAddress,
    private val baseDataDir: File,
    @Suppress("unused") //reserved for future use
    private val adbDeviceSerial: String? = null,
    @Suppress("unused") //reserved for future use
    private val adbRecordEnabled: Boolean = false,
    @Suppress("unused") //reserved for future use
    private val adbVideoName: String? = null,
    private val fromPort: Int = DEFAULT_FROM_PORT,
    private val untilPort: Int = DEFAULT_UNTIL_PORT,
) {

    val port = findFreePort(from = fromPort, until = untilPort)

    val learningSpaceUrl = if(mode == RunMode.CYPRESS) {
        controllerUrl.toString()
    }else {
        "http://${learningSpaceHost.hostAddress}:$port/"
    }

    val dataDir = File(baseDataDir, "server-$port")

    val pid: Long
        get() = serverProcess?.pid() ?: -1

    @Volatile
    private var serverProcess: Process? = null

    fun start() {
        if(dataDir.exists())
            dataDir.deleteRecursively()

        val logDir = File(dataDir, "log")
        logDir.mkdirs()

        val runServerCommand = "java -Dlogs_dir=${logDir.absolutePath} -jar build/libs/ustad-server-all.jar"

        val serverArgs = runServerCommand.split(Regex("\\s+")).filter {
            it.isNotEmpty()
        }.toMutableList()

        if(!(serverArgs[0].startsWith(".") || serverArgs[0].startsWith("/"))) {
            serverArgs[0] = SysPathUtil.findCommandInPath(serverArgs[0])?.absolutePath
                ?: throw IllegalArgumentException("Could not find server command in PATH ${serverArgs[0]}")
        }

        val configFile = File(serverDir, "src/main/resources/application.conf")

        /*
         * Manually force use of the source code resources application.conf because
         * a) ensure that any local ustad-server.conf will not interfere with the test run
         * b) application.conf in resources has been seen not to be loaded as it should be when running
         *    on command line.
         */
        val serverArgsWithSiteUrl = serverArgs +
                "runserver" +
                "-config=${configFile.absolutePath}" +
                "-P:ktor.deployment.port=$port" +
                "-P:ktor.ustad.datadir=${dataDir.absolutePath}" +
                "-P:ktor.ustad.jsDevServer="

        val commandLine = serverArgsWithSiteUrl.joinToString(separator = " ")
        println("TestServerController: exec $commandLine")

        serverProcess = ProcessBuilder(serverArgsWithSiteUrl)
            .directory(serverDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val urlToWaitFor = if(mode == RunMode.CYPRESS)
            URL(URL(learningSpaceUrl), "umapp/")
        else
            URL(URL(learningSpaceUrl), "api/centralappconfig/learningspace/getAll")

        okHttpClient.waitForUrl(urlToWaitFor.toString())

        val createLearningSpaceCommandArgs = buildList {
            addAll(serverArgs)
            add("newlearningspace")
            add("--title")
            add("TestLearningSpace")
            add("--url")
            add(learningSpaceUrl)
            add("--adminpassword")
            add("testpass")
            add("--datadir")
            add(dataDir.absolutePath)
        }

        val addingLearningSpaceProcess = ProcessBuilder(createLearningSpaceCommandArgs)
            .directory(serverDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        val status = addingLearningSpaceProcess.waitFor()

        val output = addingLearningSpaceProcess.inputStream.bufferedReader().readText()
        println(output)
        val errorOutput = addingLearningSpaceProcess.errorStream.bufferedReader().readText()
        println(errorOutput)
        if(status != 0)
            throw IllegalStateException("Could not add learning space")
    }


    fun stop() {
        println("TestServerController: stopping server on port: $port")
        serverProcess?.also {
            it.destroy()
            it.waitFor()
            serverProcess = null
        }
    }

}