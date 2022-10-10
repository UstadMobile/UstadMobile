package com.ustadmobile.port.android

import fi.iki.elonen.NanoHTTPD
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.*
import java.net.ServerSocket

class ForwardTestTmpDroid2{


    //class HttpdBase : NanoHt
    class HttpdBase: NanoHTTPD(8094) {

        fun newSession(inputStream: InputStream, outputStream: OutputStream): IHTTPSession {
            return HTTPSession(tempFileManagerFactory.create(), inputStream, outputStream)
        }

        override fun serve(session: IHTTPSession?): Response {
            return newFixedLengthResponse("Hello World")
        }
    }

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

    val httpServer: HttpdBase = HttpdBase()


    @Test
    fun canForward() {
        val serverSocket = ServerSocket(8095)

        Thread(Runnable {
            val socket = serverSocket.accept()
            val socketIn = socket.getInputStream()
            val socketOut = socket.getOutputStream()


            val requestInputBout = ByteArrayOutputStream()
            val copyRequestInput = CarbonCopyInputStream(socketIn, requestInputBout)
            val session1 = httpServer.newSession(copyRequestInput, ByteArrayOutputStream())
            session1.execute()



            requestInputBout.flush()

            val responseBout = ByteArrayOutputStream()
            val httpdSession = httpServer.newSession(ByteArrayInputStream(requestInputBout.toByteArray()),
                    responseBout)
            httpdSession.execute()
            responseBout.flush()
            socketOut.write(responseBout.toByteArray())
            socketIn.close()
            socketOut.flush()
            socketOut.close()
            socket.close()
        }).start()

//        val server = TcpRawHttpServer(8093)
//
//        server.start {req ->
//            val bout = ByteArrayOutputStream()
//            req.writeTo(bout)
//            val inStream = ByteArrayInputStream(ByteArray(4))
//            Optional.of(rawHttp.parseResponse(inStream))
//        }

        val httpClient = HttpClient()

        runBlocking {
            try {
                val serverResponse = httpClient.get("http://localhost:8095").bodyAsText()
                println(serverResponse)
            }catch(e: Exception) {
                e.printStackTrace()
            }
        }



    }
}