package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.VerbEntity

@DoorDao
@Repository
expect abstract class VerbDao  {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreAsync(entities: List<VerbEntity>)

    @Query("""
        SELECT VerbEntity.*
          FROM VerbEntity
         WHERE VerbEntity.verbUid = :uid 
    """)
    abstract suspend fun findByUid(uid: Long): VerbEntity?

}