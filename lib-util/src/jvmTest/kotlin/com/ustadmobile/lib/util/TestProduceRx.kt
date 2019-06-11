package com.ustadmobile.lib.util

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.junit.Test
import kotlin.coroutines.coroutineContext

class TestProduceRx {

    @Test
    fun testFanOut2() {
        runBlocking {
            runIt()
        }
    }


    suspend fun runIt() {
        val startTime = System.currentTimeMillis()
        withContext(coroutineContext){
            val produceNums = produce<Int> {
                var x = 1
                for(i in 1..100) {
                    send(i)
                    delay(100)
                }
            }

            repeat(5) {
                GlobalScope.launch {
                    runProcessing(it, produceNums)
                }
            }

            println("Finished launch in ${System.currentTimeMillis() - startTime}ms")
        }

        println("Finished fan out in ${System.currentTimeMillis() - startTime}ms")
    }



    @Test
    fun testFanOut() {
        runBlocking {
            val produceNums = produce<Int> {
                var x = 1
                for(i in 1..100) {
                    send(i)
                    delay(100)
                }
            }

            repeat(5) {
                launch {
                    runProcessing(it, produceNums)
                }
            }
        }
    }

    suspend fun runProcessing(id: Int, channel: ReceiveChannel<Int>) {
        for (msg in channel) {
            println("Processor #$id received $msg")
        }
    }




}