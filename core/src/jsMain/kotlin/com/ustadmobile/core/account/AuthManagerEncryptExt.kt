package com.ustadmobile.core.account

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.util.decodeBase64Bytes
import org.kodein.di.direct
import org.kodein.di.instance

//This shouldn't be used - seems that we get ReferenceError: Buffer is not defined
internal actual suspend fun AuthManager.doublePbkdf2Hash(password: String): ByteArray {
    return doublePbkdf2HashAsBase64(password).decodeBase64Bytes()
}

internal actual suspend fun AuthManager.doublePbkdf2HashAsBase64(password: String): String {
    val httpClient: HttpClient = di.direct.instance()
    return httpClient.get("${learningSpace.url}api/pbkdf2/doubleEncryptPbkdf2") {
        parameter("secret", password)
    }.bodyAsText()
}

internal actual suspend fun AuthManager.encryptPbkdf2(password: String): ByteArray {
    val httpClient: HttpClient = di.direct.instance()
    return httpClient.get("${learningSpace.url}api/pbkdf2/encryptPbkdf2") {
        parameter("secret", password)
    }.bodyAsText().decodeBase64Bytes()
}
