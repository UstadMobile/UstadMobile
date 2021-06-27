package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = StatementEntity.TABLE_ID,
    notifyOnUpdate = ["""
         SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, 
               ${StatementEntity.TABLE_ID} AS tableId 
          FROM ChangeLog
            JOIN StatementEntity 
                 ON ChangeLog.chTableId = ${StatementEntity.TABLE_ID} 
                    AND ChangeLog.chEntityPk = StatementEntity.statementUid
            JOIN ScopedGrant 
                 ON ${StatementEntity.FROM_STATEMENT_TO_SCOPEDGRANT_JOIN_ON_CLAUSE}
                    AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}) > 0
            JOIN PersonGroupMember 
                 ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
            JOIN DeviceSession
                 ON DeviceSession.dsPersonUid = PersonGroupMember.groupMemberPersonUid   
                """],
    syncFindAllQuery = """
       SELECT StatementEntity.*
              FROM DeviceSession
                   JOIN PersonGroupMember 
                        ON DeviceSession.dsPersonUid = PersonGroupMember.groupMemberPersonUid
                   JOIN ScopedGrant 
                        ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                            AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}) > 0
                   JOIN StatementEntity 
                        ON ${StatementEntity.FROM_SCOPEDGRANT_TO_STATEMENT_JOIN_ON_CLAUSE}
             WHERE DeviceSession.dsDeviceId = :clientId
          """
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

    @LastChangedTime
    var statementLct: Long = 0

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

    var statementClazzUid: Long = 0

    companion object {

        const val TABLE_ID = 60

        const val RESULT_UNSET = 0.toByte()

        const val RESULT_SUCCESS = 2.toByte()

        const val RESULT_FAILURE = 1.toByte()

        const val CONTENT_COMPLETE = 100

        const val CONTENT_INCOMPLETE = 101

        const val CONTENT_PASSED = 102

        const val CONTENT_FAILED = 103


        const val FROM_STATEMENT_TO_SCOPEDGRANT_JOIN_ON_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
             OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                AND ScopedGrant.sgEntityUid = StatementEntity.statementPersonUid)
             OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                AND ScopedGrant.sgEntityUid = StatementEntity.statementClazzUid)
             OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                AND ScopedGrant.sgEntityUid = (
                    SELECT clazzSchoolUid
                      FROM Clazz
                     WHERE clazzUid = StatementEntity.statementClazzUid))
             )
        """


        const val FROM_SCOPEDGRANT_TO_STATEMENT_JOIN_ON_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
             OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                AND ScopedGrant.sgEntityUid = StatementEntity.statementPersonUid)
             OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                AND ScopedGrant.sgEntityUid = StatementEntity.statementClazzUid)
             OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                AND ScopedGrant.sgEntityUid = (
                    SELECT clazzSchoolUid
                      FROM Clazz 
                     WHERE clazzUid = StatementEntity.statementClazzUid))
            )         
        """


    }
}
