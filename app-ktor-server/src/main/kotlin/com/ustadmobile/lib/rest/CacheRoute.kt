package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.ext.respondCacheResponse
import com.ustadmobile.lib.rest.ext.clientUrl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import io.github.aakira.napier.Napier
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get


fun Route.CacheRoute(
    cache: UstadCache
){
    get("{...}") {
        try {
            val url = call.request.clientUrl()
            val request = requestBuilder(url)
            val cacheResponse = cache.retrieve(request)
            call.respondCacheResponse(cacheResponse, cacheRequest = request)
        }catch (e: Throwable) {
            Napier.e("CacheRoute: Exception serving ${call.request.clientUrl()}", e)
            call.respondText(
                status = HttpStatusCode.InternalServerError,
                text = "${e.message}",
                contentType = ContentType.Text.Plain,
            )
        }
    }
}
