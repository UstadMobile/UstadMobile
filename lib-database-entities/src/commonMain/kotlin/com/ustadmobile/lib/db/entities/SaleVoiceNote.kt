package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity


@SyncableEntity(tableId = 75)
@Entity
class SaleVoiceNote {

    @PrimaryKey(autoGenerate = true)
    var saleVoiceNoteUid: Long = 0

    var saleVoiceNoteSaleUid: Long = 0

    var saleVoiceNoteFileSize: Int = 0

    var saleVoiceNoteTimestamp: Long = 0

    var saleVoiceNoteMime: String? = null

    @MasterChangeSeqNum
    var saleVoiceNoteMCSN: Long = 0

    @LocalChangeSeqNum
    var saleVoiceNoteCSN: Long = 0

    @LastChangedBy
    var saleVoiceNoteLCB: Int = 0


}
