package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.VerbLangMapEntry

@DoorDao
@Repository
expect abstract class VerbLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertList(list: List<VerbLangMapEntry>)

    @Query("""
        SELECT VerbLangMapEntry.*
          FROM VerbLangMapEntry
         WHERE VerbLangMapEntry.vlmeVerbUid = :verbUid
    """)
    abstract suspend fun findByVerbUidAsync(verbUid: Long): List<VerbLangMapEntry>

}