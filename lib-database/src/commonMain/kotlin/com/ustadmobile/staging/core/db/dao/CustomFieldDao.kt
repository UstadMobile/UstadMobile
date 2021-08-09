package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.CustomField

@Dao
abstract class CustomFieldDao : BaseDao<CustomField> {

    @Query("SELECT * FROM CustomField WHERE customFieldUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<CustomField?>

    @Query("SELECT * FROM CustomField WHERE customFieldUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : CustomField?

    @Update
    abstract suspend fun updateAsync(entity: CustomField): Int


}
