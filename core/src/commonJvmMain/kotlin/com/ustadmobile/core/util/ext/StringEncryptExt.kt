package com.ustadmobile.core.util.ext

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

actual fun String.encryptWithPbkdf2(salt: String, iterations: Int, keyLength: Int): ByteArray {
    val keySpec = PBEKeySpec(this.toCharArray(), salt.toByteArray(),
        iterations, keyLength)

    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return keyFactory.generateSecret(keySpec).encoded
}