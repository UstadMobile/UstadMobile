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
                   val numProcessors: Int = 1) {

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

                UMLog.l(UMLog.INFO, 0, "BleProxy: Request ID# $messageId ${httpSession.uri} " +
                        "SEND ${carbonCopyBufferArr.size} bytes BLE HTTP Request " +
                        "to $destDeviceAddr")
                networkManager.sendMessage(context, httpMessage, destNetworkNode,
                        object : BleMessageResponseListener {
                            override fun onResponseReceived(sourceDeviceAddress: String, response: BleMessage?, error: Exception?) {
                                if(response != null) {
                                    UMLog.l(UMLog.INFO, 0, "BleProxy: Request ID# $messageId : ${httpSession.uri} RECEIVE OK " +
                                            "$messageId ${response.payload?.size} bytes")
                                    deferredResponse.complete(response)
                                }else {
                                    UMLog.l(UMLog.ERROR, 0, "BleProxy: Request ID# $messageId : " +
                                            "${httpSession.uri} RECEIVE FAILED $error")
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
                if(payload != null && payload.size == response.length) {
                    UMLog.l(UMLog.INFO, 0, "BleProxy: Request ID# $messageId : ${httpSession.uri} Writing  " +
                            "$messageId ${payload.size} bytes to socket")
                    socketOut.write(payload)
                }else if(payload != null && response != null && payload.size != response.length) {
                    UMLog.l(UMLog.ERROR, 0, "BleProxy: Request ID# $messageId : ${httpSession.uri} " +
                            "ERROR: payload size != expected message length " +
                            "(expected ${response.length} received ${payload.size}")
                    throw IOException("BLE ERROR: Message length != payload size")
                }else {
                    UMLog.l(UMLog.ERROR, 0, "BleProxy: Request ID# $messageId : ${httpSession.uri}  " +
                            " ERROR: Timed out or null payload response")
                    throw IOException("BLE response packet timed out or had no payload")
                }
            }catch(e: Exception) {
                try {
                    val errorResponseBytes = e.toString().toUtf8Bytes()
                    val headerBytes = ("HTTP/1.1 502 Bad Gateway\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Connection: close\r\n" +
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
            repeat(numProcessors) { launchSocketProcessor(clientSocketProducer)}
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