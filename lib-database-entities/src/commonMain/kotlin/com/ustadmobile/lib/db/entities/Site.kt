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
@ReplicateEntity(tableId = 189, tracker = SiteTrkr::class)
@Triggers(arrayOf(
    Trigger(name = "site_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = ["""
           REPLACE INTO Site(siteUid, sitePcsn, siteLcsn, siteLcb, siteLct, siteName, guestLogin, 
                        registrationAllowed, authSalt)
                 VALUES (NEW.siteUid, NEW.sitePcsn, NEW.siteLcsn, NEW.siteLcb, NEW.siteLct, NEW.siteName,
                        NEW.guestLogin, NEW.registrationAllowed, NEW.authSalt)
        """])
))
open class Site {

    @PrimaryKey(autoGenerate = true)
    var siteUid: Long = 0

    @MasterChangeSeqNum
    var sitePcsn: Long = 0

    @LocalChangeSeqNum
    var siteLcsn: Long = 0

    @LastChangedBy
    var siteLcb: Int = 0

    @ReplicationVersionId
    @LastChangedTime
    var siteLct: Long = 0

    var siteName: String? = null

    var guestLogin: Boolean = true

    var registrationAllowed: Boolean = true

    var authSalt: String? = null

}