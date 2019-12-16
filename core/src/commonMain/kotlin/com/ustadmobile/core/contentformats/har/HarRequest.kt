package com.ustadmobile.core.contentformats.har

class HarRequest {

    var method: String? = null

    var url: String? = null

    var headers: List<HarNameValuePair> = listOf()

    var queryString: List<HarNameValuePair> = listOf()

    var headersSize: Long? = null

    var bodySize: Long? = null

}
