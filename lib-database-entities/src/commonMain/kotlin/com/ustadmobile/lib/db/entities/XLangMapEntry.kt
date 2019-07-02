package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.db.entities.XLangMapEntry.Companion.TABLE_ID

@UmEntity(tableId = TABLE_ID)
@Entity
data class XLangMapEntry(
        var verbLangMapUid: Long,
        var objectLangMapUid: Long,
        var languageLangMapUid: Long,
        var languageVariantLangMapUid: Long,
        var valueLangMap: String) {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey(autoGenerate = true)
    var statementLangMapUid: Long = 0

    companion object {

        const val TABLE_ID = 74
    }


}