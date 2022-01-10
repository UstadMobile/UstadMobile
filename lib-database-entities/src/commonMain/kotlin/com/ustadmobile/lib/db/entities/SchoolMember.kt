package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.SchoolMember.Companion.FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_PERSON_OR_CLAZZ_PERMISSION_CLAUSE
import com.ustadmobile.lib.db.entities.SchoolMember.Companion.JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PERSON_OR_CLAZZ_PERMISSION_PT1
import com.ustadmobile.lib.db.entities.SchoolMember.Companion.JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.db.entities.SchoolMember.Companion.JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
import kotlinx.serialization.Serializable

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */

@Entity(indices = [
    //Index to optimize SchoolList where it selects a count of the members of each school by role.
    Index(value = ["schoolMemberSchoolUid", "schoolMemberActive", "schoolMemberRole"])
])
//@SyncableEntity(tableId = SchoolMember.TABLE_ID,
//    notifyOnUpdate = ["""
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${SchoolMember.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember
//                    ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                        AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PERSON_OR_CLAZZ_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//
//        //Person
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${Person.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                    AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //AgentEntity
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${AgentEntity.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                    AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //ClazzLogAttendanceRecord
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${ClazzLogAttendanceRecord.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                    AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //GroupLearningSession
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${GroupLearningSession.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember
//                    ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                        AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //LearnerGroup
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${LearnerGroup.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember
//                    ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                        AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //LearnerGroupMember
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${LearnerGroupMember.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                    AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //PersonGroup
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${PersonGroup.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember
//                    ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                        AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //PersonGroupMember
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${PersonGroupMember.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember
//                    ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                       AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//            //PersonPicture
//            """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${PersonPicture.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember
//                    ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                        AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_PICTURE_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //ClazzEnrolment
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${ClazzEnrolment.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember
//                    ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                            AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //Clazz
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${Clazz.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN SchoolMember
//                    ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                            AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//               $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_CLAZZ_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """,
//        //StatementEntity
//        """
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, ${StatementEntity.TABLE_ID} AS tableId FROM
//            ChangeLog
//            JOIN SchoolMember
//                 ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
//                        AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//                 $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1
//                    ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}
//                    $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
//        """
//        ],
//        //Note: because the ScopedGrant JOIN ON clause does not include any subquery,
//        // there is currently no need to have two different versions of it for SchoolMember
//        syncFindAllQuery = """
//            SELECT SchoolMember.*
//              FROM UserSession
//                   JOIN PersonGroupMember
//                        ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
//                   JOIN ScopedGrant
//                        ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
//                           AND (ScopedGrant.sgPermissions &  ${Role.PERMISSION_PERSON_SELECT}) > 0
//                   JOIN SchoolMember
//                        ON $FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_PERSON_OR_CLAZZ_PERMISSION_CLAUSE
//             WHERE UserSession.usClientNodeId = :clientId
//                   AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
//    """)
@Serializable
@ReplicateEntity(tableId = SchoolMember.TABLE_ID, tracker = SchoolMemberReplicate::class,
    priority = ReplicateEntity.HIGHEST_PRIORITY + 1)
@Triggers(arrayOf(
 Trigger(
     name = "schoolmember_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO SchoolMember(schoolMemberUid, schoolMemberPersonUid, schoolMemberSchoolUid, schoolMemberJoinDate, schoolMemberLeftDate, schoolMemberRole, schoolMemberActive, schoolMemberLocalChangeSeqNum, schoolMemberMasterChangeSeqNum, schoolMemberLastChangedBy, schoolMemberLct) 
         VALUES (NEW.schoolMemberUid, NEW.schoolMemberPersonUid, NEW.schoolMemberSchoolUid, NEW.schoolMemberJoinDate, NEW.schoolMemberLeftDate, NEW.schoolMemberRole, NEW.schoolMemberActive, NEW.schoolMemberLocalChangeSeqNum, NEW.schoolMemberMasterChangeSeqNum, NEW.schoolMemberLastChangedBy, NEW.schoolMemberLct) 
         /*psql ON CONFLICT (schoolMemberUid) DO UPDATE 
         SET schoolMemberPersonUid = EXCLUDED.schoolMemberPersonUid, schoolMemberSchoolUid = EXCLUDED.schoolMemberSchoolUid, schoolMemberJoinDate = EXCLUDED.schoolMemberJoinDate, schoolMemberLeftDate = EXCLUDED.schoolMemberLeftDate, schoolMemberRole = EXCLUDED.schoolMemberRole, schoolMemberActive = EXCLUDED.schoolMemberActive, schoolMemberLocalChangeSeqNum = EXCLUDED.schoolMemberLocalChangeSeqNum, schoolMemberMasterChangeSeqNum = EXCLUDED.schoolMemberMasterChangeSeqNum, schoolMemberLastChangedBy = EXCLUDED.schoolMemberLastChangedBy, schoolMemberLct = EXCLUDED.schoolMemberLct
         */"""
     ]
 )
))
open class SchoolMember {

    @PrimaryKey(autoGenerate = true)
    var schoolMemberUid: Long = 0

    @ColumnInfo(index = true)
    var schoolMemberPersonUid: Long = 0

    @ColumnInfo(index = true)
    var schoolMemberSchoolUid: Long = 0

    var schoolMemberJoinDate : Long = 0

    var schoolMemberLeftDate : Long = Long.MAX_VALUE

    var schoolMemberRole: Int = 0

    var schoolMemberActive: Boolean = true

    @LocalChangeSeqNum
    var schoolMemberLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var schoolMemberMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var schoolMemberLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var schoolMemberLct: Long = 0


    constructor(){
        schoolMemberActive = true
        schoolMemberLeftDate = Long.MAX_VALUE
    }

    companion object {
        const val TABLE_ID = 200

        /**
         * This version of the where clause will find anyone who has permission to see this. This
         * is needed for updates to the entity itself.
         */
        const val FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_PERSON_OR_CLAZZ_PERMISSION_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                  AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
              OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                  AND ScopedGrant.sgEntityUid = SchoolMember.schoolMemberPersonUid)
              OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                 AND ScopedGrant.sgEntityUid = SchoolMember.schoolMemberSchoolUid))
        """

        const val JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PERSON_OR_CLAZZ_PERMISSION_PT1 = """
            JOIN ScopedGrant
                 ON $FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_PERSON_OR_CLAZZ_PERMISSION_CLAUSE
                    AND (ScopedGrant.sgPermissions &
        """

        /**
         * This version of the join clause will only find those who have permission granted by school,
         * e.g. the entity table sync status needs invalidated because they may now have permission
         * over additional entities.
         *
         * E.g. now that someone is a SchoolMember, those who have the PERSON_SELECT permission over
         * the school can now see this person profile, which was not previously the case. That means
         * the Person table must be invalidated for anyone with the PERSON_SELECT permission granted
         * on the school applicable for this SchoolMember.
         *
         * We only need to invalidate the sync status where permission was granted by school. School
         * membership changes do not have any affect on permissions that were acquired by class or
         * person scopes, or superadmin grants.
         */
        const val FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_SCHOOL_PERMISSION_CLAUSE = """
            (ScopedGrant.sgTableId = ${School.TABLE_ID}
                 AND ScopedGrant.sgEntityUid = SchoolMember.schoolMemberSchoolUid)
        """

        const val JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1 = """
            JOIN ScopedGrant
                 ON $FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_SCHOOL_PERMISSION_CLAUSE
                    AND (ScopedGrant.sgPermissions &
        """


        const val JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2 = """
            ) > 0  
            JOIN PersonGroupMember 
                   ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
            JOIN UserSession
                   ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                      AND UserSession.usStatus =${UserSession.STATUS_ACTIVE}
        """



    }
}
