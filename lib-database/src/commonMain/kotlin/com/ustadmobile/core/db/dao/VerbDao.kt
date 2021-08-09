package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.VerbDisplay
import com.ustadmobile.lib.db.entities.VerbEntity
import kotlin.js.JsName

@Dao
@Repository
abstract class VerbDao : BaseDao<VerbEntity> {

    @Query("SELECT * FROM VerbEntity WHERE urlId = :urlId")
    abstract fun findByUrl(urlId: String?): VerbEntity?

    @JsName("findByUidList")
    @Query("SELECT verbUid FROM VerbEntity WHERE verbUid IN (:uidList)")
    abstract fun findByUidList(uidList: List<Long>): List<Long>

    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entityList: List<VerbEntity>)

    fun initPreloadedVerbs() {
        val uidsInserted = findByUidList(VerbEntity.FIXED_UIDS.values.toList())
        val uidsToInsert = VerbEntity.FIXED_UIDS.filter { it.value !in uidsInserted }
        val verbListToInsert = uidsToInsert.map { verbEntry ->
            VerbEntity(verbEntry.value, verbEntry.key)
        }
        replaceList(verbListToInsert)
    }

   @Query("""SELECT VerbEntity.verbUid, VerbEntity.urlId, XLangMapEntry.valueLangMap AS display
        FROM VerbEntity LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid WHERE 
         XLangMapEntry.verbLangMapUid NOT IN (:uidList)""")
    abstract fun findAllVerbsAscList(uidList: List<Long>): List<VerbDisplay>

    @Query("""SELECT VerbEntity.verbUid, VerbEntity.urlId, XLangMapEntry.valueLangMap AS display 
         FROM VerbEntity LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid WHERE 
         VerbEntity.verbUid NOT IN (:uidList) ORDER BY display ASC""")
    abstract fun findAllVerbsAsc(uidList: List<Long>): DoorDataSourceFactory<Int, VerbDisplay>

    @Query("""SELECT VerbEntity.verbUid, VerbEntity.urlId, XLangMapEntry.valueLangMap AS display 
         FROM VerbEntity LEFT JOIN XLangMapEntry on XLangMapEntry.verbLangMapUid = VerbEntity.verbUid WHERE 
        VerbEntity.verbUid NOT IN (:uidList) ORDER BY display DESC""")
    abstract fun findAllVerbsDesc(uidList: List<Long>): DoorDataSourceFactory<Int, VerbDisplay>


}
