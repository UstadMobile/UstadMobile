package com.ustadmobile.core.domain.interop.oneroster

import com.ustadmobile.httpoveripc.server.AbstractHttpOverIpcServer
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import rawhttp.core.RawHttp
import rawhttp.core.RawHttpRequest
import rawhttp.core.RawHttpResponse
import rawhttp.core.body.StringBody

class OneRosterHttpEndpointIpcService : AbstractHttpOverIpcServer(){

    private val di: DI by closestDI { application }

    private val rawHttp = RawHttp()

    override fun handleRequest(request: RawHttpRequest): RawHttpResponse<*> {
        return rawHttp.parseResponse("HTTP/1.1 404 NOT FOUND\n" +
                "Content-Type: text/plain").withBody(StringBody("Not found"))
    }
}