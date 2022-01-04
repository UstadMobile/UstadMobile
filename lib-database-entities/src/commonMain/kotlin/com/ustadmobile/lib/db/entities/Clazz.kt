package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz.Companion.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Clazz.Companion.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.db.entities.Clazz.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Clazz.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.db.entities.Clazz.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = ClazzReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "clazz_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO Clazz(clazzUid, clazzName, clazzDesc, attendanceAverage, clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid, isClazzActive, clazzLocationUid, clazzStartTime, clazzEndTime, clazzFeatures, clazzSchoolUid, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy, clazzLct, clazzTimeZone, clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid, clazzCode) 
         VALUES (NEW.clazzUid, NEW.clazzName, NEW.clazzDesc, NEW.attendanceAverage, NEW.clazzHolidayUMCalendarUid, NEW.clazzScheuleUMCalendarUid, NEW.isClazzActive, NEW.clazzLocationUid, NEW.clazzStartTime, NEW.clazzEndTime, NEW.clazzFeatures, NEW.clazzSchoolUid, NEW.clazzMasterChangeSeqNum, NEW.clazzLocalChangeSeqNum, NEW.clazzLastChangedBy, NEW.clazzLct, NEW.clazzTimeZone, NEW.clazzStudentsPersonGroupUid, NEW.clazzTeachersPersonGroupUid, NEW.clazzPendingStudentsPersonGroupUid, NEW.clazzParentsPersonGroupUid, NEW.clazzCode) 
         /*psql ON CONFLICT (clazzUid) DO UPDATE 
         SET clazzName = EXCLUDED.clazzName, clazzDesc = EXCLUDED.clazzDesc, attendanceAverage = EXCLUDED.attendanceAverage, clazzHolidayUMCalendarUid = EXCLUDED.clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid = EXCLUDED.clazzScheuleUMCalendarUid, isClazzActive = EXCLUDED.isClazzActive, clazzLocationUid = EXCLUDED.clazzLocationUid, clazzStartTime = EXCLUDED.clazzStartTime, clazzEndTime = EXCLUDED.clazzEndTime, clazzFeatures = EXCLUDED.clazzFeatures, clazzSchoolUid = EXCLUDED.clazzSchoolUid, clazzMasterChangeSeqNum = EXCLUDED.clazzMasterChangeSeqNum, clazzLocalChangeSeqNum = EXCLUDED.clazzLocalChangeSeqNum, clazzLastChangedBy = EXCLUDED.clazzLastChangedBy, clazzLct = EXCLUDED.clazzLct, clazzTimeZone = EXCLUDED.clazzTimeZone, clazzStudentsPersonGroupUid = EXCLUDED.clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid = EXCLUDED.clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid = EXCLUDED.clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid = EXCLUDED.clazzParentsPersonGroupUid, clazzCode = EXCLUDED.clazzCode
         */"""
     ]
 )
))
@Serializable
open class Clazz() {

    @PrimaryKey(autoGenerate = true)
    var clazzUid: Long = 0

    var clazzName: String? = null

    var clazzDesc: String? = null

    var attendanceAverage: Float = -1f

    //Gives the DateRange calendar Uid
    var clazzHolidayUMCalendarUid: Long = 0

    //Gives the schedule calendar uid
    var clazzScheuleUMCalendarUid: Long = 0

    //Active
    var isClazzActive: Boolean = false

    //Location
    var clazzLocationUid: Long = 0

    var clazzStartTime: Long = 0

    var clazzEndTime: Long = Long.MAX_VALUE

    //Clazz features
    var clazzFeatures: Long = (CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_CLAZZ_ASSIGNMENT)

    var clazzSchoolUid : Long = 0L

    @MasterChangeSeqNum
    var clazzMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var clazzLct: Long = 0

    /**
     * The timezone ID as per https://www.iana.org/time-zones. If null, this means use the School
     * timezone
     */
    var clazzTimeZone: String? = null

    var clazzStudentsPersonGroupUid: Long = 0

    var clazzTeachersPersonGroupUid: Long = 0

    var clazzPendingStudentsPersonGroupUid: Long = 0

    var clazzParentsPersonGroupUid: Long = 0

    /**
     * Code that can be used to join the class
     */
    var clazzCode: String? = null

    constructor(clazzName: String) : this() {
        this.clazzName = clazzName
        this.clazzFeatures = CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ACTIVITY  or CLAZZ_FEATURE_CLAZZ_ASSIGNMENT
        this.isClazzActive = true
    }

    constructor(clazzName: String, clazzLocationUid: Long) : this() {
        this.clazzName = clazzName
        this.clazzLocationUid = clazzLocationUid
        this.clazzFeatures = CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ACTIVITY or CLAZZ_FEATURE_CLAZZ_ASSIGNMENT
        this.isClazzActive = true
    }

    companion object {

        const val TABLE_ID = 6
        const val CLAZZ_FEATURE_ATTENDANCE = 1L
        const val CLAZZ_FEATURE_ACTIVITY = 4L
        const val CLAZZ_FEATURE_CLAZZ_ASSIGNMENT = 8L

        const val CLAZZ_CODE_DEFAULT_LENGTH = 6

        //Because no subqueries are needed here, there is no need for multiple versions based
        //on which way the joins are going
        const val JOIN_SCOPEDGRANT_ON_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                                AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
                            OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                                AND ScopedGrant.sgEntityUid = Clazz.clazzUid)
                            OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                                AND ScopedGrant.sgEntityUid = Clazz.clazzSchoolUid))
        """

        const val JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1 = """
            JOIN ScopedGrant
                 ON $JOIN_SCOPEDGRANT_ON_CLAUSE
                    AND (ScopedGrant.sgPermissions & 
        """

        const val JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER = """
                                                       ) > 0
             JOIN PersonGroupMember AS PrsGrpMbr
                   ON ScopedGrant.sgGroupUid = PrsGrpMbr.groupMemberGroupUid
        """

        const val JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2 = """
              $JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER                                       
              JOIN UserSession
                   ON UserSession.usPersonUid = PrsGrpMbr.groupMemberPersonUid
                      AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE }
        """

        const val JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1 = """
               JOIN ScopedGrant
                    ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                        AND (ScopedGrant.sgPermissions & 
        """

        const val JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2 = """
                       ) > 0
               JOIN Clazz 
                    ON $JOIN_SCOPEDGRANT_ON_CLAUSE
        """


    }
}
