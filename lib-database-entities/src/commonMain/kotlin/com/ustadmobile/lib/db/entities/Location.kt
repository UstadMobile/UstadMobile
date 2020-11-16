package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Location.Companion.CATEGORY_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CATEGORY_TABLE_ID)
@Serializable
open class Location() {

    @PrimaryKey(autoGenerate = true)
    var locationUid: Long = 0

    var title: String? = null

    var description: String? = null

    var lng: String? = null

    var lat: String? = null

    var parentLocationUid: Long = 0


    @LocalChangeSeqNum
    var locationLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var locationMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var locationLastChangedBy: Int = 0

    var timeZone: String? = null

    var locationActive: Boolean = true




    companion object{
        const val CATEGORY_TABLE_ID = 318
    }


}