package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.Location.Companion.TABLE_ID

@UmEntity(tableId = TABLE_ID)
@Entity
class Location() {

    @PrimaryKey(autoGenerate = true)
    var locationUid: Long = 0

    var title: String? = null

    var description: String? = null

    var lng: String? = null

    var lat: String? = null

    var parentLocationUid: Long = 0

    var locationActive: Boolean = false

    @UmSyncLocalChangeSeqNum
    var locationLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var locationMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var locationLastChangedBy: Int = 0

    constructor(title: String, description: String) : this() {
        this.title = title
        this.description = description
    }

    companion object {

       const val TABLE_ID = 29
    }
}
