package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.School.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
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
