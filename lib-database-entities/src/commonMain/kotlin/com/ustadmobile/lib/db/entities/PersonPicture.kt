package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.PersonPicture.Companion.TABLE_ID

@Entity
@SyncableEntity(tableId = TABLE_ID)
class PersonPicture() {

    @PrimaryKey(autoGenerate = true)
    var personPictureUid: Long = 0

    var personPicturePersonUid: Long = 0

    @MasterChangeSeqNum
    var personPictureMasterCsn: Long = 0

    @LocalChangeSeqNum
    var personPictureLocalCsn: Long = 0

    @LastChangedBy
    var personPictureLastChangedBy: Int = 0

    var fileSize: Int = 0

    var picTimestamp: Int = 0

    var mimeType: String? = null

    companion object {

        const val TABLE_ID = 50
    }
}
