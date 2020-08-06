package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.port.sharedse.ext.ConcatenatedHttpResponse
import com.ustadmobile.port.sharedse.ext.generateConcatenatedFilesResponse
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.InputStream
import com.ustadmobile.port.sharedse.ext.newUnsupportedMethodResponse
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.direct
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB

class ConcatenatingFileSource(val response: ConcatenatedHttpResponse): FileResponder.IFileSource {
    override val length: Long
        get() = response.contentLength
    override val lastModifiedTime: Long
        get() = -1L
    override val inputStream: InputStream
        get() = response.dataSrc!!
    override val name: String?
        get() = response.etag
    override val exists: Boolean
        get() = true
    override val eTag: String?
        get() = response.etag
}

class ConcatenatedContainerEntryFileResponder: FileResponder(), RouterNanoHTTPD.UriResponder {

    override fun put(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?)
            = newUnsupportedMethodResponse()

    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: MutableMap<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        return serve(NanoHTTPD.Method.GET, uriResource, urlParams, session)
    }

    override fun other(method: String?, uriResource: RouterNanoHTTPD.UriResource, urlParams: MutableMap<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if("HEAD".equals(method, true)) {
            return serve(NanoHTTPD.Method.HEAD, uriResource, urlParams, session)
        }else {
            return newUnsupportedMethodResponse()
        }
    }

    override fun post(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?)
        = newUnsupportedMethodResponse()

    override fun delete(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?)
        = newUnsupportedMethodResponse()

    fun serve(method: NanoHTTPD.Method, uriResource: RouterNanoHTTPD.UriResource, urlParams: MutableMap<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val di = uriResource.initParameter(INIT_PARAM_DI_INDEX, DI::class.java)
        val endpointUrl = urlParams.get(URI_PARAM_ENDPOINT) ?: throw IllegalArgumentException("No endpoint")


        val db : UmAppDatabase = di.on(Endpoint(endpointUrl)).direct.instance(tag = TAG_DB)

        val entryFileUidsStr = session.uri.substringAfterLast("/")
        val concatenatedResponseInfo = db.containerEntryFileDao
                .generateConcatenatedFilesResponse(entryFileUidsStr)

        return newResponseFromFile(method, uriResource, session,
                ConcatenatingFileSource(concatenatedResponseInfo))
    }

    companion object {

        val INIT_PARAM_DI_INDEX = 0

        const val URI_PARAM_ENDPOINT = "endpoint"

    }
}