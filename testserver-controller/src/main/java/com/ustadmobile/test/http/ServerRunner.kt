package com.ustadmobile.test.http

import com.ustadmobile.lib.util.SysPathUtil
import okhttp3.OkHttpClient
import java.io.File
import java.net.URL

class ServerRunner(
    val mode: RunMode,
    private val okHttpClient: OkHttpClient,
    private val serverDir: File,
    val runServerCommand: String,
    private val controllerUrl: URL,
    private val baseDataDir: File,
    @Suppress("unused") //reserved for future use
    private val adbDeviceSerial: String? = null,
    @Suppress("unused") //reserved for future use
    private val adbRecordEnabled: Boolean = false,
    @Suppress("unused") //reserved for future use
    private val adbVideoName: String? = null,
) {

    val port = findFreePort()

    val siteUrl = if(mode == RunMode.CYPRESS) {
        controllerUrl.toString()
    }else {
        "http://${controllerUrl.host}:$port/"
    }

    val dataDir = File(baseDataDir, "server-$port")

    val pid: Long
        get() = serverProcess?.pid() ?: -1

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
                "-P:ktor.ustad.siteUrl=$siteUrl" +
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
            URL(URL(siteUrl), "umapp/")
        else
            URL(siteUrl)

        okHttpClient.waitForUrl(urlToWaitFor.toString())
    }


    fun stop() {
        serverProcess?.also {
            it.destroy()
            it.waitFor()
        }
    }

}