package com.ustadmobile.door.sse

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext


actual class DoorEventSource actual constructor(var url: String, var listener: DoorEventListener) {

    val eventSourceJob: Job

    init {
        eventSourceJob = GlobalScope.async {
            connect()
        }
    }

    actual fun close() {
        eventSourceJob.cancel()
    }

    private suspend fun connect() {
        var urlConnection: HttpURLConnection? = null

        var input: ByteReadChannel? = null

        while(coroutineContext.isActive) {
            try {
                urlConnection = URL(url).openConnection() as HttpURLConnection
                urlConnection.connectTimeout = CONNECT_TIMEOUT
                urlConnection.readTimeout = READ_TIMEOUT

                //these are hardcoded for test purposes only - should be put in url parameters instead
                urlConnection.setRequestProperty("door-dbversion", "2")

                input = urlConnection.inputStream.toByteReadChannel()

                var dataStr: String = ""
                var id: String = ""
                var event: String = ""

                var line: String? = ""
                while(coroutineContext.isActive &&
                        input.readUTF8Line(8 * 1024).also { line = it }.let { it != null }) {
                    val lineVal = line ?: break
                    when {
                        lineVal.startsWith("id:") -> {
                            id = lineVal.removePrefix("id:").trim()
                        }

                        lineVal.startsWith("event:") -> {
                            event = lineVal.removePrefix("event:").trim()
                        }

                        lineVal.startsWith("data:") -> {
                            dataStr += lineVal.removePrefix("data:").trim()
                        }

                        lineVal.isBlank() -> {
                            listener.onMessage(DoorServerSentEvent(id, event, dataStr))
                            id = ""
                            event = ""
                            dataStr = ""
                        }
                    }
                }

            }catch(e: Exception) {
                listener.onError(e)
            }finally {
                input?.cancel()
                urlConnection?.disconnect()
            }

            delay(1000)
        }
    }


    companion object {
        const val CONNECT_TIMEOUT = 10000

        const val READ_TIMEOUT = (60 * 60 * 1000) // 1 hour

    }

}