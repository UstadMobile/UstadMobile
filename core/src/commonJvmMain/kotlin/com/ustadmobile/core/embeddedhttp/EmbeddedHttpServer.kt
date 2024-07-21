package com.ustadmobile.core.embeddedhttp

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.libcache.headers.MimeTypeHelper
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.iRequestBuilder
import fi.iki.elonen.NanoHTTPD
import io.github.aakira.napier.Napier
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import java.io.File

/**
 * Embedded server used on Android and JVM/Desktop
 */
class EmbeddedHttpServer(
    port: Int,
    private val contentEntryVersionServerUseCase: (Endpoint) -> ContentEntryVersionServerUseCase,
    private val staticUmAppFilesDir: File?,
    private val mimeTypeHelper: MimeTypeHelper,
) : NanoHTTPD(port) {

    fun List<String>.joinPathSegments(
        start: Int,
        end: Int = this.size
    ): String{
        return subList(start, end).joinToString(separator = "/")
    }

    /**
     *
     */
    fun endpointUrl(
        endpoint: Endpoint,
        path: String,
    ): String {
        //Endpoint must be double encoded - see note on serveendpoint
        val endpointEncoded = UrlEncoderUtil.encode(UrlEncoderUtil.encode(endpoint.url))
        return "http://127.0.0.1:$listeningPort$PATH_ENDPOINT_API$endpointEncoded/${path.removePrefix("/")}"
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
                        val request = iRequestBuilder(originalUrl) {
                            session.headers.forEach {
                                header(it.key, it.value)
                                method = IHttpRequest.Companion.Method.valueOf(session.method.name)
                            }

                            if(!session.headers.any { it.key.equals("accept-encoding") }) {
                                header("accept-encoding", "gzip")
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
                            pathInContentEntryVersion = pathInContent,
                        )

                        okHttpResponse.toHttpdResponse()
                    }

                    else -> newNotFoundResponse(session)
                }
            }
            /*
             * Serve the Kotlin/JS version of the app. Used to display epubs. See
             * LaunchEpubUseCaseJvm.
             */
            "umapp" -> {
                if(staticUmAppFilesDir == null)
                    return newNotFoundResponse("Static umapp files not enabled")

                val responseFile = File(staticUmAppFilesDir, pathSegments.joinPathSegments(3)).let {
                    if(pathSegments.last().isEmpty()) {
                        File(it, "index.html")
                    }else {
                        it
                    }
                }

                return responseFile.toHttpdResponse(
                    session = session,
                    contentType = mimeTypeHelper.guessByExtension(responseFile.extension) ?: "application/octet-stream"
                )
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