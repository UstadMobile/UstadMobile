package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

import com.ustadmobile.lib.db.entities.Location.Companion.TABLE_ID

@UmEntity(tableId = TABLE_ID)
@Entity
class Location {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey
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

    constructor()

    constructor(title: String, description: String) {
        this.title = title
        this.description = description
    }

    companion object {

       const val TABLE_ID = 29
    }
}
