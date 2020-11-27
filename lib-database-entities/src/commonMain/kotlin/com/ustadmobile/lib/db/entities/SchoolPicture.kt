package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = SchoolPicture.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${SchoolPicture.TABLE_ID} AS tableId FROM
        ChangeLog 
        JOIN SchoolPicture ON ChangeLog.chTableId = ${SchoolPicture.TABLE_ID} AND ChangeLog.chEntityPk = SchoolPicture.schoolPictureUid
        JOIN School ON SchoolPicture.schoolPictureUid = School.schoolUid
        JOIN Person ON Person.personUid IN 
            (${School.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_SCHOOL_SELECT} ${School.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid"""],
    syncFindAllQuery = """
        SELECT SchoolPicture.* FROM
        SchoolPicture
        JOIN School ON SchoolPicture.schoolPictureUid = School.schoolUid
        JOIN Person ON Person.personUid IN 
            (${School.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_SCHOOL_SELECT} ${School.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Serializable
open class SchoolPicture() {

    @PrimaryKey(autoGenerate = true)
    var schoolPictureUid: Long = 0

    var schoolPictureSchoolUid : Long = 0

    @MasterChangeSeqNum
    var schoolPictureMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var schoolPictureLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var schoolPictureLastChangedBy: Int = 0

    var schoolPictureFileSize : Long = 0

    var schoolPictureTimestamp : Long = 0

    var schoolPictureMimeType : String = ""

    companion object {

        const val TABLE_ID = 175
    }
}
