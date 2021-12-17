package com.ustadmobile.core.util.ext

//buffer used on js code
@Suppress("UNUSED_VARIABLE")
actual fun ByteArray.encodeBase64(): String  {
    val buffer = this
    return js("Buffer.from(buffer).toString('base64')").toString()
}
