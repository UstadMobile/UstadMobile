package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import io.ktor.client.*

/**
 * Encyrpt the given string with Pbkdf2 (one way) encryption. This is useful for one-way password
 * encryption.
 *
 * Javascript will send the request to the server ()Pbkdf2Route), hence the requirement for an endpoint
 * and httpClient. The endpoint and httpclient will be ignored on JVM/Android.
 */
expect suspend fun String.encryptWithPbkdf2(
    salt: String,
    iterations: Int = 10000,
    keyLength: Int = 512,
    endpoint: Endpoint,
    httpClient: HttpClient,
): ByteArray
