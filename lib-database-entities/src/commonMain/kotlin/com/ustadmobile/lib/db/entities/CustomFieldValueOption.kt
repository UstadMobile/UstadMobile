package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 55)
@Entity
class CustomFieldValueOption {

    @SyncablePrimaryKey
    @PrimaryKey(autoGenerate = true)
    var customFieldValueOptionUid: Long = 0

    //name of the option
    var customFieldValueOptionName: String? = null

    //custom field uid
    var customFieldValueOptionFieldUid: Long = 0

    //icon string
    var customFieldValueOptionIcon: String? = null

    //title string (message id)
    var customFieldValueOptionMessageId: Int = 0

    //active
    var isCustomFieldValueOptionActive: Boolean = false

    @UmSyncMasterChangeSeqNum
    var customFieldValueOptionMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var customFieldValueOptionLCSN: Long = 0

    @UmSyncLastChangedBy
    var customFieldValueOptionLCB: Int = 0
}
