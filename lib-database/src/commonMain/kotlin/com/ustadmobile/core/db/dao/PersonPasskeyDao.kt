package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.PersonPasskey

@DoorDao
@Repository
expect abstract class PersonPasskeyDao : BaseDao<PersonPasskey> {


    @Query(
        """
        SELECT PersonPasskey.id
          FROM PersonPasskey
              """
    )
    abstract suspend fun allPasskey(): List<String>
    @Query(
        """
        SELECT *
          FROM PersonPasskey
         WHERE PersonPasskey.id = :id 
              """
    )
    abstract suspend fun findPersonPasskeyFromClientDataJson(id: String): PersonPasskey?

}