package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */

@Entity(indices = [
    //Index to optimize SchoolList where it selects a count of the members of each school by role.
    Index(value = ["schoolMemberSchoolUid", "schoolMemberActive", "schoolMemberRole"])
])
@SyncableEntity(tableId = SchoolMember.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${SchoolMember.TABLE_ID} AS tableId FROM 
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid""",

        //Person
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${Person.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //AgentEntity
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${AgentEntity.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzLogAttendanceRecord
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzLogAttendanceRecord.TABLE_ID} AS tableId FROM
        ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzWorkSubmission
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzWorkSubmission.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzWorkQuestionResponse
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzWorkQuestionResponse.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ContentEntryProgress
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ContentEntryProgress.CONTENT_ENTRY_PROGRESS_TABLE_ID} AS tableId FROM
        ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //EntityRole
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${EntityRole.TABLE_ID} AS tableId FROM
        ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //GroupLearningSession
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${GroupLearningSession.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //LearnerGroup
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${LearnerGroup.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //LearnerGroupMember
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${LearnerGroupMember.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //PersonGroup
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonGroup.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //PersonGroupMember
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonGroupMember.TABLE_ID} AS tableId FROM
        ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
            //PersonPicture
            """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonPicture.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_PICTURE_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //ClazzMember
        """ 
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzEnrolment.TABLE_ID} AS tableId FROM
        ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """,
        //StatementEntity
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${StatementEntity.TABLE_ID} AS tableId FROM
            ChangeLog
            JOIN SchoolMember ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID} AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """
        ],
        syncFindAllQuery = """
            SELECT SchoolMember.* FROM
            SchoolMember
            JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
            WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Serializable
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
    var schoolMemberLct: Long = 0


    constructor(){
        schoolMemberActive = true
        schoolMemberLeftDate = Long.MAX_VALUE
    }

    companion object {
        const val TABLE_ID = 200

    }
}
