package com.ustadmobile.core.domain.htmlcontentdisplayengine

import com.ustadmobile.core.util.ext.isWindowsOs
import com.ustadmobile.lib.util.SysPathUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class LaunchChromeUseCase(
    private val workingDir: File,
) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private fun getChromePathNix(): String? {
        return SysPathUtil.findCommandInPath("google-chrome")?.absolutePath
            ?: SysPathUtil.findCommandInPath("chromium")?.absolutePath
    }

    private fun getChromePathWindows(): String? {
        val process = ProcessBuilder(
            listOf("reg", "query", "\"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe\""))
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .directory(workingDir)
            .start().apply { waitFor(10, TimeUnit.SECONDS) }
        val lines = process.inputStream.bufferedReader().use { it.readText() }.lines()
        return lines.firstOrNull { it.contains("REG_SZ") }
            ?.substringAfter("REG_SZ")?.trim()
    }

    private val chromePath: String? by lazy {
        if(isWindowsOs()) {
            getChromePathWindows()
        }else {
            getChromePathNix()
        }
    }

    suspend operator fun invoke(
        url: String
    ) {
        val chromePathVal = withContext(Dispatchers.IO) {
            chromePath ?: throw IllegalArgumentException("Could not find chrome")
        }

        scope.launch {
            ProcessBuilder(chromePathVal, "--app=$url")
                .directory(workingDir)
                .start()
                .waitFor()
        }
    }

    fun close() {
        scope.cancel()
    }
}