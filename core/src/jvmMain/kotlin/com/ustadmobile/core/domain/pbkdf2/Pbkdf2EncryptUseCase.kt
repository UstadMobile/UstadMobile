package com.ustadmobile.core.domain.pbkdf2

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class Pbkdf2EncryptUseCase {

    operator fun invoke(
        password: String,
        salt: String,
        iterations: Int = 10_000,
        keyLength: Int = 512,
    ): ByteArray {
        val keySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), iterations, keyLength)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return keyFactory.generateSecret(keySpec).getEncoded()
    }

}
