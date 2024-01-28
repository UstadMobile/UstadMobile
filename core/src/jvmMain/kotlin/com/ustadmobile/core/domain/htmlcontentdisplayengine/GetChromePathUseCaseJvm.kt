package com.ustadmobile.core.domain.htmlcontentdisplayengine

import com.ustadmobile.core.util.ext.isWindowsOs
import com.ustadmobile.lib.util.SysPathUtil
import java.io.File
import java.util.concurrent.TimeUnit

class GetChromePathUseCaseJvm(
    private val workingDir: File,
): GetChromePathUseCase {

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

    override suspend fun invoke() : String? {
        return if(isWindowsOs()) {
            getChromePathWindows()
        }else {
            getChromePathNix()
        }
    }
}