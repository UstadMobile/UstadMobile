package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin.Companion.TABLE_ID

@UmEntity(tableId = TABLE_ID)
@Entity
class ContextXObjectStatementJoin {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey(autoGenerate = true)
    var contextXObjectStatementJoinUid: Long = 0

    var contextActivityFlag: Int = 0

    var contextStatementUid: Long = 0

    var contextXObjectUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var verbMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var verbLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var verbLastChangedBy: Int = 0

    companion object {

        const val TABLE_ID = 66
    }
}
