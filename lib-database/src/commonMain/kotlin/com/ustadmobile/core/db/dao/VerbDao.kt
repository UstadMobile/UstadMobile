package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.VerbEntity

@DoorDao
@Repository
expect abstract class VerbDao  {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreAsync(entities: List<VerbEntity>)

}
