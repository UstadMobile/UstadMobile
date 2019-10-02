package com.ustadmobile.core.networkmanager

import com.ustadmobile.core.model.HeadResponse
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

actual class PlatformHttpClient {

    actual fun headRequest(urlString: String): HeadResponse {
        val url = URL(urlString)

        var con = url.openConnection() as HttpURLConnection
        con.instanceFollowRedirects = false
        con.requestMethod = "HEAD"

        val responseCode = con.responseCode
        val headers = con.headerFields.mapKeys {  if(it.key != null) it.key.toLowerCase() else "" }

        con.disconnect()

        return HeadResponse(responseCode, headers)

    }


}