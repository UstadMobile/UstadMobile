package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.PersonAuth2

@Dao
@Repository
abstract class PersonAuth2Dao {

    @Insert
    abstract suspend fun insertListAsync(auths: List<PersonAuth2>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAsync(auth: PersonAuth2): Long

    @Query("""
        SELECT PersonAuth2.*
          FROM PersonAuth2
         WHERE PersonAuth2.pauthUid = :personUid 
    """)
    abstract suspend fun findByPersonUid(personUid: Long): PersonAuth2?

}