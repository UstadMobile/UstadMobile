package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * Represents the site as a whole. There is only ever one row.
 */
@Entity
@Serializable
@SyncableEntity(tableId = 189)
open class Site {

    @PrimaryKey(autoGenerate = true)
    var siteUid: Long = 0

    @MasterChangeSeqNum
    var sitePcsn: Long = 0

    @LocalChangeSeqNum
    var siteLcsn: Long = 0

    @LastChangedBy
    var siteLcb: Int = 0

    @LastChangedTime
    var siteLct: Long = 0

    var siteName: String? = null

    var guestLogin: Boolean = true

    var registrationAllowed: Boolean = true

    var authSalt: String? = null

    /**
     * Could be a simple number - e.g. "6969" - in which case it should be understood that the
     * tracker is on the same server as the endpoint itself, and just change the port
     *
     *  e.g. endpoint = https://sitename.server.com/endpoint with torrentTracker = "6969"
     *    trackerUrl = http://sitename.server.com:6969/announce
     *
     *  e.g. endpoint = https://sitename.server.com/endpoint with torrentTracker = http://sitename.otherserver.com:6970/
     *     trackerUrl = http://sitename.otherserver.com:6970/announce
     */
    var torrentTracker: String? = null

}