package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents the response from a network node to whether or not a given entry is available locally
 */
@Entity(indices = arrayOf(Index(name = "containerUid_nodeId_unique", unique = true, value = ["erContainerUid", "erNodeId"])))
open class EntryStatusResponse() {

    @PrimaryKey(autoGenerate = true)
    var erId: Int = 0

    var erContainerUid: Long = 0

    var responseTime: Long = 0

    var erNodeId: Long = 0

    var available: Boolean = false

    constructor(erContainerUid: Long, responseTime: Long, erNodeId: Long,
                available: Boolean) : this() {
        this.erContainerUid = erContainerUid
        this.responseTime = responseTime
        this.erNodeId = erNodeId
        this.available = available
    }
}
