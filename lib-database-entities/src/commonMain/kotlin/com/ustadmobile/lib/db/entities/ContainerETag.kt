package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class ContainerETag() {

    @PrimaryKey
    var ceContainerUid: Long = 0

    var cetag: String? = null

    constructor(containerUid: Long) : this() {
        this.ceContainerUid = containerUid
    }

    constructor(containerUid: Long, eTag: String) : this() {
        this.ceContainerUid = containerUid
        this.cetag = eTag
    }

}