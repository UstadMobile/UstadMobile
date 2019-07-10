package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 75)
@Entity
class SaleVoiceNote {

    @PrimaryKey(autoGenerate = true)
    var saleVoiceNoteUid: Long = 0

    var saleVoiceNoteSaleUid: Long = 0

    var saleVoiceNoteFileSize: Int = 0

    var saleVoiceNoteTimestamp: Long = 0

    var saleVoiceNoteMime: String? = null

    @UmSyncMasterChangeSeqNum
    var saleVoiceNoteMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var saleVoiceNoteCSN: Long = 0

    @UmSyncLastChangedBy
    var saleVoiceNoteLCB: Int = 0


}
