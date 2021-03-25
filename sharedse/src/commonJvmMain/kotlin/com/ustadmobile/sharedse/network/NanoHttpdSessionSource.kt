package com.ustadmobile.sharedse.network

import fi.iki.elonen.NanoHTTPD
import java.io.InputStream
import java.io.OutputStream

typealias NanoHttpdSessionSource = (inStream: InputStream, outStream: OutputStream) -> NanoHTTPD.IHTTPSession
