package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@Serializable
@SyncableEntity(tableId = 189)
open class WorkSpace {

    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0

    @MasterChangeSeqNum
    var wsPcsn: Long = 0

    @LocalChangeSeqNum
    var wsLcsn: Long = 0

    @LastChangedBy
    var wsLcb: Int = 0

    var name: String? = null

    var guestLogin: Boolean = true

    var registrationAllowed: Boolean = true
}