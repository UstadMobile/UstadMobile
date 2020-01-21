package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 11)
@Entity
@Serializable
open class ClazzActivity() {

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
    var clazzActivityDone: Boolean = false

    //the quantity of activity - from unit of measure (frequency, duration, binary)
    var clazzActivityQuantity: Long = 0

    @MasterChangeSeqNum
    var clazzActivityMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzActivityLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzActivityLastChangedBy: Int = 0
}
