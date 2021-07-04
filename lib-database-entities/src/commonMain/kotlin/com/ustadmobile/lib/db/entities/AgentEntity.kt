package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = AgentEntity.TABLE_ID,
    notifyOnUpdate = [
        """
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
               ${AgentEntity.TABLE_ID} AS tableId 
         FROM ChangeLog
              JOIN AgentEntity 
                   ON ChangeLog.chTableId = ${AgentEntity.TABLE_ID} 
                        AND ChangeLog.chEntityPk = AgentEntity.agentUid
              JOIN Person 
                   ON Person.personUid = AgentEntity.agentPersonUid
              $JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1
                    ${Role.PERMISSION_PERSON_SELECT}
                    $JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
        """
    ],

    syncFindAllQuery = """
        SELECT AgentEntity.*
          FROM UserSession
               JOIN PersonGroupMember 
                        ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1} 
                        ${Role.PERMISSION_PERSON_SELECT}
                        ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
               JOIN AgentEntity 
                    ON AgentEntity.agentPersonUid = Person.personUid
         WHERE UserSession.usClientNodeId= :clientId
           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
        """
)

@Serializable
class AgentEntity {

    @PrimaryKey(autoGenerate = true)
    var agentUid: Long = 0

    var agentMbox: String? = null

    var agentMbox_sha1sum: String? = null

    var agentOpenid: String? = null

    var agentAccountName: String? = null

    var agentHomePage: String? = null

    var agentPersonUid: Long = 0

    @MasterChangeSeqNum
    var statementMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var statementLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var statementLastChangedBy: Int = 0

    @LastChangedTime
    var agentLct: Long = 0

    companion object {

        const val TABLE_ID = 68
    }
}
