package com.ustadmobile.core.util.ext


fun String.hexStringToByteArray() = this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

expect fun String.base64StringToByteArray(): ByteArray
