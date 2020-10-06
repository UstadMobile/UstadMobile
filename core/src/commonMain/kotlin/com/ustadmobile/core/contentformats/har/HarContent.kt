package com.ustadmobile.core.contentformats.har

import kotlinx.io.InputStream
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable

@Serializable
class HarContent {

    var size: Long? = null

    var mimeType: String? = null

    var text: String? = null

    @ContextualSerialization
    var data: InputStream? = null

    var encoding: String? = null


}
