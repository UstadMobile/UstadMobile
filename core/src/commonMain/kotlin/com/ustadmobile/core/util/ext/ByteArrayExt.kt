package com.ustadmobile.core.util.ext

fun ByteArray.toHexString() = joinToString(separator = "") { it.toUByte().toString(16).padStart(2, '0') }
