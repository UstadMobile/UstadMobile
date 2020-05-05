package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable

@Serializable
class HarRequest {

    var method: String? = null

    var url: String? = null

    var regexedUrl: String? = null

    var headers: List<HarNameValuePair> = listOf()

    var queryString: List<HarNameValuePair> = listOf()

    var headersSize: Long? = null

    var bodySize: Long? = null

    var body: String? = null

}
