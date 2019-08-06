package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.Location.Companion.TABLE_ID

@UmEntity(tableId = TABLE_ID)
@Entity
open class Location() {

    @PrimaryKey(autoGenerate = true)
    var locationUid: Long = 0

    var title: String? = null

    var description: String? = null

    var lng: String? = null

    var lat: String? = null

    var parentLocationUid: Long = 0

    @UmSyncLocalChangeSeqNum
    var locationLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var locationMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var locationLastChangedBy: Int = 0

    private var timeZone: String? = null

    private var locationActive: Boolean = false

    constructor(title: String, description: String) : this() {
        this.title = title
        this.description = description
        this.locationActive = true
    }

    companion object {

       const val TABLE_ID = 29
    }
}
