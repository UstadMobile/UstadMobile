package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth

@DoorDao
expect abstract class PersonAuthDao : BaseDao<PersonAuth> {


    @Query("SELECT * FROM PersonAuth WHERE personAuthUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long):PersonAuth?

    @Query("SELECT * FROM PersonAuth WHERE personAuthUid = :uid")
    abstract fun findByUid(uid: Long) : PersonAuth?

    @Query("SELECT * FROM Person WHERE username = :username")
    abstract fun findPersonByUsername(username: String): Person?

    @Update
    abstract suspend fun updateAsync(entity: PersonAuth):Int

    @Query("UPDATE PersonAuth set passwordHash = :passwordHash " +
            " WHERE personAuthUid = :personUid")
    abstract suspend fun updatePasswordForPersonUid(personUid: Long, passwordHash: String): Int

}
