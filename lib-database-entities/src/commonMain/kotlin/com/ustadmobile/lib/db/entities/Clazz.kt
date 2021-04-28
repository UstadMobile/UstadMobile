package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz.Companion.ENTITY_PERSONS_WITH_PERMISSION_PT1
import com.ustadmobile.lib.db.entities.Clazz.Companion.ENTITY_PERSONS_WITH_PERMISSION_PT2
import com.ustadmobile.lib.db.entities.Clazz.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID,
    notifyOnUpdate = [
        """
        SELECT DISTINCT DeviceSession.dsDeviceId as deviceId, $TABLE_ID as tableId FROM 
        ChangeLog
        JOIN Clazz ON ChangeLog.chTableId = $TABLE_ID AND Clazz.clazzUid = ChangeLog.chEntityPk
        JOIN Person ON Person.personUid IN ($ENTITY_PERSONS_WITH_PERMISSION_PT1  ${Role.PERMISSION_CLAZZ_SELECT } $ENTITY_PERSONS_WITH_PERMISSION_PT2)
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        """
    ],
    syncFindAllQuery = """
        SELECT Clazz.* FROM
        Clazz
        JOIN Person ON Person.personUid IN  ($ENTITY_PERSONS_WITH_PERMISSION_PT1 ${Role.PERMISSION_CLAZZ_SELECT } $ENTITY_PERSONS_WITH_PERMISSION_PT2)
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
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
    var clazzFeatures: Long = (CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_CLAZZWORK)

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
        this.clazzFeatures = CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ACTIVITY  or CLAZZ_FEATURE_CLAZZWORK
        this.isClazzActive = true
    }

    constructor(clazzName: String, clazzLocationUid: Long) : this() {
        this.clazzName = clazzName
        this.clazzLocationUid = clazzLocationUid
        this.clazzFeatures = CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ACTIVITY or CLAZZ_FEATURE_CLAZZWORK
        this.isClazzActive = true
    }

    companion object {

        const val TABLE_ID = 6
        const val CLAZZ_FEATURE_ATTENDANCE = 1L
        const val CLAZZ_FEATURE_ACTIVITY = 4L
        const val CLAZZ_FEATURE_CLAZZWORK = 8L

        const val CLAZZ_CODE_DEFAULT_LENGTH = 6


        const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person_Perm.PersonUid FROM Person Person_Perm
            LEFT JOIN PersonGroupMember ON Person_Perm.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE 
            CAST(Person.admin AS INTEGER) = 1
            OR 
            (
            ((EntityRole.ertableId = ${Clazz.TABLE_ID} AND EntityRole.erEntityUid = Clazz.clazzUid) OR
            (EntityRole.ertableId = ${School.TABLE_ID} AND EntityRole.erEntityUid = Clazz.clazzSchoolUid)
            )
            AND
            (Role.rolePermissions &  
        """

        const val ENTITY_PERSONS_WITH_PERMISSION_PT2 = ") > 0)"



    }
}
