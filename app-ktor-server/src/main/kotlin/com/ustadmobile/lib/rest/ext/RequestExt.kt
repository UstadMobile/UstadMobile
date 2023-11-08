package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.ext.requirePostfix
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.uri

/**
 * Returns the url of the request as the client sent it. This will use the Forwarded header when
 * available (e.g. when Ktor is running behind a reverse proxy such as Nginx or Apache).
 */
fun ApplicationRequest.url() : String {
    return UrlKmp(protocolAndHost()).resolve(uri).toString()
}

/**
 * Get the protocol and host being used e.g. http://ip.addr:8087/ https://server.tld/ etc. This
 * will use the Forwarded header when available.
 */
fun ApplicationRequest.protocolAndHost(): String {
    val forwardedHeader = headers.getAll("Forwarded")?.firstOrNull()

    //First check the standard forwarded header
    if(forwardedHeader != null) {
        val directives = forwardedHeader.split(";").mapNotNull { directives ->
            directives.split("=", limit = 2).let {
                val dirName = it[0].trim()
                val dirValue = it.getOrNull(1)?.trim()
                dirValue?.let { dirName to dirValue }
            }
        }.toMap()
        val forwardedHost = directives["host"]
        val forwardedProto = directives["proto"]

        if(forwardedHost != null && forwardedProto != null) {
            return "$forwardedProto://$forwardedHost".requirePostfix("/")
        }
    }

    //Check the defacto standard X-Forwarded headers
    val xForwardedHost = headers["X-Forwarded-Host"]
    val xForwardedProtocol = headers["X-Forwarded-Proto"]
    if(xForwardedHost != null && xForwardedProtocol != null) {
        return "$xForwardedProtocol://$xForwardedHost".requirePostfix("/")
    }

    val host = headers["Host"] ?: origin.serverHost
    return "${origin.scheme}://${host}/"
}