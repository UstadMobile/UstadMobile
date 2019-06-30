package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.VerbEntity

@Dao
@UmRepository
abstract class VerbDao : BaseDao<VerbEntity> {

    @Query("SELECT * FROM VerbEntity WHERE urlId = :urlId")
    abstract fun findByUrl(urlId: String?): VerbEntity?
}
