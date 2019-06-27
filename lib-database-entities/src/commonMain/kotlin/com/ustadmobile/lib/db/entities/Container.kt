package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*

@UmEntity(tableId = 51 ,indices = arrayOf(UmIndex(name = "cnt_uid_to_most_recent", value = ["containerContentEntryUid", "lastModified"])))
@Entity(indices = arrayOf(Index(name = "cnt_uid_to_most_recent", value = ["containerContentEntryUid", "lastModified"])))
open class Container() {

    @PrimaryKey(autoGenerate = true)
    var containerUid: Long = 0

    @UmSyncLocalChangeSeqNum
    var cntLocalCsn: Long = 0

    @UmSyncMasterChangeSeqNum
    var cntMasterCsn: Long = 0

    @UmSyncLastChangedBy
    var cntLastModBy: Int = 0

    var fileSize: Long = 0

    var containerContentEntryUid: Long = 0

    @UmIndexField
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
