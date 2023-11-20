package com.ustadmobile.core.db.dao

import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.VerbDisplay
import com.ustadmobile.lib.db.entities.VerbEntity
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class VerbDao : BaseDao<VerbEntity> {

    @Query("SELECT * FROM VerbEntity WHERE urlId = :urlId")
    abstract fun findByUrl(urlId: String?): VerbEntity?

    @JsName("findByUidList")
    @Query("SELECT verbUid FROM VerbEntity WHERE verbUid IN (:uidList)")
    abstract suspend fun findByUidList(uidList: List<Long>): List<Long>

    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceList(entityList: List<VerbEntity>)

   @Query("""SELECT VerbEntity.verbUid, VerbEntity.urlId, XLangMapEntry.valueLangMap AS display
        FROM VerbEntity LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid WHERE 
         XLangMapEntry.verbLangMapUid NOT IN (:uidList)""")
    abstract fun findAllVerbsAscList(uidList: List<Long>): List<VerbDisplay>

    @Query("""SELECT VerbEntity.verbUid, VerbEntity.urlId, XLangMapEntry.valueLangMap AS display 
         FROM VerbEntity LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid WHERE 
         VerbEntity.verbUid NOT IN (:uidList) ORDER BY display ASC""")
    abstract fun findAllVerbsAsc(uidList: List<Long>): PagingSource<Int, VerbDisplay>

    @Query("""SELECT VerbEntity.verbUid, VerbEntity.urlId, XLangMapEntry.valueLangMap AS display 
         FROM VerbEntity LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid WHERE 
        VerbEntity.verbUid NOT IN (:uidList) ORDER BY display DESC""")
    abstract fun findAllVerbsDesc(uidList: List<Long>): PagingSource<Int, VerbDisplay>


}
