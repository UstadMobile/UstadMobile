package com.ustadmobile.appconfigdb

import com.ustadmobile.appconfigdb.daos.LearningSpaceConfigDao
import com.ustadmobile.appconfigdb.daos.LearningSpaceInfoDao
import com.ustadmobile.appconfigdb.daos.SystemConfigDao
import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.appconfigdb.entities.SystemConfig
import com.ustadmobile.door.annotation.DoorDatabase
import com.ustadmobile.door.room.RoomDatabase

@DoorDatabase(
    entities = [
        LearningSpaceInfo::class,
        LearningSpaceConfig::class,
        SystemConfig::class
    ],
    version = 1,
)
expect abstract class SystemDb : RoomDatabase {

    abstract fun learningSpaceInfoDao(): LearningSpaceInfoDao

    abstract fun learningSpaceConfigDao(): LearningSpaceConfigDao

    abstract fun serverConfigDao(): SystemConfigDao

}