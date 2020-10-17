package com.ustadmobile.door.sse

import com.ustadmobile.door.ext.doorIdentityHashCode
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext
import com.github.aakira.napier.Napier
import com.ustadmobile.door.ext.DoorTag

actual class DoorEventSource actual constructor(var url: String, var listener: DoorEventListener) {

    val eventSourceJob: Job

    val logPrefix: String
        get() = "[DoorEventSource@${this.doorIdentityHashCode}]"

    init {
        eventSourceJob = GlobalScope.async {
            connect()
        }
    }

    actual fun close() {
        Napier.d("$logPrefix close", tag = DoorTag.LOG_TAG)
        eventSourceJob.cancel()
    }

    private suspend fun connect() {
        var urlConnection: HttpURLConnection? = null

        var input: ByteReadChannel? = null

        while(coroutineContext.isActive) {
            try {
                Napier.d("$logPrefix Connect to server side events from: $url", tag = DoorTag.LOG_TAG)
                urlConnection = URL(url).openConnection() as HttpURLConnection
                urlConnection.connectTimeout = CONNECT_TIMEOUT
                urlConnection.readTimeout = READ_TIMEOUT


                input = urlConnection.inputStream.toByteReadChannel()

                listener.onOpen()
                Napier.d("$logPrefix connected", tag = DoorTag.LOG_TAG)

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
                            val eventObj = DoorServerSentEvent(id, event, dataStr)
                            Napier.d("$logPrefix Event: $eventObj", tag = DoorTag.LOG_TAG)
                            listener.onMessage(eventObj)
                            id = ""
                            event = ""
                            dataStr = ""
                        }
                    }
                }

            }catch(e: Exception) {
                Napier.e(logPrefix, e, tag = DoorTag.LOG_TAG)
                listener.onError(e)
            }finally {
                Napier.d("$logPrefix disconnecting", tag = DoorTag.LOG_TAG)
                input?.cancel()
                urlConnection?.disconnect()
            }

            if(coroutineContext.isActive) {
                Napier.d("$logPrefix waiting for reconnect", tag = DoorTag.LOG_TAG)
                delay(1000)
            }
        }
    }


    companion object {
        const val CONNECT_TIMEOUT = 10000

        const val READ_TIMEOUT = (60 * 60 * 1000) // 1 hour

    }

}