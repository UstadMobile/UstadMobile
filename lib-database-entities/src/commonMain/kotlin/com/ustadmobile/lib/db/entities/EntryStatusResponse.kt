package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndex
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

/**
 * Represents the response from a network node to whether or not a given entry is available locally
 */
@UmEntity(/*indices = [UmIndex(name = "containerUid_nodeId_unique", unique = true, value = ["erContainerUid", "erNodeId"])]*/)
@Entity(/*indices = [Index(name = "containerUid_nodeId_unique", unique = true, value = ["erContainerUid", "erNodeId"])]*/)
open class EntryStatusResponse {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var erId: Int = 0

    var erContainerUid: Long = 0

    var responseTime: Long = 0

    var erNodeId: Long = 0

    var available: Boolean = false

    constructor(erContainerUid: Long, responseTime: Long, erNodeId: Long,
                available: Boolean) {
        this.erContainerUid = erContainerUid
        this.responseTime = responseTime
        this.erNodeId = erNodeId
        this.available = available
    }


    constructor()
}
