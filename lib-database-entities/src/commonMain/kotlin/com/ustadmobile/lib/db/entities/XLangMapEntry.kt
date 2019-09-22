package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.XLangMapEntry.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
data class XLangMapEntry(
        var verbLangMapUid: Long = 0L,
        var objectLangMapUid: Long = 0L,
        var languageLangMapUid: Long = 0L,
        var languageVariantLangMapUid: Long = 0L,
        var valueLangMap: String = "",

        @MasterChangeSeqNum
        var statementLangMapMasterCsn: Int = 0,

        @LocalChangeSeqNum
        var statementLangMapLocalCsn: Int = 0,

        @LastChangedBy
        var statementLangMapLcb: Int = 0
) {

    @PrimaryKey(autoGenerate = true)
    var statementLangMapUid: Long = 0

    companion object {

        const val TABLE_ID = 74
    }


}