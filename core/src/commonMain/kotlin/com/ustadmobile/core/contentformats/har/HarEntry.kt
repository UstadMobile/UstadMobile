package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable


@Serializable
class HarEntry {

    var request: HarRequest? = null
    var response: HarResponse? = null

}
