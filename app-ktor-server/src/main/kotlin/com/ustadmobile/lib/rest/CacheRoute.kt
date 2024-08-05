package com.ustadmobile.lib.rest

import com.ustadmobile.ihttp.ktorserver.clientUrl
import com.ustadmobile.ihttp.ktorserver.respondIHttpResponse
import com.ustadmobile.ihttp.ktorserver.toIHttpRequest
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
            val cacheRequest = call.request.toIHttpRequest()
            val cacheResponse = cache.retrieve(cacheRequest)
            call.respondIHttpResponse(cacheResponse, iRequest = cacheRequest)
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
