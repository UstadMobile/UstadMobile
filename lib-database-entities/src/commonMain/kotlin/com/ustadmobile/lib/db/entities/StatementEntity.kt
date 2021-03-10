package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = StatementEntity.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${StatementEntity.TABLE_ID} AS tableId 
        FROM 
        ChangeLog
        JOIN StatementEntity ON ChangeLog.chTableId = ${StatementEntity.TABLE_ID} AND ChangeLog.chEntityPk = StatementEntity.statementUid
        JOIN AgentEntity ON StatementEntity.agentUid = AgentEntity.agentUid
        JOIN Person ON Person.personUid = AgentEntity.agentPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid"""],
    syncFindAllQuery = """
        SELECT StatementEntity.* FROM
        StatementEntity
        JOIN AgentEntity ON StatementEntity.agentUid = AgentEntity.agentUid
        JOIN Person ON Person.personUid = AgentEntity.agentPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId"""
)
@Serializable
open class StatementEntity {

    @PrimaryKey(autoGenerate = true)
    var statementUid: Long = 0

    var statementId: String? = null

    @ColumnInfo(index = true)
    var statementPersonUid: Long = 0

    var statementVerbUid: Long = 0

    var xObjectUid: Long = 0

    var subStatementActorUid: Long = 0

    var substatementVerbUid: Long = 0

    var subStatementObjectUid: Long = 0

    var agentUid: Long = 0

    var instructorUid: Long = 0

    var authorityUid: Long = 0

    var teamUid: Long = 0

    var resultCompletion: Boolean = false

    var resultSuccess: Byte = RESULT_UNSET

    var resultScoreScaled: Float = 0f

    var resultScoreRaw: Long = 0

    var resultScoreMin: Long = 0

    var resultScoreMax: Long = 0

    var resultDuration: Long = 0

    var resultResponse: String? = null

    var timestamp: Long = 0

    var stored: Long = 0

    var contextRegistration: String? = null

    var contextPlatform: String? = null

    var contextStatementId: String? = null

    var fullStatement: String? = null

    @MasterChangeSeqNum
    var statementMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var statementLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var statementLastChangedBy: Int = 0

    var extensionProgress: Int = 0

    /**
     *  indicates whether or not the statement is about the root contentEntry or child entries
     *  This is used by queries (e.g. for reports) e.g. to see if a "completed" verb applies
     *  to the contententry itself, or only a subsection (child) of the content
     */
    var contentEntryRoot: Boolean = false

    /**
     * Though technically the XObject is what really links to ContentEntry, the ContentEntryUid is
     * here to simplify queries used to check on student progress and avoid an extra join
     */
    var statementContentEntryUid: Long = 0


    var statementLearnerGroupUid: Long = 0

    companion object {

        const val TABLE_ID = 60

        const val RESULT_UNSET = 0.toByte()

        const val RESULT_SUCCESS = 1.toByte()

        const val RESULT_FAILURE = 2.toByte()
    }
}
