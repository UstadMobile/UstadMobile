package com.ustadmobile.sharedse.network

import fi.iki.elonen.NanoHTTPD
import kotlinx.io.InputStream
import kotlinx.io.OutputStream

typealias NanoHttpdSessionSource = (inStream: InputStream, outStream: OutputStream) -> NanoHTTPD.IHTTPSession
