package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.ext.clientHost
import com.ustadmobile.core.util.ext.clientProtocol
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.stringvalues.asIStringValues
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.uri

/**
 * Returns the url of the request as the client sent it. This will use the Forwarded header when
 * available (e.g. when Ktor is running behind a reverse proxy such as Nginx or Apache).
 */
fun ApplicationRequest.clientUrl() : String {
    return UrlKmp(clientProtocolAndHost()).resolve(uri).toString()
}

/**
 * Gets the protocol that the client used, using headers to adjust as needed when a reverse proxy
 * was used.
 *
 * Uses the Forwarded Header directive first, then
 * X-Forwarded-Proto, and finally, origin.scheme.
 */
fun ApplicationRequest.clientProtocol(): String {
    return headers.asIStringValues().clientProtocol() ?: origin.scheme
}

/**
 * Gets the host that the client used, using headers to adjust as needed when a reverse proxy was
 * used. e.g. servername.com (if using default port for protocol) or servername.com:8087
 *
 * Uses the Forwarded header directive first, then X-Forwarded-Host, then the Host header, finally,
 * origin.serverHost
 */
fun ApplicationRequest.clientHost(): String {
    return headers.asIStringValues().clientHost() ?: origin.serverHost
}

/**
 * Get the protocol and host being used e.g. http://ip.addr:8087/ https://server.tld/ etc. This
 * will use the Forwarded header when available.
 */
fun ApplicationRequest.clientProtocolAndHost(): String {
    return "${clientProtocol()}://${clientHost()}".requirePostfix("/")
}
