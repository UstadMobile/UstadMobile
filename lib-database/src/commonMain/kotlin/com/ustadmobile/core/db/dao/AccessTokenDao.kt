package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class AccessTokenDao {

    @Query("SELECT EXISTS(SELECT token FROM AccessToken WHERE accessTokenPersonUid = :personUid AND token = :token)")
    abstract fun isValidToken(personUid: Long, token: String): Boolean

}
