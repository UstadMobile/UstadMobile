package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = ClazzWorkQuestionResponse.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzWorkQuestionResponse.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN ClazzWorkQuestionResponse ON ChangeLog.chTableId = ${ClazzWorkQuestionResponse.TABLE_ID} AND ChangeLog.chEntityPk = ClazzWorkQuestionResponse.clazzWorkQuestionResponseUid
        JOIN Person ON Person.personUid = ClazzWorkQuestionResponse.clazzWorkQuestionResponsePersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid"""],
    syncFindAllQuery = """
        SELECT ClazzWorkQuestionResponse.* FROM
        ClazzWorkQuestionResponse
        JOIN Person ON Person.personUid = ClazzWorkQuestionResponse.clazzWorkQuestionResponsePersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Entity
@Serializable
open class ClazzWorkQuestionResponse {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkQuestionResponseUid: Long = 0

    //Might not need it as we already have QuestionUid
    var clazzWorkQuestionResponseClazzWorkUid: Long = 0

    var clazzWorkQuestionResponseQuestionUid : Long = 0

    // for answers that are text
    var clazzWorkQuestionResponseText: String? = null

    //for answers that are quiz option the option uid
    var clazzWorkQuestionResponseOptionSelected: Long = 0

    var clazzWorkQuestionResponsePersonUid: Long = 0

    var clazzWorkQuestionResponseInactive: Boolean = false

    var clazzWorkQuestionResponseDateResponded: Long = 0

    @MasterChangeSeqNum
    var clazzWorkQuestionResponseMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzWorkQuestionResponseLCSN: Long = 0

    @LastChangedBy
    var clazzWorkQuestionResponseLCB: Int = 0

    @LastChangedTime
    var clazzWorkQuestionResponseLct: Long = 0

    companion object {
        const val TABLE_ID = 209
    }
}
