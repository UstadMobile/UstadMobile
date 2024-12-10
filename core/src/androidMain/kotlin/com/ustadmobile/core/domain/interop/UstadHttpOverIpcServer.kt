package com.ustadmobile.core.domain.interop

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.domain.interop.oneroster.OneRosterHttpServerUseCase
import com.ustadmobile.core.util.ext.clientUrl
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.isimplerequest.asISimpleTextRequest
import com.ustadmobile.core.util.isimpleresponse.ISimpleTextResponse
import com.ustadmobile.core.util.rawhttp.newRawHttpStringResponse
import com.ustadmobile.httpoveripc.core.SimpleTextResponse
import com.ustadmobile.httpoveripc.server.AbstractHttpOverIpcServer
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import rawhttp.core.RawHttp
import rawhttp.core.RawHttpRequest
import rawhttp.core.RawHttpResponse
import rawhttp.core.body.StringBody

/**
 * Make HTTP APIs (e.g. OneRoster) accessible to other apps on the device without requiring Internet
 * access using IPC.
 */
class UstadHttpOverIpcServer : AbstractHttpOverIpcServer(){

    private fun ISimpleTextResponse.toRawResponse(rawHttp: RawHttp): RawHttpResponse<*> {
        val contentType = headers["content-type"] ?: "application/octet-stream"
        return rawHttp.parseResponse(
            "HTTP/1.1 $responseCode ${SimpleTextResponse.STATUS_RESPONSES[responseCode] ?: ""}\n" +
                    "Content-Type: $contentType\n" +
                    buildString {
                        headers.names().forEach { headerName ->
                            headers.getAll(headerName).forEach { headerVal ->
                                append("$headerName: $headerVal\n")
                            }
                        }
                    }
        ).let {
            if(responseBody != null)
                it.withBody(StringBody(responseBody))
            else
                it
        }
    }

    override fun handleRequest(request: RawHttpRequest): RawHttpResponse<*> {
        Napier.d { "${request.method} : ${request.uri}" }

        //If this is done in the constructor - it could (stupidly) be called before onCreate...
        val di: DI by closestDI(this)
        val accountManager: UstadAccountManager by di.instance()
        val rawHttp: RawHttp by di.instance()



        val requestUrl = request.clientUrl()
        val learningSpaceUrl = requestUrl.substringBefore("/api/").requirePostfix("/")

        //now find the endpoint for this request
        if(learningSpaceUrl !in accountManager.activeLearningSpaces.map { it.url }) {
            return rawHttp.newRawHttpStringResponse(400, "Bad request: no endpoint $learningSpaceUrl")
        }


        val apiName = requestUrl.substringAfter("/api/").substringBefore("/")

        val learningSpace = LearningSpace(learningSpaceUrl)
        val simpleTextRequest = request.asISimpleTextRequest()

        return runBlocking {
            try {
                when(apiName) {
                    "oneroster" -> {
                        val oneRosterEndpoint: OneRosterHttpServerUseCase = di.on(learningSpace).direct
                            .instance()
                        oneRosterEndpoint(simpleTextRequest).toRawResponse(rawHttp)
                    }

                    else -> {
                        rawHttp.newRawHttpStringResponse(404, "Not found")
                    }
                }
            }catch(e: Throwable){
                Napier.e("UStadHttpOverIpcServer: exception", e)
                rawHttp.newRawHttpStringResponse(500, "Internal error: ${e.message}")
            }
        }
    }
}