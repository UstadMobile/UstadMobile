package com.ustadmobile.sharedse.network

import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class BleHttpProxy(val networkManager: NetworkManagerBleCommon) {

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

    class HttpdBase: NanoHTTPD(8094) {

        fun newSession(inputStream: InputStream, outputStream: OutputStream): IHTTPSession {
            return HTTPSession(tempFileManagerFactory.create(), inputStream, outputStream)
        }

        override fun serve(session: IHTTPSession?): Response {
            return newFixedLengthResponse("Hello World")
        }
    }


    fun start(port: Int) {
        serverSocket = ServerSocket(port)
        active.set(true)
        GlobalScope.launch {
            do {
                var clientIn: InputStream? = null
                var socketOut: OutputStream? = null
                var socket: Socket? = null
                try {
                    socket = serverSocket?.accept()
                    val carbonCopyBuffer = ByteArrayOutputStream()
                    clientIn = CarbonCopyInputStream(socket!!.getInputStream(), carbonCopyBuffer)
                    socketOut = socket.getOutputStream()

                    val httpSession = httpd.newSession(clientIn, ByteArrayOutputStream()) //discard the response
                    httpSession.execute()
                    carbonCopyBuffer.flush()

                    val destDeviceAddr = httpSession.uri.substring(1,
                            httpSession.uri.indexOf('/', 2))

                    val httpMessage = BleMessage(BleMessage.MESSAGE_TYPE_HTTP,
                            BleMessage.getNextMessageIdForReceiver(destDeviceAddr),
                            carbonCopyBuffer.toByteArray())
                    val destNetworkNode = NetworkNode().also { it.bluetoothMacAddress = destDeviceAddr }
                    val deferredResponse = CompletableDeferred<BleMessage>()
                    networkManager.sendMessage(networkManager.context, httpMessage, destNetworkNode,
                            object : BleMessageResponseListener {
                                override fun onResponseReceived(sourceDeviceAddress: String, response: BleMessage?, error: Exception?) {
                                    if(response != null) {
                                        deferredResponse.complete(response)
                                    }else {
                                        deferredResponse.completeExceptionally(IOException("error response"))
                                    }
                                }
                            })

                    val response = deferredResponse.await()
                    val payload = response.payload
                    if(payload != null) {
                        socketOut.write(payload)
                    }
                }catch(e: Exception) {

                }finally {
                    clientIn?.close()
                    socketOut?.flush()
                    socketOut?.close()
                    socket?.close()
                }
            }while(active.get())
        }
    }

}