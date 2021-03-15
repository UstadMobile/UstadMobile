package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable
import com.ustadmobile.lib.db.entities.ContainerEntryFile

@Serializable
class HarContent {

    var size: Long? = null

    var mimeType: String? = null

    var text: String? = null

    var entryFile: ContainerEntryFile? = null

    var encoding: String? = null


}
