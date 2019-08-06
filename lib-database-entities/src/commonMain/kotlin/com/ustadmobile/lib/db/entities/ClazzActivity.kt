package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 11)
@Entity
open class ClazzActivity {

    @SyncablePrimaryKey
    @PrimaryKey(autoGenerate = true)
    var clazzActivityUid: Long = 0

    //The activity change ClazzActivityChange
    var clazzActivityClazzActivityChangeUid: Long = 0

    //thumbs up or thumbs down
    var isClazzActivityGoodFeedback: Boolean = false

    //any notes
    var clazzActivityNotes: String? = null

    //the date
    var clazzActivityLogDate: Long = 0

    //the clazz
    var clazzActivityClazzUid: Long = 0

    //is it done?
    var isClazzActivityDone: Boolean = false

    //the quantity of activity - from unit of measure (frequency, duration, binary)
    var clazzActivityQuantity: Long = 0

    @UmSyncMasterChangeSeqNum
    var clazzActivityMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var clazzActivityLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var clazzActivityLastChangedBy: Int = 0
}
