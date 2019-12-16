package com.ustadmobile.core.contentformats.har

class HarResponse {

    var status: Int? = null

    var statusText: String? = null

    var headers: List<HarNameValuePair> = listOf()

    var content: HarContent? = null

    var headersSize: Long? = null

    var bodySize: Long? = null
}
