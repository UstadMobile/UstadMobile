package com.ustadmobile.core.domain.contententry.server

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.ContentManifestEntry
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.domain.contententry.ContentManifestMap
import com.ustadmobile.core.io.ext.bodyAsDecodedByteStream
import com.ustadmobile.core.util.ext.removeHashSuffix
import com.ustadmobile.core.util.ext.removeQueryStringSuffix
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.core.util.stringvalues.asOkHttpHeaders
import com.ustadmobile.core.util.stringvalues.filtered
import com.ustadmobile.core.util.stringvalues.withOverrides
import com.ustadmobile.ihttp.okhttp.headers.asOkHttpHeaders
import com.ustadmobile.ihttp.okhttp.request.asOkHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequest
import io.github.aakira.napier.Napier
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.CacheControl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * ContentEntryVersionServerUseCase is used to serve content as per the ContentManifest.
 *
 *  Server: use by ContentEntryRoute, with onlyIfCache set to true
 *  Android: WebViewClient: used to intercept all requests for the content WebView. The OKHttp
 *           Interceptor will get data from cache where it is already available, otherwise will
 *           fetch from network and store.
 *  Server: Use with Embedded HTTPD server and/or proxy
 *
 * @param onlyIfCached if true, then on requests via OKHttp will be set to only-if-cached. This MUST
 *        be used on the server (otherwise the result could be an infinite loop). On the client
 *        (Desktop, Android) then this should be false, so that any response that is not currently
 *        available in the cache will be fetched over the network.
 */
class ContentEntryVersionServerUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    private val onlyIfCached: Boolean,
) {

    private val manifestCache = Cache.Builder<Long, ContentManifestMap>()
        .maximumCacheSize(100)
        .build()

    private fun Request.Builder.applyCacheControl(): Request.Builder {
        return if(onlyIfCached)
            cacheControl(CacheControl.FORCE_CACHE)
        else
            this
    }

    suspend fun getManifestEntry(
        contentEntryVersionUid: Long,
        pathInContentEntryVersion: String,
    ) : ContentManifestEntry? = withContext(Dispatchers.IO) {
        val manifest = manifestCache.get(contentEntryVersionUid) {
            val contentEntryVersionEntity = (repo ?: db).contentEntryVersionDao()
                .findByUidAsync(contentEntryVersionUid)
                ?: throw IllegalArgumentException("No such ContentEntryVersion : $contentEntryVersionUid")
            val contentManifestUrl = contentEntryVersionEntity.cevManifestUrl!!

            val manifestStr = okHttpClient.newCall(
                Request.Builder()
                    .applyCacheControl()
                    .header("accept-encoding", "gzip")
                    .url(contentManifestUrl)
                    .build()
            ).execute().bodyAsDecodedByteStream()?.reader()?.use { it.readText() }
                ?: throw IllegalArgumentException("No manifest body found")

            val manifest = json.decodeFromString(
                ContentManifest.serializer(), manifestStr
            )
            ContentManifestMap(manifest)
        }

        manifest[pathInContentEntryVersion]
    }

    /**
     * @param pathInContentEntryVersion the path as per ContentManifestEntry. This MUST be decoded
     *        (which is done by both NanoHTTPD and KTOR)
     */
    operator fun invoke(
        request: IHttpRequest,
        contentEntryVersionUid: Long,
        pathInContentEntryVersion: String,
    ) : Response {
        fun logResponse(response: Response) {
            Napier.v {
                "ContentEntryVersionServerUseCase: ${request.method} contentEntryVersion=$contentEntryVersionUid " +
                        "pathInContent=$pathInContentEntryVersion : ${response.code} ${response.message} "
            }
        }

        //if request is for the manifest, then directly forward the request, otherwise, use the
        //memory cache to get the manifest,
        if(request.url == "about:blank") {
            return Response.Builder()
                .header("content-type", "text/plain")
                .request(request.asOkHttpRequest())
                .protocol(Protocol.HTTP_1_1)
                .message("OK")
                .code(200)
                .build().also { logResponse(it) }
        }else if(request.url.endsWith(ContentConstants.MANIFEST_NAME)) {
            return okHttpClient.newCall(
                Request.Builder()
                    .url(request.url)
                    .headers(request.headers.asOkHttpHeaders())
                    .applyCacheControl()
                    .build()
            ).execute()
        }else {
            val entry = runBlocking {
                getManifestEntry(contentEntryVersionUid, pathInContentEntryVersion)
            } ?: return Response.Builder()
                    .header("content-type", "text/html")
                    .request(request.asOkHttpRequest())
                    .protocol(Protocol.HTTP_1_1)
                    .body("Not found in version $contentEntryVersionUid: $pathInContentEntryVersion"
                        .toResponseBody("text/plain".toMediaType()))
                    .message("NOT FOUND")
                    .code(404)
                    .build().also { logResponse(it) }

            val bodyDataUrlRequest = Request.Builder()
                .url(entry.bodyDataUrl)
                .headers(request.headers.asOkHttpHeaders())
                .applyCacheControl()
                .build()
            val bodyDataUrlResponse = okHttpClient.newCall(bodyDataUrlRequest).execute()

            Napier.v {
                "ContentEntryVersionServerUseCase: Load $pathInContentEntryVersion from body url: " +
                        "${entry.bodyDataUrl} "
            }

            val entryHeaders = entry.responseHeaders

            /*
             * Return a response where we get the body data from the http request for bodyDataUrl as
             * specified on the ContentManifestEntry. Most headers are taken directly from the
             * ContentManifestEntry (e.g. they may be different to those provided by the server
             * providing the response for bodyDataUrl e.g. EPUB mime types are specified in the OPF
             * manifest).
             *
             * The Content-Length and Content-Range header are taken from the data url response
             *
             * Note: closing an OKHttp Response is actually closing the body, because we are
             * returning the body from the bodyDataUrlResponse, bodyDataUrlResponse itself does not
             * need to be closed.
             */
            return Response.Builder()
                .request(request.asOkHttpRequest())
                .protocol(Protocol.HTTP_1_1)
                .message(bodyDataUrlResponse.message)
                .body(bodyDataUrlResponse.body)
                .code(bodyDataUrlResponse.code)
                .headers(
                    entryHeaders.filtered { headerName ->
                        !BODY_DATA_URL_RESERVED_HEADER_NAMES.any { it.equals(headerName, true) }
                    }.withOverrides(
                        buildMap {
                            BODY_DATA_URL_RESERVED_HEADER_NAMES.forEach { reservedHeaderName ->
                                bodyDataUrlResponse.headers[reservedHeaderName]?.also { reserverHeaderVal ->
                                    put(reservedHeaderName, listOf(reserverHeaderVal))
                                }
                            }
                            put("Accept-Ranges", listOf("bytes"))
                        }.asIStringValues()
                    ).asOkHttpHeaders()
                )
                .build()
                .also { logResponse(it) }
        }
    }

    companion object {

        /**
         * In order to support content ranges (required for embedded audio/video), we must use the
         * content-range and content-length specified on the bodyDataUrl response.
         *
         * Content-encoding will be as set by the bodyDataUrl. See lib-cache
         * CacheStorageCompressionFilter kdoc.
         */
        val BODY_DATA_URL_RESERVED_HEADER_NAMES = listOf(
            "content-length", "content-range", "content-encoding"
        )

    }

}