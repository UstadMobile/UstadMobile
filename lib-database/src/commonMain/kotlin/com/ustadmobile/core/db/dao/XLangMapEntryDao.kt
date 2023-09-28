package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.XLangMapEntry
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class XLangMapEntryDao : BaseDao<XLangMapEntry> {

    @JsName("getValuesWithListOfId")
    @Query("SELECT * FROM XLangMapEntry WHERE objectLangMapUid IN (:ids)")
    abstract suspend fun getValuesWithListOfId(ids: List<Int>): List<XLangMapEntry>


    @Query("""SELECT * FROM XLangMapEntry WHERE 
            verbLangMapUid = :verbUid AND languageLangMapUid = :langMapUid LIMIT 1""")
    abstract fun getXLangMapFromVerb(verbUid: Long, langMapUid: Long): XLangMapEntry?

    @Query("""SELECT * FROM XLangMapEntry WHERE 
            objectLangMapUid = :objectUid AND languageLangMapUid = :langMapUid LIMIT 1""")
    abstract fun getXLangMapFromObject(objectUid: Long, langMapUid: Long): XLangMapEntry?


}
