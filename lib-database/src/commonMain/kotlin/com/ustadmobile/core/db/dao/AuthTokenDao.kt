package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao

@DoorDao
abstract class AuthTokenDao {

    @Query("""
        SELECT EXISTS(
               SELECT atUid 
                 FROM AuthToken
                WHERE atAuth = :auth)
    """)
    abstract suspend fun validateToken(auth: String): Boolean

}