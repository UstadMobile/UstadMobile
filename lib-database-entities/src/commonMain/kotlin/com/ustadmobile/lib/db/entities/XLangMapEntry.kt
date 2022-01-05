package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.XLangMapEntry.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = TABLE_ID, tracker = XLangMapEntryReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "xlangmapentry_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO XLangMapEntry(verbLangMapUid, objectLangMapUid, languageLangMapUid, languageVariantLangMapUid, valueLangMap, statementLangMapMasterCsn, statementLangMapLocalCsn, statementLangMapLcb, statementLangMapLct, statementLangMapUid) 
         VALUES (NEW.verbLangMapUid, NEW.objectLangMapUid, NEW.languageLangMapUid, NEW.languageVariantLangMapUid, NEW.valueLangMap, NEW.statementLangMapMasterCsn, NEW.statementLangMapLocalCsn, NEW.statementLangMapLcb, NEW.statementLangMapLct, NEW.statementLangMapUid) 
         /*psql ON CONFLICT (statementLangMapUid) DO UPDATE 
         SET verbLangMapUid = EXCLUDED.verbLangMapUid, objectLangMapUid = EXCLUDED.objectLangMapUid, languageLangMapUid = EXCLUDED.languageLangMapUid, languageVariantLangMapUid = EXCLUDED.languageVariantLangMapUid, valueLangMap = EXCLUDED.valueLangMap, statementLangMapMasterCsn = EXCLUDED.statementLangMapMasterCsn, statementLangMapLocalCsn = EXCLUDED.statementLangMapLocalCsn, statementLangMapLcb = EXCLUDED.statementLangMapLcb, statementLangMapLct = EXCLUDED.statementLangMapLct
         */"""
     ]
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

        @LastChangedTime
        @ReplicationVersionId
        var statementLangMapLct: Long = 0
) {

    @PrimaryKey(autoGenerate = true)
    var statementLangMapUid: Long = 0

    companion object {

        const val TABLE_ID = 74
    }


}