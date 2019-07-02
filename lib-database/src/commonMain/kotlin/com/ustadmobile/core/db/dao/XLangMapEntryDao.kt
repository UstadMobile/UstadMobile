package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.XLangMapEntry

@Dao
@UmRepository
abstract class XLangMapEntryDao : BaseDao<XLangMapEntry> {

    @Query("SELECT valueLangMap FROM XLangMapEntry WHERE  objectLangMapUid != 0")
    abstract suspend fun getAllXObjectValuesAsync(): List<String>
}
