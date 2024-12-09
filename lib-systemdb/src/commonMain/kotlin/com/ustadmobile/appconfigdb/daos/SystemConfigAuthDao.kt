package com.ustadmobile.appconfigdb.daos

import androidx.room.Query
import com.ustadmobile.appconfigdb.entities.SystemConfigAuth
import com.ustadmobile.door.annotation.DoorDao

@DoorDao
expect abstract class SystemConfigAuthDao {

    @Query("""
        SELECT SystemConfigAuth.*
          FROM SystemConfigAuth
         WHERE scaAuthId = :scaAuthId 
    """)
    abstract suspend fun findById(scaAuthId: String): SystemConfigAuth?

}