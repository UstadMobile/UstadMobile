package com.ustadmobile.core.util.ext

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

actual fun String.encryptWithPbkdf2(salt: String, iterations: Int, keyLength: Int): ByteArray {
    //For this to work on both platform  (i.e produce the same output on JVM and JS),
    // keyLength has to be multiplied by 8
    val keySpec = PBEKeySpec(this.toCharArray(), salt.toByteArray(),
        iterations, keyLength * 8)

    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return keyFactory.generateSecret(keySpec).encoded
}