package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.annotation.Dao
import com.ustadmobile.lib.db.entities.LocallyAvailableContainer

@Dao
abstract class LocallyAvailableContainerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertList(locallyAvailableContainers: List<LocallyAvailableContainer>)

    @Delete
    abstract suspend fun deleteList(locallyAvailableContainers: List<LocallyAvailableContainer>)

    @Query("DELETE FROM LocallyAvailableContainer")
    abstract fun deleteAll()

}