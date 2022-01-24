package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
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
    @ReplicationVersionId
    var schoolPictureLct: Long = 0

    var schoolPictureFileSize : Long = 0

    var schoolPictureTimestamp : Long = 0

    var schoolPictureMimeType : String = ""

    companion object {

        const val TABLE_ID = 175
    }
}
