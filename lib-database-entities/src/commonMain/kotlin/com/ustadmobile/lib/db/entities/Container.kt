package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity(indices = arrayOf(Index(name = "cnt_uid_to_most_recent", value = ["containerContentEntryUid", "lastModified"])))
@SyncableEntity(tableId = 51)
@Serializable
open class Container() {

    @PrimaryKey(autoGenerate = true)
    var containerUid: Long = 0

    @LocalChangeSeqNum
    var cntLocalCsn: Long = 0

    @MasterChangeSeqNum
    var cntMasterCsn: Long = 0

    @LastChangedBy
    var cntLastModBy: Int = 0

    var fileSize: Long = 0

    var containerContentEntryUid: Long = 0

    @ColumnInfo(index = true)
    var lastModified: Long = 0

    var mimeType: String? = null

    var remarks: String? = null

    var mobileOptimized: Boolean = false

    /**
     * Total number of entries in this container
     */
    var cntNumEntries: Int = 0

    constructor(contentEntry: ContentEntry) : this() {
        this.containerContentEntryUid = contentEntry.contentEntryUid
    }
}
