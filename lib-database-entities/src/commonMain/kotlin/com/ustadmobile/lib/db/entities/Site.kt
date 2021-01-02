package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
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

    var siteName: String? = null

    var guestLogin: Boolean = true

    var registrationAllowed: Boolean = true
}