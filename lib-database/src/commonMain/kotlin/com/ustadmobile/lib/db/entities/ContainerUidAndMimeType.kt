package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
open class ContainerUidAndMimeType(){

    var containerUid: Long = 0

    var mimeType: String ? = null
}