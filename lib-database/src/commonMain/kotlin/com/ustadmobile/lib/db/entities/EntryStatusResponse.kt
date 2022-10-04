package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

/**
 * Represents the response from a network node to whether or not a given entry is available locally
 */
@Serializable
open class EntryStatusResponse(var erContainerUid: Long = 0L, var available: Boolean = false) {

    var erId: Int = 0

    var responseTime: Long = 0

    var erNodeId: Long = 0

}
