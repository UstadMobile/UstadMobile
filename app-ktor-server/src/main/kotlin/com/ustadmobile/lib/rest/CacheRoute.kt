package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.ext.respondCacheResponse
import com.ustadmobile.lib.rest.ext.clientUrl
import com.ustadmobile.lib.rest.util.toCacheHttpRequest
import com.ustadmobile.libcache.UstadCache
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
            val cacheRequest = call.request.toCacheHttpRequest()
            val cacheResponse = cache.retrieve(cacheRequest)
            call.respondCacheResponse(cacheResponse, cacheRequest = cacheRequest)
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
