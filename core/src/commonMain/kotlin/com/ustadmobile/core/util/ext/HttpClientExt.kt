package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.Site
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

/**
 * Get the Site object for the given endpoint url
 */
suspend fun HttpClient.verifySite(
    endpointUrl: String,
    timeout: Long = 30000,
    json: Json
): Site {
    val siteVerifyUrl = UMFileUtil.joinPaths(endpointUrl, "Site","verify")
    val responseStr = get(siteVerifyUrl) {
        //This actually means must be validated as per https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control
        header("cache-control", "no-cache")
        timeout {
            requestTimeoutMillis = timeout
        }
    }.bodyAsText()

    return  json.decodeFromString(Site.serializer(), responseStr)
}
