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
    val runServerCommand: String,
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

        val serverArgs = runServerCommand.split(Regex("\\s+")).toMutableList()
        if(!(serverArgs[0].startsWith(".") || serverArgs[0].startsWith("/"))) {
            serverArgs[0] = SysPathUtil.findCommandInPath(serverArgs[0])?.absolutePath
                ?: throw IllegalArgumentException("Could not find server command in PATH ${serverArgs[0]}")
        }

        val serverArgsWithSiteUrl = serverArgs +
                "-P:ktor.ustad.siteUrl=$learningSpaceUrl" +
                "-P:ktor.deployment.port=$port" +
                "-P:ktor.ustad.datadir=${dataDir.absolutePath}"

        val commandLine = serverArgsWithSiteUrl.joinToString(separator = " ")
        println("exec $commandLine")

        serverProcess = ProcessBuilder(serverArgsWithSiteUrl)
            .directory(serverDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val urlToWaitFor = if(mode == RunMode.CYPRESS)
            URL(URL(learningSpaceUrl), "umapp/")
        else
            URL(learningSpaceUrl)

        okHttpClient.waitForUrl(urlToWaitFor.toString())
    }


    fun stop() {
        serverProcess?.also {
            it.destroy()
            it.waitFor()
            serverProcess = null
        }
    }

}