package com.ustadmobile.core.networkmanager

import com.ustadmobile.core.model.HeadResponse
import java.net.HttpURLConnection
import java.net.URL

actual class PlatformHttpClient {

    actual fun headRequest(urlString: String): HeadResponse {
        val url = URL(urlString)

        var con = url.openConnection() as HttpURLConnection
        con.instanceFollowRedirects = false
        con.requestMethod = "HEAD"

        val responseCode = con.responseCode
        val headers = con.headerFields

        con.disconnect()

        return HeadResponse(responseCode, headers)

    }


}