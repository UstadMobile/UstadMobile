package com.ustadmobile.core.util.ext

import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.stringvalues.asIStringValues
import rawhttp.core.RawHttpRequest

/**
 * Returns the url of the request as the client sent it. This will use the Forwarded header when
 * available
 */
fun RawHttpRequest.clientUrl(): String {
    return UrlKmp(clientProtocolAndHost()).resolve(uri.rawPath).toString()
}

fun RawHttpRequest.clientProtocol(): String {
    return headers.asIStringValues().clientProtocol() ?: "http"
}

/**
 * Gets the host that the client used, using headers to adjust as needed when a reverse proxy was
 * used. e.g. servername.com (if using default port for protocol) or servername.com:8087
 *
 * Uses the Forwarded header directive first, then X-Forwarded-Host, then the Host header, finally,
 * origin.serverHost
 */
fun RawHttpRequest.clientHost(): String {
    return headers.asIStringValues().clientHost() ?: "localhost"
}

/**
 * Get the protocol and host being used e.g. http://ip.addr:8087/ https://server.tld/ etc. This
 * will use the Forwarded header when available.
 */
fun RawHttpRequest.clientProtocolAndHost(): String {
    return "${clientProtocol()}://${clientHost()}".requirePostfix("/")
}

