package com.ustadmobile.core.network

import fi.iki.elonen.NanoHTTPD
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket

class NanoHTTPDSockTest {

    class NanoHTTPDAccesor(port: Int): NanoHTTPD(port) {

        override fun serve(session: IHTTPSession?): Response {
            return newFixedLengthResponse("Hello World")
        }

        fun newSession(inputStream: InputStream, outputStream: OutputStream): IHTTPSession {
            return super.HTTPSession(DefaultTempFileManager(), inputStream, outputStream)
        }

    }

    @Test
    fun test() {
        val httpd = NanoHTTPDAccesor(0)
        httpd.start()
        //listen on socket
        val serverSocket = ServerSocket(0)
        Thread(Runnable {
            val clientSocket = serverSocket.accept()
            while(!clientSocket.isClosed) {
                val httpSession = httpd.newSession(clientSocket.getInputStream(),
                        clientSocket.getOutputStream())
                httpSession.execute()
            }
        }).start()

        println("Listening on port ${serverSocket.localPort}")
        Thread.sleep(60000* 5)

    }

}