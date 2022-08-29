package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.db.entities.CustomFieldValue

@Dao
abstract class CustomFieldValueDao : BaseDao<CustomFieldValue> {

    @Query("SELECT * FROM CustomFieldValue WHERE customFieldValueFieldUid = :fieldUid AND "
            + " customFieldValueEntityUid = :entityUid ")
    abstract suspend fun findValueByCustomFieldUidAndEntityUid(fieldUid: Long, entityUid: Long)
            : CustomFieldValue?

    @Query("SELECT * FROM CustomFieldValue WHERE customFieldValueFieldUid = :fieldUid AND "
            + " customFieldValueEntityUid = :entityUid ")
    abstract fun findValueByCustomFieldUidAndEntityUidSync(fieldUid: Long, entityUid: Long)
            : CustomFieldValue?


    @Insert
    abstract suspend fun insertListAsync(entityList: List<CustomFieldValue>)

    @Update
    abstract suspend fun updateListAsync(entityList: List<CustomFieldValue>)

}
