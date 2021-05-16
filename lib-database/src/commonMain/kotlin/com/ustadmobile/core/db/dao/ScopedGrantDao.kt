package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ScopedGrant

@Dao
@Repository
abstract class ScopedGrantDao {

    @Insert
    abstract suspend fun insertAsync(socpedGrant: ScopedGrant): Long

}