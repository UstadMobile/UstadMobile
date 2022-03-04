package com.ustadmobile.core.util

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
            SysPathUtil.findCommandInPath("command_name",
                "/usr/bin:${tmpPath.absolutePath}", "linux", ":"))
    }

    @Test
    fun givenCommandNotInPathOnUnix_whenFindCommandInPathCalled_thenReturnNull() {
        Assert.assertNull(SysPathUtil.findCommandInPath("command_name",
            "/usr/bin:${temporaryFolder.newFolder()}", "linux", ":"))
    }

    @Test
    fun givenCommandInPathOnWindows_whenCommandInPathCalled_thenReturnsFile() {
        val tmpPath = temporaryFolder.newFolder()
        val existingCmd = File(tmpPath, "command_name.exe")
        existingCmd.writeText("a")

        Assert.assertEquals(existingCmd,
            SysPathUtil.findCommandInPath("command_name",
                "C:\\Windows\\System32;${tmpPath.absolutePath}", "win", ";"))
    }

    @Test
    fun givenCommandNotInPathOnWindows_whenCommandInPathCalled_thenReturnsNull() {
        val tmpPath = temporaryFolder.newFolder()
        Assert.assertNull(
            SysPathUtil.findCommandInPath("command_name",
                "C:\\Windows\\System32;${tmpPath.absolutePath}", "win", ";"))
    }


}