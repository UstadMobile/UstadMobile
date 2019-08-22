package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum

@Entity
class ContextXObjectStatementJoin {

    @PrimaryKey(autoGenerate = true)
    var contextXObjectStatementJoinUid: Long = 0

    var contextActivityFlag: Int = 0

    var contextStatementUid: Long = 0

    var contextXObjectUid: Long = 0

    @MasterChangeSeqNum
    var verbMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var verbLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var verbLastChangedBy: Int = 0

    companion object {

        const val TABLE_ID = 66
    }
}
