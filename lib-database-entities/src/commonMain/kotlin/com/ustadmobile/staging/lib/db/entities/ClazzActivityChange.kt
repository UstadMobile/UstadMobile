package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = 32)
@Entity
@Serializable
class ClazzActivityChange {

    @PrimaryKey(autoGenerate = true)
    var clazzActivityChangeUid: Long = 0

    var clazzActivityChangeTitle: String? = null

    var clazzActivityDesc: String? = null

    var clazzActivityUnitOfMeasure: Int = 0

    var isClazzActivityChangeActive: Boolean = false

    @LastChangedBy
    var clazzActivityChangeLastChangedBy: Int = 0

    @MasterChangeSeqNum
    var clazzActivityChangeMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzActivityChangeLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzActivityLastChangedBy: Int = 0

    companion object {

        val UOM_FREQUENCY = 1
        val UOM_DURATION = 2
        val UOM_BINARY = 3
    }
}
