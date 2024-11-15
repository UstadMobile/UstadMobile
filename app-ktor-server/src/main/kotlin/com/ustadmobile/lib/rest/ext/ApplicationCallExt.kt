package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.ihttp.ext.clientProtocolAndHost
import com.ustadmobile.ihttp.headers.asIHttpHeaders
import com.ustadmobile.ihttp.ktorserver.clientUrl
import com.ustadmobile.lib.rest.CONF_DBMODE_SINGLETON
import com.ustadmobile.lib.rest.CONF_DBMODE_VIRTUALHOST
import com.ustadmobile.lib.rest.CONF_KEY_SITE_URL
import io.github.aakira.napier.Napier
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.HttpMethod
import okio.BufferedSink
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import java.io.ByteArrayInputStream

/**
 * Given a proxy to base url, resolve the url for the current call.
 *
 * e.g.
 * proxyToBaseUrl = http://localhost:8080/
 * call.request.uri = /some/file
 *
 * result = http://localhost:8080/some/file
 */
fun ApplicationCall.resolveProxyToUrl(proxyToBaseUrl: String): String {
    return proxyToBaseUrl.removeSuffix("/") + request.uri +
            request.queryParameters.toQueryParamString()
}


/**
 * Respond as a reverse proxy. Proxy the call to the given base URL.
 */
@Suppress("NewApi") //This is JVM, not Android, the warning is wrong
suspend fun ApplicationCall.respondReverseProxy(proxyToBaseUrl: String) {
    val requestBuilder = Request.Builder().url(resolveProxyToUrl(proxyToBaseUrl))
    val di: DI by closestDI()
    val httpClient: OkHttpClient by di.instance()

    request.headers.forEach { headerName, headerValues ->
        headerValues.forEach { headerValue ->
            requestBuilder.addHeader(headerName, headerValue)
        }
    }
    requestBuilder.removeHeader("cache-control")
    requestBuilder.addHeader("cache-control", "no-store")

    val bodyBytes = if(HttpMethod.requiresRequestBody(request.httpMethod.value)) {
        withContext(Dispatchers.IO) {
            receiveStream().readAllBytes()
        }
    }else {
        null
    }

    requestBuilder.method(request.httpMethod.value, bodyBytes?.let { bodyByteArr ->
        object: RequestBody() {
            override fun contentType(): MediaType? {
                return request.headers["content-type"]?.toMediaTypeOrNull()
            }

            override fun writeTo(sink: BufferedSink) {
                sink.write(bodyByteArr)
            }
        }
    })

    val originResponse = httpClient.newCall(requestBuilder.build()).execute()
    respondOkHttpResponse(originResponse)
}

/**
 * Use a response from OKHttp as the response
 */
suspend fun ApplicationCall.respondOkHttpResponse(
    response: Response
) {
    //content-type is controlled by respondOutputStream, cannot be set as a header.
    val engineHeaders = listOf("content-type", "content-length", "transfer-encoding")
    response.headers.filter { it.first.lowercase() !in engineHeaders }.forEach {
        this.response.header(it.first, it.second)
    }

    val responseInput = response.body?.byteStream() ?: ByteArrayInputStream(byteArrayOf())
    respondOutputStream(
        status = HttpStatusCode.fromValue(response.code),
        contentType = response.header("content-type")?.let { ContentType.parse(it) },
        contentLength = response.headers["content-length"]?.toLong(),
    ) {
        responseInput.use { responseIn ->
            responseIn.copyTo(this)
        }
    }
}

/**
 * Property that will resolve the endpoint for a given application call. The Endpoint.url MUST
 * be consistent with the URL that the client uses to access the server for the content entry
 * HttpCache based storage system to work.
 *
 * e.g. when a user imports a piece of content it will be stored in the HttpCache as
 * (endpoint)/api/content/versionUid/(...) This could be done by the server, or it could initially
 * be cached first on the client (and then uploaded to the server later).
 *
 * Then the ContentRoute will attempt to retrieve the item from the cache using the URL. If a
 * different URL is used (e.g. http://127.0.0.1/ instead of http://localhost/ ) then the entry will
 * not be retrieved from the cache as expected.
 *
 * If the ContentRoute cache were to retrieve content without considering the host (e.g. just use
 * the contentEntryVersion), this could lead to conflicts when there is content on different servers.
 */
val ApplicationCall.callEndpoint: Endpoint
    get() {
        val config = this.application.environment.config
        val dbMode = config.dbModeProperty()

        return if(dbMode == CONF_DBMODE_SINGLETON) {
            Endpoint(config.property(CONF_KEY_SITE_URL).getString().requirePostfix("/"))
        }else {
            Endpoint(request.headers.asIHttpHeaders().clientProtocolAndHost())
        }
    }

/**
 * Determine if the request made on the receiver ApplicationCall matches the configuration.
 */
fun ApplicationCall.urlMatchesConfig(): Boolean {
    val dbMode = application.environment.config
        .dbModeProperty()
    if(dbMode == CONF_DBMODE_VIRTUALHOST)
        return true

    val requestUrl = request.clientUrl()
    val siteUrl = application.environment.config.property(CONF_KEY_SITE_URL)
        .getString()

    return requestUrl.startsWith(siteUrl)
}

suspend fun ApplicationCall.respondRequestUrlNotMatchingSiteConfUrl() {
    val confUrl = application.environment.config.siteUrl()
    val requestUrl = request.clientUrl()
    val message = """
                <html>
                <body>
                Request url $requestUrl does not match site url ($confUrl). Please 
                access this system via the site url as set by the admin : 
                <a href="$confUrl">$confUrl</a>
                </body>
                </html>
            """.trimIndent()
    Napier.e { "Request url $requestUrl does not match $confUrl" }
    respondText(
        text = message,
        contentType = ContentType.Text.Html,
        status = HttpStatusCode.BadRequest,
    )
}


suspend fun ApplicationCall.respondContentEntryMetaDataResult(
    metadata: MetadataResult?,
    importersManager: ContentImportersManager,
) {
    if(metadata != null) {
        respond(metadata)
    }else {
        respondText(
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.NotAcceptable,
            text = importersManager.supportedFormatNames().joinToString()
        )
    }
}

/**
 * Try running the given code block. If an exception occurs, then use RespondHttpApiException
 */
suspend inline fun PipelineContext<Unit,ApplicationCall>.tryOrRespondHttpApiException(
    block:  () -> Unit
) {
    try {
        block()
    }catch(throwable: Throwable) {
        call.respondHttpApiException(throwable)
    }
}

/**
 * Generate an HTTP error response for the given throwable. If it is an HTTPApiException, then the
 * specified return error code will be set
 *
 * @param throwable the exception that occurred
 */
suspend fun ApplicationCall.respondHttpApiException(
    throwable: Throwable
) {
    val status = when(throwable) {
        is HttpApiException -> throwable.statusCode
        else -> 500
    }

    respondText(
        contentType = ContentType.Text.Plain,
        status = HttpStatusCode.fromValue(status),
        text = throwable.message ?: throwable.toString()
    )

}

