package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 43)
class PersonGroup {

    @UmPrimaryKey(autoGenerateSyncable = true)
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
