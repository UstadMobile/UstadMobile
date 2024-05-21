package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.VerbEntity

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
