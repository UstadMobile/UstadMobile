package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.pbkdf2

//secret is used by js code
@Suppress("UNUSED_VARIABLE")
actual fun String.encryptWithPbkdf2(salt: String, iterations: Int, keyLength: Int): ByteArray {
    return pbkdf2.pbkdf2Sync(this, salt, iterations, keyLength, "sha1")
}
