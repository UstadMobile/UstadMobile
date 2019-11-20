package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.db.entities.LocallyAvailableContainer

@Dao
abstract class LocallyAvailableContainerDao {

    @Insert
    abstract suspend fun insertList(locallyAvailableContainers: List<LocallyAvailableContainer>)

    @Delete
    abstract suspend fun deleteList(locallyAvailableContainers: List<LocallyAvailableContainer>)

    @Query("DELETE FROM LocallyAvailableContainer")
    abstract fun deleteAll()

}