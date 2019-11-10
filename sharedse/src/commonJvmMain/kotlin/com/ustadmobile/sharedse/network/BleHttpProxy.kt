package com.ustadmobile.sharedse.network

import com.ustadmobile.lib.db.entities.NetworkNode
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.io.IOException
import kotlinx.io.InputStream
import kotlinx.serialization.toUtf8Bytes
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import com.ustadmobile.core.impl.UMLog

/**
 * Ble Http Proxy will listen on a TCP Port and send an HTTP request as a BleMessage. When the
 * BleMessage response is received, the response will be written back to the TCP socket. It is
 * a proxy between TCP/IP Http and BLE.
 *
 *
 */
class BleHttpProxy(val networkManager: NetworkManagerBleCommon,
                   val context: Any,
                   val numProcessors: Int = 5) {

    var serverSocket: ServerSocket? = null

    val active = AtomicBoolean(false)

    val httpd = HttpdBase()

    class CarbonCopyInputStream(src: InputStream, private val copyStream: OutputStream): FilterInputStream(src) {

        override fun read(): Int {
            val i = super.read()
            copyStream.write(i)
            return i
        }

        override fun read(b: ByteArray): Int {
            val bytesRead = super.read(b)
            copyStream.write(b, 0, bytesRead)
            return bytesRead
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val bytesRead = super.read(b, off, len)
            copyStream.write(b, off, bytesRead)
            return bytesRead
        }
    }

    class HttpdBase: NanoHTTPD(0) {

        fun newSession(inputStream: InputStream, outputStream: OutputStream): IHTTPSession {
            return HTTPSession(tempFileManagerFactory.create(), inputStream, outputStream)
        }

        override fun serve(session: IHTTPSession?): Response {
            return newFixedLengthResponse("Hello World")
        }
    }


    fun CoroutineScope.produceClientSockets() = GlobalScope.produce<Socket> {
        while(true) {
            val clientSocket = serverSocket!!.accept()
            send(clientSocket)
        }
    }

    fun CoroutineScope.launchSocketProcessor(channel: ReceiveChannel<Socket>) = launch {
        for(socket in channel) {
            var clientIn: InputStream? = null
            var socketOut: OutputStream? = null
            val deferredResponse = CompletableDeferred<BleMessage>()

            try {
                val carbonCopyBuffer = ByteArrayOutputStream()
                clientIn = CarbonCopyInputStream(socket.getInputStream(), carbonCopyBuffer)
                socketOut = socket.getOutputStream()

                val httpSession = httpd.newSession(clientIn, ByteArrayOutputStream()) //discard the response
                httpSession.execute()
                carbonCopyBuffer.flush()

                val prefixEndIndex = httpSession.uri.indexOf(PREFIX) + PREFIX.length + 1

                val destDeviceAddr = httpSession.uri.substring(prefixEndIndex,
                        httpSession.uri.indexOf('/', prefixEndIndex))

                val carbonCopyBufferArr = carbonCopyBuffer.toByteArray()
                val messageId = BleMessage.getNextMessageIdForReceiver(destDeviceAddr)
                val httpMessage = BleMessage(BleMessage.MESSAGE_TYPE_HTTP,
                        messageId,
                        carbonCopyBufferArr)
                val destNetworkNode = NetworkNode().also { it.bluetoothMacAddress = destDeviceAddr }

                UMLog.l(UMLog.INFO, 0, "Sending ${carbonCopyBufferArr.size} bytes BLE HTTP Request " +
                        "to $destDeviceAddr Request ID# $messageId")
                networkManager.sendMessage(context, httpMessage, destNetworkNode,
                        object : BleMessageResponseListener {
                            override fun onResponseReceived(sourceDeviceAddress: String, response: BleMessage?, error: Exception?) {
                                if(response != null) {
                                    UMLog.l(UMLog.INFO, 0, "Received response for request ID# " +
                                            "$messageId ${response.payload?.size} bytes")
                                    deferredResponse.complete(response)
                                }else {
                                    UMLog.l(UMLog.INFO, 0, "Response with error for request ID# " +
                                            "$messageId ")
                                    val exception = if(error != null) {
                                        IOException("Exception receiving response from $sourceDeviceAddress",
                                                error)
                                    }else {
                                        IOException("Exception receiving response from $sourceDeviceAddress")
                                    }
                                    deferredResponse.completeExceptionally(exception)
                                }
                            }
                        })

                val response = withTimeoutOrNull(8000) { deferredResponse.await() }
                val payload = response?.payload
                if(payload != null) {
                    socketOut.write(payload)
                }else {
                    throw IOException("BLE response packet timed out or had no payload")
                }
            }catch(e: Exception) {
                try {
                    val errorResponseBytes = e.toString().toUtf8Bytes()
                    val headerBytes = ("HTTP/1.1 502 Bad Gateway\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: ${errorResponseBytes.size}\n" +
                            "\n").toUtf8Bytes()
                    socketOut?.write(headerBytes)
                    socketOut?.write(errorResponseBytes)
                }finally {
                    //nothing more we can do
                }
            } finally {
                clientIn?.close()
                socketOut?.flush()
                socketOut?.close()
                socket.close()
            }
        }
    }

    fun start(port: Int) {
        serverSocket = ServerSocket(port)
        active.set(true)
        GlobalScope.launch {
            val clientSocketProducer = produceClientSockets()
            repeat(5) { launchSocketProcessor(clientSocketProducer)}
        }
    }

    fun stop() {
        serverSocket?.close()
        serverSocket = null
    }

    companion object {
        const val PREFIX = "rest"
    }


}