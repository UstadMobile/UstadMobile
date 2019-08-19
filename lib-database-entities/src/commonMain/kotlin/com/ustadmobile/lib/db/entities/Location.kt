package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Location.Companion.TABLE_ID

@Entity
@SyncableEntity(tableId = TABLE_ID )
class Location() {

    @PrimaryKey(autoGenerate = true)
    var locationUid: Long = 0

    var title: String? = null

    var description: String? = null

    var lng: String? = null

    var lat: String? = null

    var parentLocationUid: Long = 0

    var locationActive: Boolean = true

    @LocalChangeSeqNum
    var locationLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var locationMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var locationLastChangedBy: Int = 0

    constructor(title: String, description: String) : this() {
        this.title = title
        this.description = description
    }

    constructor(title: String, description: String, active: Boolean) : this() {
        this.title = title
        this.description = description
        this.locationActive = active
    }

    constructor(title: String, description: String, active: Boolean, parentUid: Long) : this() {
        this.title = title
        this.description = description
        this.locationActive = active
        this.parentLocationUid = parentUid
    }


    companion object {

        const val TABLE_ID = 29
    }
}
