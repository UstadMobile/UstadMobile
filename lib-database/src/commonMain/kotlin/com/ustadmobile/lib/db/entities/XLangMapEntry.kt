package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.XLangMapEntry.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
 Trigger(
     name = "xlangmapentry_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [TRIGGER_UPSERT]
 )
))
data class XLangMapEntry(
        @ColumnInfo(index = true)
        var verbLangMapUid: Long = 0L,
        var objectLangMapUid: Long = 0L,
        var languageLangMapUid: Long = 0L,
        var languageVariantLangMapUid: Long = 0L,
        var valueLangMap: String? = "",

        @MasterChangeSeqNum
        var statementLangMapMasterCsn: Int = 0,

        @LocalChangeSeqNum
        var statementLangMapLocalCsn: Int = 0,

        @LastChangedBy
        var statementLangMapLcb: Int = 0,

        @ReplicateLastModified
        @ReplicateEtag
        var statementLangMapLct: Long = 0
) {

    @PrimaryKey(autoGenerate = true)
    var statementLangMapUid: Long = 0

    companion object {

        const val TABLE_ID = 74
    }


}