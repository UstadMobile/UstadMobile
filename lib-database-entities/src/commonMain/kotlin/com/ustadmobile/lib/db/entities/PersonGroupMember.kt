package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
//@SyncableEntity(tableId = PersonGroupMember.TABLE_ID,
//    notifyOnUpdate = ["""
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${PersonGroupMember.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN PersonGroupMember
//                    ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID}
//                       AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
//               JOIN Person
//                    ON PersonGroupMember.groupMemberPersonUid = Person.personUid
//               ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}""",
//
//        //anyone who has been added/removed from the group by this should do a complete resync
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//                ${ClientSyncManager.TABLEID_SYNC_ALL_TABLES} AS tableId
//           FROM ChangeLog
//                JOIN PersonGroupMember
//                     ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID}
//                        AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
//                JOIN UserSession
//                     ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
//                        AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
//        """
//    ],
//    syncFindAllQuery = """
//        SELECT PersonsWithPerm_GroupMember.*
//          FROM UserSession
//               JOIN PersonGroupMember
//                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
//               ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
//               JOIN PersonGroupMember PersonsWithPerm_GroupMember
//                    ON PersonsWithPerm_GroupMember.groupMemberPersonUid = Person.personUid
//         WHERE UserSession.usClientNodeId = :clientId
//               AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}""")
@Serializable
class PersonGroupMember() {


    @PrimaryKey(autoGenerate = true)
    var groupMemberUid: Long = 0


    var groupMemberActive: Boolean = true

    @ColumnInfo(index = true)
    var groupMemberPersonUid: Long = 0

    @ColumnInfo(index = true)
    var groupMemberGroupUid: Long = 0

    @MasterChangeSeqNum
    var groupMemberMasterCsn: Long = 0

    @LocalChangeSeqNum
    var groupMemberLocalCsn: Long = 0

    @LastChangedBy
    var groupMemberLastChangedBy: Int = 0

    @LastChangedTime
    var groupMemberLct: Long = 0

    constructor(personUid:Long, groupUid:Long) : this(){
        this.groupMemberPersonUid = personUid
        this.groupMemberGroupUid = groupUid
    }

    companion object {
        const val TABLE_ID = 44
    }
}
