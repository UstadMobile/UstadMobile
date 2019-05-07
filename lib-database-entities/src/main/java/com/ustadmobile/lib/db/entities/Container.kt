package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndex
import com.ustadmobile.lib.database.annotation.UmIndexField
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 51, indices = [UmIndex(name = "cnt_uid_to_most_recent", value = ["containerContentEntryUid", "lastModified"])])
open class Container {

    @UmPrimaryKey(autoGenerateSyncable = true)
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
    var lastModified: Long = 0

    var mimeType: String? = null

    var remarks: String? = null

    var isMobileOptimized: Boolean = false

    /**
     * Total number of entries in this container
     */
    var cntNumEntries: Int = 0

    constructor()

    constructor(contentEntry: ContentEntry) {
        this.containerContentEntryUid = contentEntry.contentEntryUid
    }
}
