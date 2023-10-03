package com.ustadmobile.core.account

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Note : Encryption using Pbkdf2 will be done ONLY on Jvm/Android at the moment.
 */
fun String.encryptWithPbkdf2V2(
    salt: String,
    iterations: Int,
    keyLength: Int,
): ByteArray {
    val keySpec = PBEKeySpec(this.toCharArray(), salt.toByteArray(),
        iterations, keyLength)

    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return keyFactory.generateSecret(keySpec).encoded
}

@OptIn(ExperimentalStdlibApi::class)
fun String.doubleEncryptWithPbkdf2V2(
    salt: String,
    iterations: Int,
    keyLength: Int,
): ByteArray {
    val round1 = encryptWithPbkdf2V2(salt, iterations, keyLength)
    return round1.toHexString().encryptWithPbkdf2V2(salt, iterations, keyLength)
}
