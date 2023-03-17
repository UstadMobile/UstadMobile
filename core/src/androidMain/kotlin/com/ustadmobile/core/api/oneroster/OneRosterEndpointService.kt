package com.ustadmobile.core.api.oneroster

import com.ustadmobile.core.api.DoorJsonRequest
import com.ustadmobile.core.api.DoorJsonResponse
import com.ustadmobile.httpoveripc.core.SimpleTextRequest
import com.ustadmobile.httpoveripc.core.SimpleTextResponse
import com.ustadmobile.httpoveripc.core.asSimpleTextRequest
import com.ustadmobile.httpoveripc.server.AbstractHttpOverIpcServer
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import rawhttp.core.RawHttp
import rawhttp.core.RawHttpRequest
import rawhttp.core.RawHttpResponse
import rawhttp.core.body.StringBody

class OneRosterEndpointService: AbstractHttpOverIpcServer() {

    private val rawHttp = RawHttp()

    private fun SimpleTextRequest.toDoorRequest(): DoorJsonRequest {
        return DoorJsonRequest(
            method = DoorJsonRequest.Method.valueOf(method.name),
            url = url,
            headers =  headers,
            requestBody = requestBody
        )
    }

    private fun DoorJsonResponse.toRawResponse(rawHttp: RawHttp): RawHttpResponse<*> {
        return rawHttp.parseResponse(
            "HTTP/1.1 $statusCode ${SimpleTextResponse.STATUS_RESPONSES[statusCode] ?: ""}\n" +
            "Content-Type: $contentType\n" +
            headers.entries.joinToString(separator = "") {
                "${it.key}: ${it.value}\n"
            }
        ).let {
            if(responseBody != null){
                it.withBody(StringBody(responseBody))
            }else {
                it
            }
        }
    }

    override fun handleRequest(request: RawHttpRequest): RawHttpResponse<*> {
        val di: DI by closestDI { application }
        val oneRosterEndpoint: OneRosterEndpoint by di.instance()

        val simpleRequest = request.asSimpleTextRequest()
        val response = runBlocking {
            oneRosterEndpoint.serve(simpleRequest.toDoorRequest())
        }

        return response.toRawResponse(rawHttp)
    }
}
