package com.ustadmobile.lib.db.entities

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository

@DoorDao
@Repository
expect abstract class VerbLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertList(list: List<VerbLangMapEntry>)

}