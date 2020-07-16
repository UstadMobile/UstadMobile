package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = ContainerUploadJob.TABLE_ID)
@Serializable
class ContainerUploadJob {

    @PrimaryKey(autoGenerate = true)
    var cujUid: Long = 0

    var cujContainerUid: Long = 0

    var sessionId: String? = null

    var jobStatus: Int = 0

    var bytesSoFar: Long = 0

    var containerEntryFileUids: String? = null

    @MasterChangeSeqNum
    var cujMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var cujLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var cujLastChangedBy: Int = 0

    companion object {

        const val TABLE_ID = 78
    }

}