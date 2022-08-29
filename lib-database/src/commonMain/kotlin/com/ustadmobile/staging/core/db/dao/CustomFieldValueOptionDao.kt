package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.db.entities.CustomFieldValueOption

@Dao
abstract class CustomFieldValueOptionDao : BaseDao<CustomFieldValueOption> {

    @Update
    abstract suspend fun updateAsync(entity: CustomFieldValueOption) : Int

    @Query("SELECT * FROM CustomFieldValueOption WHERE customFieldValueOptionUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : CustomFieldValueOption?
}
