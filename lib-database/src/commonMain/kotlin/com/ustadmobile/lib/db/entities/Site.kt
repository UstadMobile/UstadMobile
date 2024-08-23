package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * Represents the site as a whole. There is only ever one row. Note trigger SQL checks to make sure
 * that there is never any change to the authSalt. That MUST remain constant otherwise authentication
 * will be broken.
 */
@Entity
@Serializable
@ReplicateEntity(
    tableId = Site.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
     Trigger(
         name = "site_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         conditionSql = """
             SELECT ((SELECT COUNT(*) 
                        FROM Site) = 0
                     OR NEW.authSalt = 
                        (SELECT Site.authSalt
                           FROM Site
                          LIMIT 1))
                 AND ($TRIGGER_CONDITION_WHERE_NEWER)          
         """,
         sqlStatements = [ TRIGGER_UPSERT ]
     )
))
class Site {

    @PrimaryKey(autoGenerate = true)
    var siteUid: Long = 0

    @MasterChangeSeqNum
    var sitePcsn: Long = 0

    @LocalChangeSeqNum
    var siteLcsn: Long = 0

    @LastChangedBy
    var siteLcb: Int = 0

    @ReplicateEtag
    @ReplicateLastModified
    var siteLct: Long = 0

    var siteName: String? = null

    var guestLogin: Boolean = true

    var registrationAllowed: Boolean = true

    var authSalt: String? = null

    companion object {

        const val TABLE_ID = 189

    }

}