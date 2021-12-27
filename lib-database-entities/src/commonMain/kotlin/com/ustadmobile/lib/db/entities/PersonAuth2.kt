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
//@SyncableEntity(tableId = PersonAuth2.TABLE_ID,
//    syncFindAllQuery = """
//        SELECT PersonAuth2.*
//          FROM UserSession
//                   JOIN PersonGroupMember
//                        ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
//                   ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
//                        ${Role.PERMISSION_AUTH_SELECT}
//                        ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
//                   JOIN PersonAuth2
//                        ON PersonAuth2.pauthUid = Person.personUid
//         WHERE UserSession.usClientNodeId = :clientId
//           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
//    """,
//    notifyOnUpdate = ["""
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${PersonAuth2.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN PersonAuth2
//                    ON ChangeLog.chTableId = ${PersonAuth2.TABLE_ID}
//                       AND ChangeLog.chEntityPk = PersonAuth2.pauthUid
//               JOIN Person
//                    ON Person.personUid = PersonAuth2.pauthUid
//               ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
//                    ${Role.PERMISSION_AUTH_SELECT}
//                    ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
//    """])
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
    var pauthLcb: Int = 0

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