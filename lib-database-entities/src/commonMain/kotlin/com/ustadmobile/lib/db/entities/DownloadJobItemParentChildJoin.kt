package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@UmEntity
@Entity
class DownloadJobItemParentChildJoin {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var djiPcjUid: Long = 0

    var djiParentDjiUid: Long = 0

    var djiChildDjiUid: Long = 0

    var djiCepcjUid: Long = 0

    constructor() {

    }

    constructor(parentItem: DownloadJobItem, cepcjUid: Long) {
        this.djiParentDjiUid = parentItem.djiUid
        this.djiCepcjUid = cepcjUid
    }

    constructor(djiParentDjiUid: Long, djiChildDjiUid: Long, djiCepcjUid: Long) {
        this.djiParentDjiUid = djiParentDjiUid
        this.djiChildDjiUid = djiChildDjiUid
        this.djiCepcjUid = djiCepcjUid
    }


    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || this::class != o::class) return false

        val that = o as DownloadJobItemParentChildJoin?

        if (djiPcjUid != that!!.djiPcjUid) return false
        return if (djiParentDjiUid != that.djiParentDjiUid) false else djiChildDjiUid == that.djiChildDjiUid
    }


    override fun hashCode(): Int {
        var result = (djiPcjUid xor djiPcjUid.ushr(32)).toInt()
        result = 31 * result + (djiParentDjiUid xor djiParentDjiUid.ushr(32)).toInt()
        result = 31 * result + (djiChildDjiUid xor djiChildDjiUid.ushr(32)).toInt()
        return result
    }

    override fun toString(): String {
        return "djiPcjUid=" + djiCepcjUid + ", djiParentDjiUid=" + djiParentDjiUid + ", " +
                djiChildDjiUid
    }
}
