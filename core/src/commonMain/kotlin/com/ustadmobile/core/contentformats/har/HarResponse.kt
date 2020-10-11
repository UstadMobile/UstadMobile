package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable

@Serializable
class HarResponse {

    var status: Int = 101

    var statusText: String? = null

    var headers: List<HarNameValuePair> = listOf()

    var content: HarContent? = null

    var headersSize: Long? = null

    var bodySize: Long? = null
}
