package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*

/**
 * Entity to hold authentication information about a given person. It contains the hashed password
 * and the mechanism.
 */
@Entity
@ReplicateEntity(tableId = PersonAuth2.TABLE_ID, tracker = PersonAuth2Replicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "personauth2_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO PersonAuth2(pauthUid, pauthMechanism, pauthAuth, pauthLcsn, pauthPcsn, pauthLcb, pauthLct) 
         VALUES (NEW.pauthUid, NEW.pauthMechanism, NEW.pauthAuth, NEW.pauthLcsn, NEW.pauthPcsn, NEW.pauthLcb, NEW.pauthLct) 
         /*psql ON CONFLICT (pauthUid) DO UPDATE 
         SET pauthMechanism = EXCLUDED.pauthMechanism, pauthAuth = EXCLUDED.pauthAuth, pauthLcsn = EXCLUDED.pauthLcsn, pauthPcsn = EXCLUDED.pauthPcsn, pauthLcb = EXCLUDED.pauthLcb, pauthLct = EXCLUDED.pauthLct
         */"""
     ]
 )
))
class PersonAuth2 {

    /**
     * The pauthUid is simply the personUid for the associated Person. This is a 1:1 join. It is a
     * separate entity for permission management purposes.
     */
    @PrimaryKey
    var pauthUid: Long = 0

    //The one way hash mechanism to use. Currently only PBKDF2 is supported
    var pauthMechanism: String? = null

    //The **double** hashed string. This allows verification of the UserSession (single hashed)
    // without the actual password being stored
    var pauthAuth: String? = null

    @LocalChangeSeqNum
    var pauthLcsn: Long = 0

    @MasterChangeSeqNum
    var pauthPcsn: Long = 0

    @LastChangedBy
    var pauthLcb: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var pauthLct: Long = 0

    companion object {

        /**
         * The password will be hashed using PBKDF2 twice. When a session is created, the password
         * supplied by the user can be verified, and the session object authentication will be
         * encrypted using a single hash.
         */
        const val AUTH_MECH_PBKDF2_DOUBLE = "PBKDF2x2"

        const val TABLE_ID = 678

    }

}