package com.ustadmobile.core.db.dao

import androidx.room.Delete
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Container

@DoorDao
@Repository
expect abstract class ContainerDao {

    @Query("""
        SELECT Container.*
          FROM Container
         LIMIT 100 
    """)
    abstract suspend fun findAllBatch(): List<Container>

    @Delete
    abstract suspend fun deleteListAsync(list: List<Container>)

}
