package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndexField
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 47)
@Entity
class EntityRole {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey
    var erUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var erMasterCsn: Long = 0

    @UmSyncLocalChangeSeqNum
    var erLocalCsn: Long = 0

    @UmSyncLastChangedBy
    var erLastChangedBy: Int = 0

    @UmIndexField
    var erTableId: Int = 0

    @UmIndexField
    var erEntityUid: Long = 0

    @UmIndexField
    var erGroupUid: Long = 0

    @UmIndexField
    var erRoleUid: Long = 0

    constructor()

    constructor(erTableId: Int, erEntityUid: Long, erGroupUid: Long, erRoleUid: Long) {
        this.erTableId = erTableId
        this.erEntityUid = erEntityUid
        this.erGroupUid = erGroupUid
        this.erRoleUid = erRoleUid
    }

}
