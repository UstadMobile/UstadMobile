package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.FROM_SCOPEDGRANT_TO_CLAZZENROLMENT_JOIN__ON_CLAUSE
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2

import kotlinx.serialization.Serializable

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */

@Entity(indices = [
    //Index to streamline permission queries etc. that lookup a list of classes for a given person
    Index(value = ["clazzEnrolmentPersonUid", "clazzEnrolmentClazzUid"]),
    //Index to streamline finding which people are in a given clazzuid
    Index(value = ["clazzEnrolmentClazzUid", "clazzEnrolmentPersonUid"]),
    //Index for streamlining ClazzList where the number of users is counted by role
    Index(value = ["clazzEnrolmentClazzUid", "clazzEnrolmentRole"])
])
@SyncableEntity(tableId = ClazzEnrolment.TABLE_ID,
    /* If someone is newly added to a class this might mean that existing members of the class (e.g.
     * students and teachers) now have access to information in other tables that was not previously
     * the case.
     *
     * E.g. if a new student is added to a class, the other people in the class would normally then
     * get the select permission on that person. Hence we need to trigger an update notification for
     * the other tables (such as person, statemententity, and others where permission can be affected
     * by class membership) for everyone who has permission to see this clazzEnrolment.
     *
     * Note: There is a possibility that this could be made more efficient with a CTE, and then
     * joining to the CTE. There could then be two
     *
     */
    notifyOnUpdate = [
        //clazzEnrolment itself
        """
    SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
           ${ClazzEnrolment.TABLE_ID} AS tableId 
      FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN ScopedGrant 
                 ON ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                         AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
                      OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                          AND ScopedGrant.sgEntityUid = ClazzEnrolment.clazzEnrolmentPersonUid)
                      OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                          AND ScopedGrant.sgEntityUid = ClazzEnrolment.clazzEnrolmentClazzUid)
                      OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                         AND ScopedGrant.sgEntityUid = (
                             SELECT clazzSchoolUid
                               FROM Clazz
                              WHERE clazzUid = ClazzEnrolment.clazzEnrolmentClazzUid)))
                    AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_SELECT}) > 0                               
            JOIN PersonGroupMember 
                   ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
            JOIN UserSession
                   ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                      AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
        """,
        //Person
        """
    SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
           ${ClazzEnrolment.TABLE_ID} AS tableId 
      FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                  ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT} 
                  $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2     
        """,
        //AgentEntity
        """
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
           ${AgentEntity.TABLE_ID} AS tableId 
      FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                    ${Role.PERMISSION_PERSON_SELECT}
                    $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
        """,
        //ClazzLogAttendanceRecord
        """
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
                ${ClazzLogAttendanceRecord.TABLE_ID} AS tableId 
          FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                  ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT} 
                  $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
        """,
        //GroupLearningSession
        """
      SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
            ${GroupLearningSession.TABLE_ID} AS tableId 
       FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                    ${Role.PERMISSION_PERSON_SELECT}
                    $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
        """,
        //LearnerGroup
        """
      SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
            ${LearnerGroup.TABLE_ID} AS tableId 
       FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                    ${Role.PERMISSION_PERSON_SELECT}
                    $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2      
        """,
        //LearnerGroupMember
        """
      SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
            ${LearnerGroupMember.TABLE_ID} AS tableId 
       FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                    ${Role.PERMISSION_PERSON_SELECT}
                    $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2      
        """,
        //PersonGroup
        """
      SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
            ${PersonGroup.TABLE_ID} AS tableId 
       FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                    ${Role.PERMISSION_PERSON_SELECT}
                    $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
        """,
        //PersonGroupMember
        """
      SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
            ${PersonGroupMember.TABLE_ID} AS tableId 
       FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                    ${Role.PERMISSION_PERSON_SELECT}
                    $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
        """,
        //PersonPicture
        """
      SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
            ${PersonPicture.TABLE_ID} AS tableId 
       FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                    ${Role.PERMISSION_PERSON_PICTURE_SELECT}
                    $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
                    
        """,
        //SchoolMember
        """
      SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
            ${SchoolMember.TABLE_ID} AS tableId 
       FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                    ${Role.PERMISSION_PERSON_SELECT}
                    $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
        """,
        //StatementEntity
        """
      SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
            ${StatementEntity.TABLE_ID} AS tableId 
       FROM ChangeLog
            JOIN ClazzEnrolment 
                 ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid 
            $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1
                    ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}
                    $JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
        """
    ],
    syncFindAllQuery = """
       SELECT clazzEnrolment.* 
         FROM UserSession
              JOIN PersonGroupMember 
                   ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
              JOIN ScopedGrant 
                   ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                      AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_SELECT}) > 0 
              JOIN ClazzEnrolment 
                   ON $FROM_SCOPEDGRANT_TO_CLAZZENROLMENT_JOIN__ON_CLAUSE
             WHERE UserSession.usClientNodeId = :clientId
                   AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE} 
    """)
@Serializable
open class ClazzEnrolment()  {

    /**
     * The personUid field of the related Person entity
     *
     * @param clazzEnrolmentUid
     */
    @PrimaryKey(autoGenerate = true)
    var clazzEnrolmentUid: Long = 0

    @ColumnInfo(index = true)
    var clazzEnrolmentPersonUid: Long = 0

    @ColumnInfo(index = true)
    var clazzEnrolmentClazzUid: Long = 0

    var clazzEnrolmentDateJoined: Long = 0

    /**
     * The date the student left this class (e.g. graduated or un-enrolled).
     * Long.MAX_VALUE = no leaving date (e.g. ongoing registration)
     */
    var clazzEnrolmentDateLeft: Long = Long.MAX_VALUE

    var clazzEnrolmentRole: Int = 0

    var clazzEnrolmentAttendancePercentage: Float = 0.toFloat()

    var clazzEnrolmentActive: Boolean = true

    var clazzEnrolmentLeavingReasonUid: Long = 0

    var clazzEnrolmentOutcome: Int = OUTCOME_IN_PROGRESS

    @LocalChangeSeqNum
    var clazzEnrolmentLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var clazzEnrolmentMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzEnrolmentLastChangedBy: Int = 0

    @LastChangedTime
    var clazzEnrolmentLct: Long = 0

    constructor(clazzUid: Long, personUid: Long) : this() {
        this.clazzEnrolmentClazzUid = clazzUid
        this.clazzEnrolmentPersonUid = personUid
        this.clazzEnrolmentActive = true
    }

    constructor(clazzUid: Long, personUid: Long, role: Int):this() {
        this.clazzEnrolmentClazzUid = clazzUid
        this.clazzEnrolmentPersonUid = personUid
        this.clazzEnrolmentRole = role
        this.clazzEnrolmentActive = true
    }

    companion object {

        const val FROM_SCOPEDGRANT_TO_CLAZZENROLMENT_JOIN__ON_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                  AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
              OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                  AND ScopedGrant.sgEntityUid = ClazzEnrolment.clazzEnrolmentPersonUid)
              OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                  AND ScopedGrant.sgEntityUid = ClazzEnrolment.clazzEnrolmentClazzUid)
              OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                  AND ClazzEnrolment.clazzEnrolmentClazzUid IN (
                      SELECT clazzUid 
                        FROM Clazz
                       WHERE clazzSchoolUid = ScopedGrant.sgEntityUid))
                  )
        """

        const val FROM_CLAZZENROLMENT_TO_SCOPEDGRANT_JOIN_ON_CLAUSE = """
            (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                  AND ScopedGrant.sgEntityUid = ClazzEnrolment.clazzEnrolmentClazzUid)
        """


        /**
         * When the sync status of other tables is being invalidated because of a change on
         * ClazzEnrolment, we only need to consider grants that are scoped by class. Grants that
         * are scoped by School or Person are not affected.
         */
        const val JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_CLAZZSCOPE_ONLY_PT1 = """
            JOIN ScopedGrant 
                 ON $FROM_CLAZZENROLMENT_TO_SCOPEDGRANT_JOIN_ON_CLAUSE
                    AND (ScopedGrant.sgPermissions &
        """

        const val JOIN_FROM_CLAZZENROLMENT_TO_USERSESSION_VIA_SCOPEDGRANT_PT2 = """
            ) > 0  
            JOIN PersonGroupMember 
                   ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
            JOIN UserSession
                   ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                      AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
        """

        const val ROLE_STUDENT = 1000

        const val ROLE_TEACHER = 1001

        /**
         * The role given to someone who has the class code, however their registration is not yet approved.
         */
        const val ROLE_STUDENT_PENDING = 1002

        const val OUTCOME_IN_PROGRESS = 200

        const val OUTCOME_GRADUATED = 201

        const val OUTCOME_FAILED = 202

        const val OUTCOME_DROPPED_OUT = 203

        const val TABLE_ID = 65
    }
}
