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
@SyncableEntity(tableId = TABLE_ID,
    notifyOnUpdate = [
        """
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
               $TABLE_ID AS tableId 
          FROM ChangeLog 
                JOIN Clazz
                     ON ChangeLog.chTableId = $TABLE_ID 
                            AND Clazz.clazzUid = ChangeLog.chEntityPk
                $JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1
                    ${Role.PERMISSION_CLAZZ_SELECT}
                    $JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
        """
    ],
    syncFindAllQuery = """
        SELECT Clazz.* 
          FROM UserSession
               JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               $JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1
                    ${Role.PERMISSION_CLAZZ_SELECT} 
                    $JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2
         WHERE UserSession.usClientNodeId = :clientId 
           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
    """
)
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
    var clazzLct: Long = 0

    /**
     * The timezone ID as per https://www.iana.org/time-zones. If null, this means use the School
     * timezone
     */
    var clazzTimeZone: String? = null

    var clazzStudentsPersonGroupUid: Long = 0

    var clazzTeachersPersonGroupUid: Long = 0

    var clazzPendingStudentsPersonGroupUid: Long = 0

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

        const val JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2 = """
                                                     ) > 0
             JOIN PersonGroupMember AS PrsGrpMbr
                   ON ScopedGrant.sgGroupUid = PrsGrpMbr.groupMemberGroupUid
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
