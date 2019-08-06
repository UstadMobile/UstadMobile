package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*

@UmEntity(tableId = 43)
@Entity
open class PersonGroup() {

    @PrimaryKey(autoGenerate = true)
    var groupUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var groupMasterCsn: Long = 0

    @UmSyncLocalChangeSeqNum
    var groupLocalCsn: Long = 0

    @UmSyncLastChangedBy
    var groupLastChangedBy: Int = 0

    var groupName: String? = null

    /**
     * If this was created as a group for one person, this is the uid for that Person object.
     * Single member groups are used to avoid queries having to look things up from another table.
     *
     * @return person UID if this group is created for one user only, otherwise 0
     */
    var groupPersonUid: Long = 0
}
