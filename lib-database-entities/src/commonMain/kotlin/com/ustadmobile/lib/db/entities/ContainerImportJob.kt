package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@Serializable
class ContainerImportJob {

    @PrimaryKey(autoGenerate = true)
    var cujUid: Long = 0

    var cujContainerUid: Long = 0

    var filePath: String? = null

    var containerBaseDir: String? = null

    var contentEntryUid: Long = 0

    var mimeType: String? = null

    var sessionId: String? = null

    var jobStatus: Int = 0

    var bytesSoFar: Long = 0

    var importCompleted: Boolean = false

    var contentLength: Long = 0

    var containerEntryFileUids: String? = null

}