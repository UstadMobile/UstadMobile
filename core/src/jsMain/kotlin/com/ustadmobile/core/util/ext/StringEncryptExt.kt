package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.pbkdf2

//secret is used by js code
@Suppress("UNUSED_VARIABLE")
actual fun String.encryptWithPbkdf2(salt: String, iterations: Int, keyLength: Int): ByteArray {
    //For this to work on both platform  (i.e produce the same output on JVM and JS),
    // keyLength has to be divided by 8 since In JVM it's specified in bits while in node is in bytes
    return pbkdf2.pbkdf2Sync(this, salt, iterations, (keyLength/8), "sha1")
}
