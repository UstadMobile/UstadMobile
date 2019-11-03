package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.PersonPicture.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
@EntityWithAttachment
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

    var picTimestamp: Long = 0

    var mimeType: String? = null

    companion object {

        const val TABLE_ID = 50
    }
}
