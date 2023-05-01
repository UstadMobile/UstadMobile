package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.door.ext.toHexString
import io.ktor.client.*

suspend fun String.encryptWithPbkdf2(
    salt: String,
    pbkdf2Params: Pbkdf2Params,
    endpoint: Endpoint,
    httpClient: HttpClient,
): ByteArray =
    encryptWithPbkdf2(salt, pbkdf2Params.iterations, pbkdf2Params.keyLength, endpoint,
        httpClient)

/**
 * Two rounds of PBKDF encryption:
 * Round1: encyrpt the original password to a ByteArray
 * Round2: convert round1 byte array to a hex string, then encrypt that.
 */
suspend fun String.doublePbkdf2Hash(
    salt: String,
    pbkdf2Params: Pbkdf2Params,
    endpoint: Endpoint,
    httpClient: HttpClient,
): ByteArray {
    val round1 = encryptWithPbkdf2(salt, pbkdf2Params, endpoint, httpClient)
    return round1.toHexString().encryptWithPbkdf2(salt, pbkdf2Params, endpoint, httpClient)
}
