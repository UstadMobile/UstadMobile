package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

/**
 * Represents a set of entries that will be downloaded, with a single root entry. The DownloadSet
 * will often also include the descendant entries of the root entry (which are discovered by the
 * CrawlTask)
 *
 *
 *  1.
 * A DownloadJob has a 1:many relationship with DownloadJobRun, which represents a download run of
 * the set. It can be rerun (e.g. to update), which leads to a second DownloadJobRun entity.
 *
 *  1.
 * A DownloadJob has a 1:many relationship with DownloadJobItem, each of which represents a
 * single entry in the download set. Each DownloadJobItem has a 1:many relationship with a
 * DownloadJobItemRun
 *
 *
 */

@UmEntity
class DownloadSet {

    @UmPrimaryKey(autoIncrement = true)
    var dsUid: Int = 0

    var destinationDir: String? = null

    var isMeteredNetworkAllowed = false

    var dsRootContentEntryUid: Long = 0
}
