package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.directivesToMap
import com.ustadmobile.core.util.stringvalues.IStringValues

/**
 * Gets the protocol that the client used, using headers to adjust as needed when a reverse proxy
 * was used.
 *
 * Uses the Forwarded Header directive first, then
 * X-Forwarded-Proto, and finally, origin.scheme.
 *
 * @receiver IStringValues representing request headers as received by the server
 */
fun IStringValues.clientProtocol(): String? {
    return get("Forwarded")?.let {
        directivesToMap(it)
    }?.getCaseInsensitiveOrNull("proto") ?: get("X-Forwarded-Proto")
}

/**
 * Gets the host that the client used, using headers to adjust as needed when a reverse proxy was
 * used. e.g. servername.com (if using default port for protocol) or servername.com:8087
 *
 * Uses the Forwarded header directive first, then X-Forwarded-Host, then the Host header
 *
 * @receiver IStringValues representing request headers as received by the server
 */
fun IStringValues.clientHost(): String? {
    return get("Forwarded")?.let {
        directivesToMap(it)
    }?.getCaseInsensitiveOrNull("host") ?: get("X-Forwarded-Host") ?: get("Host")
}

/**
 * Get the protocol and host being used e.g. http://ip.addr:8087/ https://server.tld/ etc. This
 * will use the Forwarded header when available.
 */
fun IStringValues.clientProtocolAndHost(
    defaultProtocol: String = "http"
): String {
    return "${clientProtocol() ?: defaultProtocol}://${clientHost()}".requirePostfix("/")
}

fun IStringValues.clientEndpoint(
    defaultProtocol: String = "http"
): Endpoint {
    return Endpoint(clientProtocolAndHost(defaultProtocol = defaultProtocol))
}

