package com.ustadmobile.ihttp.nanohttpd

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequestWithByteBody
import com.ustadmobile.ihttp.request.IHttpRequestWithTextBody
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Method
import java.io.File

internal class NanoHttpdRequestAdapter(
    private val session: NanoHTTPD.IHTTPSession,
    private val server: NanoHTTPD,
): IHttpRequestWithTextBody, IHttpRequestWithByteBody {

    override val headers: IHttpHeaders = IHttpHeaders.fromMap(
        session.headers.map { it.key to listOf(it.value) }.toMap()
    )

    override val url: String
        get() = "http://${server.hostname}:${server.listeningPort}${session.uri}"

    override val method: IHttpRequest.Companion.Method
        get() = IHttpRequest.Companion.Method.forName(session.method.name)

    override fun queryParam(name: String): String? {
        return session.parameters[name]?.firstOrNull()
    }

    override suspend fun bodyAsBytes(): ByteArray? {
        val bodyMap = mutableMapOf<String,String>()
        session.parseBody(bodyMap)

        return if(session.method == Method.PUT) {
            //NanoHTTPD will always put the content of a PUT body into a temp file, with the path in the "content" key
            val tmpFileName = bodyMap["content"]
            tmpFileName?.let { File(it).readBytes() }
        }else if(session.method == Method.POST) {
            //NanoHTTPD will put small (less than 1024 bytes) content into the memory, otherwise it will make a file
            val mapContent = bodyMap["postData"] ?: return null
            val tmpFile = File(mapContent)
            if(tmpFile.exists()) {
                tmpFile.readBytes()
            }else {
                return mapContent.encodeToByteArray()
            }
        }else {
            return null
        }
    }

    override suspend fun bodyAsText(): String? {
        return bodyAsBytes()?.decodeToString()
    }
}

fun NanoHTTPD.IHTTPSession.asIHttpRequest(server: NanoHTTPD): IHttpRequest {
    return NanoHttpdRequestAdapter(this, server)
}
