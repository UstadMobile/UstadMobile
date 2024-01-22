package com.ustadmobile.libuicompose.util.ext

import java.net.URLDecoder

actual fun String.urlDecode(): String = URLDecoder.decode(this, "UTF-8")
