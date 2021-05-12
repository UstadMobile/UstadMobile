package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.PersonParentJoin

@Dao
@Repository
abstract class PersonParentJoinDao {

    @Insert
    abstract suspend fun insertAsync(entity: PersonParentJoin): Long

}