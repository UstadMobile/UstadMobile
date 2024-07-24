package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.StateEntity

@DoorDao
@Repository
expect abstract class StateEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(stateEntities: List<StateEntity>)

}