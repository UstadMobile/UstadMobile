package com.ustadmobile.appconfigdb.daos

import androidx.room.Query
import com.ustadmobile.appconfigdb.entities.SystemConfig
import com.ustadmobile.door.annotation.DoorDao

@DoorDao
expect abstract class SystemConfigDao {

    @Query("""
        SELECT ServerConfig.*
          FROM ServerConfig
         WHERE ServerConfig.scUid = ${SystemConfig.SC_UID} 
    """)
    abstract suspend fun getServerConfigAsync(): SystemConfig?

}