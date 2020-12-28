package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@Serializable
@SyncableEntity(tableId = WorkspaceTerms.TABLE_ID)
class WorkspaceTerms {

    @PrimaryKey(autoGenerate = true)
    var wtUid: Long = 0

    var termsHtml: String? = null

    var wtLang: String? = null

    @LastChangedBy
    var wtLastChangedBy: Int = 0

    @MasterChangeSeqNum
    var wtPrimaryCsn: Long = 0

    @LocalChangeSeqNum
    var wtLocalCsn: Long = 0

    companion object {

        const val TABLE_ID = 272

    }

}