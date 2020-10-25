package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = ClazzWorkSubmission.TABLE_ID,
    notifyOnUpdate = """
        SELECT DISTINCT DeviceSession.dsDeviceId FROM 
        ChangeLog
        JOIN ClazzWorkSubmission ON ChangeLog.chTableId = ${ClazzWorkSubmission.TABLE_ID} AND ChangeLog.chEntityPk = ClazzWorkSubmission.clazzWorkSubmissionUid
        JOIN Person ON Person.personUid = ClazzWorkSubmission.clazzWorkSubmissionPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZWORK_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid""",
    syncFindAllQuery = """
        SELECT ClazzWorkSubmission.* FROM
        ClazzWorkSubmission
        JOIN Person ON Person.personUid = ClazzWorkSubmission.clazzWorkSubmissionPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZWORK_VIEWSTUDENTPROGRESS} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Serializable
open class ClazzWorkSubmission() {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkSubmissionUid: Long = 0

    var clazzWorkSubmissionClazzWorkUid : Long = 0

    var clazzWorkSubmissionClazzMemberUid : Long = 0

    var clazzWorkSubmissionMarkerClazzMemberUid: Long = 0

    var clazzWorkSubmissionMarkerPersonUid: Long = 0

    var clazzWorkSubmissionPersonUid: Long = 0

    var clazzWorkSubmissionInactive : Boolean = false

    var clazzWorkSubmissionDateTimeStarted : Long = 0

    var clazzWorkSubmissionDateTimeUpdated: Long = 0

    var clazzWorkSubmissionDateTimeFinished: Long = 0

    var clazzWorkSubmissionDateTimeMarked: Long = 0

    var clazzWorkSubmissionText : String? = null

    var clazzWorkSubmissionScore: Int = 0


    @MasterChangeSeqNum
    var clazzWorkSubmissionMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzWorkSubmissionLCSN: Long = 0

    @LastChangedBy
    var clazzWorkSubmissionLCB: Int = 0

    companion object {
        const val TABLE_ID = 206
    }

}
