package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = 43)
@Serializable
open class PersonGroup() {

    @PrimaryKey(autoGenerate = true)
    var groupUid: Long = 0

    @MasterChangeSeqNum
    var groupMasterCsn: Long = 0

    @LocalChangeSeqNum
    var groupLocalCsn: Long = 0

    @LastChangedBy
    var groupLastChangedBy: Int = 0

    var groupName: String? = null

    var groupActive : Boolean = true

    /**
     *
     */
    var personGroupFlag: Int = 0

    constructor(name: String) : this() {
        this.groupName = name
    }

    companion object{
        const val PERSONGROUP_FLAG_DEFAULT = 0
        const val PERSONGROUP_FLAG_PERSONGROUP = 1
    }
}
