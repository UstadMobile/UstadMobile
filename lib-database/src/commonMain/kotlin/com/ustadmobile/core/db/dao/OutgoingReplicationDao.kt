package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.entities.OutgoingReplication

@DoorDao
expect abstract class OutgoingReplicationDao {

    @Insert
    abstract suspend fun insert(outgoing: List<OutgoingReplication>)

    @Query("""
        SELECT OutgoingReplication.*
          FROM OutgoingReplication
    """)
    abstract suspend fun listReplications(): List<OutgoingReplication>


}