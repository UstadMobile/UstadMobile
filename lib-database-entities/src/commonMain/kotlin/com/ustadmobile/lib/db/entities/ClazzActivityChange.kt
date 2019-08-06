package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 32)
@Entity
class ClazzActivityChange {

    @SyncablePrimaryKey
    @PrimaryKey(autoGenerate = true)
    var clazzActivityChangeUid: Long = 0

    var clazzActivityChangeTitle: String? = null

    var clazzActivityDesc: String? = null

    var clazzActivityUnitOfMeasure: Int = 0

    var isClazzActivityChangeActive: Boolean = false

    @UmSyncLastChangedBy
    var clazzActivityChangeLastChangedBy: Int = 0

    @UmSyncMasterChangeSeqNum
    var clazzActivityChangeMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var clazzActivityChangeLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var clazzActivityLastChangedBy: Int = 0

    companion object {

        val UOM_FREQUENCY = 1
        val UOM_DURATION = 2
        val UOM_BINARY = 3
    }
}
