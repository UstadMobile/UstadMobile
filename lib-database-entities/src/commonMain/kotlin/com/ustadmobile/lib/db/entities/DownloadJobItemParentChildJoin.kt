package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class DownloadJobItemParentChildJoin() {

    @PrimaryKey(autoGenerate = true)
    var djiPcjUid: Int = 0

    var djiParentDjiUid: Int = 0

    var djiChildDjiUid: Int = 0

    var djiCepcjUid: Long = 0

    constructor(parentItem: DownloadJobItem, cepcjUid: Long) : this() {
        this.djiParentDjiUid = parentItem.djiUid
        this.djiCepcjUid = cepcjUid
    }

    constructor(djiParentDjiUid: Int, djiChildDjiUid: Int, djiCepcjUid: Long) : this() {
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
