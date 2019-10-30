package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents the response from a network node to whether or not a given entry is available locally
 */
@Entity(indices = arrayOf(Index(name = "containerUid_nodeId_unique", unique = true, value = ["erContainerUid", "erNodeId"])))
@Serializable
open class EntryStatusResponse(var erContainerUid: Long = 0L, var available: Boolean = false) {

    @PrimaryKey(autoGenerate = true)
    var erId: Int = 0

    var responseTime: Long = 0

    var erNodeId: Long = 0

}
