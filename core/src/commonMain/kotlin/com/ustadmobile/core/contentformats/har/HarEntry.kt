package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable


@Serializable
class HarEntry {

    val request: HarRequest? = null
    val response: HarResponse? = null

}
