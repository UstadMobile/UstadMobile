package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContainerEntryFileUidAndPath {

    var cefUid: Long  = 0L

    var cefPath: String? = null

}