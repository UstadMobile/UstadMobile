package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.VerbEntity.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
class VerbEntity {

    @PrimaryKey(autoGenerate = true)
    var verbUid: Long = 0

    var urlId: String? = null

    @MasterChangeSeqNum
    var verbMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var verbLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var verbLastChangedBy: Int = 0

    companion object {

        const val TABLE_ID = 62
    }
}
