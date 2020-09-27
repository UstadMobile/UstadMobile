package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.XLangMapEntry
import kotlinx.serialization.Serializable
import kotlin.js.JsName

@Dao
@UmRepository
abstract class XLangMapEntryDao : BaseDao<XLangMapEntry> {

    @JsName("getValuesWithListOfId")
    @Query("SELECT * FROM XLangMapEntry WHERE objectLangMapUid IN (:ids)")
    abstract suspend fun getValuesWithListOfId(ids: List<Int>): List<XLangMapEntry>

    @Serializable
    data class Verb(var verbLangMapUid: Long = 0, var valueLangMap: String = "") {

        override fun toString(): String {
            return valueLangMap
        }
    }

    @Serializable
    data class XObject(var objectLangMapUid: Long = 0, var valueLangMap: String = "") {

        override fun toString(): String {
            return valueLangMap
        }
    }
}
