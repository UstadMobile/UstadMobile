package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity

@SyncableEntity(tableId = 57)
@Entity
class CustomFieldValue {

    @PrimaryKey(autoGenerate = true)
    var customFieldValueUid: Long = 0

    //custom field uid
    var customFieldValueFieldUid: Long = 0

    //Entity uid (eg clazz uid / person uid)
    var customFieldValueEntityUid: Long = 0

    //value as String
    var customFieldValueValue: String? = null

    @MasterChangeSeqNum
    var customFieldValueMCSN: Long = 0

    @LocalChangeSeqNum
    var customFieldValueLCSN: Long = 0

    @LastChangedBy
    var customFieldValueLCB: Int = 0
}
