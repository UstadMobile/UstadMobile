package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = ClazzWorkContentJoin.TABLE_ID,
    notifyOnUpdate = """
        SELECT DISTINCT DeviceSession.dsDeviceId FROM 
        ChangeLog
        JOIN ClazzWorkContentJoin ON ChangeLog.chTableId = ${ClazzWorkContentJoin.TABLE_ID} AND ClazzWorkContentJoin.clazzWorkContentJoinUid = ChangeLog.chEntityPk
        JOIN ClazzWork ON ClazzWork.clazzWorkUid = ClazzWorkContentJoin.clazzWorkContentJoinClazzWorkUid
        JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid 
        JOIN Person ON Person.personUid IN (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1}  ${Role.PERMISSION_CLAZZWORK_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid""",
    syncFindAllQuery = """
        SELECT ClazzWorkContentJoin.* FROM
        ClazzWorkContentJoin
        JOIN ClazzWork ON ClazzWork.clazzWorkUid = ClazzWorkContentJoin.clazzWorkContentJoinClazzWorkUid
        JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid
        JOIN Person ON Person.personUid IN  (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_CLAZZWORK_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId""")
@Serializable
open class ClazzWorkContentJoin() {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkContentJoinUid: Long = 0

    var clazzWorkContentJoinContentUid : Long = 0

    var clazzWorkContentJoinClazzWorkUid : Long = 0

    var clazzWorkContentJoinInactive : Boolean = false

    var clazzWorkContentJoinDateAdded : Long = 0

    @MasterChangeSeqNum
    var clazzWorkContentJoinMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzWorkContentJoinLCSN: Long = 0

    @LastChangedBy
    var clazzWorkContentJoinLCB: Int = 0

    companion object {

        const val TABLE_ID = 204

    }

}
