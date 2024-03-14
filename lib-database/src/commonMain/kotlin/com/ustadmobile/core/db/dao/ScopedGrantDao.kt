package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class ScopedGrantDao {


    @Insert
    abstract suspend fun insertAsync(scopedGrant: ScopedGrant): Long

    @Insert
    abstract suspend fun insertListAsync(scopedGrantList: List<ScopedGrant>)

}