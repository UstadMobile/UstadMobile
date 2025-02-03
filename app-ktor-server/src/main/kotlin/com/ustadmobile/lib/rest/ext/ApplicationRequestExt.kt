package com.ustadmobile.lib.rest.ext

import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.header
import io.ktor.util.decodeBase64String

data class BasicAuth(
    val username: String,
    val password: String,
)

fun ApplicationRequest.requireBasicAuth(): BasicAuth {
    val authorization = header("Authorization")
    if(authorization == null || !authorization.lowercase().startsWith("basic"))
        throw IllegalArgumentException("No basic auth present")

    val base64String = authorization.substring("basic".length).trim()
    val decoded = base64String.decodeBase64String()
    val (username, password) = decoded.split(":", limit = 2)

    return BasicAuth(username, password)
}
