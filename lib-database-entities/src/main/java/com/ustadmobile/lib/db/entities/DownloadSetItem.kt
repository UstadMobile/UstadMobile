package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndexField
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

/**
 * Represents an item (linked to a ContentEntryFile) which is part of a specific DownloadSet.
 */
@UmEntity
class DownloadSetItem {

    @UmPrimaryKey(autoIncrement = true)
    var dsiUid: Long = 0

    /**
     * Foreign key for DownloadSet.dsUid (many to one relationship)
     */
    @UmIndexField
    var dsiDsUid: Long = 0

    @UmIndexField
    var dsiContentEntryUid: Long = 0

    constructor()

    constructor(set: DownloadSet, contentEntry: ContentEntry) {
        this.dsiDsUid = set.dsUid.toLong()
        this.dsiContentEntryUid = contentEntry.contentEntryUid
    }

    constructor(downloadSetUid: Long, contentEntryUid: Long) {
        this.dsiDsUid = downloadSetUid
        this.dsiContentEntryUid = contentEntryUid
    }
}
