package com.ustadmobile.core.embeddedhttp

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.request.requestBuilder
import fi.iki.elonen.NanoHTTPD
import io.github.aakira.napier.Napier
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import java.io.ByteArrayInputStream
import okhttp3.Response as OkHttpResponse

class EmbeddedHttpServer(
    port: Int,
    private val contentEntryVersionServerUseCase: (Endpoint) -> ContentEntryVersionServerUseCase,
) : NanoHTTPD(port) {

    /**
     *
     */
    fun endpointUrl(
        endpoint: Endpoint,
        path: String,
    ): String {
        //Endpoint must be double encoded - see note on serveendpoint
        val endpointEncoded = UrlEncoderUtil.encode(UrlEncoderUtil.encode(endpoint.url))
        return "http://127.0.0.1:$listeningPort$PATH_ENDPOINT_API$endpointEncoded/${path.removeSuffix("/")}"
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri

        val pathSegments = uri.substring(1).split("/")
        return when {
            uri.startsWith(PATH_ENDPOINT_API) -> {
                serveApiEndpoint(session, pathSegments)
            }

            else -> {
                newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found: $uri")
            }
        }
    }

    private fun OkHttpResponse.toHttpdResponse() : Response{
        val contentLength = header("content-length")?.toLong()
        val inStream = body?.byteStream() ?: ByteArrayInputStream(ByteArray(0))
        val status = Response.Status.lookup(code)
        val contentType = header("content-type") ?: "application/octet-stream"
        val response = if(contentLength != null) {
            newFixedLengthResponse(
                status, contentType, inStream, contentLength
            )
        }else {
            newChunkedResponse(status, contentType, inStream)
        }

        headers.names().forEach { headerName ->
            response.addHeader(headerName, headers[headerName] ?: "")
        }

        return response
    }

    /**
     * When serving /e/(endpointUrl)/ - the endpoint MUST be double encoded. NanoHTTPD will
     * 'helpfully' decode it, then we won't know what slashes are part of the endpoint and which
     * are part of the api path
     */
    private fun serveApiEndpoint(
        session: IHTTPSession,
        pathSegments: List<String>,
    ): Response {
        val endpointUrl = UrlEncoderUtil.decode(pathSegments[1])
        val endpoint = Endpoint(endpointUrl)

        return when(pathSegments.getOrNull(2)) {
            "api" -> {
                when (pathSegments.getOrNull(3)) {
                    "content" -> {
                        val contentEntryVersionUid = pathSegments[4].toLong()
                        val pathInContentSegments = pathSegments.subList(5, pathSegments.size)
                        val pathInContent = pathInContentSegments.joinToString(separator = "/")

                        val originalUrl = "${endpointUrl}api/content/$contentEntryVersionUid/" +
                                pathInContentSegments.joinToString("/")
                        val request = requestBuilder(originalUrl) {
                            session.headers.forEach {
                                header(it.key, it.value)
                                method = HttpRequest.Companion.Method.valueOf(session.method.name)
                            }
                        }
                        Napier.v {
                            "EmbeddedHttpServer: content: endpoint=${endpointUrl} " +
                                "versionUid=$contentEntryVersionUid path=$pathInContent"
                        }

                        val okHttpResponse = contentEntryVersionServerUseCase(
                            endpoint
                        ).invoke(
                            request = request,
                            contentEntryVersionUid = contentEntryVersionUid,
                            pathInContentEntryVersion = pathInContent
                        )

                        okHttpResponse.toHttpdResponse()
                    }

                    else -> newNotFoundResponse(session)
                }
            }

            else -> newNotFoundResponse(session)
        }
   }



    companion object {

        fun newNotFoundResponse(session: IHTTPSession) = newNotFoundResponse("not found: ${session.uri}")

        fun newNotFoundResponse(message: String): Response {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", message)
        }

        const val PATH_ENDPOINT_API = "/e/"

    }

}