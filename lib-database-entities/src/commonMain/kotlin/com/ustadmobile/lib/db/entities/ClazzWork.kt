package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.ClazzWork.Companion.CLAZZ_WORK_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CLAZZ_WORK_TABLE_ID,
    notifyOnUpdate = """
        SELECT DISTINCT DeviceSession.dsDeviceId FROM 
        ChangeLog
        JOIN ClazzWork ON ChangeLog.chTableId = ${CLAZZ_WORK_TABLE_ID} AND ClazzWork.clazzWorkUid = ChangeLog.chEntityPk
        JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid 
        JOIN Person ON Person.personUid IN (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1}  ${Role.PERMISSION_CLAZZWORK_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
    """,
    syncFindAllQuery = """
        SELECT ClazzWork.* FROM
        ClazzWork
        JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid
        JOIN Person ON Person.personUid IN  (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_CLAZZWORK_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId  
    """)
@Serializable
open class ClazzWork {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkUid: Long = 0

    var clazzWorkCreatorPersonUid: Long = 0

    var clazzWorkClazzUid : Long = 0

    var clazzWorkTitle : String? = null

    var clazzWorkCreatedDate: Long = 0

    var clazzWorkStartDateTime: Long = 0

    var clazzWorkStartTime: Long = 0

    var clazzWorkDueTime: Long = 0

    var clazzWorkDueDateTime: Long = 0

    var clazzWorkSubmissionType: Int = 0

    var clazzWorkCommentsEnabled : Boolean = false

    var clazzWorkMaximumScore: Int = 0

    var clazzWorkInstructions : String? = null

    var clazzWorkActive: Boolean = true

    @LocalChangeSeqNum
    var clazzWorkLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var clazzWorkMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzWorkLastChangedBy: Int = 0


        constructor(){
        clazzWorkActive = true
    }

    companion object{
        const val CLAZZ_WORK_TABLE_ID = 201
        const val CLAZZ_WORK_SUBMISSION_TYPE_NONE = 0
        const val CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT = 1
        const val CLAZZ_WORK_SUBMISSION_TYPE_ATTACHMENT = 2
        const val CLAZZ_WORK_SUBMISSION_TYPE_QUIZ = 3
    }


}
