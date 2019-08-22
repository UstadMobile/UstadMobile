package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ContentEntryStatus() {

    //Always equals contentEntryUid
    @PrimaryKey
    var cesUid: Long = 0

    var totalSize: Long = 0

    var bytesDownloadSoFar: Long = 0

    var downloadStatus: Int = 0

    var locallyAvailable: Boolean = false

    var downloadSpeed: Int = 0

    var invalidated = false

    var cesLeaf: Boolean = false

    constructor(contentEntryUid: Long, isLeaf: Boolean, totalSize: Long) : this() {
        this.cesUid = contentEntryUid
        this.cesLeaf= isLeaf
        this.totalSize = totalSize
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if(other == null)
            return false

        val that = other as ContentEntryStatus

        if (cesUid != that.cesUid) return false
        if (totalSize != that.totalSize) return false
        if (bytesDownloadSoFar != that.bytesDownloadSoFar) return false
        if (downloadStatus != that.downloadStatus) return false
        return if (invalidated != that.invalidated) false else cesLeaf== that.cesLeaf
    }

    override fun hashCode(): Int {
        var result = (cesUid xor cesUid.ushr(32)).toInt()
        result = 31 * result + (totalSize xor totalSize.ushr(32)).toInt()
        result = 31 * result + (bytesDownloadSoFar xor bytesDownloadSoFar.ushr(32)).toInt()
        result = 31 * result + downloadStatus
        result = 31 * result + if (invalidated) 1 else 0
        result = 31 * result + if (cesLeaf) 1 else 0
        return result
    }

    companion object {


        const val LOCAL_STATUS_UNAVAILABLE = 0

        const val LOCAL_STATUS_AVAILABLE = 1
    }
}
