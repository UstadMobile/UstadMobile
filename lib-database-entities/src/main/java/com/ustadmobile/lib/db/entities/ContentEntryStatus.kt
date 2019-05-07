package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
class ContentEntryStatus {

    //Always equals contentEntryUid
    @UmPrimaryKey
    var cesUid: Long = 0

    var totalSize: Long = 0

    var bytesDownloadSoFar: Long = 0

    var downloadStatus: Int = 0

    var isLocallyAvailable: Boolean = false

    var downloadSpeed: Int = 0

    var isInvalidated = false

    var isCesLeaf: Boolean = false

    constructor()

    constructor(contentEntryUid: Long, isLeaf: Boolean, totalSize: Long) {
        this.cesUid = contentEntryUid
        this.isCesLeaf = isLeaf
        this.totalSize = totalSize
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as ContentEntryStatus?

        if (cesUid != that!!.cesUid) return false
        if (totalSize != that.totalSize) return false
        if (bytesDownloadSoFar != that.bytesDownloadSoFar) return false
        if (downloadStatus != that.downloadStatus) return false
        return if (isInvalidated != that.isInvalidated) false else isCesLeaf == that.isCesLeaf
    }

    override fun hashCode(): Int {
        var result = (cesUid xor cesUid.ushr(32)).toInt()
        result = 31 * result + (totalSize xor totalSize.ushr(32)).toInt()
        result = 31 * result + (bytesDownloadSoFar xor bytesDownloadSoFar.ushr(32)).toInt()
        result = 31 * result + downloadStatus
        result = 31 * result + if (isInvalidated) 1 else 0
        result = 31 * result + if (isCesLeaf) 1 else 0
        return result
    }

    companion object {


        const val LOCAL_STATUS_UNAVAILABLE = 0

        const val LOCAL_STATUS_AVAILABLE = 1
    }
}
