package com.ustadmobile.lib.util

import java.io.File
import kotlin.test.Test
import org.junit.Assert
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class TestSysPathUtil {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenCommandInPathOnUnix_whenFindCommandInPathCalled_thenReturnsFile() {
        val tmpPath = temporaryFolder.newFolder()
        val existingCmd = File(tmpPath, "command_name")
        existingCmd.writeText("a")


        Assert.assertEquals(existingCmd,
            SysPathUtil.findCommandInPath("command_name", null,
                "/usr/bin:${tmpPath.absolutePath}", osName = "linux", fileSeparator = ":"))
    }

    @Test
    fun givenCommandNotInPathOnUnix_whenFindCommandInPathCalled_thenReturnNull() {
        Assert.assertNull(
            SysPathUtil.findCommandInPath("command_name", null,
            "/usr/bin:${temporaryFolder.newFolder()}", osName = "linux", fileSeparator = ":"))
    }

    @Test
    fun givenCommandInPathOnWindows_whenCommandInPathCalled_thenReturnsFile() {
        val tmpPath = temporaryFolder.newFolder()
        val existingCmd = File(tmpPath, "command_name.exe")
        existingCmd.writeText("a")

        Assert.assertEquals(existingCmd,
            SysPathUtil.findCommandInPath("command_name", null,
                "C:\\Windows\\System32;${tmpPath.absolutePath}",
                osName= "win", fileSeparator = ";"))
    }

    @Test
    fun givenCommandNotInPathOnWindows_whenCommandInPathCalled_thenReturnsNull() {
        val tmpPath = temporaryFolder.newFolder()
        Assert.assertNull(
            SysPathUtil.findCommandInPath("command_name", null,
                "C:\\Windows\\System32;${tmpPath.absolutePath}", osName= "win", fileSeparator = ";"))
    }


}