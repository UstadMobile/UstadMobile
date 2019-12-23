package com.ustadmobile.sharedse.network

import kotlinx.io.InputStream
import kotlinx.io.OutputStream

typealias HttpSessionFactory = (inputStream: InputStream, outputStream: OutputStream) -> IHttpSessionSe