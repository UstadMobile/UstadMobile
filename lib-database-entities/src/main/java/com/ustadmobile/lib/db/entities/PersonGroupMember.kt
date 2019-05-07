package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndexField
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 44)
class PersonGroupMember {

    @UmPrimaryKey(autoGenerateSyncable = true)
    var groupMemberUid: Long = 0

    @UmIndexField
    var groupMemberPersonUid: Long = 0

    @UmIndexField
    var groupMemberGroupUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var groupMemberMasterCsn: Long = 0

    @UmSyncLocalChangeSeqNum
    var groupMemberLocalCsn: Long = 0

    @UmSyncLastChangedBy
    var groupMemberLastChangedBy: Int = 0
}
