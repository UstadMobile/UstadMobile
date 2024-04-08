package com.ustadmobile.core.util.ext

import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assume
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ProcessExtTest {

    @Test
    fun givenProcessStarted_whenWaitForInterrupted_thenWillDestroyProcess() {
        Assume.assumeTrue(File("/usr/bin/sleep").exists())
        val startTime = systemTimeInMillis()
        runBlocking {
            val process = ProcessBuilder(listOf("/usr/bin/sleep", "30")).start()
            val processResult = async {
                println("process.waitForAsync")
                process.waitForAsync().also {
                    println("process done")
                }
            }
            println("Delaying...")
            delay(100)
            println("canceling result")
            processResult.cancel()
            println("Canceled")

            assertTrue(
                systemTimeInMillis() - startTime < 1000,
                "If cancellation worked as expected, then the full 30s should not have happened")
        }
    }


}