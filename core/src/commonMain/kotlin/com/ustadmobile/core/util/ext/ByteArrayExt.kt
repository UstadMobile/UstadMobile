package com.ustadmobile.core.util.ext

import kotlinx.io.ByteArrayOutputStream

fun ByteArray.toHexString() = joinToString(separator = "") { it.toUByte().toString(16).padStart(2, '0') }

expect fun ByteArray.encodeBase64(): String
