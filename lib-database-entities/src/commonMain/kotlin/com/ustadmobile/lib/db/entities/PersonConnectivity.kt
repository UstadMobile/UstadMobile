package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.PersonConnectivity.Companion.TABLE_ID
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, $TABLE_ID AS tableId 
        FROM 
        ChangeLog
        JOIN PersonConnectivity ON ChangeLog.chTableId = $TABLE_ID AND ChangeLog.chEntityPk = PersonConnectivity.pcUid
        JOIN Person ON Person.personUid = PersonConnectivity.pcPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_CONNECTIVITY_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid"""],
        syncFindAllQuery = """
        SELECT PersonConnectivity.* FROM
        PersonConnectivity
        JOIN Person ON Person.personUid = PersonConnectivity.pcPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_CONNECTIVITY_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId"""
)
@Serializable
@Entity
class PersonConnectivity {

    @PrimaryKey(autoGenerate = true)
    var pcUid: Long = 0

    var pcPersonUid: Long = 0

    var pcConType: Int = 0

    var pcConStatus: Int = 0

    @MasterChangeSeqNum
    var pcMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var pcLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var pcLastChangedBy: Int = 0

    @LastChangedTime
    var pcLct: Long = 0

    companion object {

        const val TABLE_ID = 153

        const val CONNECTIVITY_STATUS_NONE = 1

        const val CONNECTIVITY_STATUS_LIMIT = 2

        const val CONNECTIVITY_STATUS_FULL = 3

        const val CONNECTIVITY_STATUS_NOT_TO_SAY = 4

        const val CONNECTIVITY_TYPE_HOME =  100

        const val CONNECTIVITY_TYPE_MOBILE =  101


    }


}