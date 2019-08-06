package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 57)
@Entity
class CustomFieldValue {

    @SyncablePrimaryKey
    @PrimaryKey(autoGenerate = true)
    var customFieldValueUid: Long = 0

    //custom field uid
    var customFieldValueFieldUid: Long = 0

    //Entity uid (eg clazz uid / person uid)
    var customFieldValueEntityUid: Long = 0

    //value as String
    var customFieldValueValue: String? = null

    @UmSyncMasterChangeSeqNum
    var customFieldValueMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var customFieldValueLCSN: Long = 0

    @UmSyncLastChangedBy
    var customFieldValueLCB: Int = 0
}
