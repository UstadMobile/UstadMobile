package com.ustadmobile.sharedse.network

import fi.iki.elonen.NanoHTTPD
import java.io.InputStream
import java.io.OutputStream

open class NanoHttpdWithSessionSource(port: Int) : NanoHTTPD(port) {
    val sessionSource: NanoHttpdSessionSource = { inStream: InputStream, outStream: OutputStream ->
        HTTPSession(tempFileManagerFactory.create(), inStream, outStream)
    }
}