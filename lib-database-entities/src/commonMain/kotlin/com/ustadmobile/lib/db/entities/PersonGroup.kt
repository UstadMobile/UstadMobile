package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity

@Entity
@SyncableEntity(tableId = 43)
class PersonGroup() {

    @PrimaryKey(autoGenerate = true)
    var groupUid: Long = 0

    @MasterChangeSeqNum
    var groupMasterCsn: Long = 0

    @LocalChangeSeqNum
    var groupLocalCsn: Long = 0

    @LastChangedBy
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
