package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
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

@Entity
@SyncableEntity(tableId = ClazzMember.TABLE_ID,
    /* If someone is newly added to a class this might mean that existing members of the class (e.g.
     * students and teachers) now have access to information in other tables that was not previously
     * the case.
     *
     * E.g. if a new student is added to a class, the other people in the class would normally then
     * get the select permission on that person. Hence we need to trigger an update notification for
     * the other tables (such as person, statemententity, and others where permission can be affected
     * by class membership) for everyone who has permission to see this clazzmember.
     */
    notifyOnUpdate = [
        //ClazzMember itself
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzMember.TABLE_ID} AS tableId FROM 
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //Person
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${Person.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //AgentEntity
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${AgentEntity.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzLogAttendanceRecord
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzLogAttendanceRecord.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzWorkSubmission
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzWorkSubmission.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZWORK_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzWorkQuestionResponse
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzWorkQuestionResponse.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZWORK_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ContentEntryProgress
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ContentEntryProgress.CONTENT_ENTRY_PROGRESS_TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //EntityRole
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${EntityRole.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //GroupLearningSession
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${GroupLearningSession.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //LearnerGroup
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${LearnerGroup.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //LearnerGroupMember
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${LearnerGroupMember.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //PersonGroup
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonGroup.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //PersonGroupMember
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonGroupMember.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //PersonPicture
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonPicture.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_PICTURE_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //SchoolMember
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${SchoolMember.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //StatementEntity
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${StatementEntity.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN ClazzMember ON ChangeLog.chTableId = ${ClazzMember.TABLE_ID} AND ChangeLog.chEntityPk = ClazzMember.clazzMemberUid
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """
    ],
    syncFindAllQuery = """
        SELECT ClazzMember.* FROM
            ClazzMember
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
            WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Serializable
open class ClazzMember()  {

    /**
     * The personUid field of the related Person entity
     *
     * @param clazzMemberUid
     */
    @PrimaryKey(autoGenerate = true)
    var clazzMemberUid: Long = 0

    @ColumnInfo(index = true)
    var clazzMemberPersonUid: Long = 0

    @ColumnInfo(index = true)
    var clazzMemberClazzUid: Long = 0

    var clazzMemberDateJoined: Long = 0

    /**
     * The date the student left this class (e.g. graduated or un-enrolled).
     * Long.MAX_VALUE = no leaving date (e.g. ongoing registration)
     */
    var clazzMemberDateLeft: Long = Long.MAX_VALUE

    var clazzMemberRole: Int = 0

    var clazzMemberAttendancePercentage: Float = 0.toFloat()

    var clazzMemberActive: Boolean = false

    @LocalChangeSeqNum
    var clazzMemberLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var clazzMemberMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzMemberLastChangedBy: Int = 0

    constructor(clazzUid: Long, personUid: Long) : this() {
        this.clazzMemberClazzUid = clazzUid
        this.clazzMemberPersonUid = personUid
        this.clazzMemberActive = true
    }

    constructor(clazzUid: Long, personUid: Long, role: Int):this() {
        this.clazzMemberClazzUid = clazzUid
        this.clazzMemberPersonUid = personUid
        this.clazzMemberRole = role
        this.clazzMemberActive = true
    }

    companion object {

        const val ROLE_STUDENT = 1000

        const val ROLE_TEACHER = 1001

        /**
         * The role given to someone who has the class code, however their registration is not yet approved.
         */
        const val ROLE_STUDENT_PENDING = 1002

        const val TABLE_ID = 65
    }
}
