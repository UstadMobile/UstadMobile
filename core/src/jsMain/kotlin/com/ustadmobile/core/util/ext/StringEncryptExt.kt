package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.asmcrypto
import kotlin.js.Date

//secret is used by js code
@Suppress("UNUSED_VARIABLE")
actual fun String.encryptWithPbkdf2(salt: String, iterations: Int, keyLength: Int): ByteArray {
    //For this to work on both platform  (i.e produce the same output on JVM and JS),
    // keyLength has to be divided by 8 since In JVM it's specified in bits while in node is in bytes
    val password = this
    val uint8ArrayVal =  asmcrypto.Pbkdf2HmacSha1(js("new TextEncoder().encode(password)"), js("new TextEncoder().encode(salt)"), iterations, (keyLength/8))
    val buffer = js("Buffer.from(new Uint8Array(uint8ArrayVal))")
    return buffer
}
