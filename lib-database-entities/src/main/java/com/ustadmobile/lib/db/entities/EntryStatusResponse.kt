package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndex
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

/**
 * Represents the response from a network node to whether or not a given entry is available locally
 */
@UmEntity(indices = [UmIndex(name = "containerUid_nodeId_unique", unique = true, value = ["erContainerUid", "erNodeId"])])
open class EntryStatusResponse {

    @UmPrimaryKey(autoIncrement = true)
    var erId: Int = 0

    var erContainerUid: Long = 0

    var responseTime: Long = 0

    var erNodeId: Long = 0

    var isAvailable: Boolean = false

    constructor(erContainerUid: Long, responseTime: Long, erNodeId: Long,
                available: Boolean) {
        this.erContainerUid = erContainerUid
        this.responseTime = responseTime
        this.erNodeId = erNodeId
        this.isAvailable = available
    }


    constructor()
}
