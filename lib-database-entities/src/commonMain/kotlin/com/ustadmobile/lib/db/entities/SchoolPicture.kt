package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = SchoolPicture.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, 
               ${SchoolPicture.TABLE_ID} AS tableId 
          FROM ChangeLog 
                JOIN SchoolPicture 
                     ON ChangeLog.chTableId = ${SchoolPicture.TABLE_ID}
                            AND ChangeLog.chEntityPk = SchoolPicture.schoolPictureUid
                JOIN School 
                     ON SchoolPicture.schoolPictureUid = School.schoolUid
                ${School.JOIN_FROM_SCHOOL_TO_DEVICESESSION_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_SCHOOL_SELECT}
                    ${School.JOIN_FROM_SCHOOL_TO_DEVICESESSION_VIA_SCOPEDGRANT_PT2}"""],
    syncFindAllQuery = """
        SELECT SchoolPicture.* 
          FROM DeviceSession
               JOIN PersonGroupMember
                    ON DeviceSession.dsPersonUid = PersonGroupMember.groupMemberPersonUid
               ${School.JOIN_FROM_PERSONGROUPMEMBER_TO_SCHOOL_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_SCHOOL_SELECT}
                    ${School.JOIN_FROM_PERSONGROUPMEMBER_TO_SCHOOL_VIA_SCOPEDGRANT_PT2}     
               JOIN SchoolPicture
                    ON SchoolPicture.schoolPictureUid = School.schoolUid
         WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Serializable
open class SchoolPicture() {

    @PrimaryKey(autoGenerate = true)
    var schoolPictureUid: Long = 0

    //This is not really used. This is effectively a 1:1 join. schoolPictureUid should equal
    // the uid of the school itself.
    var schoolPictureSchoolUid : Long = 0

    @MasterChangeSeqNum
    var schoolPictureMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var schoolPictureLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var schoolPictureLastChangedBy: Int = 0

    @LastChangedTime
    var schoolPictureLct: Long = 0

    var schoolPictureFileSize : Long = 0

    var schoolPictureTimestamp : Long = 0

    var schoolPictureMimeType : String = ""

    companion object {

        const val TABLE_ID = 175
    }
}
