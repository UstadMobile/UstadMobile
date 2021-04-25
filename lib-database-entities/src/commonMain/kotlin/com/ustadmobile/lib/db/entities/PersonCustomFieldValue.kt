package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = PersonCustomFieldValue.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonCustomFieldValue.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN PersonCustomFieldValue ON ChangeLog.chTableId = ${PersonCustomFieldValue.TABLE_ID} AND ChangeLog.chEntityPk = PersonCustomFieldValue.personCustomFieldValueUid
        JOIN Person ON Person.personUid = PersonCustomFieldValue.personCustomFieldValuePersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid"""],
    syncFindAllQuery = """
        SELECT PersonCustomFieldValue.* FROM 
        PersonCustomFieldValue
        JOIN Person ON Person.personUid = PersonCustomFieldValue.personCustomFieldValuePersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
        """)
@Entity
@Serializable
class PersonCustomFieldValue() {

    @PrimaryKey(autoGenerate = true)
    var personCustomFieldValueUid: Long = 0

    //The Custom field's uid
    var personCustomFieldValuePersonCustomFieldUid: Long = 0

    //The person associated uid
    var personCustomFieldValuePersonUid: Long = 0

    //The value itself
    var fieldValue: String? = null

    @MasterChangeSeqNum
    var personCustomFieldValueMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var personCustomFieldValueLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var personCustomFieldValueLastChangedBy: Int = 0

    @LastChangedTime
    var personCustomFieldValueLct: Long = 0

    companion object {
        const val TABLE_ID = 178
    }
}
