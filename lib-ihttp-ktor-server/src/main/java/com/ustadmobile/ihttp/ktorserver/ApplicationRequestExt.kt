package com.ustadmobile.ihttp.ktorserver

import com.ustadmobile.ihttp.ext.clientProtocolAndHost
import com.ustadmobile.ihttp.headers.asIHttpHeaders
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.uri
import java.net.URL


/**
 * Returns the url of the request as the client sent it. This will use the Forwarded header when
 * available (e.g. when Ktor is running behind a reverse proxy such as Nginx or Apache).
 */
fun ApplicationRequest.clientUrl() : String {
    val iHeaders = this.headers.asIHttpHeaders()
    return URL(URL(iHeaders.clientProtocolAndHost()), uri).toString()
}
