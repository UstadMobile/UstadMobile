package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.asmcrypto
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import js.buffer.ArrayBufferLike
import js.core.ReadonlyArray
import js.typedarrays.Uint8Array
import kotlin.js.Date
import kotlinext.js.require

//This should probably switch over to using the standard web crypto on the client - but in the
// meantime this can be sent to the server.
actual suspend fun String.encryptWithPbkdf2(
    salt: String,
    iterations: Int,
    keyLength: Int,
    endpoint: Endpoint,
    httpClient: HttpClient,
): ByteArray {
    val response = httpClient.get("${endpoint.url}api/pbkdf2/encrypt") {
        parameter("salt", salt)
        parameter("iterations", iterations.toString())
        parameter("keyLength", keyLength.toString())
        parameter("secret", this)

        retry {
            maxRetries = 3
        }
    }.bodyAsText()

    return response.decodeBase64Bytes()
}
