package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.Site
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*

/**
 * Get the Site object for the given endpoint url
 */
suspend fun HttpClient.verifySite(endpointUrl: String, timeout: Long = 30000): Site {
    val siteVerifyUrl = UMFileUtil.joinPaths(endpointUrl, "Site","verify")
    return get(siteVerifyUrl) {
        header("cache-control", "must-revalidate")
        timeout {
            requestTimeoutMillis = timeout
        }
    }.body()
}
