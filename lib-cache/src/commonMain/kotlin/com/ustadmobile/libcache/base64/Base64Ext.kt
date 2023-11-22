package com.ustadmobile.libcache.base64

internal expect fun ByteArray.encodeBase64(): String

internal expect fun String.decodeBase64(): ByteArray

