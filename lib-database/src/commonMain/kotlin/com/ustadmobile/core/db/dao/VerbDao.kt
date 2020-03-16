package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.VerbEntity
import kotlin.js.JsName

@Dao
@UmRepository
abstract class VerbDao : BaseDao<VerbEntity> {

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<VerbEntity>)

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
        val verbListToInsert = uidsToInsert.map { verbEntry -> VerbEntity(verbUid = verbEntry.value,
                urlId = verbEntry.key) }
        replaceList(verbListToInsert)
    }


}
