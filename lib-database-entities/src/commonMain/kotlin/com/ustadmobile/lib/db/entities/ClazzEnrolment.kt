package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
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
     */
    notifyOnUpdate = [
        //clazzEnrolment itself
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzEnrolment.TABLE_ID} AS tableId FROM 
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //Person
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${Person.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //AgentEntity
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${AgentEntity.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzLogAttendanceRecord
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzLogAttendanceRecord.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzWorkSubmission
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzWorkSubmission.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZWORK_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzWorkQuestionResponse
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzWorkQuestionResponse.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZWORK_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ContentEntryProgress
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ContentEntryProgress.CONTENT_ENTRY_PROGRESS_TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //EntityRole
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${EntityRole.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //GroupLearningSession
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${GroupLearningSession.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //LearnerGroup
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${LearnerGroup.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //LearnerGroupMember
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${LearnerGroupMember.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //PersonGroup
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonGroup.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //PersonGroupMember
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonGroupMember.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //PersonPicture
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonPicture.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_PICTURE_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //SchoolMember
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${SchoolMember.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //StatementEntity
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${StatementEntity.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzEnrolment ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID} AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """
    ],
    syncFindAllQuery = """
        SELECT clazzEnrolment.* FROM
            ClazzEnrolment
            JOIN Person ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
            WHERE DeviceSession.dsDeviceId = :clientId
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

        const val ROLE_STUDENT = 1000

        const val ROLE_TEACHER = 1001

        const val OUTCOME_IN_PROGRESS = 200

        const val OUTCOME_GRADUATED = 201

        const val OUTCOME_FAILED = 202

        const val OUTCOME_DROPPED_OUT = 203

        /**
         * The role given to someone who has the class code, however their registration is not yet approved.
         */
        const val ROLE_STUDENT_PENDING = 1002

        const val TABLE_ID = 65
    }
}
