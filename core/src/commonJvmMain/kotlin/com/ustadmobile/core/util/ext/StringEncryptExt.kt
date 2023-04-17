package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import io.ktor.client.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

actual suspend fun String.encryptWithPbkdf2(
    salt: String,
    iterations: Int,
    keyLength: Int,
    endpoint: Endpoint,
    httpClient: HttpClient,
): ByteArray {
    val keySpec = PBEKeySpec(this.toCharArray(), salt.toByteArray(),
        iterations, keyLength)

    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return keyFactory.generateSecret(keySpec).encoded
}
